package com.example.diary;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

import static android.content.ContentValues.TAG;

public class SaveActivity {
    public int image_num;//图片数量
    public int video_num;//视频数量
    public int sound_num;//声音数量
    public int text_num;//文字控件数量
    //public int item_num;//物件数量
    public int diary_index;//当前日记序号
    public int diary_num;//当日日记数量
    public int max_index;//当日日记最大序列号
    public int total_diary_num;//总日记数量

    public String item_index;//物件顺序序列
    public String first_date;//第一个日记的日期
    public String weather;//对应日期天气
    public String Title;

    private MyDate myDate;

    public String[] img_filepath;//图片路径名称
    public String[] text_filepath;//文本路径名称
    public String[] video_filepath;//视频路径名称
    public String[] sound_filepath;//声音路径名称
    public String[] sound_name;//声音的名称
//存储宏定义
    public static final String MAX_INDEX = "max_index";
    public static final String IMAGE_NUM = "image_num";
    public static final String VIDEO_NUM = "video_num";
    public static final String SOUND_NUM = "sound_num";
    public static final String TEXT_NUM = "text_num";
    public static final String ITEM_NUM = "item_num";
    public static final String TOTAL_DIARY_NUM = "total_diary_num";
    public static final String DIARY_NUM = "diary_num";
    public static final String DIARY_TITLE = "diary_title";
    public static final String ITEM_INDEX = "item_index";
    public static final String IMG_FILEPATH = "img_filepath";
    public static final String TEXT_FILEPATH = "text_filepath";
    public static final String VIDEO_FILEPATH = "video_filepath";
    public static final String SOUND_FILEPATH = "sound_filepath";
    public static final String SOUND_NAME = "sound_name";
    public static final String FIRST_DATE = "first_date";
    public static final String TODAY_WEATHER = "today_weather";
    public static final String CACHE = "cache";

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SaveActivity(boolean new_diary,String date,int index,Context c){
        context = c;
        preferences = context.getSharedPreferences(CACHE,Context.MODE_PRIVATE);
        editor = preferences.edit();
        diary_index = index;
        total_diary_num = preferences.getInt(TOTAL_DIARY_NUM,0);
        diary_num = preferences.getInt(date + DIARY_NUM, 0);
        max_index = preferences.getInt(date + MAX_INDEX,-1);
        first_date = preferences.getString(FIRST_DATE,"");
        myDate = new MyDate(date);
        if(!new_diary) {
            weather = preferences.getString(date+ TODAY_WEATHER,"Rainy");
            Title = preferences.getString(date + DIARY_TITLE + diary_index,"");
            item_index = preferences.getString(date + ITEM_INDEX + diary_index, "t;");
            text_num = preferences.getInt(date + TEXT_NUM + diary_index, 1);
            image_num = preferences.getInt(date + IMAGE_NUM + diary_index, 0);
            video_num = preferences.getInt(date + VIDEO_NUM + diary_index, 0);
            sound_num = preferences.getInt(date + SOUND_NUM + diary_index, 0);
            //item_num = preferences.getInt(date + ITEM_NUM + diary_index,1);
            if (image_num > 0) {
                img_filepath = new String[image_num];
                for (int i = 0; i < image_num; i++) {
                    img_filepath[i] = preferences.getString(date + IMG_FILEPATH + diary_index + i, "");
                    if (img_filepath[i].isEmpty()) {
                        //item_num -= image_num;
                        image_num = 0;
                        break;
                    }
                }
            }
            if (video_num > 0) {
                video_filepath = new String[video_num];
                for (int i = 0; i < video_num; i++) {
                    video_filepath[i] = preferences.getString(date + VIDEO_FILEPATH + diary_index + i, "");
                    if (video_filepath[i].isEmpty()) {
                        //item_num -= video_num;
                        video_num = 0;
                        break;
                    }
                }
            }
            if (sound_num > 0) {
                sound_filepath = new String[sound_num];
                sound_name = new String[sound_num];
                for (int i = 0; i < sound_num; i++) {
                    sound_filepath[i] = preferences.getString(date + SOUND_FILEPATH + diary_index + i, "");
                    sound_name[i] = preferences.getString(date + SOUND_NAME + diary_index + i,"");
                    if (sound_filepath[i].isEmpty()) {
                        //item_num -= sound_num;
                        sound_num = 0;
                        break;
                    }
                }
            }
            if (text_num > 0) {
                text_filepath = new String[text_num];
                for (int i = 0; i < text_num; i++) {
                    text_filepath[i] = preferences.getString(date + TEXT_FILEPATH + diary_index + i, "");
                    if (text_filepath[i].isEmpty()) {
                        //item_num -= (text_num-1);
                        text_num = 1;
                        break;
                    }
                }
            }
        }
        else{
            if(total_diary_num == 0){
                first_date = date;
            }
            Title ="";
            item_index = "t;";
            max_index = index;
            text_num = 1;
            image_num = 0;
            video_num = 0;
            sound_num = 0;
            //item_num = 1;
        }
    }

    public void put_data(String date,String w, String Title,boolean new_diary){
        if(new_diary) {
            diary_num++;
            total_diary_num++;
        }
        weather = w;
        editor.putInt(date + MAX_INDEX, max_index);
        editor.putString(FIRST_DATE,first_date);
        editor.putString(date + ITEM_INDEX + diary_index,item_index);
        editor.putString(date + TODAY_WEATHER,weather);
        editor.putString(date + DIARY_TITLE + diary_index,Title);
        editor.putInt(date + DIARY_NUM,diary_num);
        //editor.putInt(date + ITEM_NUM + diary_index,item_num);
        editor.putInt(date + IMAGE_NUM + diary_index,image_num);
        editor.putInt(date + TEXT_NUM + diary_index,text_num);
        editor.putInt(date + VIDEO_NUM + diary_index,video_num);
        editor.putInt(date + SOUND_NUM + diary_index,sound_num);
        editor.putInt(TOTAL_DIARY_NUM,total_diary_num);

        if(image_num>0) {
            for (int i = 0;i<image_num;i++){
                editor.putString(date + IMG_FILEPATH + diary_index + i,img_filepath[i]);
            }
        }
        if(video_num>0) {
            for (int i = 0;i<video_num;i++){
                editor.putString(date + VIDEO_FILEPATH + diary_index + i,video_filepath[i]);
            }
        }
        if(sound_num>0) {
            for (int i = 0;i<sound_num;i++){
                editor.putString(date + SOUND_FILEPATH+ diary_index + i,sound_filepath[i]);
                editor.putString(date + SOUND_NAME + diary_index + i,sound_name[i]);
            }
        }
    }

    public void put_text_path(String date, int i){
        editor.putString(date + TEXT_FILEPATH + diary_index + i,text_filepath[i]);
    }

    public void delete_diary(String date,int index){
        diary_index = index;
        myDate = new MyDate(date);
        total_diary_num = preferences.getInt(TOTAL_DIARY_NUM,total_diary_num);
        diary_num = preferences.getInt(date + DIARY_NUM,diary_num);
        total_diary_num--;
        diary_num--;
        editor.remove(date + ITEM_INDEX + diary_index);
        editor.remove(date + TEXT_NUM + diary_index);
        editor.remove(date + IMAGE_NUM + diary_index);
        editor.remove(date + VIDEO_NUM + diary_index);
        editor.remove(date + SOUND_NUM + diary_index);
        editor.remove(date + ITEM_NUM + diary_index);
        editor.remove(date + DIARY_TITLE + diary_index);
        if (image_num > 0) {
            for (int i = 0; i < image_num; i++) {
                remove_file(img_filepath[i],true);
                editor.remove(date + IMG_FILEPATH + diary_index + i);
            }
        }
        if (video_num > 0) {
            for (int i = 0; i < video_num; i++) {
                remove_file(video_filepath[i],true);
                editor.remove(date + VIDEO_FILEPATH + diary_index + i);
            }
        }
        if (sound_num > 0) {
            for (int i = 0; i < sound_num; i++) {
                remove_file(sound_filepath[i],false);
                editor.remove(date + SOUND_FILEPATH + diary_index + i);
                editor.remove(date + SOUND_NAME + diary_index + i);
            }
        }
        if (text_num > 0) {
            for (int i = 0; i < text_num; i++) {
                remove_file(text_filepath[i],false);
                editor.remove(date + TEXT_FILEPATH + diary_index + i);
            }
        }
        if(total_diary_num == 0){
            editor.remove(FIRST_DATE);
        }
        else{
            String temp_date = date;
            int temp_num;
            if(temp_date == first_date){
                for(int i = 0;i<total_diary_num;i++) {
                    temp_num = preferences.getInt(temp_date + DIARY_NUM,diary_num);
                    if (temp_num == 0) {
                        myDate.plusDAY();
                        temp_date = myDate.getDate_str();
                    } else {
                        first_date = temp_date;
                        break;
                    }
                }
                editor.putString(FIRST_DATE,first_date);
            }
        }
        editor.putInt(date + DIARY_NUM,diary_num);
        editor.putInt(TOTAL_DIARY_NUM,total_diary_num);
        editor.commit();
    }

    public void save_data(){
        editor.commit();
    }

    public void remove_file(String filepath,boolean i_v){
        File delete_file = new File(filepath);
        if(delete_file.delete()) {
            Log.d(TAG, "remove_file: " + filepath);
        }
        if(i_v) {
            String[] path = {filepath};
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + "=?", path);
        }
    }
}
