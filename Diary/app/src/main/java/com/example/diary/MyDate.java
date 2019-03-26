package com.example.diary;

public class MyDate {

    private int year;
    private int month;
    private int day;
    private boolean leap_year;

    public MyDate(String date){
        String[] temp_Date = date.split("/");
        try {
            year = Integer.parseInt(temp_Date[0]);
            month = Integer.parseInt(temp_Date[1]);
            day = Integer.parseInt(temp_Date[2]);
        }catch (Exception e){
            e.printStackTrace();
        }
        leap_year = is_leap_year(year);
    }

    public MyDate(int year,int month,int day){
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getDate_str() {
        String date_str;
        date_str = year + "/" + month + "/" + day;
        return date_str;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void plusDAY(){
        day++;
        if(day>get_month_day(month)){
            day = 1;
            month++;
            if (month>12){
                month = 1;
                year++;
                leap_year = is_leap_year(year);
            }
        }
    }

    public void minusDAY(){
        day--;
        if(day<1){
            month--;
            day = get_month_day(month);
            if (month<1){
                month = 12;
                year--;
                leap_year = is_leap_year(year);
            }
        }
    }

    public int get_month_day(int month){
        int days = 30;
        switch (month){
            case 1:{days = 31;}break;
            case 2:{
                if(leap_year){
                    days = 29;
                }
                else {
                    days = 28;
                }
            }break;
            case 3:{days = 31;}break;
            case 4:{days = 30;}break;
            case 5:{days = 31;}break;
            case 6:{days = 30;}break;
            case 7:{days = 31;}break;
            case 8:{days = 31;}break;
            case 9:{days = 30;}break;
            case 10:{days = 31;}break;
            case 11:{days = 30;}break;
            case 12:{days = 31;}break;
        }
        return days;
    }

    private boolean is_leap_year(int year){
        if((year%4 == 0&&year%100 != 0)||year%400 == 0) {
            return true;
        }
        else{
            return false;
        }
    }
}
