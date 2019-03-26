package com.example.diary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class SoundActivity extends AppCompatActivity {
    MediaRecorder recorder = new MediaRecorder();

    private String filePath;
    private String sound_name;
    public boolean sound_enable;
    private boolean record_sound;
    private boolean opened_sound;

    public int sound_power;
    private int Duration;
    public static int second;
    public static int minute;


    private TextView record_time;
    private TextView play_time;
    private SeekBar sb;
    private MediaPlayer mp;

    private  Handler handler ;
    private Timer record_timer;
    private Timer play_timer;
    private TimerTask record_time_task ;
    private TimerTask play_time_task;
    private Uri sound_uri;

    private DialogInterface.OnClickListener confirm;
    private DialogInterface.OnClickListener cancel;

    public static final int RECORDING = 1000;
    public static final int PLAYING = 1001;


    private void stop_record(){
        recorder.stop();
        recorder.reset();
        record_sound = true;
        opened_sound = false;
        stop_record_timer();
        Toast.makeText(SoundActivity.this, "Record time limited to 600s!", Toast.LENGTH_SHORT).show();
        Toast.makeText(SoundActivity.this, "Record success!", Toast.LENGTH_SHORT).show();
    }
    //初始化各种事件
    private void initial(){
        handler = new Handler(){
            public void handleMessage(Message message){
                switch (message.what) {
                    case RECORDING: {
                        if (second == 60) {
                            second = 0;
                            minute++;
                        }
                        String second_str = "";
                        if (second < 10) {
                            second_str = "0" + second;
                        } else second_str = second + "";
                        record_time.setText("time:0" + minute + ":" + second_str);

                    }break;
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
        confirm = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                Intent i = new Intent();
                i.putExtra("changed", false);
                SoundActivity.this.setResult(Protocl.ADD_SOUND, i);
                recorder.release();
                if(record_sound){
                    File delete_file = new File(filePath);
                    delete_file.delete();
                    //String[] path = {filePath};
                    //SoundActivity.this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?",path);
                }
                finish();
            }
        };
        cancel=new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                arg0.cancel();
            }
        };

        sb = findViewById(R.id.Music_seekBar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress>=sb.getMax()){
                    stop_play_timer();
                    mp.seekTo(0);
                    findViewById(R.id.Music_Play_Pause_bt).setBackgroundResource(R.drawable.play_bt_selector);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(sb.getProgress());
                int temp = (int)((float)sb.getProgress()/1000.0f);
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

    public TimerTask getRecord_time_task(){
        return new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                second++;
                if((minute*60 + second)>=600) {
                    stop_record_timer();
                    stop_record();
                }
                message.what = RECORDING;
                handler.sendMessage(message);
            }
        };
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sound);
        ActivityCompat.requestPermissions(SoundActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, Protocl.STORAGE_PERMISSION);
        ActivityCompat.requestPermissions(SoundActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Protocl.READE_PERMISSION);
        if(ContextCompat.checkSelfPermission(SoundActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SoundActivity.this, new String[]{android.Manifest.permission.RECORD_AUDIO}, Protocl.RECORD_PERMISSION);
        }
        else{
            initial_recorder();
        }
        initial();

        play_time = findViewById(R.id.Sound_time);
        record_time = findViewById(R.id.Record_time);


        record_sound = false;
        opened_sound = false;
    }

    private void initial_recorder(){
        Button record_bt = findViewById(R.id.Record_bt);
        record_bt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Toast.makeText(SoundActivity.this, "Record begin!", Toast.LENGTH_SHORT).show();
                    if(mp != null) {
                        if (mp.isPlaying()) {
                            View tv = new View(SoundActivity.this);
                            stop_sound(tv);
                        }
                    }
                    if(filePath == null) {
                        filePath = "";
                    }
                    delete_record_sound();
                    filePath = Environment.getExternalStorageDirectory() + "/Diary_Data/Sounds/" + System.currentTimeMillis() + ".m4a";
                    File output_sound = new File(filePath);
                    if (!output_sound.getParentFile().exists()) {
                        output_sound.getParentFile().mkdirs();
                    }
                    try {
                        if (output_sound.exists()) {
                            output_sound.delete();
                        }
                        output_sound.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC_ELD);
                    recorder.setOutputFile(output_sound);
                    try {
                        recorder.prepare();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                    recorder.start();
                    second = 0;
                    minute = 0;
                    record_timer = new Timer();
                    record_time_task = getRecord_time_task();
                    record_timer.schedule(record_time_task,0,1000);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    try{
                        recorder.stop();
                        recorder.reset();
                        record_sound = true;
                        opened_sound = false;
                        stop_record_timer();
                        set_sound(filePath);
                        Toast.makeText(SoundActivity.this, "Record success!", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
                        Toast.makeText(SoundActivity.this, "Record time is too short!", Toast.LENGTH_SHORT).show();
                        recorder.reset();
                        record_sound = false;
                        second = 0;
                        minute = 0;
                        stop_record_timer();
                    }
                }
                return false;
            }
        });
    }

    public void back_sound(View view){
        AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
        alert_dialog_builder.setMessage("Leave without saving?");
        alert_dialog_builder.setPositiveButton("confirm", confirm);
        alert_dialog_builder.setNegativeButton("cancel", cancel);
        AlertDialog alert_dialog = alert_dialog_builder.create();
        alert_dialog.show();
    }

    private void copy_Sound(String from_File){
        String save_File_Name = Environment.getExternalStorageDirectory() + "/Diary_Data/Sounds/" + System.currentTimeMillis();
        File save_File = new File(save_File_Name);
        if (!save_File.getParentFile().exists()) {
            save_File.getParentFile().mkdirs();
        }
        else if(save_File.exists()||save_File.isDirectory()){
            save_File.delete();
        }
        try {
            InputStream inputStream = new FileInputStream(from_File);
            FileOutputStream outputStream = new FileOutputStream(save_File);
            byte[] bytes = new byte[1024];
            int i;
            while ((i = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, i);
            }
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        filePath = save_File_Name;
    }

    public void complete_sound(View view){
        //to do:add your media
        if(opened_sound||record_sound) {
            Intent i = new Intent();
            if (opened_sound) {
                copy_Sound(filePath);
            }
            if (filePath == null) {
                filePath = "";
            }
            if (mp != null) {
                if (mp.isPlaying()) {
                    View tv = new View(SoundActivity.this);
                    stop_sound(tv);
                }
            }
            recorder.release();
            i.putExtra("changed", true);
            i.putExtra("sound_path", filePath);
            i.putExtra("sound_name", sound_name);
            this.setResult(Protocl.ADD_SOUND, i);
            finish();
        }
        else {
            Toast.makeText(this, "You did not choose anything!", Toast.LENGTH_SHORT).show();
        }
    }

    public void open_sound(View view){
//如果没有权限则申请权限
        if (ContextCompat.checkSelfPermission(SoundActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SoundActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Protocl.OPEN_PERMISSION);
        }
        else{
            openAlbum();
        }
    }

    private void openAlbum(){
        if(filePath == null) {
            filePath = "";
        }
        if(mp != null) {
            if (mp.isPlaying()) {
                View tv = new View(SoundActivity.this);
                stop_sound(tv);
            }
        }
        delete_record_sound();
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("audio/*");

        startActivityForResult(intent, Protocl.CHOOSE_SOUND); // 打开相册
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
            alert_dialog_builder.setMessage("Leave without saving?");
            alert_dialog_builder.setPositiveButton("confirm", confirm);
            alert_dialog_builder.setNegativeButton("cancel", cancel);
            AlertDialog alert_dialog = alert_dialog_builder.create();
            alert_dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Protocl.RECORD_PERMISSION:{
                if(ContextCompat.checkSelfPermission(SoundActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SoundActivity.this, "Getting record permission failed\nYou should open the permission manually", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Or you denied the permission", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent();
                    i.putExtra("changed", false);
                    SoundActivity.this.setResult(Protocl.ADD_SOUND, i);
                    recorder.release();
                    finish();
                }
                else {
                    initial_recorder();
                }
            }break;
            case  Protocl.OPEN_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
            }break;
        }
    }

    private void stop_record_timer(){
        if (record_timer != null) {
            record_timer.cancel();
            record_timer = null;
        }

        if (record_time_task != null) {
            record_time_task.cancel();
            record_time_task = null;
        }
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

    public void play_pause_sound(View view){
        if(opened_sound||record_sound) {
            if (mp.isPlaying()) {
                mp.pause();
                stop_play_timer();
                findViewById(R.id.Music_Play_Pause_bt).setBackgroundResource(R.drawable.play_bt_selector);
            } else {
                play_timer = new Timer();
                play_time_task = getPlay_time_task();
                play_timer.schedule(play_time_task,0,1000);
                handler.post(start);
                findViewById(R.id.Music_Play_Pause_bt).setBackgroundResource(R.drawable.pause_bt_selector);
            }
        }
        else{
            Toast.makeText(this, "Sound is not loaded!", Toast.LENGTH_SHORT).show();
        }

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
            sb.setProgress(mp.getCurrentPosition());
            handler.postDelayed(update_sb, 1000);
            //每秒钟更新一次
        }

    };

    private void delete_record_sound(){
        File file = new File(filePath);
        if(file.exists()) {
            file.delete();
        }
    }

    public void mute_sound(View view){
        if(opened_sound||record_sound) {
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (!sound_enable) {
                sound_enable = true;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, sound_power, 0);
                findViewById(R.id.Sound_Mute_bt).setBackgroundResource(R.drawable.sound_bt_selector);
            } else {
                sound_power = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                sound_enable = false;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                findViewById(R.id.Sound_Mute_bt).setBackgroundResource(R.drawable.mute_bt_selector);
            }
        }
        else{
            Toast.makeText(this, "Sound is not loaded!", Toast.LENGTH_SHORT).show();
        }
    }

    public void stop_sound(View view){
        if(opened_sound||record_sound) {
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
            findViewById(R.id.Music_Play_Pause_bt).setBackgroundResource(R.drawable.play_bt_selector);
        }
        else{
            Toast.makeText(this, "Sound is not loaded!", Toast.LENGTH_SHORT).show();
        }
    }

    private void set_sound(String path){
        filePath = path;
        File file = new File(filePath);
        sound_name = file.getName();
        if(record_sound) {
            Uri uri = Uri.fromFile(file);
            mp = MediaPlayer.create(this, uri);
        }
        else{
            mp = MediaPlayer.create(this, sound_uri);
        }
        //监听器
        Duration=mp.getDuration();
        //音乐文件持续时间
        sb.setMax(Duration);
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
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sound_power = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        play_time.setText("time:0" + minute + ":" + second_str + max_str);
        ((TextView)findViewById(R.id.Sound_name)).setText(sound_name);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case Protocl.CHOOSE_SOUND:{
                // 判断手机系统版本号
                if (Build.VERSION.SDK_INT >= 19) {
                    // 4.4及以上系统使用这个方法处理图片
                    handleSoundOnKitKat(data);
                } else {
                    // 4.4以下系统使用这个方法处理图片
                    handleSoundBeforeKitKat(data);
                }
            }break;
        }
    }
    @TargetApi(19)
    private void handleSoundOnKitKat(Intent data)         {
        String Path = null;
        if(data != null) {
            Uri uri = data.getData();
            sound_uri = uri;
            Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
            Path = UriUtils.getPath(SoundActivity.this,uri);
            opened_sound = true;
            record_sound = false;
            set_sound(Path); // 根据图片路径显示图片
        }
        else{
            Toast.makeText(this, "You canceled open", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSoundBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        sound_uri = uri;
        String imagePath = getSoundPath(uri, null);
        opened_sound = true;
        record_sound = false;
        set_sound(imagePath);
    }

    private String getSoundPath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

}
