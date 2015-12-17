package com.districtofwonders.pack;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

/**
 * Created by liorsaar on 2015-12-16
 */
public class DowSingleton {
    private static DowSingleton mInstance;
    private RequestQueue mRequestQueue;
    private static Context mApplicationContext;

    private DowSingleton(Context context) {
        // getApplicationContext() is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        mApplicationContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue(mApplicationContext);
    }

    public static synchronized DowSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DowSingleton(context);
        }
        return mInstance;
    }

//    public RequestQueue getRequestQueue() {
//        if (mRequestQueue == null) {
//            // getApplicationContext() is key, it keeps you from leaking the
//            // Activity or BroadcastReceiver if someone passes one in.
//            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
//        }
//        return mRequestQueue;
//    }

    public RequestQueue getRequestQueue(Context context) {

        if (mRequestQueue == null) {
            // Instantiate the cache
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);

            // Start the queue
            mRequestQueue.start();
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        mRequestQueue.add(req);
    }
}