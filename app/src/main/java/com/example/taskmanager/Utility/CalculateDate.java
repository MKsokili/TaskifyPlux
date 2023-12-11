package com.example.taskmanager.Utility;

import android.graphics.Color;
import android.util.Log;

import com.example.taskmanager.CustomerClass.DateString;
import com.example.taskmanager.CustomerClass.TimeString;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class CalculateDate {
    public static String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        return date.format(formatter);
    }

    /*public static String monthFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
        return date.format(formatter);
    }*/

    public static int dayFromDate(LocalDate date) {
        return(date.getDayOfMonth());
    }

    public static int monthFromDate(LocalDate date) {
        return(date.getMonthValue());
    }

/*    public static String YearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy");
        return date.format(formatter);
    }*/

    public static int yearFromDate(LocalDate date) {
        return(date.getYear());
    }

    public static DateString getDateFromString(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M-dd-yyyy");
        try {
            Date date = dateFormat.parse(dateString);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; // Months are 0-based
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DateString dateInteger = new DateString(year, month, day);

            return dateInteger;

        } catch (ParseException e) {
            // Handle the case where the date string is not in the expected format
            Log.d("Date calculation", "Invalid date format");
        }
        return null;
    }

    public static TimeString getTimeFromString(String timeString) {
        String[] time = timeString.split ( ":" );
        int hour = Integer.parseInt ( time[0].trim() );
        int min = Integer.parseInt ( time[1].trim() );

        TimeString timeInteger = new TimeString(hour, min);
        return timeInteger;
    }


}
