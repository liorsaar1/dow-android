package com.districtofwonders.pack.gcm;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.districtofwonders.pack.DowSingleton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by liorsaar on 2016-01-25
 */
public class AnalyticsHelper {

    private static final String TAG = AnalyticsHelper.class.getSimpleName();

    public static void navigate(Context context, String fragmentName) {
        String action = fragmentName.substring(fragmentName.lastIndexOf(".") + 1);
        if (action.endsWith("Fragment")) {
            action = action.substring(0,action.lastIndexOf("Fragment"));
        }
        event(context, "Navigation", action, "");
    }

    private static void event(Context context, String category, String action, String label) {
        Log.e(TAG, "event: " + category + ": " + action + ": " + label);
        Tracker tracker = DowSingleton.getInstance(context).getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(category)
                        .setAction(action)
                        .setLabel(label)
                        .build()
        );
    }

    public static void screen(Context context, String name) {
        Log.e(TAG, "screen: " + name);
        Tracker tracker = DowSingleton.getInstance(context).getDefaultTracker();
        tracker.setScreenName(name);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void playAudioStream(Context context, String url) {
        event(context, "PlayStream", url, "");
    }

    public static void playLocalAudio(Context context, String url) {
        event(context, "PlayLocal", url, "");
    }

    public static void download(Context context, String url) {
        event(context, "Download", url, "");
    }

    public static void downloadCompleted(Context context, Uri uri) {
        event(context, "DownloadCompleted", uri.toString(), "");
    }

    public static void viewNewsletter(Context context, String pubDate) {
        event(context, "ViewNewsletter", pubDate, "");
    }

    public static void patreonLink(Context context, String url) {
        // catch bePatron bePatronConfirm
        if (url.contains("/bePatronConfirm")) {
            event(context, "Patreon", "BePatronConfirm", url);
            return;
        }
        if (url.contains("/bePatron")) {
            event(context, "Patreaon", "BePatron", url);
            return;
        }
    }

    public static void notificationPrefReceive(Context context, boolean isChecked) {
        event(context, "NotificationPref", "Receive", ""+isChecked);
    }

    public static void notificationPrefTopic(Context context, String topicName, boolean isChecked) {
        event(context, "NotificationPref", topicName, ""+isChecked);
    }

    public static void about(Context context, String link) {
        event(context, "About", link, "");
    }

    public static void notificationReceived(Context context, String from, String message) {
        event(context, "NotificationReceived", from, message);
    }

    public static void notificationClicked(Context context, String from) {
        event(context, "NotificationClicked", from, "");
    }

    public static void showNotes(Context context, String url) {
        event(context, "ShowNotes", url, "");
    }

    public static void error(Context context, String error) {
        event(context, "Error", error, "");
    }
}
