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
        return timeStr.replaceFirst("00:", "");
    }

    public static String[] formatTimeLength(String... timeArr) {
        String[] result = new String[timeArr.length];
        boolean trim = true;
        for (String time : timeArr) {
            if (time.indexOf("00:") != 0) {
                trim = false;
                break;
            }
        }
        if (trim) {
            for (int i = 0; i < timeArr.length; i++) {
                result[i] = trimTime(timeArr[i]);
            }
        }
        return result;
    }

}
