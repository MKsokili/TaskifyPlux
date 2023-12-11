package com.example.taskmanager.Calendar;

import java.util.ArrayList;

public class DateClass {
    int year;
    int month;
    ArrayList<String> daysInMonth;

    public DateClass(int year, int month, ArrayList<String> daysInMonth) {
        this.year = year;
        this.month = month;
        this.daysInMonth = daysInMonth;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public ArrayList<String> getDaysInMonth() {
        return daysInMonth;
    }

    public void setDaysInMonth(ArrayList<String> daysInMonth) {
        this.daysInMonth = daysInMonth;
    }
}
