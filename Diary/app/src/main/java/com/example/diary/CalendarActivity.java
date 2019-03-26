package com.example.diary;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String today_date;
    private String today;
    private String weather;
    private String selected_date;
    private String selected_day;
    private boolean created;

    private LinearLayout item_body;
    private MyDate myDate;
    private String getDayOfWeek(String dateTime) {
        Calendar cal = Calendar.getInstance();
        if (dateTime.equals("")) {
            cal.setTime(new Date(System.currentTimeMillis()));
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            Date date;
            try {
                date = sdf.parse(dateTime);
            }
            catch (ParseException e) {
                date = null;
                e.printStackTrace();
            }
            if (date != null) {
                cal.setTime(new Date(date.getTime()));
            }
        }
        int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
        String day_str = "";
        switch (day_of_week){
            case 1:{day_str = "Monday";}break;
            case 2:{day_str = "Tuesday";}break;
            case 3:{day_str = "Wednesday";}break;
            case 4:{day_str = "Thursday";}break;
            case 5:{day_str = "Friday";}break;
            case 6:{day_str = "Saturday";}break;
            case 7:{day_str = "Sunday";}break;
        }
        return day_str;
    }

    private void initial_view(){
        item_body = findViewById(R.id.Item_body);
        final CalendarView myCalendar = findViewById(R.id.MyCalendar);
        myCalendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                myDate = new MyDate(year,month+1,dayOfMonth);
                selected_date = myDate.getDate_str();
                selected_day = getDayOfWeek(selected_date);
                item_body.addView(new View(CalendarActivity.this));
                item_body.removeAllViews();
                add_list();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        created = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);
        Intent intent = getIntent();
        context = getApplicationContext();
        preferences = context.getSharedPreferences(SaveActivity.CACHE, Context.MODE_PRIVATE);
        editor = preferences.edit();
        today_date = intent.getStringExtra(Protocl.DATE);
        today = intent.getStringExtra(Protocl.DAY);
        weather = intent.getStringExtra(Protocl.WEATHER);
        selected_date = today_date;
        selected_day = today;
        myDate = new MyDate(today_date);
        initial_view();
        created = true;
    }

    private void add_list(){
        item_body.addView(new View(CalendarActivity.this));
        item_body.removeAllViews();
        List_Item list_item;
        int the_num = preferences.getInt(selected_date + SaveActivity.MAX_INDEX,-1);
        if(the_num != -1) {
            if (preferences.getInt(selected_date + SaveActivity.DIARY_NUM, 0) != 0) {
                for (int j = the_num; j >= 0; j--) {
                    if (preferences.getString(selected_date + SaveActivity.DIARY_TITLE + j, "").equals("")) {
                        continue;
                    } else {
                        list_item = new List_Item(CalendarActivity.this, selected_date, selected_day, j, item_body, context);
                        item_body.addView(list_item);
                    }
                }
            }
        }
    }

    public void back(View view){
        finish();
    }
//打开新日记
    public void openDiary(View view){
        int max_index = preferences.getInt(today_date + SaveActivity.MAX_INDEX,-1);
        Button mImage = findViewById(R.id.New_Diary_bt);
        ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(mImage,mImage.getWidth() / 2, mImage.getHeight() / 2, 0, 0);
        Intent i = new Intent(this,DiaryActivity.class);
        i.putExtra(Protocl.DATE,today_date);
        i.putExtra(Protocl.WEATHER,weather);
        i.putExtra(Protocl.DAY,today);
        i.putExtra(Protocl.NEW_DIARY,true);
        i.putExtra(Protocl.DIARY_INDEX,max_index+1);
        startActivity(i,compat.toBundle());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(created)
            add_list();
    }

}
