package com.example.appointment.Management;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.appointment.Adapter.BarberTimeSlotAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.SpacesItemDecoration;
import com.example.appointment.Interface.ITimeSlotLoadBarberListener;
import com.example.appointment.Model.TimeSlot;
import com.example.appointment.Model.TimeSlotBarber;
import com.example.appointment.Model.WorkingDays;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class TimeFragment extends Fragment implements ITimeSlotLoadBarberListener {

    View inf;

    //Variable
    DocumentReference barberDoc;
    ITimeSlotLoadBarberListener iTimeSlotLoadBarberListener;
    AlertDialog dialog;
    HorizontalCalendar horizontalCalendar;
    LocalBroadcastManager localBroadcastManager;
    RecyclerView recycler_time_slot;
    HorizontalCalendarView calendarView;
    SimpleDateFormat simpleDateFormat;
    TextView naTv;


    public TimeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        iTimeSlotLoadBarberListener = this;
        simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy"); // 28_03_2020 this is key
        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View itemView = inflater.inflate(R.layout.fragment_time, container, false);

        inf = itemView;
        recycler_time_slot = itemView.findViewById(R.id.recycler_time_slot_barber);
        calendarView = itemView.findViewById(R.id.calendarViewBarber);
        naTv = itemView.findViewById(R.id.timeFragment_NA_tv);
        init(itemView);
        today();
        return itemView;
    }

    public void today() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        Common.bookingDate = date;
        loadAvailableTimeSlotOfBarber(Common.currentUser.getBarberId(), simpleDateFormat.format(Common.bookingDate.getTime()), date);
        Common.fragmentPage = 2;
    }

    public void cancelDate() {
        loadAvailableTimeSlotOfBarber(Common.currentUser.getBarberId(), simpleDateFormat.format(Common.bookingDate.getTime()), Common.bookingDate);
        Common.fragmentPage = 2;
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
        endDate.add(Calendar.DATE, 30);


        Calendar defaultDate = Calendar.getInstance();
//        Toast.makeText(getActivity(), ""+Common.city+" "+Common.currentSalon.getSalonId()+" "+Common.currentBarber.getBarberId(), Toast.LENGTH_SHORT).show();
        horizontalCalendar = new HorizontalCalendar.Builder(itemView, R.id.calendarViewBarber)
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
                    loadAvailableTimeSlotOfBarber(Common.currentUser.getBarberId(),
                            simpleDateFormat.format(date.getTime()), date);
                }
            }

        });
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, final String bookDate, Calendar date) {

        dialog.show();
        //  /AllSalon/Tel Aviv/Branch/tRnp7skIRgFmm7xYH9DL/Barbers/rkuKQilZKw5oPriutwAy
        naTv.setVisibility(View.GONE);
        recycler_time_slot.setVisibility(View.VISIBLE);

        String day = "";
        if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) day = "Sunday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) day = "Monday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) day = "Tuesday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) day = "Wednesday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) day = "Thursday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) day = "Friday";
        else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) day = "Saturday";

        final String finalday = day;

        //////////////////// Current Barber Reference ///////////////////

        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId());

        //////////////////// Current Barber Booking dates Reference ///////////////////

        CollectionReference dateNslot = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("Bookings")
                .document("FutureBookings")
                .collection(bookDate);

        //////////////////// Current Barber WorkingDays Reference The Default Days ///////////////////

        CollectionReference dayCol = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("WorkingDays");

        //////////////////// Current Barber WorkingDays Reference Specific Day ("Sunday","Monday" etc...) ///////////////////

        DocumentReference dayDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("WorkingDays").document(finalday);

        //////////////////// Current Barber CustomDates Reference If he changed a Specific Day Schedule by Date (Instead of the Default days!) ///////////////////

        CollectionReference cDatesRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("CustomDates");

        cDatesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentReference dDatesRef = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.currentUser.getSalonCity())
                            .collection("Branch")
                            .document(Common.currentUser.getSalonId())
                            .collection("Barbers")
                            .document(Common.currentUser.getBarberId())
                            .collection("CustomDates").document(bookDate);

                    dDatesRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot ds = task.getResult();
                                if (ds.exists()) {
                                    WorkingDays workday = ds.toObject(WorkingDays.class);
                                    String startHour = workday.getStartHour();
                                    String endHour = workday.getEndHour();
                                    if (startHour.equals("-1") && endHour.equals("-1")) {
                                        Common.TIME_SLOT_TOTAL = 0;
                                        iTimeSlotLoadBarberListener.onTimeSlotLoadempty();
                                    }
                                } else {
                                    dayCol.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            QuerySnapshot qs = task.getResult();
                                            if (task.isSuccessful()) {
                                                if (qs.isEmpty()) {
                                                    Common.TIME_SLOT_TOTAL = 0;
                                                    iTimeSlotLoadBarberListener.onTimeSlotLoadempty();
                                                } else {
                                                    dayDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                if (documentSnapshot.exists()) {
                                                                    WorkingDays workday = documentSnapshot.toObject(WorkingDays.class);
                                                                    String startHour = workday.getStartHour();
                                                                    String endHour = workday.getEndHour();
                                                                    barberDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                                if (documentSnapshot.exists()) // if barber available
                                                                                {
                                                                                    int minTreat = Integer.parseInt(documentSnapshot.get("minTreat").toString());
                                                                                    timeSlotCalculate(startHour, endHour, minTreat);
                                                                                    dateNslot.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                QuerySnapshot querySnapshot = task.getResult();
                                                                                                if (querySnapshot.isEmpty()) {// if dont have any appointment
                                                                                                    iTimeSlotLoadBarberListener.onTimeSlotLoadempty();
                                                                                                } else {
                                                                                                    // if have appointment
                                                                                                    List<TimeSlotBarber> timeSlots = new ArrayList<>();
                                                                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                                        timeSlots.add(document.toObject(TimeSlotBarber.class));

                                                                                                    }
                                                                                                    iTimeSlotLoadBarberListener.onTimeSlotLoadSuccess(timeSlots);
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                        @Override
                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                            iTimeSlotLoadBarberListener.onTimeSlotLoadFailed(e.getMessage());
                                                                                        }
                                                                                    });

                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Common.TIME_SLOT_TOTAL = 0;
                                                                    iTimeSlotLoadBarberListener.onTimeSlotLoadempty();
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
        Common.timeCards = new ArrayList<>();
        Long time = start.getTime();
        do {
            temp = new Date(time);
            String timeCard = "";
            timeCard = "" + Common.simpleHourMinutes.format(temp.getTime());
            timeCard += " - ";
            time += ((minTreat) * 60000);
            temp = new Date(time);
            timeCard += "" + Common.simpleHourMinutes.format(temp.getTime());
            Common.timeCards.add(timeCard);
            cnt++;
            //Log.d("TIMEE :::::::",""+df.format(temp.getTime())+" this is count "+cnt);
        } while (end.getTime() > temp.getTime());
        Common.TIME_SLOT_TOTAL = cnt;
    }

    @Override
    public void onResume() {
        super.onResume();
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        Common.bookingDate = date;
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlotBarber> timeSlotBarberList) {
        BarberTimeSlotAdapter adapter = new BarberTimeSlotAdapter(getContext(), timeSlotBarberList,TimeFragment.this);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadempty() {

        if(Common.TIME_SLOT_TOTAL == 0)
        {
            naTv.setVisibility(View.VISIBLE);
            recycler_time_slot.setVisibility(View.GONE);
        }

        BarberTimeSlotAdapter adapter = new BarberTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onDestroy() {
        //getParentFragmentManager().beginTransaction().detach(TimeFragment.this).attach(TimeFragment.this).addToBackStack(null).commit();
        super.onDestroy();
    }

}