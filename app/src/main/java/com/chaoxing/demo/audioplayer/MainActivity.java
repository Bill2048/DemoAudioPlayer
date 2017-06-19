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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRvAudio;
    private ArrayList<Audio> mAudioList = new ArrayList<>();
    private AudioAdapter mAdapter;

//    private TextView mTvProgress;
//    private TextView mTvLength;
//    private AppCompatSeekBar mSbProgress;
//    private boolean mDraggingSeekBar;
//
//    private ImageButton mIbPrevious;
//    private ImageButton mIbPlay;
//    private ImageButton mIbNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        AudioPlayerController.getInstance().addOnPlayListener(this, mOnPlayListener);
        loadAudio();
        initRecyclerView();
        AudioPlayerController.getInstance().showFloatWindow(this.getApplicationContext());
    }

    private void initView() {

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();

        }
    };






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

            }

            @Override
            public void onItemLongClick(RecyclerView rv, View childView, int position) {

            }
        }));
    }


//    private AudioPlayerController.OnPlayListener mOnPlayListener = new AudioPlayerController.OnPlayListener() {
//        @Override
//        public void onProgressChanged(int currentPosition, int length) {
//            if (!mDraggingSeekBar) {
//                updateProgress(currentPosition, length);
//            }
//        }
//    };

}
