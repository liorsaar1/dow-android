package com.districtofwonders.pack.fragment.feed;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.districtofwonders.pack.R;
import com.districtofwonders.pack.util.DateUtils;
import com.districtofwonders.pack.util.DowDownloadManager;
import com.districtofwonders.pack.util.ViewUtils;

import java.util.HashMap;
import java.util.Map;

public class EpisodeFragment extends Fragment {

    public static final String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";
    private static final String ARG_FEED_ITEM = "ARG_FEED_ITEM";

    private Map<String, String> mFeedItem;
    private int mPageNumber;
    private TextView mEpisodePlay;
    private TextView mEpisodeDownload;
    /**
     * when a download is completed - update the play/download buttons state
     */
    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
        // show notes
        String content = mFeedItem.get(FeedParser.Tags.CONTENT_ENCODED);
        WebView webView = (WebView) root.findViewById(R.id.episodeShowNotes);
        webView.loadData(content, "text/html; charset=UTF-8", null);

        // update buttons state
        updateButtons(getActivity());

        // register the receiver
        IntentFilter downloadCompleteIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(mDownloadCompleteReceiver, downloadCompleteIntentFilter);
        return root;
    }

    // enqueue a download request
    private void onClickDownload(final Context context) {
        final String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        final String title = mFeedItem.get(FeedParser.Tags.TITLE);
        if (!DowDownloadManager.getInstance(context).isWiFiAvailable(context)) {
            ViewUtils.showWifiWarning(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    enqueueRequest(context, mPageNumber, url, title);
                }
            });
        }
        enqueueRequest(context, mPageNumber, url, title);
    }

    private void enqueueRequest(Context context, int pageNumber, String url, String title) {
        DowDownloadManager.getInstance(context).enqueueRequest(pageNumber, url, title);
        updateButtons(context);
    }

    /**
     * enable/disable the buttons
     */
    private void updateButtons(Context context) {
        int greyOut = context.getResources().getColor(R.color.colorTextSecondary);
        String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        // play button - disabled if no url
        if (url == null) {
            mEpisodePlay.setTextColor(greyOut);
            mEpisodePlay.setEnabled(false);
            mEpisodeDownload.setTextColor(greyOut);
            mEpisodeDownload.setEnabled(false);
            return;
        }
        // download button - greyed out if the file was already downloaded or is downloading
        boolean isDownloadDisabled = DowDownloadManager.isDownloaded(url) || DowDownloadManager.getInstance(context).isDownloadInProgress(url);
        if (isDownloadDisabled) {
            mEpisodeDownload.setTextColor(greyOut);
            mEpisodeDownload.setEnabled(false);
        }
    }

    private void onClickPlay(Context context) {
        String url = mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL);
        boolean isDownloaded = DowDownloadManager.isDownloaded(url);
        // not downloaded - stream
        if (!isDownloaded) {
            ViewUtils.playAudio(context, mFeedItem.get(FeedParser.Keys.ENCLOSURE_URL));
            return;
        }
        // downloaded - play local file
        Uri uri = DowDownloadManager.getDownloadUri(url);
        ViewUtils.playLocalAudio(context, context.getString(R.string.choose_player), uri);
    }


}