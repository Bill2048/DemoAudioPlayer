package com.chaoxing.demo.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by HuWei on 2017/6/14.
 */

public class AudioPlayerUtils {

    public static void scanLocalAudio(Context context, final ScanLocalAudioCallbacks callbacks) {
        if (callbacks != null) {
            callbacks.onStart();
        }
        final Context appContext = context.getApplicationContext();
        MediaScannerConnection.scanFile(context, new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()}, new String[]{"audio/mpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContentResolver contentResolver = appContext.getContentResolver();
                        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
                        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
                        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
                        final List<Audio> audioList = new ArrayList<>();
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                                audioList.add(new Audio(data, title, album, artist));
                            }
                            cursor.close();
                        }
                        if (callbacks != null) {
                            callbacks.onCompletionInBackground(audioList);
                        }
                    }
                }).start();
            }
        });
    }

    public interface ScanLocalAudioCallbacks {
        void onStart();

        void onCompletionInBackground(List<Audio> audioList);
    }

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
