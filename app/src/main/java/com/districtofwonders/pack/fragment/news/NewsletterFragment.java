package com.districtofwonders.pack.fragment.news;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.districtofwonders.pack.MainActivity;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.util.ViewUtils;

import java.io.IOException;

public class NewsletterFragment extends Fragment {

    private static final String TAG = MainActivity.TAG; //EpisodeFragment.class.getSimpleName();
    private static final String ARG_URL = "ARG_URL";

    public static Fragment newInstance(String url) {
        NewsletterFragment fragment = new NewsletterFragment();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_URL, url);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        String url = arguments.getString(ARG_URL);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.newsletter_fragment, null);

        // show notes
        String content = null;
        try {
            content = ViewUtils.getAssetAsString(getActivity(), url);
        } catch (IOException e) {
            content = "Read Error " + e;
        }
        WebView webView = (WebView) root.findViewById(R.id.webview);
        webView.loadData(content, "text/html; charset=UTF-8", null);

        return root;
    }

}