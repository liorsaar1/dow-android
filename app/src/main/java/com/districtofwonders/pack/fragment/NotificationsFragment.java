package com.districtofwonders.pack.fragment;

import android.app.ProgressDialog;
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
import com.districtofwonders.pack.gcm.GcmPreferences;
import com.districtofwonders.pack.util.ViewUtils;

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
    private View errorContainer;
    private View uiContainer;
    private View topicsContainer;

    public static Fragment newInstance(Context context) {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.notifications_fragment, null);

        errorContainer = root.findViewById(R.id.notificationsErrorContainer);
        uiContainer = root.findViewById(R.id.notificationsUiContainer);
        boolean isEnabled = isNotificationsEnabled(getActivity());
        setEnabled(isEnabled);
        // if connection to GCM failed - disable UI
        if (!isEnabled) {
            return root;
        }

        // receive
        boolean isReceive = getReceiveNotificationsPref(getActivity());
        topicsContainer = root.findViewById(R.id.notificationsTopicsContainer);
        setReceiving(isReceive);
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

        setReceiving(isReceive);
        return root;
    }

    private void setEnabled(boolean isEnabled) {
        if (isEnabled) {
            errorContainer.setVisibility(View.GONE);
            uiContainer.setVisibility(View.VISIBLE);
        } else {
            errorContainer.setVisibility(View.VISIBLE);
            uiContainer.setVisibility(View.GONE);
        }
    }

    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(GcmPreferences.REGISTRATION_ERROR, null) == null;
    }

    public static boolean getReceiveNotificationsPref(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(PREF_RECEIVE_NOTIFICATIONS, true);
    }

    private void onCheckedReceive(final boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);
        setReceiveNotificationsPref(getActivity(), isChecked);

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getActivity().getString(R.string.notifications_updating_preferences), getActivity().getString(R.string.please_wait), true);

        GcmHelper.setSubscriptions(getActivity(), getTopicsMap(getActivity()), new GcmHelper.RegistrationListener() {
            @Override
            public void success() {
                progressDialog.dismiss();
            }

            @Override
            public void error(String error) {
                progressDialog.dismiss();
                setEnabled(false);
                ViewUtils.showError(getActivity(), error);
            }
        });
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

    public void setReceiveNotificationsPref(Context context, boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(PREF_RECEIVE_NOTIFICATIONS, value).apply();

        setReceiving(value);
    }

    private void setReceiving(boolean isReceive) {
        topicsContainer.setVisibility(isReceive?View.VISIBLE:View.GONE);
    }

    private void onCheckedTopic(final String topicName, final boolean isChecked) {
        Log.e(TAG, "receive:" + isChecked);

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), getActivity().getString(R.string.notifications_updating_preferences), getActivity().getString(R.string.please_wait), true);

        GcmHelper.setSubscription(getActivity(), topicName, isChecked, new GcmHelper.RegistrationListener() {
            @Override
            public void success() {
                progressDialog.dismiss();
                // write the pref only after subscription success
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                sharedPreferences.edit().putBoolean(topicName, isChecked).apply();
            }

            @Override
            public void error(String error) {
                progressDialog.dismiss();
                setEnabled(false);
                ViewUtils.showError(getActivity(), error);
            }
        });
    }
}