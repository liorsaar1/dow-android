package com.districtofwonders.pack.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

    // this is the "to:" value used by the server to generate a notification
    private static String[] topics = {
            "/topics/global",
            FeedsFragment.feeds[0].topic, // sss
            FeedsFragment.feeds[1].topic, // fff
            FeedsFragment.feeds[2].topic, // ttt
    };

    public static boolean getReceiveNotificationsPref(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, true);
    }

    public static void setReceiveNotificationsPref(Context context, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PREF_RECEIVE_NOTIFICATIONS, value).apply();
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

    public static Fragment newInstance(Context context) {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.notifications_fragment, null);

        boolean receive = getReceiveNotificationsPref(getActivity());
        Switch receiveSwitch = (Switch) root.findViewById(R.id.notificationsReceive);
        receiveSwitch.setChecked(receive);
        receiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedReceive(isChecked);
            }
        });
        // get topics prefs
        Map<String, Boolean> topicsMap = getTopicsMap(getActivity());
        // set switches
        Switch globalSwitch = (Switch) root.findViewById(R.id.notificationsGlobal);
        globalSwitch.setChecked(topicsMap.get(topics[0]));
        globalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[0], isChecked);
            }
        });
        Switch sssSwitch = (Switch) root.findViewById(R.id.notificationsSSS);
        sssSwitch.setChecked(topicsMap.get(topics[1]));
        sssSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[1], isChecked);
            }
        });
        Switch fffSwitch = (Switch) root.findViewById(R.id.notificationsFFF);
        fffSwitch.setChecked(topicsMap.get(topics[2]));
        fffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[2], isChecked);
            }
        });
        Switch tttSwitch = (Switch) root.findViewById(R.id.notificationsTTT);
        tttSwitch.setChecked(topicsMap.get(topics[3]));
        tttSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedTopic(topics[3], isChecked);
            }
        });
        return root;
    }

    private void onCheckedReceive(boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);
        setReceiveNotificationsPref(getActivity(), isChecked);
    }

    private void onCheckedTopic(String topicName, boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.edit().putBoolean(topicName, isChecked).apply();

        GcmHelper.setSubscription(getActivity(), topicName, isChecked);
    }
}