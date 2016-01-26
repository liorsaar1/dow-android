package com.districtofwonders.pack.fragment.news;

/**
 * Created by liorsaar on 2016-01-11
 */

import android.content.Context;
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
import com.districtofwonders.pack.BuildConfig;
import com.districtofwonders.pack.DowSingleton;
import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.fragment.feed.FeedParser;
import com.districtofwonders.pack.gcm.AnalyticsHelper;
import com.districtofwonders.pack.util.DateUtils;
import com.districtofwonders.pack.util.ViewUtils;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

public class NewsletterListFragment extends Fragment {
    private static final String TAG = MainActivity.TAG;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mError;
    private List<Map<String, String>> mList = new ArrayList<>();
    private NewsletterRecyclerAdapter mNewsletterRecyclerAdapter;

    private NewsletterRecyclerAdapter.OnClickListener mListener = new NewsletterRecyclerAdapter.OnClickListener() {
        @Override
        public void onClick(int position) {
            AnalyticsHelper.viewNewsletter(getActivity(), mList.get(position).get(FeedParser.Tags.PUB_DATE));
            MainActivity.setChildFragment(getActivity(), NewsletterFragment.newInstance(mList.get(position)));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.newsletter_list_fragment, container, false);
        mError = (TextView) view.findViewById(R.id.newsletterError);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.newsletterSwipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearData();
                String url = getActivity().getString(R.string.link_newsletter);
                load(url);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.newsletterRecycler);
        mNewsletterRecyclerAdapter = new NewsletterRecyclerAdapter(getActivity(), mList, mListener);
        mRecyclerView.setAdapter(mNewsletterRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String url = getActivity().getString(R.string.link_newsletter);
        load(url);
    }

    private void load(String url) {
        mError.setVisibility(View.GONE);
        if (mList.size() != 0) {
            mNewsletterRecyclerAdapter.setData(mList);
            return;
        }
        // DEBUG - read an xml
        if (false) {
            try {
                String xmlString = ViewUtils.getAssetAsString(getActivity(), "newsletter/newsletter_feed.xml");
                setData(xmlString);
            } catch (Exception e) {
                setError(e.getMessage());
            }
            return;
        }

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
                            mSwipeRefreshLayout.setRefreshing(false);
                        } catch (Exception e) {
                            setError(e.getMessage()); // parse error
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        setError(error); // network error
                    }
                });
        DowSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private void setError(VolleyError volleyError) {
        String statusCode = (volleyError.networkResponse != null) ? "" + volleyError.networkResponse.statusCode : "";
        String message = getActivity().getString(R.string.server_error) + " " + statusCode;
        setError(message);
    }

    private void setError(String message) {
        // in production - do not show detailed error
        if (!BuildConfig.DEBUG) {
            message = getActivity().getString(R.string.server_error);
        }
        mError.setVisibility(View.VISIBLE);
        mError.setText(message);
    }

    private void setData(String xmlString) throws IOException, ParserConfigurationException, SAXException {
        if (BuildConfig.DEBUG)
            Log.e(TAG, xmlString.substring(300, 600));

        FeedParser parser = new FeedParser(xmlString);
        mList = parser.getItems();
        mNewsletterRecyclerAdapter.setData(mList);
    }

    private void clearData() {
        mList.clear();
        mNewsletterRecyclerAdapter.setData(mList);
    }

}

class NewsletterRecyclerAdapter extends RecyclerView.Adapter<NewsletterRecyclerAdapter.NewsletterRecyclerViewHolder> {
    private final String TAG = NewsletterRecyclerAdapter.class.getSimpleName();
    private final OnClickListener listener;
    private List<Map<String, String>> list = new ArrayList<>();
    private LayoutInflater inflater;

    public NewsletterRecyclerAdapter(Context context, List<Map<String, String>> list, OnClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.list = list;
        this.listener = listener;
    }

    public void setData(List<Map<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public NewsletterRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, final int i) {
        View root = inflater.inflate(R.layout.newsletter_item_row, viewGroup, false);
        return new NewsletterRecyclerViewHolder(root);
    }

    @Override
    public void onBindViewHolder(NewsletterRecyclerViewHolder holder, final int position) {
        holder.pubDate.setText(getPubDate(position));
        holder.title.setText(getTitle(position));
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(position);
            }
        });
    }

    private String getTitle(int position) {
        return list.get(position).get(FeedParser.Tags.TITLE);
    }

    private String getPubDate(int position) {
        String pubDateString = list.get(position).get(FeedParser.Tags.PUB_DATE);
        return DateUtils.getMmDd(pubDateString);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    interface OnClickListener {
        void onClick(int position);
    }

    class NewsletterRecyclerViewHolder extends RecyclerView.ViewHolder {

        View root;
        TextView title, pubDate;

        public NewsletterRecyclerViewHolder(View itemView) {
            super(itemView);
            pubDate = (TextView) itemView.findViewById(R.id.newsletterItemPubDate);
            title = (TextView) itemView.findViewById(R.id.newsletterItemTitle);
            root = itemView;
        }
    }
}