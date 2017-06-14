package com.chaoxing.demo.audioplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AudioPlayerService player;
    private boolean serviceBound;

    private RecyclerView rvAudio;
    private ArrayList<Audio> audioList = new ArrayList<>();

    private TextView mTvProgress;
    private TextView mTvLength;
    private AppCompatSeekBar mSbProgress;
    private boolean mDraggingSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        CourseAudioPlayer.getInstance().addOnPlayListener(this, mOnPlayListener);
        loadAudio();
        initRecyclerView();
    }

    private void initView() {
        mTvProgress = (TextView) findViewById(R.id.tv_progress);
        mTvLength = (TextView) findViewById(R.id.tv_length);
        mSbProgress = (AppCompatSeekBar) findViewById(R.id.sb_progress);
        mSbProgress.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        updateProgress(0, 0);
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mDraggingSeekBar = true;
                updateProgress(progress, mSbProgress.getMax());
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            CourseAudioPlayer.getInstance().setPlayProgress(MainActivity.this, seekBar.getProgress());
            mDraggingSeekBar = false;
        }
    };

    private void updateProgress(int progress, int length) {
        if (length < 0) {
            length = 0;
        }
        if (progress > length) {
            progress = length;
        }
        if (length == 0) {
            mSbProgress.setEnabled(false);
        } else {
            mSbProgress.setEnabled(true);
        }
        mSbProgress.setMax(length);
        mSbProgress.setProgress(progress);
        String fLength = AudioPlayerUtils.formatTime(length);
        String fProgress = AudioPlayerUtils.formatTime(progress);
        int count = 0;
        do {
            String str = fLength.replaceFirst("00:", "");
            if (!str.equals(fLength)) {
                count++;
                fLength = str;
            } else {
                break;
            }
        } while (true);
        while (count > 0) {
            fProgress = fProgress.replaceFirst("00:", "");
            count--;
        }
        mTvLength.setText(fLength);
        mTvProgress.setText(fProgress);
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

    private void initRecyclerView() {
        rvAudio = (RecyclerView) findViewById(R.id.rv_audio);
        AudioAdapter adapter = new AudioAdapter(this, audioList);
        rvAudio.setAdapter(adapter);
        rvAudio.setLayoutManager(new LinearLayoutManager(this));
        rvAudio.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(rvAudio, new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(RecyclerView rv, View childView, int position) {
                CourseAudioPlayer.getInstance().playList(MainActivity.this, audioList, position, 0);
            }

            @Override
            public void onItemLongClick(RecyclerView rv, View childView, int position) {

            }
        }));
    }


    private CourseAudioPlayer.OnPlayListener mOnPlayListener = new CourseAudioPlayer.OnPlayListener() {
        @Override
        public void onProgressChanged(int currentPosition, int length) {
            if (!mDraggingSeekBar) {
                updateProgress(currentPosition, length);
            }
        }
    };

}
