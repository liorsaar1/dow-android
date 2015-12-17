package com.districtofwonders.pack;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class FeedsFragment extends Fragment {

    public static final Feed[] feeds = {
            new Feed() {{ title = "StarShipSofa"; url = "http://www.starshipsofa.com/feed/"; }},
            new Feed() {{ title = "Far Fetched Fables"; url = "http://www.starshipsofa.com/feed/"; }},
            new Feed() {{ title = "Tales to Terrify"; url = "http://www.starshipsofa.com/feed/"; }}
    };

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private FeedsPagerAdapter mAdapter;
    TextView tv;

    public static Fragment newInstance(Context context) {
        return new FeedsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.feed_fragment, null);

        mTabLayout = (TabLayout) root.findViewById(R.id.feed_tab_layout);
        mAdapter = new FeedsPagerAdapter(getActivity().getSupportFragmentManager());
        mPager = (ViewPager) root.findViewById(R.id.feed_view_pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        tv = (TextView) root.findViewById(R.id.feed_text);

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


}

class Feed {
    public String title;
    public String url;
}

//class FeedEnum {
//    public enum Type {
//        SSS("StarShipSofa"),
//        FFF("Far Fetched Fables"),
//        TTT("Tales to Terrify");
//
//        private final String value;
//
//        Type(String value) {
//            this.value = value;
//        }
//
//        public String getValue() {
//            return value;
//        }
//    }
//}

class FeedsPagerAdapter extends FragmentStatePagerAdapter {

    public FeedsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return FeedViewFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return FeedsFragment.feeds.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return FeedsFragment.feeds[position].title;
    }
}
