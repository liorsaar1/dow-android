package com.districtofwonders.pack.fragment.feed;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.districtofwonders.pack.R;

public class EpisodeFragment extends Fragment {

    public static Fragment newInstance(Context context) {
        EpisodeFragment fragment = new EpisodeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_fragment, null);
        return root;
    }
}