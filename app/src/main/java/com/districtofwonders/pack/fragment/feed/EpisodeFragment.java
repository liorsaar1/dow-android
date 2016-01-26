package com.districtofwonders.pack.fragment.feed;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.gcm.AnalyticsHelper;
import com.districtofwonders.pack.util.DateUtils;
import com.districtofwonders.pack.util.DowDownloadManager;
import com.districtofwonders.pack.util.ViewUtils;

import java.util.HashMap;
import java.util.Map;

public class EpisodeFragment extends Fragment {

    private static final String TAG = MainActivity.TAG; //EpisodeFragment.class.getSimpleName();
    private static final String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";
    private static final String ARG_FEED_ITEM = "ARG_FEED_ITEM";

    private Map<String, String> mFeedItem;
    private int mPageNumber;
    private TextView mEpisodePlay;
    private TextView mEpisodeDownload;
    private TextView mEpisodeDelete;
    /**
     * when a download is completed - update the play/download buttons state
     */
    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "episode: onReceive: <<<");
            updateButtons(context);
        }
    };

    public static Fragment newInstance(int pageNumber, Map<String, String> feedItem) {
        EpisodeFragment fragment = new EpisodeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PAGE_NUMBER, pageNumber);
        arguments.putSerializable(ARG_FEED_ITEM, (HashMap) feedItem);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mPageNumber = arguments.getInt(ARG_PAGE_NUMBER);
        mFeedItem = (HashMap<String, String>) arguments.getSerializable(ARG_FEED_ITEM);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.episode_fragment, null);

        // feed name
        ((TextView) root.findViewById(R.id.episodeFeedName)).setText(FeedsFragment.feeds[mPageNumber].title);
        // episode title
        String title = FeedsFragment.extractFeedItemTitle(mPageNumber, mFeedItem.get(FeedParser.Tags.TITLE));
        ((TextView) root.findViewById(R.id.episodeTitle)).setText(title);
        // date
        String pubDate = DateUtils.getPubDate(mFeedItem.get(FeedParser.Tags.PUB_DATE));
        ((TextView) root.findViewById(R.id.episodePubDate)).setText(pubDate);
        // duration
        String durationString = "";
        if (mFeedItem.get(FeedParser.Tags.DURATION) != null) {
            int duration = DateUtils.getMinutes(mFeedItem.get(FeedParser.Tags.DURATION));
            durationString = duration + " " + "min";
        }
        ((TextView) root.findViewById(R.id.episodeDuration)).setText(durationString);

        // buttons
        mEpisodePlay = (TextView) root.findViewById(R.id.episodePlay);
        mEpisodePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlay(getActivity());
            }
        });
        mEpisodeDownload = (TextView) root.findViewById(R.id.episodeDownload);
        mEpisodeDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDownload(getActivity());
            }
        });
        mEpisodeDelete = (TextView) root.findViewById(R.id.episodeDelete);
        mEpisodeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDelete(getActivity());
            }
        });
        // show notes
        String content = mFeedItem.get(FeedParser.Tags.CONTENT_ENCODED);
        WebView webView = (WebView) root.findViewById(R.id.episodeShowNotes);
        webView.loadData(content, "text/html; charset=UTF-8", null);

        // update buttons state
        updateButtons(getActivity());
        // analytics
        AnalyticsHelper.screen(getActivity(), EpisodeFragment.class.getSimpleName());
        return root;
    }

    /**
     * enqueue a download request
     *
     * @param context
     */
    private void onClickDownload(final Context context) {
        final String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        final String title = FeedsFragment.feeds[mPageNumber].title;
        final String desc = FeedsFragment.extractFeedItemTitle(mPageNumber, mFeedItem.get(FeedParser.Tags.TITLE));
        if (!DowDownloadManager.getInstance(context).isWiFiAvailable(context)) {
            ViewUtils.showWifiWarning(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enqueueRequest(context, url, title, desc);
                }
            });
            return;
        }
        enqueueRequest(context, url, title, desc);
        AnalyticsHelper.download(context, url);
    }

    /**
     * queue the request
     * @param context
     * @param url - download url
     * @param title notification title display
     * @param desc notification desc display
     */
    private void enqueueRequest(final Context context, String url, String title, String desc) {
        final long downloadID = DowDownloadManager.getInstance(context).enqueueRequest(url, title, desc);
        updateButtons(context);
        checkDownloadStatus(context, downloadID);
    }

    /**
     * issue: when the network is unavailable, the download is PAUSED without triggering an error intent
     * solution: manually check the download status after a few seconds
     * @param context
     * @param downloadID
     */
    private void checkDownloadStatus(final Context context, final long downloadID) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int status = DowDownloadManager.getInstance(context).getDownloadStatus(downloadID);
                if (status == DownloadManager.STATUS_PAUSED) {
                    DowDownloadManager.getInstance(context).cancelDownload(downloadID);
                    ViewUtils.showError(context, context.getString(R.string.server_unreachable));
                    updateButtons(context);
                }
            }
        }, 2000);
    }

    /**
     * enable/disable the buttons
     */
    private void updateButtons(Context context) {
        String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        int textColor = context.getResources().getColor(R.color.episode_header_text);
        int greyOut = context.getResources().getColor(R.color.colorTextSecondary);
        int accentColor = context.getResources().getColor(R.color.colorAccent);

        // no url - remove buttons
        if (url == null) {
            mEpisodePlay.setVisibility(View.INVISIBLE);
            mEpisodeDownload.setVisibility(View.INVISIBLE);
            mEpisodeDelete.setVisibility(View.INVISIBLE);
            return;
        }
        // download button - invisible if file was already downloaded or is downloading
        boolean isDownloaded = DowDownloadManager.getInstance(context).isDownloaded(url);
        boolean isDownloadInProgress = DowDownloadManager.getInstance(context).isDownloadInProgress(url);
        if (isDownloaded || isDownloadInProgress) {
            mEpisodeDownload.setTextColor(greyOut);
            mEpisodeDownload.setEnabled(false);
        } else {
            mEpisodeDownload.setTextColor(textColor);
            mEpisodeDownload.setEnabled(true);
        }
        // play button - highlighted if download completed
        int playColor = isDownloaded ? accentColor : textColor;
        mEpisodePlay.setTextColor(playColor);
        // delete button
        int deleteVisibility = isDownloaded ? View.VISIBLE : View.INVISIBLE;
        mEpisodeDelete.setVisibility(deleteVisibility);
    }

    private void onClickPlay(Context context) {
        String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        playEpisode(context, url);
    }

    public static void playEpisode(Context context, String url) {
        boolean isDownloaded = DowDownloadManager.getInstance(context).isDownloaded(url);
        // not downloaded - stream
        if (!isDownloaded) {
            AnalyticsHelper.playAudioStream(context, url);
            ViewUtils.playAudioStream(context, url);
            return;
        }
        // downloaded - play local file
        AnalyticsHelper.playLocalAudio(context, url);
        Uri uri = DowDownloadManager.getDownloadUri(url);
        ViewUtils.playLocalAudio(context, context.getString(R.string.choose_player), uri);
    }

    private void onClickDelete(final Context context) {
        final String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        ViewUtils.showDeleteWarning(context, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DowDownloadManager.delete(url);
                updateButtons(context);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "episode: onResume: +++  " + mDownloadCompleteReceiver);
        getActivity().registerReceiver(mDownloadCompleteReceiver, DowDownloadManager.getDownloadCompleteIntentFilter());
        updateButtons(getActivity());
    }

    @Override
    public void onPause() {
        Log.e(TAG, "episode: onPause: ---");
        getActivity().unregisterReceiver(mDownloadCompleteReceiver);
        super.onPause();
    }
}