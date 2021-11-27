package com.example.appointment.Model;

public class FutureBooking {

    String barberName,salonAddress,salonName,time,barberId,hour,salonId,date,salonCity;

    public FutureBooking() {
    }

    public FutureBooking(String salonCity, String barberName, String salonAddress, String salonName, String time, String barberId, String hour, String salonId, String date) {
        this.barberName = barberName;
        this.salonAddress = salonAddress;
        this.salonName = salonName;
        this.time = time;
        this.barberId = barberId;
        this.hour = hour;
        this.salonId = salonId;
        this.date = date;
        this.salonCity = salonCity;
    }

    public String getSalonCity() {
        return salonCity;
    }

    public void setSalonCity(String salonCity) {
        this.salonCity = salonCity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
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

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }
}
