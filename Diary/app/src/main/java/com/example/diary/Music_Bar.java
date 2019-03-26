package com.example.diary;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;


public class Music_Bar extends ConstraintLayout {

    private Context music_context;
    private TextView sound_name;
    private TextView play_time;
    private SeekBar music_seek_bar;
    private Button play_pause_bt;
    private Button stop_bt;
    private Button sound_mute_bt;
    private MediaPlayer mp;
    private Handler handler;
    private Timer play_timer;
    private TimerTask play_time_task;
    public boolean sound_enable;

    public int sound_power;
    private int Duration;
    public static int second;
    public static int minute;

    private String name;

    public static final int PLAYING = 1001;

    private void initial(){
        handler = new android.os.Handler(){
            public void handleMessage(Message message){
                switch (message.what) {
                    case PLAYING:{
                        if (second == 60) {
                            second = 0;
                            minute++;
                        }
                        String second_str = "";
                        if (second < 10) {
                            second_str = "0" + second;
                        } else second_str = second + "";
                        int max_min = (int)((float)Duration/60000.0f);
                        int max_sec = (int)((float)Duration/1000.0f) - max_min*60;
                        String max_str = "/";
                        if(max_min<10){
                            max_str += "0"+max_min + ":";
                        }
                        else max_str += max_min + ":";
                        if(max_sec<10){
                            max_str += "0"+max_sec;
                        }
                        else max_str += max_sec;
                        play_time.setText("time:0" + minute + ":" + second_str + max_str);
                    }break;
                }
            }
        };
        music_seek_bar = findViewById(R.id.Music_Seek_Bar);
        music_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress>=music_seek_bar.getMax()){
                    stop_play_timer();
                    mp.seekTo(0);
                    play_pause_bt.setBackgroundResource(R.drawable.music_play_bt_selector);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(music_seek_bar.getProgress());
                int temp = (int)((float)music_seek_bar.getProgress()/1000.0f);
                minute = temp/60;
                second = temp - minute*60;
                String second_str;
                if (second < 10) {
                    second_str = "0" + second;
                } else second_str = second + "";
                int max_min = (int)((float)Duration/60000.0f);
                int max_sec = (int)((float)Duration/1000.0f) - max_min*60;
                String max_str = "/";
                if(max_min<10){
                    max_str += "0"+max_min + ":";
                }
                else max_str += max_min + ":";
                if(max_sec<10){
                    max_str += "0"+max_sec;
                }
                else max_str += max_sec;
                play_time.setText("time:0" + minute + ":" + second_str + max_str);
            }
        });
    }

    private void stop_play_timer(){
        if (play_timer != null) {
            play_timer.cancel();
            play_timer = null;
        }

        if (play_time_task != null) {
            play_time_task.cancel();
            play_time_task = null;
        }
    }

    private void init_Listener(){
        play_pause_bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mp.isPlaying()) {
                    mp.pause();
                    stop_play_timer();
                    play_pause_bt.setBackgroundResource(R.drawable.music_play_bt_selector);
                }
                else {
                    play_timer = new Timer();
                    play_time_task = getPlay_time_task();
                    play_timer.schedule(play_time_task, 0, 1000);
                    handler.post(start);
                    play_pause_bt.setBackgroundResource(R.drawable.music_pause_bt_selector);
                }
            }
        });
        stop_bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.seekTo(0);
                mp.pause();
                stop_play_timer();
                second = 0;
                minute = 0;
                String second_str;
                if (second < 10) {
                    second_str = "0" + second;
                } else second_str = second + "";
                int max_min = (int)((float)Duration/60000.0f);
                int max_sec = (int)((float)Duration/1000.0f) - max_min*60;
                String max_str = "/";
                if(max_min<10){
                    max_str += "0"+max_min + ":";
                }
                else max_str += max_min + ":";
                if(max_sec<10){
                    max_str += "0"+max_sec;
                }
                else max_str += max_sec;
                play_time.setText("time:0" + minute + ":" + second_str + max_str);
                play_pause_bt.setBackgroundResource(R.drawable.music_play_bt_selector);
            }
        });
        sound_mute_bt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager mAudioManager = (AudioManager) music_context.getSystemService(Context.AUDIO_SERVICE);
                if (!sound_enable) {
                    sound_enable = true;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound_power, 0);
                    sound_mute_bt.setBackgroundResource(R.drawable.music_sound_bt_selector);
                } else {
                    sound_power = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    sound_enable = false;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                    sound_mute_bt.setBackgroundResource(R.drawable.music_mute_bt_selector);
                }
            }
        });
    }

    public TimerTask getPlay_time_task() {
        return new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                second++;
                if((minute*60 + second)>=(float)Duration/1000.0f) {
                    stop_play_timer();
                    second = 0;
                    minute = 0;
                }
                message.what = PLAYING;
                handler.sendMessage(message);
            }
        };
    }


    Runnable start=new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mp.start();
            handler.post(update_sb);

            //用一个handler更新SeekBar
        }

    };
    Runnable update_sb =new Runnable(){

        @Override
        public void run() {
            // TODO Auto-generated method stub
            music_seek_bar.setProgress(mp.getCurrentPosition());
            handler.postDelayed(update_sb, 1000);
            //每秒钟更新一次
        }

    };
    public void init_Music_Bar(Context context,String path){
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        initial();
        mp = MediaPlayer.create(context, uri);
        //监听器
        Duration=mp.getDuration();
        //音乐文件持续时间
        music_seek_bar.setMax(Duration);
        second = 0;
        minute = 0;
        String second_str;
        if (second < 10) {
            second_str = "0" + second;
        } else second_str = second + "";
        int max_min = (int)((float)Duration/60000.0f);
        int max_sec = (int)((float)Duration/1000.0f) - max_min*60;
        String max_str = "/";
        if(max_min<10){
            max_str += "0"+max_min + ":";
        }
        else max_str += max_min + ":";
        if(max_sec<10){
            max_str += "0"+max_sec;
        }
        else max_str += max_sec;
        sound_enable = true;
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        sound_power = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        play_time.setText("time:0" + minute + ":" + second_str + max_str);
        sound_name.setText(name);
        init_Listener();
    }
    //构造函数
    public Music_Bar(Context context,String path,String in_name) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.sound_play_layout,this,true);
        name = in_name;
        music_context = context;
        sound_name = findViewById(R.id.Sound_Name);
        play_time = findViewById(R.id.Sound_Time);
        play_pause_bt = findViewById(R.id.Music_Play_Pause_bt);
        stop_bt = findViewById(R.id.Music_Stop_bt);
        sound_mute_bt = findViewById(R.id.Music_Sound_Mute_bt);
        init_Music_Bar(music_context,path);
    }
    public Music_Bar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public Music_Bar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
