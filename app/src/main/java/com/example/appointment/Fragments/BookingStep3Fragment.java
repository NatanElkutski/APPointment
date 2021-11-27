package com.example.appointment.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Adapter.MyTimeSlotAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.SpacesItemDecoration;
import com.example.appointment.Interface.ITimeSlotLoadListener;
import com.example.appointment.Model.TimeSlot;
import com.example.appointment.Model.WorkingDays;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class BookingStep3Fragment extends Fragment implements ITimeSlotLoadListener {

    LinearLayout notAvailabeLL;
    View inf;
    Boolean customFound;
    //Variable
    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    AlertDialog dialog;
    HorizontalCalendar horizontalCalendar;
    LocalBroadcastManager localBroadcastManager;

    RecyclerView recycler_time_slot;
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;

    BroadcastReceiver displayTimeSlot = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, 0); // add current date
            if(Common.bookingDate != date) {
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        simpleDateFormat.format(Common.bookingDate.getTime()), Common.bookingDate);
            }
            else
            {
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                        simpleDateFormat.format(date.getTime()), date);
            }
        }
    };

    private void loadAvailableTimeSlotOfBarber(String barberId, final String bookDate, Calendar date) {

        recycler_time_slot.setVisibility(View.VISIBLE);
        notAvailabeLL.setVisibility(View.GONE);
        dialog.show();

        String day = "";
        if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) day = "Sunday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) day = "Monday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) day = "Tuesday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) day = "Wednesday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) day = "Thursday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) day = "Friday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) day = "Saturday";

        final String finalday = day;

        String simpleDate = Common.simpleDateFormat.format(date.getTime());

        //////////////////// Current Barber Reference ///////////////////

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());

        //////////////////// Current Barber Booking dates Reference ///////////////////

        CollectionReference dateNslot = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("Bookings")
                .document("FutureBookings")
                .collection(bookDate);

        //////////////////// Current Barber WorkingDays Reference The Default Days ///////////////////

        CollectionReference dayCol = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("WorkingDays");

        //////////////////// Current Barber WorkingDays Reference Specific Day ("Sunday","Monday" etc...) ///////////////////

        DocumentReference dayDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("WorkingDays").document(finalday);

        //////////////////// Current Barber CustomDates Reference If he changed a Specific Day Schedule by Date (Instead of the Default days!) ///////////////////

        CollectionReference cDatesRef = FirebaseFirestore.getInstance().collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("CustomDates");

        cDatesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentReference dDatesRef = FirebaseFirestore.getInstance().collection("AllSalon")
                            .document(Common.city)
                            .collection("Branch")
                            .document(Common.currentSalon.getSalonId())
                            .collection("Barbers")
                            .document(Common.currentBarber.getBarberId())
                            .collection("CustomDates").document(bookDate);

                    dDatesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot ds = task.getResult();
                                if (ds.exists()) {
                                    WorkingDays workday = ds.toObject(WorkingDays.class);
                                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                                    String startHour = workday.getStartHour();
                                    String endHour = workday.getEndHour();
                                    if (startHour.equals("-1") && endHour.equals("-1")) {
                                        Common.TIME_SLOT_TOTAL = 0;
                                        recycler_time_slot.setVisibility(View.GONE);
                                        notAvailabeLL.setVisibility(View.VISIBLE);
                                        iTimeSlotLoadListener.onTimeSlotLoadempty();
                                    } else {
                                        int minTreat = Common.currentBarber.getMinTreat();
                                        timeSlotCalculate(startHour, endHour, minTreat);
                                        // Get information of this barber
                                        barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if (documentSnapshot.exists()) // if barber available
                                                    {
                                                        // bookDate is date simpleformat with dd_MM_yyyy = 13_08_2020

                                                        dateNslot.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    QuerySnapshot querySnapshot = task.getResult();
                                                                    if (querySnapshot.isEmpty()) {// if dont have any appointment
                                                                        iTimeSlotLoadListener.onTimeSlotLoadempty();
                                                                    } else {
                                                                        // if have appointment
                                                                        List<TimeSlot> timeSlots = new ArrayList<>();
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            timeSlots.add(document.toObject(TimeSlot.class));

                                                                        }
                                                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                                                    }
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            }
                                        });
                                    }
                                } else {

                                    dayCol.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            QuerySnapshot qs = task.getResult();
                                            if (task.isSuccessful()) {
                                                if (qs.isEmpty()) {
                                                    Common.TIME_SLOT_TOTAL = 0;
                                                    iTimeSlotLoadListener.onTimeSlotLoadempty();
                                                } else {
                                                    dayDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                if (documentSnapshot.exists()) {
                                                                    WorkingDays workday = documentSnapshot.toObject(WorkingDays.class);
                                                                    SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                                                                    String startHour = workday.getStartHour();
                                                                    String endHour = workday.getEndHour();
                                                                    int minTreat = Common.currentBarber.getMinTreat();
                                                                    timeSlotCalculate(startHour, endHour, minTreat);

                                                                    barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                                if (documentSnapshot.exists()) // if barber available
                                                                                {
                                                                                    dateNslot.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                QuerySnapshot querySnapshot = task.getResult();
                                                                                                if (querySnapshot.isEmpty()) {// if dont have any appointment
                                                                                                    iTimeSlotLoadListener.onTimeSlotLoadempty();
                                                                                                } else {
                                                                                                    // if have appointment
                                                                                                    List<TimeSlot> timeSlots = new ArrayList<>();
                                                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                                        timeSlots.add(document.toObject(TimeSlot.class));

                                                                                                    }
                                                                                                    iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                                                                                        }
                                                                                    });

                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Common.TIME_SLOT_TOTAL = 0;
                                                                    recycler_time_slot.setVisibility(View.GONE);
                                                                    notAvailabeLL.setVisibility(View.VISIBLE);
                                                                    iTimeSlotLoadListener.onTimeSlotLoadempty();
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });

    }

    public void timeSlotCalculate(String startHour, String endHour, int minTreat) {
        Common.TIME_SLOT_TOTAL = 0;
        Common.timeCards = new ArrayList<>();
        Date start = null;
        Date end = null;
        Date temp;
        try {
            start = Common.simpleHourMinutes.parse(startHour);
            end = Common.simpleHourMinutes.parse(endHour);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int cnt = 0;
        Long time = start.getTime();
        do {
            temp = new Date(time);
            String sumTemp = "";
            sumTemp = "" + Common.simpleHourMinutes.format(temp.getTime());
            sumTemp += " - ";
            time += ((minTreat) * 60000);
            temp = new Date(time);
            if (temp.getTime() > end.getTime()) break;
            sumTemp += "" + Common.simpleHourMinutes.format(temp.getTime());
            Common.timeCards.add(sumTemp);
            cnt++;
        } while (end.getTime() > temp.getTime());
        Common.TIME_SLOT_TOTAL = cnt;
    }


    static BookingStep3Fragment instance;

    public static BookingStep3Fragment getInstance() {
        instance = new BookingStep3Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iTimeSlotLoadListener = this;
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(displayTimeSlot, new IntentFilter(Common.KEY_DISPLAY_TIME_SLOT));

        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // 28_03_2020 this is key

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();

    }

    @Override
    public void onDestroy() {

        localBroadcastManager.unregisterReceiver(displayTimeSlot);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_three, container, false);
        notAvailabeLL = itemView.findViewById(R.id.Booking3_txt_ll);
        inf = itemView;
        recycler_time_slot = itemView.findViewById(R.id.recycler_time_slot);
        recycler_time_slot.setRotationY(180);
        calendarView = itemView.findViewById(R.id.calendarView);
        calendarView.setRotationY(180);
        notAvailabeLL.setRotationY(180);
        init(itemView);

        return itemView;
    }

    private void init(View itemView) {

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));
        //Calendar
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Common.currentDate = startDate;
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 40);


        Calendar defaultDate = Calendar.getInstance();
//        Toast.makeText(getActivity(), ""+Common.city+" "+Common.currentSalon.getSalonId()+" "+Common.currentBarber.getBarberId(), Toast.LENGTH_SHORT).show();
        horizontalCalendar = new HorizontalCalendar.Builder(itemView, R.id.calendarView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(defaultDate)
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {

                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(),
                            simpleDateFormat.format(date.getTime()), date);

                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                    intent.putExtra("DisableNext", 1);
                    intent.putExtra(Common.KEY_STEP, -1);
                    localBroadcastManager.sendBroadcast(intent);
                }
            }

        });
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext(), timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadempty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);
        dialog.dismiss();

    }

    @Override
    public void onResume() {
        super.onResume();

    }


}