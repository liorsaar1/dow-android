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

import java.util.ArrayList;

/**
 * Created by liorsaar on 2015-12-16
 */
public class FeedViewFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    RecyclerView mRecyclerView;
    FeedRecyclerAdapter mFeedRecyclerAdapter;
    private String mUrl;

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
        int pageNumber = arguments.getInt(ARG_PAGE);
        // feed url
        mUrl = FeedsFragment.feeds[pageNumber].url;
        // ui
        mRecyclerView = new RecyclerView(getActivity());
        mFeedRecyclerAdapter = new FeedRecyclerAdapter(getActivity());
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("ZZZZZ", response);
                        mFeedRecyclerAdapter.setData(response);
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

}


class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {
    private ArrayList<String> list = new ArrayList<>();
    private LayoutInflater inflater;

    public FeedRecyclerAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setData(String xmlString) {
        list.add("A " );
        list.add("B " );
        list.add("C " );
        list.add("D " );
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
        feedRecyclerViewHolder.textView.setText(list.get(i));
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