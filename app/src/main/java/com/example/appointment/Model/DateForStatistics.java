package com.example.appointment.Model;

public class DateForStatistics {

    float date;
    int values;

    public DateForStatistics() {
    }

    public DateForStatistics(float date, int values)
    {
        this.date = date;
        this.values = values;
    }

    public float getDate() {
        return date;
    }

    public void setDate(float date) {
        this.date = date;
    }

    public int getValues() {
        return values;
    }

    public void setValues(int values) {
        this.values = values;
    }
}
