package com.districtofwonders.pack.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.districtofwonders.pack.R;

import java.util.HashMap;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private static Map<String, Boolean> topicsMap = new HashMap<String, Boolean>() {
        {
            put("global", true);
            put("sss", false);
            put("fff", false);
            put("ttt", true);
        }
    };

    public static Map<String, Boolean> getTopicsMap() {
        return topicsMap;
    }

    public static Fragment newInstance(Context context) {
        NotificationsFragment fragment = new NotificationsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_fragment, null);
        return root;
    }

}