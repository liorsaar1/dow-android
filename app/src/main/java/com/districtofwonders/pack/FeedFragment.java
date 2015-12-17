package com.districtofwonders.pack;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.districtofwonders.pack.FeedFragment.FeedViewFragment;

import java.util.ArrayList;

public class FeedFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private FeedPagerAdapter mAdapter;
    TextView tv;

    public static Fragment newInstance(Context context) {
        FeedFragment feedFragment = new FeedFragment();
        return feedFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.feed_fragment, null);

        mTabLayout = (TabLayout) root.findViewById(R.id.feed_tab_layout);
        mAdapter = new FeedPagerAdapter(getActivity().getSupportFragmentManager());
        mPager = (ViewPager) root.findViewById(R.id.feed_view_pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        tv = (TextView) root.findViewById(R.id.feed_text);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                test();
            }
        }, 1000);

        return root;
    }

    private void test() {
        String url = "http://www.starshipsofa.com/feed/";
        tv.setText(url);

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        tv.setText(response);
                        return;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tv.setText(error.getMessage());
                        return;
                        // Handle error
                    }
                });
        DowSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }

    public static class FeedViewFragment extends Fragment {
        public static final java.lang.String ARG_PAGE = "ARG_PAGE";

        public FeedViewFragment() {
        }

        public static FeedViewFragment newInstance(int pageNumber) {
            FeedViewFragment myFragment = new FeedViewFragment();
            Bundle arguments = new Bundle();
            arguments.putInt(ARG_PAGE, pageNumber);
            myFragment.setArguments(arguments);
            return myFragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            int pageNumber = arguments.getInt(ARG_PAGE);
            RecyclerView recyclerView = new RecyclerView(getActivity());
            recyclerView.setAdapter(new FeedRecyclerAdapter(getActivity(), pageNumber));
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            return recyclerView;
        }
    }
}

class Feed {
    public enum Type {
        SSS("StarShipSofa"),
        FFF("Far Fetched Fables"),
        TTT("Tales to Terrify");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}

class FeedPagerAdapter extends FragmentStatePagerAdapter {

    public FeedPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        FeedViewFragment myFragment = FeedViewFragment.newInstance(position);
        return myFragment;
    }

    @Override
    public int getCount() {
        return Feed.Type.values().length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Feed.Type.values()[position].getValue();
    }
}

class FeedRecyclerAdapter extends RecyclerView.Adapter<FeedRecyclerAdapter.FeedRecyclerViewHolder> {
    private ArrayList<String> list = new ArrayList<>();
    private LayoutInflater inflater;

    public FeedRecyclerAdapter(Context context, int pageNumber) {
        inflater = LayoutInflater.from(context);
        list.add("A " + pageNumber);
        list.add("B " + pageNumber);
        list.add("C " + pageNumber);
        list.add("D " + pageNumber);
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