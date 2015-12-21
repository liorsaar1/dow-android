package com.districtofwonders.pack.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    // <pubDate>Sun, 29 Nov 2015 01:00:26 +0000</pubDate>
    private static final String PUB_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

    private static SimpleDateFormat pubDateSdf = new SimpleDateFormat(PUB_DATE_FORMAT, Locale.US);

    public static Calendar getPubDateCal(String pubDateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(pubDateSdf.parse(pubDateString));
            return calendar;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Illegal date format" + pubDateString);
        }
    }

    public static String getMonthString(Calendar calendar) {
        return new SimpleDateFormat("MMM").format(calendar.getTime());
    }

    public static String getDayString(Calendar calendar) {
        return new SimpleDateFormat("dd").format(calendar.getTime());
    }

}
