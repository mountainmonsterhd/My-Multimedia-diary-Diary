package com.example.diary;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DiaryActivity extends AppCompatActivity {
    private Context context;
    private SaveActivity saveActivity;
    private String date; //日期
    private String weather;//天气
    private String today;//星期

    private EditText Title;   //题目
    private EditText Sub_Title;  //第一个内容
    private EditText[] editTexts;//文本内容
    private ImageView image_Views;
    private MyVideoView video_Views;
    private MediaController mediaController;
    private Music_Bar music_bar;
    private LinearLayout video_Content;
    private LinearLayout diary_body;
    private ViewSwitcher viewSwitcher;
    private boolean changed;//是否修改过日记内容
    private boolean editable;
    private boolean new_Diary;
    private int diary_index;

    private DialogInterface.OnClickListener confirm;
    private DialogInterface.OnClickListener cancel;

    private void initial_dialog(){
        confirm=new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                if(new_Diary) {
                    for (int i = 0; i < saveActivity.image_num; i++) {
                        File delete_file = new File(saveActivity.img_filepath[i]);
                        delete_file.delete();
                        String[] path = {saveActivity.img_filepath[i]};
                        DiaryActivity.this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", path);
                    }
                    for (int i = 0; i < saveActivity.video_num; i++) {
                        File delete_file = new File(saveActivity.video_filepath[i]);
                        delete_file.delete();
                        String[] path = {saveActivity.video_filepath[i]};
                        DiaryActivity.this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", path);
                    }
                    for (int i = 0; i < saveActivity.sound_num; i++) {

                    }
                }
                Intent intent = new Intent();
                intent.putExtra(Protocl.SAVED,false);
                DiaryActivity.this.setResult(Protocl.OPEN_NEW_DIARY,intent);
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
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary);
        context = getApplicationContext();
        diary_body = findViewById(R.id.Diary_Item);
        ActivityCompat.requestPermissions(DiaryActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(DiaryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        Intent intent = getIntent();
        changed  = false;
        new_Diary = intent.getBooleanExtra(Protocl.NEW_DIARY,true);
        editable = new_Diary;
        initial_dialog();
        initData();
        initial_Edit();
    }
    //初始化页面数据
    private void initData() {
        viewSwitcher = findViewById(R.id.Diary_Menu_Layout);
        findViewById(R.id.Diary_Menu_include).setVisibility(View.INVISIBLE);
        Intent intent = getIntent();
        date = intent.getStringExtra(Protocl.DATE);
        today = intent.getStringExtra(Protocl.DAY);
        weather = intent.getStringExtra(Protocl.WEATHER);
        diary_index = intent.getIntExtra(Protocl.DIARY_INDEX,0);
        saveActivity = new SaveActivity(new_Diary,date,diary_index,context);
        initLayout();
    }
    //初始化页面
    private void initLayout() {
        editTexts = new EditText[saveActivity.text_num];
        editTexts[0] = findViewById(R.id.Sub_Title);
        int text_index = 0;
        int image_index = 0;
        int video_index = 0;
        int sound_index = 0;
        if (!new_Diary) {
            diary_body.removeAllViews();
            editTexts[0].setId(R.id.Sub_Title);
            if (saveActivity.diary_num > 0) {
                for (int i = 0; i < saveActivity.item_index.length(); i++) {
                    char c = saveActivity.item_index.charAt(i);
                    switch (c) {
                        case 't': {
                            setText(saveActivity.text_filepath[text_index], text_index);
                            text_index++;
                        }
                        break;
                        case 'i': {
                            setImage(saveActivity.img_filepath[image_index]);
                            image_index++;
                        }
                        break;
                        case 'v': {
                            setVideo(saveActivity.video_filepath[video_index]);
                            video_index++;
                        }
                        break;
                        case 's': {
                            setSound(saveActivity.sound_filepath[sound_index], saveActivity.sound_name[sound_index]);
                            sound_index++;
                        }
                        break;
                        case ';': {
                            break;
                        }
                    }
                }
            }
        }
    }
//初始化控件数据
    private void initial_Edit(){
        Title = findViewById(R.id.Title_Name);
        Sub_Title = findViewById(R.id.Sub_Title);
        Title.setText(saveActivity.Title);
        if(new_Diary) {
            String sub_title = today + "   " + change_date_format(date) + "   " + weather + "\n";
            Sub_Title.setText(sub_title);
        }
        if(editable){
            findViewById(R.id.save_edit_bt).setBackgroundResource(R.drawable.save_bt_selector);
            Title.setEnabled(true);
            for (int i = 0;i<saveActivity.text_num;i++){
                editTexts[i].setEnabled(true);
            }
        }
        else{
            findViewById(R.id.save_edit_bt).setBackgroundResource(R.drawable.edit_bt_selector);
            Title.setEnabled(false);
            for (int i = 0;i<saveActivity.text_num;i++){
                editTexts[i].setEnabled(false);
            }
        }
    }
//返回按钮
    public void back(View view){

        if(editable) {
            AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
            alert_dialog_builder.setMessage("Leave without saving?");
            alert_dialog_builder.setPositiveButton("confirm", confirm);
            alert_dialog_builder.setNegativeButton("cancel", cancel);
            AlertDialog alert_dialog = alert_dialog_builder.create();
            alert_dialog.show();
        }
        else {
            finish();
        }
    }
//保存||编辑按钮
    public void save_edit(View view){
        if(((EditText)findViewById(R.id.Title_Name)).getText().toString().isEmpty()){
            Toast.makeText(this, "Title can not be empty!", Toast.LENGTH_SHORT).show();
        }
        else {
            if (editable) {
                findViewById(R.id.save_edit_bt).setBackgroundResource(R.drawable.edit_bt_selector);
                Title.setEnabled(false);
                for (int i = 0;i<saveActivity.text_num;i++){
                    editTexts[i].setEnabled(false);
                }
                save_data();
                Toast.makeText(this, "Diary saved!", Toast.LENGTH_SHORT).show();
                editable = false;
            } else {
                findViewById(R.id.save_edit_bt).setBackgroundResource(R.drawable.save_bt_selector);
                Title.setEnabled(true);
                for (int i = 0;i<saveActivity.text_num;i++){
                    editTexts[i].setEnabled(true);
                }
                editable = true;
            }
        }
    }
//打开日记菜单
    public void open_Diary_Menu(View view){
        toggleScene();
    }
//添加图片
    public void open_picture(View view){
        Button mImage = findViewById(R.id.Picture_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        Intent i = new Intent(this,PictureActivity.class);
        startActivityForResult(i,Protocl.ADD_PICTURE,compat.toBundle());
    }
//添加视频
    public void open_video(View view){
        Button mImage = findViewById(R.id.Video_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        Intent i = new Intent(this,VideoActivity.class);
        startActivityForResult(i,Protocl.ADD_VIDEO,compat.toBundle());
    }
//添加声音
    public void open_sound(View view){
        Button mImage = findViewById(R.id.Sound_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        Intent i = new Intent(this,SoundActivity.class);
        startActivityForResult(i,Protocl.ADD_SOUND,compat.toBundle());
    }
    //文本储存
    private void save_txt(){
        saveActivity.text_filepath = new String[saveActivity.text_num];
        for(int i = 0;i<saveActivity.text_num;i++) {
            String save_Text_Name = Environment.getExternalStorageDirectory() + "/Diary_Data/Text/" + System.currentTimeMillis();
            saveActivity.text_filepath[i] = save_Text_Name;
            saveActivity.put_text_path(date,i);
            try {
                File text_file = new File(saveActivity.text_filepath[i]);
                if(!text_file.getParentFile().exists()){
                    if(!text_file.getParentFile().mkdirs()){
                        Log.d("TAG","Create dictionary failed!");
                    }
                }
                if (text_file.exists()) {
                    if(!text_file.delete()){
                        Log.d("TAG","Delete file failed!");
                    }
                }
                if(!text_file.createNewFile()){
                    Log.d("TAG","Create file failed!");
                }
                FileOutputStream outputStream = new FileOutputStream(text_file);
                byte[] bytes = new byte[1024];
                bytes = editTexts[i].getText().toString().getBytes();
                int j = bytes.length;
                outputStream.write(bytes, 0, j);
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    //储存数据
    private void save_data(){
        saveActivity.put_data(date,weather ,Title.getText().toString(),new_Diary);
        save_txt();
        saveActivity.save_data();
    }
    //改变日期格式
    private String change_date_format(String d){
        MyDate date = new MyDate(d);
        int month = date.getMonth();
        int day = date.getDay();
        int year = date.getYear();
        String day_str;
        if(day == 1){
            day_str = day + "st, ";
        }
        else if(day == 2){
            day_str = day + "nd, ";
        }
        else if(day == 3){
            day_str = day + "rd, ";
        }
        else{
            day_str = day + "th, ";
        }
        switch (month){
            case 1:{d = "January " + day_str + year;}break;
            case 2:{d = "February " + day_str + year;}break;
            case 3:{d = "March " + day_str + year;}break;
            case 4:{d = "April " + day_str + year;}break;
            case 5:{d = "May " + day_str + year;}break;
            case 6:{d = "June " + day_str + year;}break;
            case 7:{d = "July " + day_str + year;}break;
            case 8:{d = "August " + day_str + year;}break;
            case 9:{d = "September " + day_str + year;}break;
            case 10:{d = "October " + day_str + year;}break;
            case 11:{d = "November " + day_str + year;}break;
            case 12:{d = "December " + day_str + year;}break;
        }
        return d;
    }

    private void setText(String path,int i){
        File text_file = new File(path);
        String text_content = "";
        try{
            InputStream is = new FileInputStream(text_file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuffer buffer = new StringBuffer();
            String line;
            line = reader.readLine();
            while (line != null) { // 如果 line 为空说明读完了
                buffer.append(line); // 将读到的内容添加到 buffer 中
                buffer.append("\n"); // 添加换行符
                line = reader.readLine(); // 读取下一行
            }
            text_content = buffer.toString();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        editTexts[i] = new EditText(DiaryActivity.this);
        editTexts[i].setSingleLine(false);
        editTexts[i].setHorizontallyScrolling(false);
        editTexts[i].setGravity(Gravity.START|Gravity.TOP);
        editTexts[i].setInputType(0x00020001);
        editTexts[i].setTextColor(Color.parseColor("#090000"));
        editTexts[i].setTypeface(Typeface.SERIF);
        diary_body.addView(editTexts[i]);
        editTexts[i].setText(text_content);
    }
    private void setImage(String path){
        image_Views = new ImageView(DiaryActivity.this);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int degree = getDegree(path);
        bitmap = rotate_ImageView(degree, bitmap);
        image_Views.setImageBitmap(bitmap);
        image_Views.setAdjustViewBounds(true);
        diary_body.addView(image_Views);
    }
    private void setVideo(String path){
        video_Views = new MyVideoView(DiaryActivity.this);
        video_Content = new LinearLayout(DiaryActivity.this);
        mediaController=new MediaController(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,600);
        video_Content.addView(video_Views);
        video_Content.setLayoutParams(lp );
        video_Views.setVideoPath(path);
        video_Views.setMediaController(mediaController);
        mediaController.setMediaPlayer(video_Views);
        video_Views.requestFocus();
        diary_body.addView(video_Content);
    }
    private void setSound(String path,String name){
        music_bar = new Music_Bar(DiaryActivity.this,path,name);
        diary_body.addView(music_bar);
    }

//变换菜单
    private void toggleScene() {
        if(changed) {
            viewSwitcher.setDisplayedChild(1);
            findViewById(R.id.Arrow_bt).setBackgroundResource(R.drawable.arrow_selector);
        }
        else {
            viewSwitcher.setDisplayedChild(0);
            findViewById(R.id.Diary_Menu_include).setVisibility(View.VISIBLE);
            findViewById(R.id.Arrow_bt).setBackgroundResource(R.drawable.arrow_back_selector);
        }
        changed = !changed;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:{
                if(editable) {
                    AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(this);
                    alert_dialog_builder.setMessage("Leave without saving?");
                    alert_dialog_builder.setPositiveButton("confirm", confirm);
                    alert_dialog_builder.setNegativeButton("cancel", cancel);
                    AlertDialog alert_dialog = alert_dialog_builder.create();
                    alert_dialog.show();
                }
                else {
                    finish();
                }
            }break;
            case KeyEvent.KEYCODE_DEL:{

            }
        }
        return super.onKeyDown(keyCode, event);
    }
    //每次添加完多媒体后，需要添加输入框
    private void add_edit_text(){
        EditText new_text = new EditText(DiaryActivity.this);
        new_text.setSingleLine(false);
        new_text.setHorizontallyScrolling(false);
        new_text.setGravity(Gravity.START|Gravity.TOP);
        new_text.setInputType(0x00020001);
        new_text.setTextColor(Color.parseColor("#090000"));
        new_text.setTypeface(Typeface.SERIF);
        diary_body.addView(new_text);
        //saveActivity.item_num++;
        saveActivity.text_num++;
        saveActivity.item_index += "t;";
        EditText[] temp_text = new EditText[saveActivity.text_num-1];
        System.arraycopy(editTexts,0,temp_text,0,saveActivity.text_num-1);
        editTexts = new EditText[saveActivity.text_num];
        System.arraycopy(temp_text,0,editTexts,0,saveActivity.text_num-1);
        editTexts[saveActivity.text_num-1] = new_text;
    }
    //得到的图片
    private void addImage(Intent data){
        //data changed
        String[] temp_path = new String[saveActivity.image_num];
        if(saveActivity.image_num>0) {
            System.arraycopy(saveActivity.img_filepath,0,temp_path,0,saveActivity.image_num);
        }
        saveActivity.image_num++;
        //saveActivity.item_num++;
        saveActivity.img_filepath = new String[saveActivity.image_num];
        saveActivity.img_filepath[saveActivity.image_num-1] = data.getStringExtra("image_path");
        image_Views = new ImageView(DiaryActivity.this);
        if(saveActivity.image_num>0){
            System.arraycopy(temp_path,0,saveActivity.img_filepath,0,saveActivity.image_num-1);
        }

        Bitmap bitmap = BitmapFactory.decodeFile(saveActivity.img_filepath[saveActivity.image_num-1]);
        int degree = getDegree(saveActivity.img_filepath[saveActivity.image_num-1]);
        bitmap = rotate_ImageView(degree,bitmap);
        image_Views.setImageBitmap(bitmap);
        image_Views.setAdjustViewBounds(true);
        diary_body.addView(image_Views);
        saveActivity.item_index += "i;";

        add_edit_text();
    }
    //得到的视频
    private void addVideo(Intent data){
        String[] temp_path = new String[saveActivity.video_num];
        if(saveActivity.video_num>0) {
            System.arraycopy(saveActivity.video_filepath,0,temp_path,0,saveActivity.video_num);
        }
        saveActivity.video_num++;
        //saveActivity.item_num++;
        saveActivity.video_filepath = new String[saveActivity.video_num];
        saveActivity.video_filepath[saveActivity.video_num-1] = data.getStringExtra("video_path");
        video_Views = new MyVideoView(DiaryActivity.this);
        video_Content = new LinearLayout(DiaryActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,600);
        if(saveActivity.video_num>0){
            System.arraycopy(temp_path,0,saveActivity.video_filepath,0,saveActivity.video_num-1);
        }
        video_Content.addView(video_Views);
        video_Content.setLayoutParams(lp );

        video_Views.setVideoPath(saveActivity.video_filepath[saveActivity.video_num-1]);
        mediaController=new MediaController(this);
        video_Views.setMediaController(mediaController);
        mediaController.setMediaPlayer(video_Views);
        video_Views.requestFocus();
        diary_body.addView(video_Content);
        saveActivity.item_index += "v;";
        add_edit_text();
    }
    //得到的声音
    private void addSound(Intent data){
        String[] temp_path = new String[saveActivity.sound_num];
        String[] temp_name = new String[saveActivity.sound_num];
        if(saveActivity.sound_num>0) {
            System.arraycopy(saveActivity.sound_filepath,0,temp_path,0,saveActivity.sound_num);
            System.arraycopy(saveActivity.sound_name,0,temp_name,0,saveActivity.sound_num);
        }
        saveActivity.sound_num++;
        //saveActivity.item_num++;
        saveActivity.sound_filepath = new String[saveActivity.sound_num];
        saveActivity.sound_name = new String[saveActivity.sound_num];
        saveActivity.sound_filepath[saveActivity.sound_num-1] = data.getStringExtra("sound_path");
        saveActivity.sound_name[saveActivity.sound_num-1] = data.getStringExtra("sound_name");
        music_bar = new Music_Bar(DiaryActivity.this,saveActivity.sound_filepath[saveActivity.sound_num-1],saveActivity.sound_name[saveActivity.sound_num-1]);
        if(saveActivity.sound_num>0){
            System.arraycopy(temp_path,0,saveActivity.sound_filepath,0,saveActivity.sound_num-1);
            System.arraycopy(temp_name,0,saveActivity.sound_name,0,saveActivity.sound_num-1);
        }
        diary_body.addView(music_bar);
        saveActivity.item_index += "s;";

        add_edit_text();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data != null) {
            boolean is_Get_data = data.getBooleanExtra("changed", false);
            switch (requestCode) {
                //picture
                case Protocl.ADD_PICTURE: {
                    if (is_Get_data) {
                        addImage(data);
                    } else {
                        //no data
                        Toast.makeText(this, "You did not add anything!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                //video
                case Protocl.ADD_VIDEO: {
                    if (is_Get_data) {
                        //data changed
                        addVideo(data);
                    } else {
                        //no data
                        Toast.makeText(this, "You did not add anything!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
                //sound
                case Protocl.ADD_SOUND: {
                    if (is_Get_data) {
                        //data changed
                        addSound(data);
                    } else {
                        //no data
                        Toast.makeText(this, "You did not add anything!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
        else{
            Toast.makeText(this, "No data return!", Toast.LENGTH_SHORT).show();
        }
    }
    //获取图片的方向
    private int getDegree(String path) {
        int digree = 0;
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        return digree;
    }
    //旋转图片
    private Bitmap rotate_ImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
