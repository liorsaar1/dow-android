package com.districtofwonders.pack.fragment.feed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.districtofwonders.pack.DowSingleton;
import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.util.DateUtils;
import com.districtofwonders.pack.util.ViewUtils;

import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by liorsaar on 2015-12-16
 */
public class FeedViewFragment extends Fragment {
    public static final String ARG_PAGE_NUMBER = "ARG_PAGE_NUMBER";
    private static final String TAG = FeedViewFragment.class.getSimpleName();
    private FeedRecyclerAdapter mFeedRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mPageNumber;
    private List<Map<String, String>> mList = new ArrayList<>();
    private TextView mError;

    private FeedRecyclerAdapter.OnClickListener mFeedItemOnClickListener = new FeedRecyclerAdapter.OnClickListener() {
        @Override
        public void onClickLink(int position) {
            String link = mList.get(position).get("link");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        }

        @Override
        public void onClickPlay(int position) {
            String link = mList.get(position).get("enclosure.url");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        }

        @Override
        public void onClickDownload(int position) {

        }
    };

    public FeedViewFragment() {
    }

    public static FeedViewFragment newInstance(int pageNumber) {
        FeedViewFragment feedViewFragment = new FeedViewFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PAGE_NUMBER, pageNumber);
        feedViewFragment.setArguments(arguments);
        return feedViewFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mPageNumber = arguments.getInt(ARG_PAGE_NUMBER);
        // ui
        View root = inflater.inflate(R.layout.feed_view_fragment, container, false);
        mError = (TextView) root.findViewById(R.id.feed_view_error);

        mSwipeRefreshLayout = (SwipeRefreshLayout)root.findViewById(R.id.feed_view_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearData();
                load();
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) root.findViewById(R.id.feed_view_recycler);
        mFeedRecyclerAdapter = new FeedRecyclerAdapter(getActivity(), mList, mPageNumber, mFeedItemOnClickListener);
        mRecyclerView.setAdapter(mFeedRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load();
    }

    private void load() {
        mError.setVisibility(View.GONE);
        if (mList.size() != 0) {
            mFeedRecyclerAdapter.setData(mList);
            return;
        }
        // DEBUG - read an xml
        if (false) {
            try {
                String xmlString = ViewUtils.getAssetAsString(getActivity(), "feed/feed_sss.xml");
                setData(xmlString);
            } catch (Exception e) {
                setError(e.getMessage());
            }
            return;
        }

        // get the feed url
        String url = FeedsFragment.feeds[mPageNumber].url;

        // setup ui
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            setData(response);
                        } catch (Exception e) {
                            setError(e.getMessage()); // parse error
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setError(error); // network error
                    }
                });
        DowSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void setError(VolleyError error) {
        String message = "Server Error " + error.networkResponse.statusCode;
        setError(message);
    }

    private void setError(String message) {
        // in production - do not show detailed error
        if (!MainActivity.DEBUG) {
            message = "Server Error";
        }
        mError.setVisibility(View.VISIBLE);
        mError.setText(message);
    }

    private void setData(String xmlString) throws IOException, XmlPullParserException, ParserConfigurationException, SAXException {
        if (MainActivity.DEBUG)
            Log.e(TAG, xmlString.substring(300, 600));

        FeedParser parser = new FeedParser(xmlString);
        mList = parser.getItems();
        mFeedRecyclerAdapter.setData(mList);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void clearData() {
        mList.clear();
        mFeedRecyclerAdapter.setData(mList);
    }
}

class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {
    private static final String TAG = FeedRecyclerAdapter.class.getSimpleName();
    private final OnClickListener listener;
    private final int pageNumber;
    private List<Map<String, String>> list = new ArrayList<>();
    private LayoutInflater inflater;

    public FeedRecyclerAdapter(Context context, List<Map<String, String>> list, int pageNumber, OnClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.pageNumber = pageNumber;
        this.listener = listener;
    }

    public void setData(List<Map<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public FeedRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View root = inflater.inflate(R.layout.feed_item_row, viewGroup, false);
        return new FeedRecyclerViewHolder(root);
    }

    @Override
    public void onBindViewHolder(FeedRecyclerViewHolder feedRecyclerViewHolder, final int position) {
        feedRecyclerViewHolder.date.setText(getPubDate(position));
        feedRecyclerViewHolder.title.setText(getTitle(position));
        String duration = getDuration(position);
        feedRecyclerViewHolder.duration.setText(duration);
        int durationVisibility = duration == null ? View.GONE : View.VISIBLE;
        feedRecyclerViewHolder.duration.setVisibility(durationVisibility);
        feedRecyclerViewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickLink(position);
            }
        });
        feedRecyclerViewHolder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickPlay(position);
            }
        });
        feedRecyclerViewHolder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickDownload(position);
            }
        });
    }

    private String getTitle(int position) {
        String title = list.get(position).get(FeedParser.Tags.TITLE);
        String feedTitle = FeedsFragment.feeds[pageNumber].title;
        if (title.toLowerCase().startsWith(feedTitle.toLowerCase())) {
            title = title.substring(feedTitle.length()+1);
        }
        return title;
    }

    private String getPubDate(int position) {
        String pubDateString = list.get(position).get(FeedParser.Tags.PUB_DATE);
        Calendar cal = DateUtils.getPubDateCal(pubDateString);
        String month = DateUtils.getMonthString(cal);
        String day = DateUtils.getDayString(cal);
        return month +"\n" + day;
    }

    private String getDuration(int position) {
        String durationString = list.get(position).get(FeedParser.Tags.DURATION);
        if (durationString == null) {
            return null;
        }
        int minutes = DateUtils.getMinutes(durationString);
        return minutes + " min";
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnClickListener {
        void onClickLink(int position);
        void onClickPlay(int position);
        void onClickDownload(int position);
    }

    static class FeedRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView date, title, download, duration;
        View play;

        public FeedRecyclerViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.feed_item_date);
            title = (TextView) itemView.findViewById(R.id.feed_item_title);
            play = itemView.findViewById(R.id.feed_item_play);
            download = (TextView) itemView.findViewById(R.id.feed_item_download);
            duration = (TextView) itemView.findViewById(R.id.feed_item_duration);
        }
    }
}