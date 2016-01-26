package com.districtofwonders.pack.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.gcm.AnalyticsHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liorsaar on 2016-01-07
 */
public class DowDownloadManager {
    public static final String DOWNLOAD_SUB_FOLDER = "DistrictOfWonders";
    private static final String TAG = MainActivity.TAG; //DowDownloadManager.class.getSimpleName();
    private static DowDownloadManager mInstance;
    private static Map<Long, DownloadDesc> mDownloadIdMap;
    private DownloadManager mDownloadManager;

    /**
     * when a download is completed, pop a playback chooser
     */
    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "ddm: onReceive: <<<");
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (!mDownloadIdMap.containsKey(id)) {
                Log.e(TAG, "Ingnoring unrelated download " + id);
                return;
            }
            // query
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = mDownloadManager.query(query);
            // check cursor
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Empty row");
                return;
            }
            // if failed - cleanup
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                Log.e(TAG, "Download Failed " + cursor.getInt(statusIndex));
                // delete any partially downloaded file
                delete(mDownloadIdMap.get(id).url);
                // remove failed id/url from the map
                mDownloadIdMap.remove(id);
                return;
            }
            // success !
            mDownloadIdMap.get(id).completed = true;

            // http://developer.android.com/reference/android/app/DownloadManager.html
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String downloadedPackageUriString = cursor.getString(uriIndex);

            String title = context.getString(R.string.download_completed) + " - " + mDownloadIdMap.get(id).title;
            Uri uri = Uri.parse(downloadedPackageUriString);
            ViewUtils.playLocalAudio(context, title, uri);
            AnalyticsHelper.downloadCompleted(context, uri);
        }
    };

    private DowDownloadManager(Context context) {
        // get a manager
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // downloads
        mDownloadIdMap = new HashMap<>();
        // the receiver is registered in onResume
    }

    public static synchronized DowDownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DowDownloadManager(context);
        }
        return mInstance;
    }

    public static Uri getDownloadUri(String url) {
        return Uri.fromFile(getFile(url));
    }

    private static File getFile(String url) {
        File path = Environment.getExternalStoragePublicDirectory(getDownloadDirectory());
        String filename = getFilename(url);
        return new File(path, filename);
    }

    private static String getFilename(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private static String getDownloadDirectory() {
        return Environment.DIRECTORY_DOWNLOADS + "/" + DOWNLOAD_SUB_FOLDER;
    }

    public static boolean delete(String url) {
        return getFile(url).delete();
    }

    /**
     * enqueue a download request
     * download via WIFI or MOBILE - the caller must confirm with the user first !
     * @param url file url
     * @param title notification title display
     * @param desc notification desc display
     * @return
     */
    public long enqueueRequest(String url, String title, String desc) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // download via WIFI or MOBILE - the caller must confirm with the user first !
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI|DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle(title);
        request.setDescription(desc);

        // visible
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(getDownloadDirectory(), getFilename(url));

        // enqueue this request
        final long downloadID = mDownloadManager.enqueue(request);
        mDownloadIdMap.put(downloadID, new DownloadDesc(title + " " + desc, url));
        return downloadID;
    }

    public boolean isWiFiAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public boolean isDownloaded(String url) {
        // partial file exists, but the download is still in progress
        if (isDownloadInProgress(url))
            return false;
        // file ?
        return fileExists(url);
    }

    private static boolean fileExists(String url) {
        return getFile(url).exists();
    }

    public boolean isDownloadInProgress(String url) {
        // if the url exists, but not completed - it is in progress
        for (DownloadDesc desc : mDownloadIdMap.values()) {
            if (desc.url.equals(url) && !desc.completed)
                return true;
        }
        return false;
    }

    public void onResume(Context context) {
        Log.e(TAG, "ddm: onResume: +++ " + mDownloadCompleteReceiver);
        context.registerReceiver(mDownloadCompleteReceiver, getDownloadCompleteIntentFilter());
    }

    public static IntentFilter getDownloadCompleteIntentFilter() {
        return new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    }

    public void onPause(Context context) {
        Log.e(TAG, "ddm: onPause: ---");
        context.unregisterReceiver(mDownloadCompleteReceiver);
    }

    public int getDownloadStatus(long id) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = mDownloadManager.query(query);
        if (!cursor.moveToFirst()) {
            Log.e(TAG, "Error reading status " + id);
            return 0;
        }
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                String failedReason = "";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        failedReason = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        failedReason = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        failedReason = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        failedReason = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        failedReason = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        failedReason = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        failedReason = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        failedReason = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        failedReason = "ERROR_UNKNOWN";
                        break;
                }
                Log.e(TAG, "failedReason:" + failedReason);
                break;
            case DownloadManager.STATUS_PAUSED:
                String pausedReason = "";

                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        pausedReason = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        pausedReason = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        pausedReason = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        pausedReason = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                Log.e(TAG, "pausedReason:" + pausedReason);

                break;
            case DownloadManager.STATUS_PENDING:
                Log.e(TAG, "STATUS_PENDING");
                break;
            case DownloadManager.STATUS_RUNNING:
                Log.e(TAG, "STATUS_RUNNING");
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                Log.e(TAG, "STATUS_SUCCESSFUL");
                break;
        }

        return status;
    }

    public void cancelDownload(long downloadID) {
        mDownloadIdMap.remove(downloadID);
    }

    class DownloadDesc {
        public final String title;
        public final String url;
        public boolean completed;

        public DownloadDesc(String title, String url) {
            this.title = title;
            this.url = url;
            completed = false;
        }
    }

}
