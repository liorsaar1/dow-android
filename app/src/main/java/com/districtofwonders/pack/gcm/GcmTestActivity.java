/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.districtofwonders.pack.gcm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.districtofwonders.pack.R;

import java.util.HashMap;
import java.util.Map;

public class GcmTestActivity extends AppCompatActivity {
    private static final String TAG = GcmTestActivity.class.getSimpleName();
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private GcmHelper gcmHelper;
    private Map<String, Boolean> topicsMap = new HashMap<String, Boolean>() {
        {
            put("/topics/global", true);
            put("/topics/sss", false);
        }
    };

    private GcmHelper.RegistrationListener mRegistrationListener = new GcmHelper.RegistrationListener() {
        @Override
        public void success() {
            Toast.makeText(GcmTestActivity.this, "OKOKOKOK", Toast.LENGTH_LONG).show();
        }

        @Override
        public void error(String error) {
            Toast.makeText(GcmTestActivity.this, "ERROR:" + error, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gcm_activity);
        Log.e(TAG, "--------------------------------------- create");

        // UI
        mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mInformationTextView = (TextView) findViewById(R.id.informationTextView);
        Button mSub = (Button) findViewById(R.id.gcmSubscribe);
        mSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topicsMap.put("/topics/sss", true);
                gcmHelper.setSubscriptions(GcmTestActivity.this, topicsMap, mRegistrationListener);
            }
        });
        Button mUnsub = (Button) findViewById(R.id.gcmUnsubscribe);
        mUnsub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topicsMap.put("/topics/sss", false);
                gcmHelper.setSubscriptions(GcmTestActivity.this, topicsMap, mRegistrationListener);
            }
        });

        gcmHelper = new GcmHelper(this, topicsMap, new GcmHelper.RegistrationListener() {

            @Override
            public void success() {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(GcmTestActivity.this);
                boolean sentToken = sharedPreferences.getBoolean(GcmPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    mInformationTextView.setText(getString(R.string.gcm_token_error_message));
                }
            }

            @Override
            public void error(String error) {
                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(GcmTestActivity.this, "ERROR:" + error, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause: gcmHelper:" + gcmHelper);
        gcmHelper.onPause(this);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "-----  new intent");
        String from = intent.getStringExtra(GcmHelper.NOTIFICATION_FROM);
        Log.e(TAG, "from:" + from);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: gcmHelper:" + gcmHelper);
        gcmHelper.onResume(this);
    }
}
