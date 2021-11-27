package com.example.appointment.Management;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.appointment.Adapter.BarberTimeSlotAdapter;
import com.example.appointment.Adapter.SpinnerBarberAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.SpacesItemDecoration;
import com.example.appointment.Interface.ITimeSlotLoadBarberListener;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.TimeSlotBarber;
import com.example.appointment.Model.WorkingDays;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;
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
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;

public class ManagerTimeFragment extends Fragment implements ITimeSlotLoadBarberListener {

    DocumentReference barberDoc;
    ITimeSlotLoadBarberListener iTimeSlotLoadBarberListener;
    AlertDialog dialog;
    HorizontalCalendar horizontalCalendar;
    RecyclerView recycler_time_slot;
    SimpleDateFormat simpleDateFormat;
    Spinner barberSpinner;
    ArrayList<Barber> barberList = new ArrayList<>();
    List<String> BarbersIdList;
    String choosenBarberId;
    SpinnerBarberAdapter mAdapter;
    LinearLayout emptyTimeManager, recyclerManagerll;
    View itemView;

    public ManagerTimeFragment() {
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
        // Inflate the layout for this fragment
        dialog.show();
        itemView = inflater.inflate(R.layout.fragment_manager_time, container, false);
        recycler_time_slot = itemView.findViewById(R.id.recycler_time_slot_Manager);
        recyclerManagerll = itemView.findViewById(R.id.recycler_manager_ll);
        emptyTimeManager = itemView.findViewById(R.id.empty_manager_time_ll);
        barberSpinner = itemView.findViewById(R.id.manager_time_spinner);
        barberSpinner.setPopupBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.barbers_spinner_dropdown_background));
        Calendar curDate = Calendar.getInstance();
        curDate.add(Calendar.DATE, 0);
        Common.bookingDate = curDate;

        init();
        loadSalonBarbers();
        return itemView;
    }

    private void loadSalonBarbers() {

        CollectionReference salonBarbersRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers");

        salonBarbersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            BarbersIdList = new ArrayList<>();
                            QuerySnapshot qs = task.getResult();
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot barberQDoc : qs) {
                                    Barber barber = barberQDoc.toObject(Barber.class);
                                    barberList.add(barber);
                                    BarbersIdList.add(barberQDoc.getId().toString());
                                }
                            }
                            barbersLoadSuccess();
                        }
                    }
                });
    }


    public void barbersLoadSuccess() {
        mAdapter = new SpinnerBarberAdapter(getContext(), barberList);
        barberSpinner.setAdapter(mAdapter);
        dialog.dismiss();
        barberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Barber clickedItem = (Barber) parent.getItemAtPosition(position);
                choosenBarberId = BarbersIdList.get(position);
                recyclerManagerll.setVisibility(View.VISIBLE);

                loadDate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                recyclerManagerll.setVisibility(View.GONE);
            }
        });
    }

    public void loadDate() {
        int pos = horizontalCalendar.positionOfDate(Common.bookingDate);
        horizontalCalendar.centerCalendarToPosition(pos);
        Log.d("BARBER ID ", choosenBarberId);
        loadAvailableTimeSlotOfBarber(simpleDateFormat.format(Common.bookingDate.getTime()), Common.bookingDate);
    }


    private void init() {

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        recycler_time_slot.setLayoutManager(gridLayoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));

        //Calendar
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 30);
        Common.bookingDate = startDate;

        horizontalCalendar = new HorizontalCalendar.Builder(itemView, R.id.calendarViewManager)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .mode(HorizontalCalendar.Mode.DAYS)
                .defaultSelectedDate(startDate).build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(simpleDateFormat.format(Common.bookingDate.getTime()),
                            date);
                }
            }
        });
    }

    private void loadAvailableTimeSlotOfBarber(final String bookDate, Calendar date) {

        dialog.show();
        //  /AllSalon/Tel Aviv/Branch/tRnp7skIRgFmm7xYH9DL/Barbers/rkuKQilZKw5oPriutwAy

        recyclerManagerll.setVisibility(View.VISIBLE);
        emptyTimeManager.setVisibility(View.GONE);

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
                .document(choosenBarberId);

        //////////////////// Current Barber Booking dates Reference ///////////////////

        CollectionReference dateNslot = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(choosenBarberId)
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
                .document(choosenBarberId)
                .collection("WorkingDays");

        //////////////////// Current Barber WorkingDays Reference Specific Day ("Sunday","Monday" etc...) ///////////////////

        DocumentReference dayDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(choosenBarberId)
                .collection("WorkingDays").document(finalday);

        //////////////////// Current Barber CustomDates Reference If he changed a Specific Day Schedule by Date (Instead of the Default days!) ///////////////////

        CollectionReference cDatesRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(choosenBarberId)
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
                            .document(choosenBarberId)
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
                                                                    emptyTimeManager.setVisibility(View.VISIBLE);
                                                                    recyclerManagerll.setVisibility(View.GONE);
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
            String timeCard = "";
            temp = new Date(time);
            timeCard = Common.simpleHourMinutes.format(temp.getTime());
            timeCard += " - ";
            time += ((minTreat) * 60000);
            temp = new Date(time);
            timeCard += "" + Common.simpleHourMinutes.format(temp.getTime());
            Common.timeCards.add(timeCard);
            cnt++;
            //Log.d("TIMEE :::::::",""+df.format(temp.getTime())+" this is count "+cnt);
        } while (end.getTime() > temp.getTime());
        Common.TIME_SLOT_TOTAL = cnt;
        if (Common.TIME_SLOT_TOTAL == 0) {
            emptyTimeManager.setVisibility(View.VISIBLE);
            recyclerManagerll.setVisibility(View.GONE);
        } else {
            emptyTimeManager.setVisibility(View.GONE);
            recyclerManagerll.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlotBarber> timeSlotBarberList) {
        BarberTimeSlotAdapter adapter = new BarberTimeSlotAdapter(getContext(), timeSlotBarberList, ManagerTimeFragment.this);
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadempty() {

        if (Common.TIME_SLOT_TOTAL == 0) {
            recyclerManagerll.setVisibility(View.GONE);
            emptyTimeManager.setVisibility(View.VISIBLE);
        }
        BarberTimeSlotAdapter adapter = new BarberTimeSlotAdapter(getContext());
        recycler_time_slot.setAdapter(adapter);

        dialog.dismiss();
    }

    public void cancelDate() {
        loadAvailableTimeSlotOfBarber(simpleDateFormat.format(Common.bookingDate.getTime()), Common.bookingDate);
        Common.fragmentPage = 2;
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
    }

}