package com.example.taskmanager.TaskList;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TaskClass implements Parcelable {
    String id, taskTitle, doctorId, patientName, patientEmail, patientId;
    String description, category, status;

    boolean setAlarm;

    int year, month, day, hour, minute;


    public TaskClass() {
        // Default constructor required for Firestore
    }

    public TaskClass(String taskTitle, String doctorId, String patientName, String patientEmail,
                     String patientId, String description, String category, String status,
                     int year, int month, int day, int hour, int minute, boolean setAlarm) {

        this.taskTitle = taskTitle;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientId = patientId;
        this.description = description;
        this.category = category;
        this.status = status;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.setAlarm = setAlarm;
    }

    public boolean isSetAlarm() {
        return setAlarm;
    }

    public void setSetAlarm(boolean setAlarm) {
        this.setAlarm = setAlarm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

    }
}