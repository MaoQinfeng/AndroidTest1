package com.example.admin.easylife.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.easylife.Adapter.MusicAdapter;
import com.example.admin.easylife.R;
import com.example.admin.easylife.Service.DownloadService;
import com.example.admin.easylife.db.Music;

import org.litepal.LitePal;

import java.io.File;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private List<Music> musicList;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LitePal.initialize(this);
        listView = (ListView)findViewById(R.id.main_listview);

        //初始化五个音乐
        addMusic();
        musicList = LitePal.findAll(Music.class);

        init();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music music = musicList.get(position);
                String url = music.getUrl();
                downloadBinder.startDownload(url);

                String fileName = url.substring(url.lastIndexOf("/"));
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();    //SDK Download目录
                File file = new File(directory + fileName);
                if (file.exists()){
                    if (music.getLen() == file.length()){
                        Intent intent = new Intent(MainActivity.this,MediaActivity.class);
                        intent.putExtra("musicname",music.getName());
                        intent.putExtra("musicurl",url);
                        intent.putExtra("position",position);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"拒绝权限将无法使用程序",Toast.LENGTH_SHORT).show();
                    finish();
                }
                default:
                    break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public void addMusic(){
        LitePal.deleteAll(Music.class);

        Music c1 = new Music();
        c1.setName("Winter is coming: Adagio - First Snow");
        c1.setUrl("https://freemusicarchive.org/genre/Classical/");
        c1.save();
        Music c2 = new Music();
        c2.setName("Weep no more");
        c2.setUrl("http://music.163.com/song/media/outer/url?id=1357785909.mp3");
        c2.save();
        Music c3 = new Music();
        c3.setName("Fly of the Brants A (ID 950)");
        c3.setUrl("https://freemusicarchive.org/music/download/f5bdea97f13e24290bf7b7822197b72983ddb5f7");
        c3.save();
        Music c4 = new Music();
        c4.setName("Evening Fly of the Brants (ID 951)");
        c4.setUrl("https://freemusicarchive.org/music/download/cbea9c5935a746177d20ac6da3f3575c71bc1de1");
        c4.save();
        Music c5 = new Music();
        c5.setName("Big Discussions (ID 880)");
        c5.setUrl("https://freemusicarchive.org/music/download/4b2c819baf9d3efb9f4e1aeb2680ff9e444826c5");
        c5.save();
    }

    public void init(){
        Intent intent1 = new Intent(MainActivity.this,DownloadService.class);               //下载服务
        startService(intent1);
        bindService(intent1,connection,BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new
                    String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        for (final Music music : musicList){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = music.getUrl();
                    try {
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                .build();
                        Response response = okHttpClient.newCall(request).execute();
                        if (response != null && response.isSuccessful()) {
                            music.setLen(response.body().contentLength());
                            music.setUrl(url);
                            music.setName(music.getName());
                            System.out.println("len = "+music.getLen());
                            music.save();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        MusicAdapter musicAdapter = new MusicAdapter(MainActivity.this,R.layout.item_title,musicList);
        listView.setAdapter(musicAdapter);
    }
}
