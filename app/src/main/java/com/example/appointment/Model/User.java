package com.example.appointment.Model;

import com.google.firebase.Timestamp;

public class User {
    private String email,name,phone,salonId,barberId,salonCity,salonName,permission,birthdate;
    private int regStep;
    private Timestamp timestamp;


    public User() {
    }

    public User(String email, String name, String phone, String permission,String birthdate,int regStep,Timestamp timestamp)
    {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.permission = permission;
        this.birthdate = birthdate;
        this.regStep = regStep;
        this.timestamp = timestamp;
    }

    public User(String email, String name, String phone, String salonId, String barberId,String salonCity, String salonName ,String permission,String birthdate,int regStep,Timestamp timestamp) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.salonId = salonId;
        this.barberId = barberId;
        this.salonCity = salonCity;
        this.salonName = salonName;
        this.permission = permission;
        this.birthdate = birthdate;
        this.regStep = regStep;
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getRegStep() {
        return regStep;
    }

    public void setRegStep(int regStep) {
        this.regStep = regStep;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }

    public String getSalonCity() {
        return salonCity;
    }

    public void setSalonCity(String salonCity) {
        this.salonCity = salonCity;
    }


}
