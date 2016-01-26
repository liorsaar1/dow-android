package com.districtofwonders.pack;

/**
 * Created by liorsaar on 2015-12-16
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.districtofwonders.pack.fragment.AboutFragment;
import com.districtofwonders.pack.fragment.NotificationsFragment;
import com.districtofwonders.pack.fragment.PatreonFragment;
import com.districtofwonders.pack.fragment.feed.FeedsFragment;
import com.districtofwonders.pack.fragment.news.NewsletterListFragment;
import com.districtofwonders.pack.gcm.AnalyticsHelper;
import com.districtofwonders.pack.gcm.GcmHelper;
import com.districtofwonders.pack.util.DowDownloadManager;
import com.districtofwonders.pack.util.ViewUtils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String FIRST_TIME = "first_time";
    private static Map<Integer, String> fragmentMap;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;
    private GcmHelper gcmHelper;

    public static void setChildFragment(FragmentActivity activity, String className) {
        setChildFragment(activity, Fragment.instantiate(activity, className));
    }

    public static void setChildFragment(FragmentActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(fragment.getClass().getName())
                .replace(R.id.main_content, fragment)
                .commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // init fragments
        initFragments();

        // init navigation
        Toolbar mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);
        NavigationView mDrawer = (NavigationView) findViewById(R.id.main_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        if (!didUserSeeDrawer()) {
            showDrawer();
            markDrawerSeen();
        } else {
            hideDrawer();
        }
        mSelectedId = getSelectedId(savedInstanceState);
        navigate(mSelectedId);

        // gcm notifications handler
        initGcmHelper(this);

        // check if invoked from notification
        String from = getIntent().getStringExtra(GcmHelper.NOTIFICATION_FROM);
        if (from != null) {
            handleNotification(getIntent());
        }
    }

    private int getSelectedId(Bundle savedInstanceState) {
        // get the id from the saved instance or prefs
        if (savedInstanceState == null) {
            mSelectedId = getNavSelection(this);
        } else {
            mSelectedId = savedInstanceState.getInt(SELECTED_ITEM_ID);
        }
        // in case of app upgrade, the saved id might be stale
        if (!fragmentMap.containsKey(mSelectedId)) {
            mSelectedId = R.id.drawer_nav_feed;
        }
        return mSelectedId;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * handle all GCM setup
     * get the topics map from the prefs, and register them to gcm
     *
     * @param activity - the parent activity to be invoked when a notification comes in
     */
    private void initGcmHelper(final Activity activity) {
        // topics map from prefs
        Map<String, Boolean> topicsMap = NotificationsFragment.getRegistrationMap(this);
        // progress
        final ProgressDialog progressDialog = ProgressDialog.show(activity, getString(R.string.please_wait), getString(R.string.contacting_notification_server), true);
        // start the background service
        gcmHelper = new GcmHelper(activity, topicsMap, new GcmHelper.RegistrationListener() {

            @Override
            public void success() {
                progressDialog.dismiss();
            }

            @Override
            public void error(String error) {
                progressDialog.dismiss();
                Log.e(TAG, "RegistrationListener:" + error);
                ViewUtils.showError(activity, error + "\n\n" + getString(R.string.notifications_disabled_restart));
            }
        });
    }

    private boolean didUserSeeDrawer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserSawDrawer = sharedPreferences.getBoolean(FIRST_TIME, false);
        return mUserSawDrawer;
    }

    private void markDrawerSeen() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserSawDrawer = true;
        sharedPreferences.edit().putBoolean(FIRST_TIME, mUserSawDrawer).apply();
    }

    private void showDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void hideDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void initFragments() {
        fragmentMap = new HashMap<>();
        fragmentMap.put(R.id.drawer_nav_feed, FeedsFragment.class.getName());
        fragmentMap.put(R.id.drawer_nav_about, AboutFragment.class.getName());
        fragmentMap.put(R.id.drawer_nav_notifications, NotificationsFragment.class.getName());
        fragmentMap.put(R.id.drawer_nav_patreon, PatreonFragment.class.getName());
        fragmentMap.put(R.id.drawer_nav_newsletter, NewsletterListFragment.class.getName());
    }

    private void handleNotificationGlobal(Intent intent) {
        Bundle data = intent.getExtras().getParcelable(GcmHelper.NOTIFICATION_DATA);
        String message = data.getString(FeedsFragment.NOTIFICATION_DATA_MESSAGE);
        String url = data.getString(FeedsFragment.NOTIFICATION_DATA_URL);
        if (url != null) {
            Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browseIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();

        navigate(mSelectedId);
        return true;
    }

    private void navigate(int selectedId) {
        AnalyticsHelper.navigate( this, fragmentMap.get(selectedId));
        mDrawerLayout.closeDrawer(GravityCompat.START);
        setFragment(selectedId);
    }

    private void setFragment(int navId) {
        setRootFragment(this, fragmentMap.get(navId));
    }

    public static void setRootFragment(FragmentActivity activity, String className) {
        Fragment fragment = Fragment.instantiate(activity, className);
        activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "main: onPause ---");
        gcmHelper.onPause(this);
        DowDownloadManager.getInstance(this).onPause(this);
        setNavSelection(this, mSelectedId);
        super.onPause();
    }

    private void setNavSelection(Context context, int value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt(SELECTED_ITEM_ID, value).apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String from = intent.getStringExtra(GcmHelper.NOTIFICATION_FROM);
        Log.e(TAG, "onNewIntent: from:" + from);
        // assert
        if (from == null) {
            ViewUtils.showError(this, getString(R.string.malformed_notification));
            return;
        }
        // 'from' exists - handle notification
        handleNotification(intent);
    }

    private void handleNotification(Intent intent) {
        String from = intent.getStringExtra(GcmHelper.NOTIFICATION_FROM);
        // analytics
        AnalyticsHelper.notificationClicked(this, from);
        // global notification - used for newsletter for now
        if (from.startsWith(FeedsFragment.FEED_TOPICS_GLOBAL)) {
            setFragment(R.id.drawer_nav_newsletter);
            return;
        }
        // feed notification
        setFeedsFragment(from);
    }

    private void setFeedsFragment(String topic) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Fragment feedsFragment = FeedsFragment.newInstance(topic);
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(FeedsFragment.class.getName())
                .replace(R.id.main_content, feedsFragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: main");
        gcmHelper.onResume(this);
        DowDownloadManager.getInstance(this).onResume(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }

    private int getNavSelection(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(SELECTED_ITEM_ID, R.id.drawer_nav_feed);
    }
}
