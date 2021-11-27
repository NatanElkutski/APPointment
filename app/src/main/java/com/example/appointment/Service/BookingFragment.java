package com.example.appointment.Service;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuView;
import com.example.appointment.Adapter.MyViewPagerAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.NonSwipeViePager;
import com.example.appointment.Fragments.BookingStep1Fragment;
import com.example.appointment.Fragments.BookingStep2Fragment;
import com.example.appointment.Fragments.BookingStep3Fragment;
import com.example.appointment.Fragments.BookingStep4Fragment;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.AlertReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shuhart.stepview.StepView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;


public class BookingFragment extends Fragment {


    LocalBroadcastManager localBroadcastManager;
    AlertDialog dialog;
    CollectionReference barberRef;
    StepView stepView;
    NonSwipeViePager viewPager;
    Button btn_previous_step;
    Button btn_next_step;
    Calendar alarmTimeCalendar;
    SimpleDateFormat simpleDateFormat;
    DocumentReference bookingDate;

    public BookingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemview = inflater.inflate(R.layout.fragment_booking, container, false);
        return itemview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(buttonNextReceiver, new IntentFilter(Common.KEY_ENABLE_BUTTON_NEXT));

        stepView = view.findViewById(R.id.step_view);
        viewPager = view.findViewById(R.id.view_pager);

        btn_previous_step = view.findViewById(R.id.btn_previous_step);
        btn_next_step = view.findViewById(R.id.btn_next_step);
        setupStepView();
        setColorButton();


        //View
        viewPager.setAdapter(new MyViewPagerAdapter(getChildFragmentManager()));
        viewPager.setOffscreenPageLimit(4);
        viewPager.setRotationY(180);// We have 4 fragment so we need to keep state of this 4 screen page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                stepView.go(i, true);
                btn_previous_step.setEnabled(i != 0);
                //set disable button next here
                btn_next_step.setEnabled(false);

                setColorButton();

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });


        //Event
        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.step < 3) {
                    Common.step++;
                    if (Common.step == 1) // After choose salon
                    {
                        if (Common.currentSalon != null) {
                            loadBarberBySalon(Common.currentSalon.getSalonId());
                        }
                    } else if (Common.step == 2) // Pick time slot
                    {
                        //getChildFragmentManager().beginTransaction().detach(BookingStep3Fragment.getInstance()).attach(BookingStep3Fragment.getInstance()).addToBackStack(null).commit();
                        if (Common.currentBarber != null)
                            loadTimeSlotOfBarber(Common.currentBarber.getBarberId());

                    } else if (Common.step == 3) // Confirm
                    {
                        if (Common.currentTimeSlot != -1) {
                            confirmBooking();
                        }
                    }
                    viewPager.setCurrentItem(Common.step);
                } else if (Common.step == 3) Common.step++;

                btn_next_step.setSoundEffectsEnabled(true);

                if (Common.step == 4) {
                    setBooking();
                }

            }
        });

        //Event
        btn_previous_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_next_step.setText("הבא");
                if (Common.step == 4 || Common.step > 0) {
                    Common.step--;
                    viewPager.setCurrentItem(Common.step);
                    if (Common.step < 4) // Always enable NEXT when Step < 4
                    {
                        btn_next_step.setEnabled(true);
                        setColorButton();
                    }
                }
            }
        });
    }


    private void loadBarberBySalon(String salonID) {
        dialog.show();

        //Now, select all barber of Salon
        if (!TextUtils.isEmpty(Common.city)) {
            barberRef = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.city)
                    .collection("Branch")
                    .document(salonID)
                    .collection("Barbers");

            barberRef.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            ArrayList<Barber> barbers = new ArrayList<>();
                            for (QueryDocumentSnapshot barberSnapShot : task.getResult()) {
                                Barber barber = barberSnapShot.toObject(Barber.class);
                                barber.setBarberId(barberSnapShot.getId());// Get Id of barber

                                barbers.add(barber);
                            }
                            //Send Broadcast to BookingStep2Fragment to load Recycler
                            Intent intent = new Intent(Common.KEY_BARBER_LOAD_DONE);
                            intent.putParcelableArrayListExtra(Common.KEY_BARBER_LOAD_DONE, barbers);
                            localBroadcastManager.sendBroadcast(intent);
                            dialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                        }
                    });
        }
    }

    //Broadcast Receiver
    private final BroadcastReceiver buttonNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int step = intent.getIntExtra(Common.KEY_STEP, 0);
            if (step == 0) {
                btn_next_step.setEnabled(false);
                btn_previous_step.setEnabled(false);
            } else if (step == 1) {
                Common.currentSalon = intent.getParcelableExtra(Common.KEY_SALON_STORE);
            } else if (step == 2) {
                Common.currentBarber = intent.getParcelableExtra(Common.KEY_BARBER_SELECTED);
            } else if (step == 3) {
                Common.currentTimeSlot = intent.getIntExtra(Common.KEY_TIME_SLOT, -1);
            } else if (step == 4) {
                btn_next_step.setText("אישור");
            }

            if (step > 0) {
                btn_next_step.setEnabled(true);
                btn_next_step.setBackgroundResource(R.drawable.btn_bg);
                btn_next_step.setSoundEffectsEnabled(false);
                if (step < 4) btn_next_step.performClick();
            }

            if (intent.getIntExtra("DisableNext", 0) == 1 && step == -1) {
                btn_next_step.setEnabled(false);
            }
            setColorButton();
        }
    };

    private void loadTimeSlotOfBarber(String barberId) {
        // send local broadcast to fragment 3
        Intent intent = new Intent(Common.KEY_DISPLAY_TIME_SLOT);
        localBroadcastManager.sendBroadcast(intent);

    }

    private void setColorButton() {
        if (btn_next_step.isEnabled()) {
            btn_next_step.setBackgroundResource(R.drawable.btn_bg);
        } else {
            btn_next_step.setBackgroundResource(R.drawable.disabled_booking_nextbtn);
        }
        btn_next_step.setTextColor(getContext().getColor(R.color.BtnMainColor));

        if (btn_previous_step.isEnabled()) {
            btn_previous_step.setBackgroundResource(R.drawable.btn_transparent_background);
        } else {
            btn_previous_step.setBackgroundResource(R.drawable.disabled_booking_prevbtn);
        }
    }

    private void setupStepView() {
        List<String> stepList = new ArrayList<>();
        stepList.add("עיר");
        stepList.add("ספר");
        stepList.add("זמן");
        stepList.add("אישור");
        stepView.setSteps(stepList);

    }

    private void confirmBooking() {
        //Send broadcast to fragment step four
        Intent intent = new Intent(Common.KEY_CONFIRM_BOOKING);
        localBroadcastManager.sendBroadcast(intent);

    }


    // פונקציה המוסיפה את הפגישה לDB של העובד (ספר) בטבלת הנתונים של בית העסק
    private void setBooking() {

        dialog.show();

        String startTime = Common.timeCards.get(Common.bookingTimeSlotNumber);
        String[] converTime = startTime.split(" - "); // Split ex:  9:00 - 10:00

        //Get start time for example 9:00
        String[] startTimeConvert = converTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // We get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // We get 00

        Calendar bookingDateAndTime = Calendar.getInstance();
        bookingDateAndTime.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        Date date = Common.bookingDate.getTime();
        bookingDateAndTime.setTime(date);
        bookingDateAndTime.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateAndTime.set(Calendar.MINUTE, startMinInt);
        bookingDateAndTime.set(Calendar.SECOND, 0);
        alarmTimeCalendar = Calendar.getInstance();
        alarmTimeCalendar = bookingDateAndTime;
        //Create timestamp object and apply to BookingInformation
        Timestamp timestamp = new Timestamp(bookingDateAndTime.getTime());

        //Create booking information
        BookingInformation bookingInformation = new BookingInformation();


        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setDone(false);
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhone());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonCity(Common.city);
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setCustomerEmail(Common.currentUser.getEmail());
        StringBuilder time = new StringBuilder();
        String[] newstring = Common.timeCards.get(Common.bookingTimeSlotNumber).split(" - ");
        time.append(newstring[1]).append(" - ").append(newstring[0]).toString();
        String setTimes = "ב-";
        setTimes += " " + simpleDateFormat.format(bookingDateAndTime.getTime());
        setTimes += " בשעה: ";
        setTimes += time;
        bookingInformation.setTime(setTimes);
        bookingInformation.setSlot(Long.valueOf(Common.bookingTimeSlotNumber));
        bookingInformation.setHour(Common.timeCards.get(Common.bookingTimeSlotNumber));
        bookingInformation.setDate(Common.simpleDateFormat.format(Common.bookingDate.getTime()));
        bookingInformation.setCustomerUid(Common.fuser.getUid());


        Common.previousBarberBooked = Common.currentBarber.getBarberId();


        // Submit to barber document
        // bookDate is date simpleformat with dd_MM_yyyy = 13_08_2020
        DocumentReference futureBookingExtRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.city)
                .collection("Branch")
                .document(Common.currentSalon.getSalonId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("Bookings")
                .document("FutureBookings");

        futureBookingExtRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot ds = task.getResult();
                    if (ds.exists()) {
                        futureBookingExtRef.update(Common.simpleDateFormat.format(Common.bookingDate.getTime()), "");
                    } else {
                        HashMap<String, Object> dateFieldMap = new HashMap<>();
                        dateFieldMap.put(Common.simpleDateFormat.format(Common.bookingDate.getTime()), "");
                        futureBookingExtRef.set(dateFieldMap);
                    }

                    bookingDate = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.city)
                            .collection("Branch")
                            .document(Common.currentSalon.getSalonId())
                            .collection("Barbers")
                            .document(Common.currentBarber.getBarberId())
                            .collection("Bookings")
                            .document("FutureBookings")
                            .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                            .document(Common.timeCards.get(Common.bookingTimeSlotNumber));

                    // בדיקה למניעה הדדית של קביעת תור לאותה השעה במידה ולקוח אחד או יותר מנסים לקבוע תור לאותה השעה בו זמנית
                    bookingDate.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                if(task.getResult().exists())
                                {
                                    dialog.dismiss();
                                    Common.bookingTimeSlotNumber = -1;
                                    Common.currentBarber = null;
                                    Common.bookingInformation = null;
                                    Common.step = 1;
                                    viewPager.setCurrentItem(Common.step);
                                    Toast.makeText(getActivity(), "התור שנבחר אינו זמין אנא בחר תור חדש.", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    //Write data
                                    bookingDate.set(bookingInformation)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    addToUserBooking(bookingInformation);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    // פונקציה המוסיפה את הפגישה לDB של הלקוח (משתמש)
    private void addToUserBooking(BookingInformation bookingInformation) {


        //First, create new Collection if not exists if exists refers to it.
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.fuser.getUid()).collection("Booking");

        //Check if exist document in this collection
        userBooking // If have any document with field done = false
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //Set data
                        userBooking.document()
                                .set(bookingInformation)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        if (dialog.isShowing()) dialog.dismiss();

                                        CollectionReference clientsRef = FirebaseFirestore.getInstance()
                                                .collection("AllSalon")
                                                .document(Common.city)
                                                .collection("Branch")
                                                .document(Common.currentSalon.getSalonId())
                                                .collection("Clients");

                                        clientsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Boolean userExist = false;
                                                    if (!task.getResult().isEmpty()) {
                                                        for (QueryDocumentSnapshot qds : task.getResult()) {
                                                            if (qds.getId().equals(Common.fuser.getUid())) {
                                                                userExist = true;
                                                            }
                                                        }
                                                    }

                                                    if (!userExist) {
                                                        Calendar regTime = Calendar.getInstance();
                                                        regTime.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                                                        Timestamp timestamp = new Timestamp(regTime.getTime());
                                                        HashMap<String, Object> userMap = new HashMap<>();
                                                        userMap.put("name", Common.currentUser.getName());
                                                        userMap.put("timestamp", timestamp);
                                                        userMap.put("uid", Common.fuser.getUid());
                                                        clientsRef.document(Common.fuser.getUid()).set(userMap);
                                                    }

                                                    //addToCalendar(Common.bookingDate, Common.convertTimeSlotToString(Common.bookingTimeSlotNumber));
                                                    setAlarmForAppointment(alarmTimeCalendar, Common.currentBarber.getBarberId(), Common.timeCards.get(Common.bookingTimeSlotNumber));

                                                    final AlertDialog.Builder alert_builder = new AlertDialog.Builder(getActivity()).setMessage("האם אתה רוצה להוסיף את התור ללוח שנה?");
                                                    alert_builder.setPositiveButton("כן", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            onAddEventClicked();
                                                        }
                                                    }).setNegativeButton("לא", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    });
                                                    final AlertDialog alrt = alert_builder.create();
                                                    alrt.show();
                                                    alrt.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                        @Override
                                                        public void onDismiss(DialogInterface dialog) {
                                                            resetStaticData();
                                                            Toast.makeText(getActivity(), "הזמנתך התקבלה בהצלחה תודה!", Toast.LENGTH_SHORT).show();
                                                            getActivity().onBackPressed();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

    }

    // פונקציה שמוסיפה התראה בעזרת BROADCAST RECEIVER שעה לפני הפגישה ואם התור נקבע עד שעה לפני הפגישה הBROADCAST RECEIVER ישלח התראה מידית על הפגישה
    private void setAlarmForAppointment(Calendar alarmTimeCalendar, String barberId, String bookingTime) {

        String date = Common.simpleDateFormat.format(alarmTimeCalendar.getTime());
        int hour = alarmTimeCalendar.get(Calendar.HOUR) - 1;
        alarmTimeCalendar.set(Calendar.HOUR, hour);
        AlarmManager alarmManager = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);

        final int id = (int) System.currentTimeMillis();

        Intent intent = new Intent(getActivity(), AlertReceiver.class);
        intent.putExtra("barberId", barberId);
        intent.putExtra("date", date);
        intent.putExtra("bookingTime", bookingTime);

        Log.d("ALARM TIME :", "" + bookingTime);

        PendingIntent pendingIntent = PendingIntent
                .getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_ONE_SHOT);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeCalendar.getTimeInMillis(), pendingIntent);

    }

    // פונקציה רושמת את הפגישה ללוח שנה של המכשיר
    public void onAddEventClicked() {

        String startTime = Common.timeCards.get(Common.bookingTimeSlotNumber);
        String[] converTime = startTime.split("-"); // Split ex:  9:00 - 10:00

        //Get start time : get 9:00
        String[] startTimeConvert = converTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim()); // We get 9
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim()); // We get 00

        String[] endTimeConvert = converTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim()); // We get 10
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim()); // We get 00

        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt); // set event start hour
        startEvent.set(Calendar.MINUTE, startMinInt); // set event start minutes

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt); // set event start hour
        endEvent.set(Calendar.MINUTE, endMinInt); // set event start minutes

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType("vnd.android.cursor.item/event");

        long start1 = startEvent.getTimeInMillis();
        long end1 = endEvent.getTimeInMillis();

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start1);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end1);
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false);
        intent.putExtra(CalendarContract.Events.RRULE, "FREQ=NEVER");
        intent.putExtra(CalendarContract.Events.TITLE, "קביעת פגישה");

        intent.putExtra(CalendarContract.Events.DESCRIPTION, "תור אצל " + Common.currentBarber.getName() + " ב- " + Common.currentSalon.getName());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, Common.currentSalon.getAddress() + ", " + Common.city);

        startActivity(intent);
    }

    // איפוס כל המשתנים הסטטיים
    private void resetStaticData() {
        Common.step = 0;
        Common.bookingTimeSlotNumber = -1;
        Common.currentBarber = null;
        Common.currentSalon = null;
        Common.bookingDate = Common.currentDate;
        Common.bookingInformation = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
        Common.step = 0;
        resetStaticData();
    }

    @Override
    public void onDestroy() {
        Common.step = 0;
        Common.bookingTimeSlotNumber = -1;
        Common.currentBarber = null;
        Common.currentSalon = null;
        Common.bookingDate = Common.currentDate;
        Common.bookingInformation = null;
        localBroadcastManager.unregisterReceiver(buttonNextReceiver);
        getParentFragmentManager().beginTransaction().detach(BookingFragment.this).attach(BookingFragment.this).addToBackStack(null).commit();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}