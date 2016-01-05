package com.districtofwonders.pack.fragment.feed;

public class FeedDesc {
    public String title;
    public String url;
    public String topic;

    public static int getTopic(FeedDesc[] feeds, String topic) {
        for (int i = 0; i < feeds.length; i++) {
            if (feeds[i].topic.equals(topic))
                return i;
        }
        throw new IllegalArgumentException("Illegal topic " + topic);
    }
}
