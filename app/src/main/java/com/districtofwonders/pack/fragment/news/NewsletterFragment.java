package com.districtofwonders.pack.fragment.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.fragment.feed.FeedParser;
import com.districtofwonders.pack.util.DateUtils;

import java.util.HashMap;
import java.util.Map;

public class NewsletterFragment extends Fragment {

    private static final String TAG = MainActivity.TAG; //EpisodeFragment.class.getSimpleName();
    private static final String ARG_FEED_ITEM = "ARG_FEED_ITEM";

    public static Fragment newInstance(Map<String, String> feedItem) {
        NewsletterFragment fragment = new NewsletterFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_FEED_ITEM, (HashMap) feedItem);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        HashMap<String, String> mFeedItem = (HashMap<String, String>) arguments.getSerializable(ARG_FEED_ITEM);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.newsletter_fragment, null);

        // header
        String pubDate = DateUtils.getPubDate(mFeedItem.get(FeedParser.Tags.PUB_DATE));
        ((TextView) root.findViewById(R.id.newsletterPubDate)).setText(pubDate);
        ((TextView) root.findViewById(R.id.newsletterTitle)).setText(mFeedItem.get(FeedParser.Tags.TITLE));
        // show content
        WebView webView = (WebView) root.findViewById(R.id.webview);
        webView.loadData(mFeedItem.get(FeedParser.Tags.CONTENT_ENCODED), "text/html; charset=UTF-8", null);

        return root;
    }
}