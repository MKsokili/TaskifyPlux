package com.example.taskmanager.TaskList;

public class TaskCategoryClass {
    int appointment, medicine, workout, Planning , Development , Testing  , others;

    public TaskCategoryClass() {
        this.appointment = 0;
        this.medicine = 0;
        this.workout = 0;
        this.Planning = 0;
        this.Development = 0;
        this.Testing = 0;
        this.others = 0;
    }

    public TaskCategoryClass(int appointment, int medicine, int workout, int planning, int development, int testing, int others) {
        this.appointment = appointment;
        this.medicine = medicine;
        this.workout = workout;
        this.others = others;
        this.Planning = planning;
        this.Development = development;
        this.Testing = testing;

    }

    public int getAppointment() {
        return appointment;
    }

    public void setAppointment(int appointment) {
        this.appointment = appointment;
    }

    public int getMedicine() {
        return medicine;
    }

    public void setMedicine(int medicine) {
        this.medicine = medicine;
    }

    public int getWorkout() {
        return workout;
    }

    public void setWorkout(int workout) {
        this.workout = workout;
    }

    public int getOthers() {
        return others;
    }

    public void setOthers(int others) {
        this.others = others;
    }

    public int getPlanning() {return Planning;}

    public void setPlanning(int planning) {
        this.Planning = planning;
    }

    public int getDevelopment() {return Development;}
    public void setDevelopment(int development) {
        this.Development = development;
    }

    public int getTesting() {return Testing;}
    public void setTesting(int testing) {
        this.Testing = testing;
    }
}
