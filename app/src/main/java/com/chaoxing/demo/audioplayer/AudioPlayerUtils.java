package com.chaoxing.demo.audioplayer;

import java.util.concurrent.TimeUnit;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerUtils {

    public static String formatTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static String trimTime(String timeStr) {
        return trimTime(timeStr, 3);
    }

    public static String trimTime(String timeStr, int count) {
        while (count > 0) {
            if (timeStr.indexOf("00:") != 0) {
                break;
            }
            timeStr = timeStr.replaceFirst("00:", "");
            count--;
        }
        return timeStr;
    }

    public static String[] formatTimeLength(String[] timeArr) {
        String[] result = new String[timeArr.length];
        int minCount = 3;
        for (String time : timeArr) {
            int count = 0;
            do {
                if (time.indexOf("00:") != 0) {
                    break;
                }
                time = time.replaceFirst("00:", "");
                count++;
            } while (true);
            if (count < minCount) {
                minCount = count;
            }
        }
        for (int i = 0; i < timeArr.length; i++) {
            result[i] = trimTime(timeArr[i], minCount);
        }
        return result;
    }

}
