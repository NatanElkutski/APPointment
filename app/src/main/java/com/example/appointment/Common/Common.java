package com.example.appointment.Common;


import android.view.View;

import com.example.appointment.Model.Barber;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.Salon;
import com.example.appointment.Model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Common {

    public static View header;
    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_SALON_STORE = "SALON_SAVE";
    public static final String KEY_BARBER_LOAD_DONE = "BARBER_LOAD_DONE";
    public static final String KEY_DISPLAY_TIME_SLOT = "DISPLAY_TIME_SLOT";
    public static final String KEY_STEP = "STEP";
    public static final String KEY_BARBER_SELECTED = "BARBER_SELECTED";
    public static int TIME_SLOT_TOTAL;
    public static final Object DISABLE_TAG = "DISABLE";
    public static final String KEY_TIME_SLOT = "TIME_SLOT";
    public static final String KEY_CONFIRM_BOOKING = "CONFIRM_BOOKING";
    public static String IS_LOGIN = "IsLogin";
    public static User currentUser;
    public static Salon currentSalon;
    public static BookingInformation bookingInformation;
    public static String bookingTimeSlotDate;
    public static int bookingTimeSlotNumber = -1;
    public static String previousBarberBooked;
    public static List <String> timeCards;
    public static int regStep = 0;
    public static List<Date> vacationList;
    public static int seenMessages = 0;
    public static String profilePicture = "";
    public static int step = 0; // Init first step is 0
    public static String city = "";
    public static Barber currentBarber;
    public static int currentTimeSlot = -1;
    public static Calendar bookingDate = Calendar.getInstance();
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // Only use when need format key
    public static SimpleDateFormat simpleBirthFormat = new SimpleDateFormat("dd/MM/yyyy"); // Only use when need format key
    public static SimpleDateFormat simpleHourMinutes = new SimpleDateFormat("HH:mm");
    public static Calendar currentDate;
    public static String HistoryHour = "";
    public static String HistoryBarberId = "";
    public static int fragmentPage = 0;
    public static String ActiveBarberId;
    public static String ActiveHour;
    public static String ActiveSalonCity;
    public static String ActiveSalonid;
    public static String ActiveDate;
    public static FirebaseUser fuser;

    public static Timestamp makeTimeStampMessage(String sentDateWithTime) throws ParseException {
        String[] sentDateAndTime = sentDateWithTime.split(" ");
        String[] TimeConvert = sentDateAndTime[1].split(":");
        int sentHourInt = Integer.parseInt(TimeConvert[0].trim());
        int sentMinInt = Integer.parseInt(TimeConvert[1].trim());
        int sentSeconds = Integer.parseInt(TimeConvert[2].trim());

        Calendar sentDate = Calendar.getInstance();
        sentDate.setTime(Common.simpleDateFormat.parse(sentDateAndTime[0]));

        Calendar DateWithTime = Calendar.getInstance();
        DateWithTime.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        DateWithTime.setTimeInMillis(sentDate.getTimeInMillis());
        DateWithTime.set(Calendar.HOUR_OF_DAY, sentHourInt);
        DateWithTime.set(Calendar.MINUTE, sentMinInt);
        DateWithTime.set(Calendar.SECOND, sentSeconds);

        Timestamp timestamp = new Timestamp(DateWithTime.getTime());

        return timestamp;
    }

    public static String currentTimeWithDate()
    {
        DateFormat timeFormat = new SimpleDateFormat("dd_MM_yyyy HH:mm:ss");
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curTime = timeFormat.format(new Date());
        return curTime;
    }



}
