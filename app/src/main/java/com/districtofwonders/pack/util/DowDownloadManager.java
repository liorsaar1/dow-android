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

import com.districtofwonders.pack.fragment.feed.FeedsFragment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liorsaar on 2016-01-07
 */
public class DowDownloadManager {
    private static final String TAG = DowDownloadManager.class.getSimpleName();
    private static DowDownloadManager mInstance;
    private static Map<Long, String[]> mDownloadIdMap;
    private DownloadManager mDownloadManager;

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (!mDownloadIdMap.containsKey(id)) {
                Log.e(TAG, "Ingnoring unrelated download " + id);
                return;
            }

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = mDownloadManager.query(query);

            // it shouldn't be empty, but just in case
            if (!cursor.moveToFirst()) {
                Log.e(TAG, "Empty row");
                return;
            }

            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                Log.w(TAG, "Download Failed");
                return;
            }
            // http://developer.android.com/reference/android/app/DownloadManager.html
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String downloadedPackageUriString = cursor.getString(uriIndex);

            String title = "Download Completed: " + mDownloadIdMap.get(id)[0];
            Uri uri = Uri.parse(downloadedPackageUriString);
            ViewUtils.playLocalAudio(context, title, uri);
        }
    };

    public DowDownloadManager(Context context) {
        // get a manager
        mDownloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // register the receiver
        IntentFilter downloadCompleteIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(mDownloadCompleteReceiver, downloadCompleteIntentFilter);
        // downloads
        mDownloadIdMap = new HashMap<>();
    }

    public static synchronized DowDownloadManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DowDownloadManager(context);
        }
        return mInstance;
    }

    public void enqueueRequest(int pageNumber, String url, String title) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // only download via WIFI
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(FeedsFragment.feeds[pageNumber].title);
        request.setDescription(title);

        // visible
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        String filename = url.substring(url.lastIndexOf("/") + 1);
        request.setDestinationInExternalPublicDir(getDownloadDirectory(), filename);

        // enqueue this request
        long downloadID = mDownloadManager.enqueue(request);
        mDownloadIdMap.put(downloadID, new String[] {title, url});
    }

    public static String getDownloadDirectory() {
        return Environment.DIRECTORY_DOWNLOADS;
    }

    public static boolean isDownloaded(String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        File path = Environment.getExternalStoragePublicDirectory(getDownloadDirectory());
        File file = new File(path, filename);
        return file.exists();
    }

    public static Uri getDownloadUri(String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        File path = Environment.getExternalStoragePublicDirectory(getDownloadDirectory());
        File file = new File(path, filename);
        return Uri.fromFile(file);
    }

    public boolean isWiFiAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public boolean isDownloadInProgress(String url) {
        for (String[] value : mDownloadIdMap.values()) {
            if (value[1].equals(url))
                return true;
        }
        return false;
    }
}
