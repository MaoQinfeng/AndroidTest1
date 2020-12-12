package com.example.admin.easylife.Activity;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.text.UnicodeSetSpanner;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.easylife.R;
import com.example.admin.easylife.Service.DownloadService;
import com.example.admin.easylife.db.Music;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Music> musicList;

    private TextView textView;
    private TextView music_cur;
    private TextView music_length;

    private Button btn_play;
    private Button btn_stop;
    private Button btn_next;
    private Button btn_front;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private Timer timer;

    private String name;
    private String url;
    private int position;


    private int currentPosition;
    private boolean isSeekBarChanging;

    SimpleDateFormat format;
    private AudioManager audioManager;
    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);

        musicList = LitePal.findAll(Music.class);

        seekBar = (SeekBar)findViewById(R.id.media_seekbar);
        textView = (TextView)findViewById(R.id.media_name);
        music_cur = (TextView)findViewById(R.id.music_cur);
        music_length = (TextView)findViewById(R.id.music_length);

        btn_stop = (Button)findViewById(R.id.media_stop);
        btn_play = (Button)findViewById(R.id.media_play);
        btn_next = (Button)findViewById(R.id.media_next);
        btn_stop = (Button)findViewById(R.id.media_stop);
        btn_front = (Button)findViewById(R.id.media_front);

        format = new SimpleDateFormat("mm:ss");
        seekBar.setOnSeekBarChangeListener(new MySeekBar());

        Intent intent = getIntent();
        name = intent.getStringExtra("musicname");
        url = intent.getStringExtra("musicurl");
        position = intent.getIntExtra("position",0);
        textView.setText(name);

        Intent intent1 = new Intent(MediaActivity.this,DownloadService.class);  //前后播放，需要下载
        startService(intent1);
        bindService(intent1,connection,BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(MediaActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MediaActivity.this,new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            initMediaPlayer();
        }

        btn_play.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                currentPosition = 0;

                position = (position+1)%5;
                Music music = musicList.get(position);
                textView.setText(music.getName());

                url = music.getUrl();
                downloadBinder.startDownload(url);

                initMediaPlayer();
                mediaPlayer.start();

            }
        });

        btn_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                currentPosition = 0;

                if (position == 0){
                    position = 4;
                }else {
                    position --;
                }
                Music music = musicList.get(position);
                textView.setText(music.getName());

                url = music.getUrl();
                downloadBinder.startDownload(url);

                initMediaPlayer();
                mediaPlayer.start();
            }
        });

    }
    private void initMediaPlayer(){
        try{
            currentPosition = 0;
            String f = url.substring(url.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();    //SDK Download目录
            File file = new File(directory+f);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    music_length.setText(format.format(mediaPlayer.getDuration())+"");
                    music_cur.setText("00:00");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initMediaPlayer();
                }else {
                    Toast.makeText(MediaActivity.this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
                default:
                    break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.media_play:
                if (!mediaPlayer.isPlaying()){

                    mediaPlayer.start();
                    mediaPlayer.seekTo(currentPosition);

                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        Runnable updateUI = new Runnable() {
                            @Override
                            public void run() {
                                music_cur.setText(format.format(mediaPlayer.getCurrentPosition())+"");
                            }
                        };
                        @Override
                        public void run() {
                            if(!isSeekBarChanging){
                                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                                runOnUiThread(updateUI);
                            }
                        }
                    },0,50);
                }
                break;
            case R.id.media_stop:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    currentPosition = mediaPlayer.getCurrentPosition();
                }
                break;
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        isSeekBarChanging = true;
        if (mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }


    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

        }


        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }
        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            mediaPlayer.seekTo(seekBar.getProgress());
        }
    }

}

