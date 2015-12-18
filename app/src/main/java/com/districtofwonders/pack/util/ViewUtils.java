package com.districtofwonders.pack.util;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by liorsaar on 2015-08-11
 */
public class ViewUtils {
    /**
     * when including a listview in a scrollview, the list cant compute a height of wrap_content properly
     * this function adjusts the height of a list to the total height of its members
     * @param listView
     */
    public static void setListViewFullHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void datePicker(Context context, final TextView textView, final String format) {
        // Process to get Current Date
        Calendar c = getCalendar(textView, format);
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        // Launch Date Picker Dialog
        DatePickerDialog dpd = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar c = Calendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                        textView.setText(sdf.format(c.getTime()));
                    }
                }, mYear, mMonth, mDay);
        dpd.show();
    }

    public static Calendar getCalendar(TextView textView, String format) {
        if (textView.getText().length() == 0) {
            return Calendar.getInstance();
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(textView.getText().toString()));
            return c;
        } catch (ParseException e) {
            return Calendar.getInstance();
        }
    }

    public static boolean isEmpty(TextView editText) {
        if (editText == null) return true;
        return (editText.getText().toString().trim().length() == 0);
    }

    public static void hideSoftInputFromWindow(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

//    // note: due to the need to support api 10, there is no option to do this in the theme
//    public static void setAllCaps(TextView textView, boolean allCaps) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            textView.setAllCaps(allCaps);
//        }
//    }

//    public static void replaceFragment(FragmentActivity activity, Fragment newFragment) {
//        FragmentManager fragmentManager = activity.getSupportFragmentManager();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//
//        transaction
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_out_left, R.anim.slide_in_left)
//                .replace(R.id.container, newFragment)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .addToBackStack(null)
//                .commit();
//    }

    public static void pickVideo(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void pickVideo(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        fragment.startActivityForResult(intent, requestCode);
        return;
    }

    public static void setAllCaps(TabLayout tabLayout, boolean isAllCaps) {
        ViewGroup slidingTabStrip = (ViewGroup) tabLayout.getChildAt(0);
        int childCount = slidingTabStrip.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewGroup tabView = (ViewGroup)slidingTabStrip.getChildAt(i);
            TextView textView = (TextView) tabView.getChildAt(0);
            textView.setAllCaps(false);
        }
    }

    public static String getAssetAsString(Context context, String assetName) throws IOException {
        InputStream inputStream = context.getAssets().open(assetName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

//    public static void playVideo(Activity activity, Uri uri) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setType("video/*");
//        intent.setData(uri);
//        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.choose_video_player)));
//    }
//
//    public static void playVideo(Fragment fragment, String url) {
//        playVideo(fragment, Uri.parse(url));
//    }
//
//    public static void playVideo(Fragment fragment, Uri uri) {
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        intent.setType("video/*");
//        intent.setData(uri);
//        fragment.startActivity(Intent.createChooser(intent, fragment.getString(R.string.choose_video_player)));
//    }


//    public static void setEmpty(View view, NavigationItem.Type type, String title) {
//        TextView tvEmptyIcon = (TextView) view.findViewById(R.id.tv_empty_icon);
//        NavigationItem navItem = UnifyApp.getDeviceInfo().getNavigationItem(type);
//        tvEmptyIcon.setText(navItem.getFontAwesomeIcon());
//        TextView tvEmpty = (TextView) view.findViewById(R.id.tv_empty);
//        tvEmpty.setText("No " + title);
//    }
}
