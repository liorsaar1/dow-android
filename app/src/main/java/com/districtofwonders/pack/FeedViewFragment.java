package com.districtofwonders.pack;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.districtofwonders.pack.util.RssFeedParser;
import com.districtofwonders.pack.util.ViewUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liorsaar on 2015-12-16
 */
public class FeedViewFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = FeedViewFragment.class.getSimpleName();
    RecyclerView mRecyclerView;
    FeedRecyclerAdapter mFeedRecyclerAdapter;
    private String mUrl;
    private List<Map<String, String>> mList = new ArrayList<>();

    public FeedViewFragment() {
    }

    public static FeedViewFragment newInstance(int pageNumber) {
        FeedViewFragment feedViewFragment = new FeedViewFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_PAGE, pageNumber);
        feedViewFragment.setArguments(arguments);
        return feedViewFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        int pageNumber = arguments.getInt(ARG_PAGE); // TODO move to load()
        // feed url
        mUrl = FeedsFragment.feeds[pageNumber].url;
        // ui
        mRecyclerView = new RecyclerView(getActivity());
        mFeedRecyclerAdapter = new FeedRecyclerAdapter(getActivity(), mList, mFeedItemOnClickListener);
        mRecyclerView.setAdapter(mFeedRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRecyclerView;
    }

    private FeedRecyclerAdapter.OnClickListener mFeedItemOnClickListener = new FeedRecyclerAdapter.OnClickListener() {
        @Override
        public void onClickLink(int position) {
            String link = mList.get(position).get("link");
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
        }

        @Override
        public void onClickPlay(int position) {

        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        load(mUrl);
    }

    private void load(String url) {
        if (mList.size() != 0) {
            mFeedRecyclerAdapter.setData(mList);
            return;
        }

        if(false) {
            try {
                String xmlString = ViewUtils.getAssetAsString(getActivity(), "feed_sss.xml");
                mList = parse(xmlString);
                mFeedRecyclerAdapter.setData(mList);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, response.substring(300,600));  // DEBUG
                        mList = parse(response);
                        mFeedRecyclerAdapter.setData(mList);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { // TODO error
                    }
                });
        DowSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private List<Map<String, String>> parse(String xmlString) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            list = RssFeedParser.parse(xmlString);
        } catch (Exception e) { // TODO error handling
            e.printStackTrace();
        }
        return list;
    }

}


class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {
    private static final String TAG = FeedRecyclerAdapter.class.getSimpleName();
    private final OnClickListener listener;
    private List<Map<String, String>> list = new ArrayList<>();
    private LayoutInflater inflater;

    public interface OnClickListener {
        void onClickLink(int position);
        void onClickPlay(int position);
    }

    public FeedRecyclerAdapter(Context context, List<Map<String, String>> list, OnClickListener listener) {
        inflater = LayoutInflater.from(context);
        this.list = list;
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
        feedRecyclerViewHolder.title.setText(list.get(position).get("title"));
        feedRecyclerViewHolder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClickLink(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FeedRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public FeedRecyclerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.feed_item_title);
        }
    }
}