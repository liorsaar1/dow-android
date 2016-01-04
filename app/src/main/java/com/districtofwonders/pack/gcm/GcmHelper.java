package com.districtofwonders.pack.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;
import java.util.Map;

/**
 * Created by liorsaar on 2015-12-31
 */
public class GcmHelper {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static Class<? extends Activity> sParentActivityClass;
    private final BroadcastReceiver mRegistrationBroadcastReceiver;
    private final RegistrationListener mRegistrationListener;
    private String mToken;

    public GcmHelper(final Activity parentActivity, final Map<String, Boolean> topicsMap, RegistrationListener registrationListener) {
        sParentActivityClass = parentActivity.getClass();
        mRegistrationListener = registrationListener;
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                String token = sharedPreferences.getString(GcmPreferences.TOKEN, null);
                // error - no token
                if (token == null) {
                    String error = sharedPreferences.getString(GcmPreferences.REGISTRATION_ERROR, null);
                    mRegistrationListener.error(error);
                    return;
                }
                // success
                mToken = token;
                setSubscriptions(parentActivity, topicsMap);
                mRegistrationListener.success();
            }
        };

        if (checkPlayServices(parentActivity)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(parentActivity, RegistrationIntentService.class);
            parentActivity.startService(intent);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e(activity.getClass().getSimpleName(), "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public void setSubscriptions(final Activity activity, final Map<String, Boolean> topicsMap) {
        new AsyncTask<Object, Void, Exception>() {
            @Override
            protected Exception doInBackground(Object... params) {
                GcmPubSub pubSub = GcmPubSub.getInstance(activity);
                try {
                    for (String key : topicsMap.keySet()) {
                        Boolean value = topicsMap.get(key);
                        if (value) {
                            pubSub.subscribe(mToken, "/topics/" + key, null);
                        } else {
                            pubSub.unsubscribe(mToken, "/topics/" + key);
                        }
                    }
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception exception) {
                if (exception != null) {
                    Toast.makeText(activity, "ERROR:" + exception.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(activity, "Subscriptions Updated.", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }

    public static Class getParentActivityClass() {
        return sParentActivityClass;
    }

    public void onResume(Activity activity) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mRegistrationBroadcastReceiver,
                new IntentFilter(GcmPreferences.REGISTRATION_COMPLETE));
    }

    public void onPause(Activity activity) {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public interface RegistrationListener {
        void success();

        void error(String error);
    }
}
