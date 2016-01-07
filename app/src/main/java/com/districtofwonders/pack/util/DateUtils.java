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
        return new SimpleDateFormat("d").format(calendar.getTime());
    }

    public static int getMinutes(String durationString) {
        String[] parts = durationString.split(":");
        try {
            switch (parts.length) {
                // less than 1 minute
                case 1:
                    return 1;
                // mm:ss
                case 2:
                    return Integer.parseInt(parts[0]);
                // hh:mm:ss
                case 3:
                    return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Illegal duration format" + durationString);
        }
        throw new IllegalArgumentException("Illegal duration format" + durationString);
    }

    public static String getPubDate(String pubDateString) {
        Calendar calendar = getPubDateCal(pubDateString);
        return new SimpleDateFormat("d MMM yyyy").format(calendar.getTime());
    }
}
