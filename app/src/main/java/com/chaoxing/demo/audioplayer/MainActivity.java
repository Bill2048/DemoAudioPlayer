package com.chaoxing.demo.audioplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRvAudio;
    private ArrayList<Audio> mAudioList = new ArrayList<>();
    private AudioAdapter mAdapter;

    private TextView mTvProgress;
    private TextView mTvLength;
    private AppCompatSeekBar mSbProgress;
    private boolean mDraggingSeekBar;

    private ImageButton mIbPrevious;
    private ImageButton mIbPlay;
    private ImageButton mIbNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        AudioPlayerController.getInstance().addOnPlayListener(this, mOnPlayListener);
        loadAudio();
        initRecyclerView();
        AudioPlayerController.getInstance().launchPlayerFloatWindow(this.getApplicationContext());
    }

    private void initView() {
        mTvProgress = (TextView) findViewById(R.id.tv_progress);
        mTvLength = (TextView) findViewById(R.id.tv_length);
        mSbProgress = (AppCompatSeekBar) findViewById(R.id.sb_progress);
        mSbProgress.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        updateProgress(0, 0);

        mIbPrevious = (ImageButton) findViewById(R.id.ib_previous);
        mIbPrevious.setOnClickListener(mOnClickListener);
        mIbPlay = (ImageButton) findViewById(R.id.ib_play);
        mIbPlay.setOnClickListener(mOnClickListener);
        mIbNext = (ImageButton) findViewById(R.id.ib_next);
        mIbNext.setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.ib_previous) {
                previous();
            } else if (id == R.id.ib_play) {
                switchPlay();
            } else if (id == R.id.ib_next) {
                next();
            }
        }
    };

    private boolean mPlayed;
    private boolean mPause = true;
    private int mActiveIndex;

    private void switchPlay() {
        if (mPause) {
            play();
        } else {
            pause();
        }
    }

    private void play() {
        if (mPause) {
            if (!mPlayed) {
                if (!mAudioList.isEmpty()) {
                    mPause = false;
                    play(0);
                }
            } else {
                if (AudioPlayerController.getInstance().isPause(this)) {
                    AudioPlayerController.getInstance().resumePlay(this);
                    mIbPlay.setImageResource(R.mipmap.ic_pause_circle_outline_black_48dp);
                    mPause = false;
                } else {
                    play(mActiveIndex);
                    mPause = false;
                }
            }
        }
    }

    private void play(int index) {
        mPlayed = true;
        AudioPlayerController.getInstance().play(this, mAudioList, index, 0);
        mActiveIndex = index;
        mIbPlay.setImageResource(R.mipmap.ic_pause_circle_outline_black_48dp);
    }

    private void previous() {
        if (mPlayed) {
            AudioPlayerController.getInstance().previous(this);
        }
    }

    private void next() {
        AudioPlayerController.getInstance().next(this);
    }

    private void pause() {
        AudioPlayerController.getInstance().parsePlay(this);
        mIbPlay.setImageResource(R.mipmap.ic_play_circle_outline_black_48dp);
        mPause = true;
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
            AudioPlayerController.getInstance().setPlayProgress(MainActivity.this, seekBar.getProgress());
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
            if (fLength.indexOf("00:") != 0) {
                break;
            }
            String str = fLength.replaceFirst("00:", "");
            if (!str.equals(fLength)) {
                count++;
                fLength = str;
            } else {
                break;
            }
        } while (true);
        while (count > 0) {
            if (fLength.indexOf("00:") != 0) {
                break;
            }
            fProgress = fProgress.replaceFirst("00:", "");
            count--;
        }
        mTvLength.setText(fLength);
        mTvProgress.setText(fProgress);
    }

    private Handler mHandler = new Handler();

    private void loadAudio() {
        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().getAbsolutePath()}, new String[]{"audio/mpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                if (MainActivity.this.isFinishing()) {
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ContentResolver contentResolver = getContentResolver();
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
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!MainActivity.this.isFinishing()) {
                                        mAudioList.addAll(audioList);
                                        int position = 0;
                                        LinearLayoutManager layoutManager = (LinearLayoutManager) mRvAudio.getLayoutManager();
                                        if (mRvAudio.getAdapter() != null) {
                                            position = layoutManager.findFirstVisibleItemPosition();
                                        }
                                        mRvAudio.setAdapter(mAdapter);
                                        layoutManager.scrollToPosition(position);
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    private void initRecyclerView() {
        mRvAudio = (RecyclerView) findViewById(R.id.rv_audio);
        mAdapter = new AudioAdapter(this, mAudioList);
        mRvAudio.setLayoutManager(new LinearLayoutManager(this));
        mRvAudio.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(mRvAudio, new OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(RecyclerView rv, View childView, int position) {
                play(position);
            }

            @Override
            public void onItemLongClick(RecyclerView rv, View childView, int position) {

            }
        }));
    }


    private AudioPlayerController.OnPlayListener mOnPlayListener = new AudioPlayerController.OnPlayListener() {
        @Override
        public void onProgressChanged(int currentPosition, int length) {
            if (!mDraggingSeekBar) {
                updateProgress(currentPosition, length);
            }
        }
    };

}
