package com.districtofwonders.pack.fragment.feed;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.districtofwonders.pack.R;
import com.districtofwonders.pack.util.DateUtils;

import java.util.HashMap;
import java.util.Map;

public class EpisodeFragment extends Fragment {

    public static final String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";
    private static final String ARG_FEED_ITEM = "ARG_FEED_ITEM";

    private Map<String, String> mFeedItem;
    private int mPageNumber;

    public static Fragment newInstance(int pageNumber, Map<String, String> feedItem) {
        EpisodeFragment fragment = new EpisodeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PAGE_NUMBER, pageNumber);
        arguments.putSerializable(ARG_FEED_ITEM, (HashMap)feedItem);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mPageNumber = arguments.getInt(ARG_PAGE_NUMBER);
        mFeedItem = (HashMap<String, String>) arguments.getSerializable(ARG_FEED_ITEM);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.episode_fragment, null);

        // feed name
        ((TextView)root.findViewById(R.id.episodeFeedName)).setText(FeedsFragment.feeds[mPageNumber].title);
        // episode title
        String title = FeedsFragment.extractFeedItemTitle(mPageNumber, mFeedItem.get(FeedParser.Tags.TITLE));
        ((TextView)root.findViewById(R.id.episodeTitle)).setText(title);
        // date + duration
        String pubDate = DateUtils.getPubDate(mFeedItem.get(FeedParser.Tags.PUB_DATE));
        if (mFeedItem.get(FeedParser.Tags.DURATION) != null) {
            int duration = DateUtils.getMinutes(mFeedItem.get(FeedParser.Tags.DURATION));
            pubDate += " - " + duration + " " + "min";
        }
        ((TextView)root.findViewById(R.id.episodePubDate)).setText(pubDate);

        Spanned showNotes = Html.fromHtml(mFeedItem.get(FeedParser.Tags.CONTENT_ENCODED));
        ((TextView)root.findViewById(R.id.episodeShowNotes)).setText(showNotes);
        ((TextView)root.findViewById(R.id.episodeShowNotes)).setMovementMethod(new ScrollingMovementMethod());

        return root;
    }
}