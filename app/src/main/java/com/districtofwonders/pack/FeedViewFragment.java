package com.districtofwonders.pack;

import android.content.Context;
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

import org.xmlpull.v1.XmlPullParserException;

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
        mFeedRecyclerAdapter = new FeedRecyclerAdapter(getActivity(), mList);
        mRecyclerView.setAdapter(mFeedRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRecyclerView;
    }

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
                        Log.e(TAG, response.substring(300,600));
                        mList = parse(response);
                        mFeedRecyclerAdapter.setData(mList);
                        return;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        return;
                    }
                });
        DowSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    private List<Map<String, String>> parse(String xmlString) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            list = RssFeedParser.parse(xmlString);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}


class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {
    private List<Map<String, String>> list = new ArrayList<>();
    private LayoutInflater inflater;

    public FeedRecyclerAdapter(Context context, List<Map<String, String>> list) {
        inflater = LayoutInflater.from(context);
        this.list = list;
    }

    public void setData(List<Map<String, String>> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public FeedRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View root = inflater.inflate(R.layout.custom_row, viewGroup, false);
        FeedRecyclerViewHolder holder = new FeedRecyclerViewHolder(root);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedRecyclerViewHolder feedRecyclerViewHolder, int i) {
        feedRecyclerViewHolder.textView.setText(list.get(i).get("title"));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class FeedRecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public FeedRecyclerViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text_superhero);
        }
    }
}