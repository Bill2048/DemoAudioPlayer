package com.chaoxing.demo.audioplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.chaoxing.demo.audioplayer.subject.AudioList;
import com.chaoxing.demo.audioplayer.subject.SubjectAudioHelper;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    private Button mBtnPlay;

    private static String playlist = "{\"sourceConfig\":{\"weblink\":\"http://zhuanti.chaoxing.com/mobile/mooc/tocard/89707638?courseId=89707341&name=21%E2%80%94%E2%80%9440\"},\"sourceType\":1,\"title\":\"列表名称\",\"activeIndex\":1,\"list\":[{\"mediaId\":\"b1a93946c3f263416c2109d1df4a7f63\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第021回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/b1a93946c3f263416c2109d1df4a7f63\"},{\"mediaId\":\"7ca3f4cb058055ccf5e4933d8c30766e\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第022回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/7ca3f4cb058055ccf5e4933d8c30766e\"},{\"mediaId\":\"8cad0e719a1fc68c63870642dfd6630b\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第023回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/8cad0e719a1fc68c63870642dfd6630b\"},{\"mediaId\":\"4665e7fab604594513bfe1df18983ec9\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第024回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/4665e7fab604594513bfe1df18983ec9\"},{\"mediaId\":\"a6ace5cfb3613d3528581e6c87d34312\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第025回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/a6ace5cfb3613d3528581e6c87d34312\"},{\"mediaId\":\"18483f5be88d02bd1c66ddad3620fecc\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第026回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/18483f5be88d02bd1c66ddad3620fecc\"},{\"mediaId\":\"ac76a10c5a9050ec9d1882121b05f2aa\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第027回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/ac76a10c5a9050ec9d1882121b05f2aa\"},{\"mediaId\":\"208d8e425c1d527115b93dae19fbd408\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第028回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/208d8e425c1d527115b93dae19fbd408\"},{\"mediaId\":\"8ccfd163fc0b1d49117062945ef499af\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第029回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/8ccfd163fc0b1d49117062945ef499af\"},{\"mediaId\":\"e05182a1b7b25f717aaf81c51ad48176\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第030回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/e05182a1b7b25f717aaf81c51ad48176\"},{\"mediaId\":\"88d961d8e5c78c6b648fe65294f91ee9\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第031回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/88d961d8e5c78c6b648fe65294f91ee9\"},{\"mediaId\":\"53dd857b6b42e29459eb151bb2b6c0c9\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第032回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/53dd857b6b42e29459eb151bb2b6c0c9\"},{\"mediaId\":\"c77b9793d2a84349a691532b9ae8cd7a\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第033回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/c77b9793d2a84349a691532b9ae8cd7a\"},{\"mediaId\":\"2b194d17e36a5e3f6ccdc526ed47647e\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第034回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/2b194d17e36a5e3f6ccdc526ed47647e\"},{\"mediaId\":\"86b633257c664bca8d00e02adf6ec04f\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第035回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/86b633257c664bca8d00e02adf6ec04f\"},{\"mediaId\":\"7ecb6e6e8c8f2a42318a38e8b0769cd0\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第036回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/7ecb6e6e8c8f2a42318a38e8b0769cd0\"},{\"mediaId\":\"05e01d6c87b0ec31cb50e59cead859aa\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第037回.mp3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/05e01d6c87b0ec31cb50e59cead859aa\"},{\"mediaId\":\"7744325fd24d66649b12a4966dc966c9\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第038回.mp3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/7744325fd24d66649b12a4966dc966c9\"},{\"mediaId\":\"4bd4c52835b3e76221434ea869a1e2ff\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第039回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/4bd4c52835b3e76221434ea869a1e2ff\"},{\"mediaId\":\"6995eb648f36514d3a52f2bbeb516b85\",\"mediaTitle\":\"单田芳 - 水浒外传 - 第040回.MP3\",\"mediaInfoUrl\":\"http://mooc1-api.chaoxing.com/ananas/audiostatus/6995eb648f36514d3a52f2bbeb516b85\"}]}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AudioList audioList = new Gson().fromJson(playlist, AudioList.class);
        mBtnPlay = (Button) findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubjectAudioHelper.getInstance().play(MainActivity.this, audioList);
            }
        });
    }

}
