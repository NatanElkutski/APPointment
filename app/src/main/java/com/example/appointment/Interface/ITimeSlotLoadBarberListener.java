package com.example.appointment.Interface;

import com.example.appointment.Model.TimeSlotBarber;

import java.util.List;

public interface ITimeSlotLoadBarberListener {
    void onTimeSlotLoadSuccess(List<TimeSlotBarber> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadempty();
}
