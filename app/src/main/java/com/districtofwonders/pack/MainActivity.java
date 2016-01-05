package com.districtofwonders.pack;
/**
 * Author Vivz
 * Date 15/06/15
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.districtofwonders.pack.fragment.NotificationsFragment;
import com.districtofwonders.pack.fragment.feed.FeedsFragment;
import com.districtofwonders.pack.gcm.GcmHelper;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final boolean DEBUG = true;
    private static final String SELECTED_ITEM_ID = "selected_item_id";
    private static final String FIRST_TIME = "first_time";
    private static final String TAG = MainActivity.class.getSimpleName();
    final String[] fragments = {
            "com.districtofwonders.pack.fragment.feed.FeedsFragment",
            "com.districtofwonders.pack.fragment.AboutFragment"};
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId;
    private boolean mUserSawDrawer = false;
    private GcmHelper gcmHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
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
        mSelectedId = savedInstanceState == null ? R.id.drawer_nav_feed : savedInstanceState.getInt(SELECTED_ITEM_ID);
        navigate(mSelectedId);

        // gcm notifications handler
        Map<String, Boolean> topicsMap = NotificationsFragment.getTopicsMap();
        gcmHelper = new GcmHelper(this, topicsMap, new GcmHelper.RegistrationListener() {

            @Override
            public void success() {
            }

            @Override
            public void error(String error) {
                Log.e(TAG, "RegistrationListener:" + error);
                Toast.makeText(MainActivity.this, "Error while connecting to the notification server: " + error, Toast.LENGTH_LONG).show();
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

    private void navigate(int mSelectedId) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        if (mSelectedId == R.id.drawer_nav_feed) {
            setFragment(0);
        }
        if (mSelectedId == R.id.drawer_nav_about) {
            setFragment(1);
        }
    }

    private void setFragment(int position) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction(); // TODO refactor
        tx.replace(R.id.main_content, Fragment.instantiate(MainActivity.this, fragments[position]));
        tx.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setFeedsFragment(String topic) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        Fragment feedsFragment = FeedsFragment.newInstance(topic);
        tx.replace(R.id.main_content, feedsFragment);
        tx.commit();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();

        navigate(mSelectedId);
        return true;
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
        Log.e(TAG, "onPause:");
        gcmHelper.onPause(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String from = intent.getStringExtra(GcmHelper.NOTIFICATION_FROM);
        Log.e(TAG, "onNewIntent: from:" + from);
        // assert
        if (from == null) {
            Toast.makeText(this, "Malformed Notification", Toast.LENGTH_LONG).show();
            return;
        }
        // global notification - should probably launch a url
        if (from.startsWith("/topics/global")) {
            Toast.makeText(this, "Global Notification", Toast.LENGTH_LONG).show();
            return;
        }
        // feed notification
        setFeedsFragment(from);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume:");
        gcmHelper.onResume(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, mSelectedId);
    }
}
