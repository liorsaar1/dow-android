package com.districtofwonders.pack.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.districtofwonders.pack.BuildConfig;
import com.districtofwonders.pack.R;
import com.districtofwonders.pack.util.ViewUtils;

public class AboutFragment extends Fragment {

    public static Fragment newInstance(Context context) {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_fragment, null);
        String version = "Version " + BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE;
        ((TextView) root.findViewById(R.id.aboutVersion)).setText(version);

        root.findViewById(R.id.aboutSSS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.openBrowser(getActivity(), "http://www.starshipsofa.com/staff/");
            }
        });
        root.findViewById(R.id.aboutTTT).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.openBrowser(getActivity(), "http://talestoterrify.com/staff/");
            }
        });
        root.findViewById(R.id.aboutFFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.openBrowser(getActivity(), "http://farfetchedfables.com/staff/");
            }
        });
        root.findViewById(R.id.aboutCredits).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewUtils.openBrowser(getActivity(), getActivity().getString(R.string.credits_url));
            }
        });


        return root;
    }

}