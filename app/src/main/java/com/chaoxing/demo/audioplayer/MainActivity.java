package com.chaoxing.demo.audioplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private View mContainer;
    private Button mBtnPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = findViewById(R.id.container);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPlay.setVisibility(View.GONE);
                mContainer.setBackgroundColor(0xFF000000);
                AudioPlayerController.getInstance().bindMediaService(MainActivity.this);
            }
        });
    }

}
