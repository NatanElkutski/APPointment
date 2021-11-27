package com.example.appointment.Management;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.WorkingDays;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.sendNotification;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class SelectHoursFragment extends Fragment {

    int idArray;
    CheckBox[] c = new CheckBox[7];

    TextView[][] dayOfWeek = new TextView[7][2];

    int cnt1, cnt2;

    Button confirmbtn, removeDaybtn;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    AlertDialog dialog;

    public SelectHoursFragment() {
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
        return inflater.inflate(R.layout.fragment_select_hours, container, false);
    }

    static SelectHoursFragment instance;

    public static SelectHoursFragment getInstance() {
        if (instance == null)
            instance = new SelectHoursFragment();

        return instance;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
        confirmbtn = (Button) view.findViewById(R.id.confirm_button);
        removeDaybtn = (Button) view.findViewById(R.id.close_selected_days);

        for (int i = 0; i < 7; i++) {
            idArray = getResources().getIdentifier("check" + (i + 1), "id", getActivity().getPackageName());

            c[i] = (CheckBox) view.findViewById(idArray);
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 2; j++) {
                idArray = getResources().getIdentifier("day" + (i + 1) + "" + (j + 1), "id", getActivity().getPackageName());
                dayOfWeek[i][j] = view.findViewById(idArray);
            }

        }

        dialog.show();
        showBarberWorkingHours();

        Context context = getContext();
        Calendar calendar = Calendar.getInstance();

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cnt1 = 0;
                cnt2 = 0;
                if (c[0].isChecked() || c[1].isChecked() || c[2].isChecked() || c[3].isChecked() || c[4].isChecked() || c[5].isChecked() || c[6].isChecked()) {
                    if (c[0].isChecked()) {
                        if (dayOfWeek[0][0].getText().toString().indexOf(':') != -1 && dayOfWeek[0][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[0][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[0][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Sunday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[1].isChecked()) {
                        if (dayOfWeek[1][0].getText().toString().indexOf(':') != -1 && dayOfWeek[1][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[1][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[1][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Monday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[2].isChecked()) {
                        if (dayOfWeek[2][0].getText().toString().indexOf(':') != -1 && dayOfWeek[2][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[2][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[2][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Tuesday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[3].isChecked()) {
                        if (dayOfWeek[3][0].getText().toString().indexOf(':') != -1 && dayOfWeek[3][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[3][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[3][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Wednesday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[4].isChecked()) {
                        if (dayOfWeek[4][0].getText().toString().indexOf(':') != -1 && dayOfWeek[4][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[4][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[4][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Thursday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[5].isChecked()) {
                        if (dayOfWeek[5][0].getText().toString().indexOf(':') != -1 && dayOfWeek[5][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[5][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[5][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Friday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }
                    if (c[6].isChecked()) {
                        if (dayOfWeek[6][0].getText().toString().indexOf(':') != -1 && dayOfWeek[6][1].getText().toString().indexOf(':') != -1) {
                            String[] s1 = dayOfWeek[6][0].getText().toString().split(":");
                            String[] s2 = dayOfWeek[6][1].getText().toString().split(":");
                            insertHoursToDB(s1, s2, "Saturday");
                        } else
                            Toast.makeText(getActivity(), "Cannot be empty!", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), "Selected Days Set Successfully", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getActivity(), "Days not Chosen!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dayOfWeek[0][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[0][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[0][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[0][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[0][0].setText(hourOfDay + ":" + minute);

                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[0][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[0][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[0][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[0][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[0][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[1][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[1][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[1][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[1][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[1][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[1][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[1][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[1][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[1][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[1][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[2][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[2][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[2][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[2][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[2][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[2][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[2][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[2][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[2][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[2][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[3][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[3][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[3][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[3][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[3][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[3][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[3][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[3][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[3][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[3][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[4][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[4][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[4][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[4][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[4][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[4][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[4][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[4][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[4][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[4][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[5][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[5][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[5][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[5][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[5][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[5][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[5][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[5][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[5][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[5][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[6][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[6][0].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[6][0].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[6][0].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[6][0].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });
        dayOfWeek[6][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            dayOfWeek[6][1].setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10) dayOfWeek[6][1].setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            dayOfWeek[6][1].setText("0" + hourOfDay + ":" + minute);
                        else dayOfWeek[6][1].setText(hourOfDay + ":" + minute);
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });

        removeDaybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (c[0].isChecked() || c[1].isChecked() || c[2].isChecked() || c[3].isChecked() || c[4].isChecked() || c[5].isChecked() || c[6].isChecked()) {

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle("סגירת ימי עבודה").setMessage("אתה בטוח שאתה רוצה לסגור את הימים שנבחרו?");
                    dialog.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Hide after some seconds
                            if (c[0].isChecked()) {
                                removeDayFromDB("Sunday");
                            }
                            if (c[1].isChecked()) {
                                removeDayFromDB("Monday");
                            }
                            if (c[2].isChecked()) {
                                removeDayFromDB("Tuesday");
                            }
                            if (c[3].isChecked()) {
                                removeDayFromDB("Wednesday");
                            }
                            if (c[4].isChecked()) {
                                removeDayFromDB("Thursday");
                            }
                            if (c[5].isChecked()) {
                                removeDayFromDB("Friday");
                            }
                            if (c[6].isChecked()) {
                                removeDayFromDB("Saturday");
                            }

                        }
                    }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    final AlertDialog alert = dialog.create();
                    alert.show();

                } else Toast.makeText(getActivity(), "לא נבחרו ימים!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertHoursToDB(String[] s1, String[] s2, String dayName) {
        int temp1 = Integer.parseInt(s1[0] + s1[1]);
        int temp2 = Integer.parseInt(s2[0] + s2[1]);
        if (temp2 > temp1) {

            WorkingDays workingDays = new WorkingDays();
            String temp = s1[0] + ":" + s1[1];
            workingDays.setStartHour(temp.trim());
            temp = s2[0] + ":" + s2[1];
            workingDays.setEndHour(temp.trim());

            DocumentReference dayReference = db.collection("AllSalon").document(Common.currentUser.getSalonCity()).collection("Branch").document(Common.currentUser.getSalonId())
                    .collection("Barbers").document(Common.currentUser.getBarberId()).collection("WorkingDays").document(dayName);
            int dayNum = 0;
            switch (dayName) {
                case "Sunday":
                    dayNum = 1;
                    break;
                case "Monday":
                    dayNum = 2;
                    break;
                case "Tuesday":
                    dayNum = 3;
                    break;
                case "Wednesday":
                    dayNum = 4;
                    break;
                case "Thursday":
                    dayNum = 5;
                    break;
                case "Friday":
                    dayNum = 6;
                    break;
                case "Saturday":
                    dayNum = 7;
                    break;
            }

            int finalDayNum = dayNum;
            dayReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            dayReference.update("startHour", workingDays.getStartHour());
                            dayReference.update("endHour", workingDays.getEndHour());
                        } else {
                            dayReference.set(workingDays);
                        }

                        List<Date> calendarDates = new ArrayList<>();
                        for (int i = 0; i < 30; i++) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, i);
                            calendarDates.add(calendar.getTime());
                        }

                        List<String> calendarStringDates = new ArrayList<>();

                        for (int i = 0; i < 30; i++) {

                            calendarStringDates.add(Common.simpleDateFormat.format(calendarDates.get(i).getTime()));
                            Calendar date = Calendar.getInstance();

                            try {
                                date.setTime(Common.simpleDateFormat.parse(calendarStringDates.get(i)));
                                if (date.get(Calendar.DAY_OF_WEEK) == finalDayNum) {
                                    final String barDate = calendarStringDates.get(i);
                                    CollectionReference baberBookingRef = db.collection("AllSalon").document(Common.currentUser.getSalonCity()).collection("Branch").document(Common.currentUser.getSalonId())
                                            .collection("Barbers").document(Common.currentUser.getBarberId()).collection("Bookings").document("FutureBookings").collection(calendarStringDates.get(i));

                                    baberBookingRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                QuerySnapshot dateSnapshot = task.getResult();
                                                if (!dateSnapshot.isEmpty()) {
                                                    for (DocumentSnapshot hourSnapshot : dateSnapshot) {
                                                        String docId = hourSnapshot.getId();
                                                        BookingInformation bInfo = hourSnapshot.toObject(BookingInformation.class);
                                                        String[] userHourStr = bInfo.getHour().split(" - ");
                                                        String[] userStartHour = userHourStr[0].split(":");
                                                        int userHour = Integer.parseInt(userStartHour[0] + userStartHour[1]);
                                                        if (userHour < temp1) {
                                                            DocumentReference baberBookingRef = db.collection("AllSalon").document(Common.currentUser.getSalonCity()).collection("Branch").document(Common.currentUser.getSalonId())
                                                                    .collection("Barbers").document(Common.currentUser.getBarberId()).collection("Bookings").document("FutureBookings").collection(barDate).document(docId);

                                                            CollectionReference userBookingRef = db.collection("User").document(bInfo.getCustomerUid()).collection("Booking");

                                                            Query query = userBookingRef.whereEqualTo("barberId", bInfo.getBarberId()).whereEqualTo("hour", bInfo.getHour()).whereEqualTo("date", bInfo.getDate());

                                                            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        QuerySnapshot userQDocs = task.getResult();
                                                                        if (!userQDocs.isEmpty()) {
                                                                            for (QueryDocumentSnapshot qs : userQDocs) {
                                                                                String docId = qs.getId();
                                                                                DocumentReference useDocRef = FirebaseFirestore.getInstance()
                                                                                        .collection("User")
                                                                                        .document(bInfo.getCustomerUid())
                                                                                        .collection("Booking").document(docId);

                                                                                DocumentReference userTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(bInfo.getCustomerUid());
                                                                                userTokenRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            DocumentSnapshot ds = task.getResult();
                                                                                            String userToken = ds.get("token").toString().trim();
                                                                                            String message = "שלום ";
                                                                                            message += bInfo.getCustomerName() + "." + "\n";
                                                                                            message += "תורך בתאריך : ";
                                                                                            String userdate = bInfo.getDate().replace("_", "/");
                                                                                            message += userdate + "\n";
                                                                                            message += "בשעה : ";
                                                                                            String[] hour = bInfo.getHour().split(" - ");
                                                                                            String newHour = hour[1] + " - " + hour[0];
                                                                                            message += newHour;
                                                                                            message += "\n";
                                                                                            message += "אצל ";
                                                                                            message += bInfo.getBarberName() + " ";
                                                                                            message += "בוטל. \nלפרטים נוספים אנא פנה למספרה. \nתודה.";
                                                                                            sendNotification.sendNotifications(userToken, "Appointment-הודעה חדשה מ", message);
                                                                                        }
                                                                                    }
                                                                                });

                                                                                useDocRef.delete();
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            });

                                                        }
                                                        DocumentReference baberdocRef = FirebaseFirestore.getInstance()
                                                                .collection("AllSalon")
                                                                .document(Common.currentUser.getSalonCity())
                                                                .collection("Branch")
                                                                .document(Common.currentUser.getSalonId())
                                                                .collection("Barbers")
                                                                .document(Common.currentUser.getBarberId())
                                                                .collection("Bookings")
                                                                .document("FutureBookings").collection(barDate).document(docId);
                                                        baberdocRef.delete();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                }
            });

        } else
            Toast.makeText(getActivity(), "שעת סגירה לא יכולה להיות יותר גדולה משעת פתיחה", Toast.LENGTH_SHORT).show();
    }

    private void removeDayFromDB(String dayToRemove) {

        DocumentReference dayReference = db.collection("AllSalon").document(Common.currentUser.getSalonCity()).collection("Branch").document(Common.currentUser.getSalonId())
                .collection("Barbers").document(Common.currentUser.getBarberId()).collection("WorkingDays").document(dayToRemove);
        dayReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    dayReference.delete();
                    Toast.makeText(getActivity(), "הימים שנבחרו הוסרו מימי העבודה בהצלחה!", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                } else {
                    Toast.makeText(getActivity(), "היום שנבחר לא קיים במערכת!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void showBarberWorkingHours() {

        for(int i=0;i<dayOfWeek.length;i++)
        {
            dayOfWeek[i][0].setText("שעת פתיחה");
            dayOfWeek[i][1].setText("שעת סגירה");
        }

        CollectionReference barberWorkingDaysColRef = db.collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("WorkingDays");

        barberWorkingDaysColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        for (QueryDocumentSnapshot qDs : qs) {
                            String dayId = qDs.getId();
                            WorkingDays workingDays = qDs.toObject(WorkingDays.class);
                            switch (dayId) {
                                case "Sunday":
                                    dayOfWeek[0][0].setText(workingDays.getStartHour());
                                    dayOfWeek[0][1].setText(workingDays.getEndHour());
                                    break;

                                case "Monday":
                                    dayOfWeek[1][0].setText(workingDays.getStartHour());
                                    dayOfWeek[1][1].setText(workingDays.getEndHour());
                                    break;

                                case "Tuesday":
                                    dayOfWeek[2][0].setText(workingDays.getStartHour());
                                    dayOfWeek[2][1].setText(workingDays.getEndHour());
                                    break;

                                case "Wednesday":
                                    dayOfWeek[3][0].setText(workingDays.getStartHour());
                                    dayOfWeek[3][1].setText(workingDays.getEndHour());
                                    break;

                                case "Thursday":
                                    dayOfWeek[4][0].setText(workingDays.getStartHour());
                                    dayOfWeek[4][1].setText(workingDays.getEndHour());
                                    break;

                                case "Friday":
                                    dayOfWeek[5][0].setText(workingDays.getStartHour());
                                    dayOfWeek[5][1].setText(workingDays.getEndHour());
                                    break;

                                case "Saturday":
                                    dayOfWeek[6][0].setText(workingDays.getStartHour());
                                    dayOfWeek[6][1].setText(workingDays.getEndHour());
                                    break;

                                default:
                                    break;
                            }
                        }
                        dialog.dismiss();

                    }
                    else {
                        dialog.dismiss();

                    }
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
    }
}