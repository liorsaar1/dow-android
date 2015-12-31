/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.districtofwonders.pack.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.districtofwonders.pack.R;

public class GcmTestActivity extends AppCompatActivity {

    private static final String TAG = "GcmTestActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private Button mSub, mUnsub;
    private GcmHelper gcmHelper;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_activity);

        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                token = sharedPreferences.getString(GcmPreferences.TOKEN, null);
                if (token == null) {
                    String error = sharedPreferences.getString(GcmPreferences.REGISTRATION_ERROR, null);
                    Toast.makeText(GcmTestActivity.this, "ERROR:"+ error, Toast.LENGTH_LONG).show();
                    return;
                }

                boolean sentToken = sharedPreferences.getBoolean(GcmPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.gcm_token_error_message));
                }
            }
        };
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        mSub = (Button)findViewById(R.id.gcmSubscribe);
        mUnsub = (Button)findViewById(R.id.gcmUnsubscribe);
        mSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcmHelper.subscribeTopics(GcmTestActivity.this, token, new String[]{"feed"});
            }
        });
        mUnsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gcmHelper.unsubscribeTopics(GcmTestActivity.this, token, new String[]{"feed"});
            }
        });

        gcmHelper = new GcmHelper(this, mRegistrationBroadcastReceiver);

        if (gcmHelper.checkPlayServices(this)) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gcmHelper.onResume(this);
    }

    @Override
    protected void onPause() {
        gcmHelper.onPause(this);
        super.onPause();
    }

}
