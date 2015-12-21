package com.districtofwonders.pack.fragment.feed;

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

import com.districtofwonders.pack.R;

import java.util.ArrayList;
import java.util.List;

public class FeedsFragment extends Fragment {

    public static final FeedDesc[] feeds = {
            new FeedDesc() {{ title = "StarShipSofa"; url = "http://www.starshipsofa.com/feed/"; }},
            new FeedDesc() {{ title = "Far Fetched Fables"; url = "http://farfetchedfables.com/feed/"; }},
            new FeedDesc() {{ title = "Tales to Terrify"; url = "http://talestoterrify.com/feed/"; }}
    };

    private TabLayout mTabLayout;
    private ViewPager mPager;
    private FeedsPagerAdapter mAdapter;

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
        return root;
    }
}

class FeedDesc {
    public String title;
    public String url;
}

class FeedsPagerAdapter extends FragmentStatePagerAdapter {

    List<Fragment> fragments = new ArrayList<>();

    public FeedsPagerAdapter(FragmentManager fm) {
        super(fm);
        for (int i = 0; i < FeedsFragment.feeds.length; i++) {
            fragments.add(FeedViewFragment.newInstance(i));
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
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
