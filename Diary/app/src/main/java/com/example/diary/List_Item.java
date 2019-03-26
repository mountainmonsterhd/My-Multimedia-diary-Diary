package com.example.diary;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class List_Item extends RelativeLayout {

    private Context context;
    private Button Diary_Button;
    private ImageView List_Item_BG;
    private TextView Title_View;
    private TextView Date_View;
    private TextView Day_View;
    private String Title;
    private String Day;
    private String Weather;

    private LinearLayout linearLayout;

    private MyDate Date;
    private SaveActivity saveActivity;
    private DialogInterface.OnClickListener confirm;
    private DialogInterface.OnClickListener cancel;
    private DialogInterface.OnClickListener delete;

    private void initial_dialog(){
        confirm=new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0,int arg1)
            {
                saveActivity.delete_diary(Date.getDate_str(),saveActivity.diary_index);
                linearLayout.removeView(List_Item.this);
            }
        };

        delete = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context);
                alert_dialog_builder.setMessage("Are you sure?");
                alert_dialog_builder.setPositiveButton("confirm", confirm);
                alert_dialog_builder.setNegativeButton("cancel", cancel);
                AlertDialog alert_dialog = alert_dialog_builder.create();
                alert_dialog.show();
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

    private void initial_content_View(){
        Diary_Button = findViewById(R.id.Open_Diary_Bt);
        List_Item_BG = findViewById(R.id.Item_BG);
        Title_View = findViewById(R.id.Item_Title);
        Date_View = findViewById(R.id.Item_Date);
        Day_View = findViewById(R.id.Item_Day);

        initial_button_listener();
        setData();
    }

    private void setData(){
        switch (Weather){
            case "Sunny" :{ List_Item_BG.setImageResource(R.drawable.sunny); }break;
            case "Rainy" :{ List_Item_BG.setImageResource(R.drawable.rain); }break;
            case "Cloudy" :{ List_Item_BG.setImageResource(R.drawable.cloudy);}break;
            case "Overcast" :{ List_Item_BG.setImageResource(R.drawable.overcast); }break;
        }
        Title_View.setText(Title);
        Date_View.setText(change_date_format(Date.getDate_str()));
        Day_View.setText(Day);
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

    public List_Item(Context context, String date, String day, int diary_index, LinearLayout layout, Context app_context){
        super(context);
        LayoutInflater.from(context).inflate(R.layout.list_item,this,true);
        this.context = context;
        saveActivity = new SaveActivity(false,date,diary_index,app_context);
        Date = new MyDate(date);
        Day = day;
        linearLayout = layout;
        Title = saveActivity.Title;
        Weather = saveActivity.weather;
        initial_content_View();
    }
    public List_Item(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public List_Item(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initial_button_listener(){
        Diary_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptions compat = ActivityOptions.makeScaleUpAnimation(Diary_Button,Diary_Button.getWidth() / 2, Diary_Button.getHeight() / 2, 0, 0);
                Intent i = new Intent(context,DiaryActivity.class);
                i.putExtra(Protocl.DATE,Date.getDate_str());
                i.putExtra(Protocl.WEATHER,Weather);
                i.putExtra(Protocl.DAY,Day);
                i.putExtra(Protocl.NEW_DIARY,false);
                i.putExtra(Protocl.DIARY_INDEX,saveActivity.diary_index);
                context.startActivity(i,compat.toBundle());
            }
        });
        Diary_Button.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder alert_dialog_builder = new AlertDialog.Builder(context);
                alert_dialog_builder.setMessage("Delete this diary?");
                alert_dialog_builder.setPositiveButton("delete", delete);
                alert_dialog_builder.setNegativeButton("cancel", cancel);
                AlertDialog alert_dialog = alert_dialog_builder.create();
                alert_dialog.show();
                return true;
            }
        });
        initial_dialog();
    }

}
