package com.districtofwonders.pack.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.districtofwonders.pack.R;
import com.districtofwonders.pack.fragment.feed.FeedsFragment;
import com.districtofwonders.pack.gcm.GcmHelper;

import java.util.HashMap;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private static final String TAG = NotificationsFragment.class.getSimpleName();
    private static final String PREF_RECEIVE_NOTIFICATIONS = "PREF_RECEIVE_NOTIFICATIONS";

    private Switch globalSwitch;
    private Switch sssSwitch;
    private Switch fffSwitch;
    private Switch tttSwitch;

    // this is the "to:" value used by the server to generate a notification
    private static String[] topics = {
            "/topics/global",
            FeedsFragment.feeds[0].topic, // sss
            FeedsFragment.feeds[1].topic, // fff
            FeedsFragment.feeds[2].topic, // ttt
    };

    public static Fragment newInstance(Context context) {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.notifications_fragment, null);

        boolean isReceive = getReceiveNotificationsPref(getActivity());
        Switch receiveSwitch = (Switch) root.findViewById(R.id.notificationsReceive);
        receiveSwitch.setChecked(isReceive);
        receiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedReceive(isChecked);
            }
        });
        // get topics prefs
        Map<String, Boolean> topicsMap = getTopicsMap(getActivity());
        // set switches
        globalSwitch = (Switch) root.findViewById(R.id.notificationsGlobal);
        globalSwitch.setChecked(topicsMap.get(topics[0]));
        globalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[0], isChecked);
            }
        });
        sssSwitch = (Switch) root.findViewById(R.id.notificationsSSS);
        sssSwitch.setChecked(topicsMap.get(topics[1]));
        sssSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[1], isChecked);
            }
        });
        fffSwitch = (Switch) root.findViewById(R.id.notificationsFFF);
        fffSwitch.setChecked(topicsMap.get(topics[2]));
        fffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[2], isChecked);
            }
        });
        tttSwitch = (Switch) root.findViewById(R.id.notificationsTTT);
        tttSwitch.setChecked(topicsMap.get(topics[3]));
        tttSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[3], isChecked);
            }
        });

        setEnabled(isReceive);
        return root;
    }

    private void setEnabled(boolean isReceive) {
        // disable the switches
        globalSwitch.setClickable(isReceive);
        sssSwitch.setClickable(isReceive);
        fffSwitch.setClickable(isReceive);
        tttSwitch.setClickable(isReceive);
        // grey out the ui
        float alpha = isReceive ? 1.0f : 0.3f;
        globalSwitch.setAlpha(alpha);
        sssSwitch.setAlpha(alpha);
        fffSwitch.setAlpha(alpha);
        tttSwitch.setAlpha(alpha);
    }

    public static boolean getReceiveNotificationsPref(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, true);
    }

    public static Map<String, Boolean> getTopicsMap(Context context) {

        Map<String, Boolean> topicsMap = new HashMap<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean receive = sharedPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, true);

        for (String topic : topics) {
            if (receive)
                topicsMap.put(topic, sharedPreferences.getBoolean(topic, true));
            else
                topicsMap.put(topic, false);
        }

        return topicsMap;
    }

    private void onCheckedReceive(boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);
        setReceiveNotificationsPref(getActivity(), isChecked);

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Updating Preference", "Please Wait...", true);

        GcmHelper.setSubscriptions(getActivity(), getTopicsMap(getActivity()), new GcmHelper.RegistrationListener() {
            @Override
            public void success() {
                progressDialog.dismiss();
            }

            @Override
            public void error(String error) {
                progressDialog.dismiss();
                showSubscriptionError(error);
            }
        });
    }

    public void setReceiveNotificationsPref(Context context, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PREF_RECEIVE_NOTIFICATIONS, value).apply();

        setEnabled(value);
    }

    private void onCheckedTopic(String topicName, boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.edit().putBoolean(topicName, isChecked).apply();

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Updating Preference", "Please Wait...", true);

        GcmHelper.setSubscription(getActivity(), topicName, isChecked, new GcmHelper.RegistrationListener() {
            @Override
            public void success() {
                progressDialog.dismiss();
            }

            @Override
            public void error(String error) {
                progressDialog.dismiss();
                showSubscriptionError(error);
            }
        });
    }

    private void showSubscriptionError(String error) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage(error)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}