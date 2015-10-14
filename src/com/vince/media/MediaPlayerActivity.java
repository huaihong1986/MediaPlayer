package com.vince.media;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MediaPlayerActivity extends Activity implements OnCompletionListener,OnErrorListener,OnSeekBarChangeListener,OnItemClickListener,Runnable{

    protected static final int SEARCH_MUSIC_SUCCESS = 0;// 搜索成功标记
    private SeekBar seekBar;
    private ListView listView;
    private ImageButton btnPlay;
    private TextView tv_currTime,tv_totalTime,tv_showName;
    private List<String> list;
    private ProgressDialog pd; // 进度条对话框
    private MusicListAdapter ma;// 适配器
    private MediaPlayer mp;
    private int currIndex = 0;// 表示当前播放的音乐索引
    private boolean flag = true;//控制进度条线程标记

    // 定义当前播放器的状态״̬
    private static final int IDLE = 0;
    private static final int PAUSE = 1;
    private static final int START = 2;
    private static final int CURR_TIME_VALUE = 1;

    private int currState = IDLE; // 当前播放器的状态
    //定义线程池（同时只能有一个线程运行）
    ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediaplayer_layout);
        list = new ArrayList<String>();
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        initView();
    }

    @Override
    protected void onDestroy() {
        if (mp != null) {
            mp.stop();
            flag= false;
            //释放资源
            mp.release();
        }
        super.onDestroy();
    }

    /**
     * 初始化UI组件
     */
    private void initView() {
        btnPlay = (ImageButton) findViewById(R.id.media_play);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(this);
        listView = (ListView) findViewById(R.id.listView1);
        listView.setOnItemClickListener(this);
        tv_currTime = (TextView) findViewById(R.id.textView1_curr_time);
        tv_totalTime = (TextView) findViewById(R.id.textView1_total_time);
        tv_showName = (TextView) findViewById(R.id.tv_showName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //从xml文件中装载菜单
        getMenuInflater().inflate(R.menu.media_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private Handler hander = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SEARCH_MUSIC_SUCCESS:
                    //搜索音乐文件结束时
                    ma = new MusicListAdapter();
                    listView.setAdapter(ma);
                    pd.dismiss();
                    break;
                case CURR_TIME_VALUE:
                    //设置当前时间
                    tv_currTime.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
        };
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //搜索本地音乐菜单
            case R.id.item1_search:
                list.clear();
                //是否有外部存储设备
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    pd = ProgressDialog.show(this, "", "正在搜索音乐文件...", true);
                    new Thread(new Runnable() {
                        String[] ext = { ".mp3" };
                        File file = Environment.getExternalStorageDirectory();

                        public void run() {
                            search(file, ext);
                            hander.sendEmptyMessage(SEARCH_MUSIC_SUCCESS);
                        }
                    }).start();

                } else {
                    Toast.makeText(this, "请插入外部存储设备..", Toast.LENGTH_LONG).show();
                }

                break;
            //清除播放列表菜单
            case R.id.item2_clear:
                list.clear();
                ma.notifyDataSetChanged();
                break;
            //退出菜单
            case R.id.item3_exit:
                flag = false;
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // 搜索音乐文件
    private void search(File file, String[] ext) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] listFile = file.listFiles();
                if (listFile != null) {
                    for (int i = 0; i < listFile.length; i++) {
                        search(listFile[i], ext);
                    }
                }
            } else {
                String filename = file.getAbsolutePath();
                for (int i = 0; i < ext.length; i++) {
                    if (filename.endsWith(ext[i])) {
                        list.add(filename);
                        break;
                    }
                }
            }
        }
    }

    class MusicListAdapter extends BaseAdapter {

        public int getCount() {
            return list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item,
                        null);
            }
            TextView tv_music_name = (TextView) convertView
                    .findViewById(R.id.textView1_music_name);
            tv_music_name.setText(list.get(position));
            return convertView;
        }

    }

    private void play() {
        switch (currState) {
            case IDLE:
                start();
                break;
            case PAUSE:
                mp.pause();
                btnPlay.setImageResource(R.drawable.ic_media_play);
                currState = START;
                break;
            case START:
                mp.start();
                btnPlay.setImageResource(R.drawable.ic_media_pause);
                currState = PAUSE;
        }
    }

    //上一首
    private void previous() {
        if((currIndex-1)>=0){
            currIndex--;
            start();
        }else{
            Toast.makeText(this, "当前已经是第一首歌曲了", Toast.LENGTH_SHORT).show();
        }
    }

    //下一自首
    private void next() {
        if(currIndex+1<list.size()){
            currIndex++;
            start();
        }else{
            Toast.makeText(this, "当前已经是最后一首歌曲了", Toast.LENGTH_SHORT).show();
        }
    }

    //开始播放
    private void start() {
        if (list.size() > 0 && currIndex < list.size()) {
            String SongPath = list.get(currIndex);
            mp.reset();
            try {
                mp.setDataSource(SongPath);
                mp.prepare();
                mp.start();
                initSeekBar();
                es.execute(this);
                tv_showName.setText(list.get(currIndex));
                btnPlay.setImageResource(R.drawable.ic_media_pause);
                currState = PAUSE;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
        }
    }

    //播放按钮
    public void play(View v){
        play();
    }

    //上一首按钮
    public void previous(View v){
        previous();
    }

    //下一首按钮
    public void next(View v){
        next();
    }

    //监听器，当当前歌曲播放完时触发，播放下一首
    public void onCompletion(MediaPlayer mp) {
        if(list.size()>0){
            next();
        }else{
            Toast.makeText(this, "播放列表为空", Toast.LENGTH_SHORT).show();
        }
    }

    //当播放异常时触发
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    //初始化SeekBar
    private void initSeekBar(){
        seekBar.setMax(mp.getDuration());
        seekBar.setProgress(0);
        tv_totalTime.setText(toTime(mp.getDuration()));
    }

    private String toTime(int time){
        int minute = time / 1000 / 60;
        int s = time / 1000 % 60;
        String mm = null;
        String ss = null;
        if(minute<10)mm = "0" + minute;
        else mm = minute + "";

        if(s <10)ss = "0" + s;
        else ss = "" + s;

        return mm + ":" + ss;
    }

    public void run() {
        flag = true;
        while(flag){
            if(mp.getCurrentPosition()<seekBar.getMax()){
                seekBar.setProgress(mp.getCurrentPosition());
                Message msg = hander.obtainMessage(CURR_TIME_VALUE, toTime(mp.getCurrentPosition()));
                hander.sendMessage(msg);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }
    }

    //SeekBar监听器
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        //是否由用户改变
        if(fromUser){
            mp.seekTo(progress);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    //ListView监听器
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        currIndex = position;
        start();
    }
}
