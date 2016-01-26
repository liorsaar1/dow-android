package com.districtofwonders.pack.fragment.feed;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.districtofwonders.pack.R;
import com.districtofwonders.pack.gcm.AnalyticsHelper;

import java.util.ArrayList;
import java.util.List;

public class FeedsFragment extends Fragment {

    public static final String NOTIFICATION_DATA_MESSAGE = "message";
    public static final String NOTIFICATION_DATA_URL = "url";

    public static final FeedDesc[] feeds = {
            new FeedDesc() {{
                title = "StarShipSofa";
                topic = "/topics/sss";
                url = "http://www.starshipsofa.com/feed/";
            }},
            new FeedDesc() {{
                title = "Far Fetched Fables";
                topic = "/topics/fff";
                url = "http://farfetchedfables.com/feed/";
            }},
            new FeedDesc() {{
                title = "Tales to Terrify";
                topic = "/topics/ttt";
                url = "http://talestoterrify.com/feed/";
            }}
    };
    public static final String FEED_TOPICS_GLOBAL = "/topics/global";
    private static final String ARG_TOPIC = "topic";
    private static final int[] feedIcon = {R.drawable.ic_sss, R.drawable.ic_fff, R.drawable.ic_ttt, R.drawable.ic_dow};
    private static List<Bitmap> feedIconBitmap;
    private ViewPager mPager;

    public static Fragment newInstance(String topic) {
        FeedsFragment fragment = new FeedsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_TOPIC, topic);
        fragment.setArguments(arguments);
        return fragment;
    }

    /**
     * create a notification with feed name and message, that will launch the right feed
     *
     * @param context       context
     * @param pendingIntent the class to be launched by the notification
     * @param from          incoming 'from' field (topic)
     * @param data          incoming json bundle
     * @return notification
     */
    public static Notification getNotification(Context context, PendingIntent pendingIntent, String from, Bundle data) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String contentTitle;
        Bitmap bitmap;
        int icon;
        if (from.startsWith(FEED_TOPICS_GLOBAL)) {
            contentTitle = context.getString(R.string.app_label);
            icon = R.mipmap.ic_launcher;
            bitmap = getNotificationBitmap(context, 3);
        } else {
            int feedIndex = FeedDesc.getFeedIndex(feeds, from);
            contentTitle = feeds[feedIndex].title;
            icon = feedIcon[feedIndex];
            bitmap = getNotificationBitmap(context, feedIndex);
        }

        String message = data.getString(NOTIFICATION_DATA_MESSAGE);
        AnalyticsHelper.notificationReceived(context, from, message);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setLargeIcon(bitmap)
                .setContentTitle(contentTitle)
                .setContentText(message)
                .setExtras(data)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        return notificationBuilder.build();
    }

    // notification display bitmap
    private static Bitmap getNotificationBitmap(Context context, int feedIndex) {
        if (feedIconBitmap == null) {
            feedIconBitmap = new ArrayList<>();
            for (int iconResId : feedIcon) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iconResId);
                feedIconBitmap.add(bitmap);
            }
        }
        return feedIconBitmap.get(feedIndex);
    }

    public static String extractFeedItemTitle(int pageNumber, String title) {
        String feedTitle = FeedsFragment.feeds[pageNumber].title;
        if (title.toLowerCase().startsWith(feedTitle.toLowerCase())) {
            title = title.substring(feedTitle.length() + 1);
        }
        return title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.feed_fragment, null);

        TabLayout mTabLayout = (TabLayout) root.findViewById(R.id.feed_tab_layout);
        FeedsPagerAdapter mAdapter = new FeedsPagerAdapter(getChildFragmentManager());
        mPager = (ViewPager) root.findViewById(R.id.feed_view_pager);
        mPager.setAdapter(mAdapter);
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
        mTabLayout.setupWithViewPager(mPager);

        mPager.addOnPageChangeListener(new AnalyticsOnPageChangeListener(mTabLayout));

        // display selected page
        Bundle arguments = getArguments();
        if (arguments != null) {
            String topic = arguments.getString(ARG_TOPIC);
            if (topic != null) {
                setFeed(topic);
            }
        } else {
            // none selected - screen analytics
            screen(getActivity(), 0);
        }
        return root;
    }

    public static void screen(Context context, int pageNumber) {
        String feedTitle = FeedsFragment.feeds[pageNumber].title;
        AnalyticsHelper.screen(context, feedTitle);
    }

    public void setFeed(String topic) {
        int pageNumber = FeedDesc.getFeedIndex(feeds, topic);
        mPager.setCurrentItem(pageNumber);
    }

    /**
     * custom listener, to send analytics for onPageSelected events
     */
    class AnalyticsOnPageChangeListener extends TabLayout.TabLayoutOnPageChangeListener {

        public AnalyticsOnPageChangeListener(TabLayout tabLayout) {
            super(tabLayout);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            screen(getActivity(), position);
        }
    }
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
