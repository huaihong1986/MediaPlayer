package com.vince.media;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MediaActivity extends Activity {
    private Button btnSrc,btnSys,btnNet;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        btnSrc = (Button) findViewById(R.id.button1_src);
        btnSys = (Button) findViewById(R.id.button2_sys);
        btnNet = (Button) findViewById(R.id.button3_net);

        btnSrc.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                playFromSrc();
            }
        });
        btnSys.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                playFromSys();
            }
        });

        btnNet.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                playFromNet();
            }
        });
    }

    //从网络中播放
    protected void playFromNet() {
        MediaPlayer mp = new MediaPlayer();
//    	mp.reset()
        try {
            mp.setDataSource(this,Uri.parse("http://2.2.2.44:8080/a1.mp3"));
            mp.prepareAsync();//异步预处理

            //监听异步预处理完成的事件
            mp.setOnPreparedListener(new OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从系统文件中播放
    protected void playFromSys() {
//    	MediaPlayer mp = MediaPlayer.create(this, Uri.parse("/sdcard/a1.mp3"));
        //创建播放器对象
        MediaPlayer mp = new MediaPlayer();
        try {
            //设置数据源
            mp.setDataSource("/sdcard/a1.mp3");
            mp.prepare();//预处理
            mp.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //从源文件中播放
    protected void playFromSrc() {
        //创建播放器对象，并绑定音频文件
        MediaPlayer mp = MediaPlayer.create(this, R.raw.a1);
        mp.start();
    }


}



















