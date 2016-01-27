package com.districtofwonders.pack;

import android.app.Application;
import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by liorsaar on 2015-12-16
 */
public class DowSingleton {
    private static DowSingleton mInstance;
    private RequestQueue mRequestQueue;
    private Tracker mTracker;

    private DowSingleton(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Context mApplicationContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue(mApplicationContext);
        mTracker = getDefaultTracker(mApplicationContext);
    }

    private RequestQueue getRequestQueue(Context context) {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();

        return mRequestQueue;
    }

//    public RequestQueue getRequestQueue() {
//        if (mRequestQueue == null) {
//            // getApplicationContext() is key, it keeps you from leaking the
//            // Activity or BroadcastReceiver if someone passes one in.
//            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
//        }
//        return mRequestQueue;
//    }

    private Tracker getDefaultTracker(Context context) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        mTracker = analytics.newTracker(R.xml.global_tracker);
        mTracker.setAnonymizeIp(true); // Anonymize IP
        mTracker.enableExceptionReporting(true);

        String appName = context.getResources().getString(R.string.app_name);
        mTracker.setAppName(appName);
        String appVersion = BuildConfig.VERSION_NAME;
        mTracker.setAppVersion(appVersion);

        // @see https://developers.google.com/analytics/devguides/collection/android/v4/exceptions
        Thread.UncaughtExceptionHandler myHandler = new ExceptionReporter(
                mTracker,                                         // Currently used Tracker.
                Thread.getDefaultUncaughtExceptionHandler(),      // Current default uncaught exception handler.
                context);                                         // Context of the application.

        // Make myHandler the new default uncaught exception handler.
        Thread.setDefaultUncaughtExceptionHandler(myHandler);
        return mTracker;
    }

    public static synchronized DowSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DowSingleton(context);
        }
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }

    public void cancelAll(String tag) {
        mRequestQueue.cancelAll(tag);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    public Tracker getDefaultTracker() {
        return mTracker;
    }

}