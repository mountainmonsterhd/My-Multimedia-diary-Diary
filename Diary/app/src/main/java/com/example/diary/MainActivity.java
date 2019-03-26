package com.example.diary;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private ViewSwitcher viewSwitcher;
    private boolean   changed;
    public static String date;
    public static String day;
    public static String first_date;
    public static String weather;
    public static long last_time;
    private int total_diary_num;
    private int max_index;

    public static LinearLayout VL;
    private ScrollView scrollView;



    private long mExitTime;//第一次按返回的时间

    private class TouchListenerImpl implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    break;
                case MotionEvent.ACTION_UP:
                    int scrollY=view.getScrollY();
                    int height=view.getHeight();
                    int scrollViewMeasuredHeight=scrollView.getChildAt(0).getMeasuredHeight();
                    if(scrollY==0){
                        //Toast.makeText(MainActivity.this, "Top", Toast.LENGTH_SHORT).show();
                    }
                    if((scrollY+height)==scrollViewMeasuredHeight){
                        //Toast.makeText(MainActivity.this, "button", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
            return false;
        }

    }

    //Dynamic addition of diaries and timeline
    private void addTimeLine(){
        String start_date = date;
        String end_date = first_date;
        MyDate temp_date;
        int the_num;
        Time_Line time_line;
        for(int i = 0;i<total_diary_num;i++) {
            the_num = preferences.getInt(start_date + SaveActivity.MAX_INDEX,-1);
            if(preferences.getInt(start_date + SaveActivity.DIARY_NUM,0) != 0) {
                for(int j = the_num;j>=0;j--) {
                    if(preferences.getString(start_date + SaveActivity.DIARY_TITLE + j,"").equals("")){
                        continue;
                    }
                    else {
                        time_line = new Time_Line(MainActivity.this, start_date, day, j, VL, context);
                        VL.addView(time_line);
                    }
                }
            }
            if(start_date != end_date){
                temp_date = new MyDate(start_date);
                temp_date.minusDAY();
                start_date = temp_date.getDate_str();
            }
            else {
                break;
            }
        }
    }

    private void initial_scroll(){
        scrollView.setOnTouchListener(new TouchListenerImpl());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VL = findViewById(R.id.Itemlayout);
        viewSwitcher = findViewById(R.id.Main_Menu_Layout);
        scrollView = findViewById(R.id.my_scroll);
        findViewById(R.id.Menu_include).setVisibility(View.INVISIBLE);
        findViewById(R.id.Arrow_bt).setBackgroundResource(R.drawable.arrow_selector);
        changed = false;
        context = getApplicationContext();
        preferences = context.getSharedPreferences(SaveActivity.CACHE, Context.MODE_PRIVATE);
        editor = preferences.edit();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        initial_Weather();
        initData();
        initial_scroll();
        if(last_time-System.currentTimeMillis()<60000)
            new DownloadUpdate().execute();
    }
    //初始化数据
    private void initData() {
        VL.removeAllViews();
        last_time = preferences.getLong("last_time",System.currentTimeMillis());
        first_date = preferences.getString(SaveActivity.FIRST_DATE,date);
        total_diary_num = preferences.getInt(SaveActivity.TOTAL_DIARY_NUM,0);
        max_index = preferences.getInt(date + SaveActivity.MAX_INDEX,-1);
        addTimeLine();
    }
//初始化天气
    private void initial_Weather(){
        String GetCity = preferences.getString("city", "Chongqing");
        String GetDate = preferences.getString("date", "2019/1/1");
        String GetDay = preferences.getString("day","Tuesday");
        String GetWeather = preferences.getString("weather","Rainy");
        String GetTemperature = preferences.getString("temperature","10");
        String GetNotice = preferences.getString("notice","雨虽小，注意保暖别感冒");

        ((TextView)findViewById(R.id.Location)).setText(GetCity);
        ((TextView)findViewById(R.id.Date)).setText(GetDate);
        ((TextView)findViewById(R.id.Today)).setText(GetDay);
        ((TextView)findViewById(R.id.Weathertext)).setText(GetWeather);
        ((TextView)findViewById(R.id.Temperature)).setText(GetTemperature);
        //((TextView)findViewById(R.id.Notice)).setText(GetNotice);

        switch (GetWeather){
            case "Sunny" :{ImageView m_image = findViewById(R.id.Weather);
                m_image.setImageResource(R.drawable.sunny); }break;
            case "Rainy" :{ImageView m_image = findViewById(R.id.Weather);
                m_image.setImageResource(R.drawable.rain); }break;
            case "Cloudy" :{ImageView m_image = findViewById(R.id.Weather);
                m_image.setImageResource(R.drawable.cloudy);}break;
            case "Overcast" :{ImageView m_image = findViewById(R.id.Weather);
                m_image.setImageResource(R.drawable.overcast); }break;
        }
        MainActivity.weather  = GetWeather;
        MainActivity.date = GetDate;
        MainActivity.day = GetDay;
    }
    //Today's weather forecast
    private class DownloadUpdate extends AsyncTask<String, Void, String> {

        public String getDateStr(String str) {
            String Date = "";
            for(int i=0;i<str.length();i++) {
                int chr=str.charAt(i);
                if(chr>=48 && chr<=57)
                    Date += str.charAt(i);
                else if(str.charAt(i) == ' ')
                    break;
                else
                    Date += "/";
            }
            return Date;
        }
        @Override
        protected String doInBackground(String... strings) {
            String stringUrl = "http://t.weather.sojson.com/api/weather/city/101040900";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;

            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mainly needed for debugging
                    Log.d("TAG", line);
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //The temperature
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String My_weather) {
            //Update the temperature displayed
            try{
                if(My_weather!=null)
                {
                    last_time = System.currentTimeMillis();
                    JSONObject obj = new JSONObject(My_weather);
                    int result = obj.getInt("status");
                    if (result == 200) {
                        JSONObject cityInfo = obj.getJSONObject("cityInfo");
                        JSONObject data = obj.getJSONObject("data");
                        JSONArray forecastArray = data.getJSONArray("forecast");

                        String date = obj.getString("time");
                        String city;// = cityInfo.getString("city");
                        String temperature = data.getString("wendu");
                        String day;
                        String type;
                        String notice;
                        day = forecastArray.getJSONObject(0).getString("week");
                        type = forecastArray.getJSONObject(0).getString("type");
                        notice = forecastArray.getJSONObject(0).getString("notice");
                        date = new MyDate(getDateStr(date)).getDate_str();
                        city = "Chongqing";
                        //((TextView)findViewById(R.id.Notice)).setText(notice);
                        ((TextView) findViewById(R.id.Date)).setText(change_date_format(date));
                        ((TextView) findViewById(R.id.Location)).setText(city);
                        ((TextView) findViewById(R.id.Temperature)).setText(temperature);
                        switch(day){
                            case "星期一": day = "Monday";break;
                            case "星期二": day = "Tuesday";break;
                            case "星期三": day = "Wednesday";break;
                            case "星期四": day = "Thursday";break;
                            case "星期五": day = "Friday";break;
                            case "星期六": day = "Saturday";break;
                            case "星期日": day = "Sunday";break;
                        }
                        ((TextView) findViewById(R.id.Today)).setText(day);
                        String wetherstr = "Sunny";
                        switch (type){
                            case "晴" :{ImageView m_image = findViewById(R.id.Weather);
                                m_image.setImageResource(R.drawable.sunny);wetherstr="Sunny"; }break;
                            case "小雨" :{ImageView m_image = findViewById(R.id.Weather);
                                m_image.setImageResource(R.drawable.rain); wetherstr="Rainy";}break;
                            case "多云" :{ImageView m_image = findViewById(R.id.Weather);
                                m_image.setImageResource(R.drawable.cloudy); wetherstr="Cloudy";}break;
                            case "阴" :{ImageView m_image = findViewById(R.id.Weather);
                                m_image.setImageResource(R.drawable.overcast);wetherstr="Overcast"; }break;
                        }
                        ((TextView) findViewById(R.id.Weathertext)).setText(wetherstr);
                        MainActivity.weather  = wetherstr;
                        MainActivity.date = date;
                        MainActivity.day = day;
                        editor.putString("date",date);
                        editor.putString("day",day);
                        editor.putString("city",city);
                        editor.putString("weather",wetherstr);
                        editor.putString("notice",notice);
                        editor.putString("temperature",temperature);
                        editor.putLong("last_time",last_time);
                        editor.commit();
                    }

                }
                else{
                    Toast toastCenter = Toast.makeText(MainActivity.this, "No weather upgrade！", Toast.LENGTH_SHORT);
                    toastCenter.setGravity(Gravity.CENTER, 0, 0);
                    toastCenter.show();
                }
            }catch(JSONException e){
                Toast toastCenter = Toast.makeText(MainActivity.this, "Update Failed！", Toast.LENGTH_SHORT);
                toastCenter.setGravity(Gravity.CENTER, 0, 0);
                toastCenter.show();
                e.printStackTrace();
            }
        }
    }
//打开日历
    public void openCalendar(View view){
        total_diary_num = preferences.getInt(SaveActivity.TOTAL_DIARY_NUM,0);
        max_index = preferences.getInt(date + SaveActivity.MAX_INDEX,-1);
        Button mImage = findViewById(R.id.Calendar_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        Intent i = new Intent(this,CalendarActivity.class);
        i.putExtra(Protocl.DATE,date);
        i.putExtra(Protocl.WEATHER,weather);
        i.putExtra(Protocl.DAY,day);
        startActivity(i,compat.toBundle());
    }
//新日记
    public void openDiary(View view){
        Button mImage = findViewById(R.id.New_Diary_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        total_diary_num = preferences.getInt(SaveActivity.TOTAL_DIARY_NUM,0);
        max_index = preferences.getInt(date + SaveActivity.MAX_INDEX,-1);
        Intent i = new Intent(this,DiaryActivity.class);
        i.putExtra(Protocl.DATE,date);
        i.putExtra(Protocl.WEATHER,weather);
        i.putExtra(Protocl.DAY,day);
        i.putExtra(Protocl.NEW_DIARY,true);
        i.putExtra(Protocl.DIARY_INDEX,max_index+1);
        startActivity(i,compat.toBundle());
    }
//打开菜单
    public void openMenu(View view){
        toggleScene();
    }

//切换菜单
    private void toggleScene() {
        if(changed) {
            viewSwitcher.setDisplayedChild(1);
            findViewById(R.id.Arrow_bt).setBackgroundResource(R.drawable.arrow_selector);
        }

        else {
            viewSwitcher.setDisplayedChild(0);
            findViewById(R.id.Menu_include).setVisibility(View.VISIBLE);
            findViewById(R.id.Arrow_bt).setBackgroundResource(R.drawable.arrow_back_selector);
        }
        changed = !changed;
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        initData();
    }
}
