package com.districtofwonders.pack.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.districtofwonders.pack.R;

/**
 * KISS TTM
 * load the existing patreon page into a webview
 * <p/>
 * Created by liorsaar on 2015-12-16
 */
public class PatreonFragment extends Fragment {

    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.patreon_fragment, null);

        String url = getActivity().getString(R.string.link_patreon);

        mProgressBar = (ProgressBar) root.findViewById(R.id.patreonProgress);

        WebView webView = (WebView) root.findViewById(R.id.patreonWebview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new MyWebViewClient());
        webView.loadUrl(url);

        return root;
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mProgressBar.setVisibility(View.GONE);
        }
    }
}