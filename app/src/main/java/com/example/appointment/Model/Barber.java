package com.example.appointment.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Barber implements Parcelable {
    private String name, barberId,email,uid; // Watch 30:00
    private int minTreat;

    public Barber()
    {

    }

    public Barber(String name, int minTreat, String email, String uid)
    {
        this.name = name;
        this.minTreat = minTreat;
        this.email = email;
        this.uid = uid;
    }

    public Barber(String name) {
        this.name = name;
    }


    protected Barber(Parcel in) {
        name = in.readString();
        barberId = in.readString();
        minTreat = in.readInt();
        uid = in.readString();
    }

    public static final Creator<Barber> CREATOR = new Creator<Barber>() {
        @Override
        public Barber createFromParcel(Parcel in) {
            return new Barber(in);
        }

        @Override
        public Barber[] newArray(int size) {
            return new Barber[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMinTreat() {
        return minTreat;
    }

    public void setMinTreat(int minTreat) {
        this.minTreat = minTreat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarberId() {
        return barberId;
    }

    public void setBarberId(String barberId) {
        this.barberId = barberId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(barberId);
        dest.writeInt(minTreat);
    }
}
