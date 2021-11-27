package com.example.appointment.Model;

import java.sql.Timestamp;

public class HistoryBooking {
    String barberName,salonAddress,salonName,time,barberId,hour;

    public HistoryBooking() {
    }

    public HistoryBooking(String barberName, String salonAddress, String salonName, String time, String barberId, String hour) {
        this.barberName = barberName;
        this.salonAddress = salonAddress;
        this.salonName = salonName;
        this.time = time;
        this.barberId = barberId;
        this.hour = hour;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }
    public String getBarberName() {
        return barberName;
    }

    public void setBarberName(String barberName) {
        this.barberName = barberName;
    }

    public String getSalonAddress() {
        return salonAddress;
    }

    public void setSalonAddress(String salonAddress) {
        this.salonAddress = salonAddress;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
