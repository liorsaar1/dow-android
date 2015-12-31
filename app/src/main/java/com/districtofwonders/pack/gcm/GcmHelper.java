package com.districtofwonders.pack.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;

/**
 * Created by liorsaar on 2015-12-31
 */
public class GcmHelper {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final BroadcastReceiver mRegistrationBroadcastReceiver;
    private static Class<? extends Activity> sParentActivityClass;

    public GcmHelper(Activity parentActivity, BroadcastReceiver broadcastReceiver) {
        sParentActivityClass = parentActivity.getClass();
        mRegistrationBroadcastReceiver = broadcastReceiver;
    }

    public static Class getParentActivityClass() {
        return sParentActivityClass;
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

    public void onResume(Activity activity) {
        LocalBroadcastManager.getInstance(activity).registerReceiver(
                mRegistrationBroadcastReceiver,
                new IntentFilter(GcmPreferences.REGISTRATION_COMPLETE));
    }

    public void onPause(Activity activity) {
        LocalBroadcastManager.getInstance(activity).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    public void subscribeTopics(final Activity activity, String token, String[] topics) {
        new AsyncTask<Object, Void, Exception>() {
            @Override
            protected Exception doInBackground(Object... params) {
                String token = (String) params[0];
                String[] topics = (String[]) params[1];
                try {
                    RegistrationIntentService.subscribeTopics(activity, token, topics);
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
                Toast.makeText(activity, "Subscribed", Toast.LENGTH_LONG).show();
            }
        }.execute(token, topics);
    }

    public void unsubscribeTopics(final Activity activity, String token, String[] topics) {
        new AsyncTask<Object, Void, Exception>() {
            @Override
            protected Exception doInBackground(Object... params) {
                String token = (String) params[0];
                String[] topics = (String[]) params[1];
                try {
                    RegistrationIntentService.unsubscribeTopics(activity, token, topics);
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
                Toast.makeText(activity, "Subscribed", Toast.LENGTH_LONG).show();
            }
        }.execute(token, topics);
    }

}
