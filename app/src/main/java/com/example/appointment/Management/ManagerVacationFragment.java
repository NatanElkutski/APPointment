package com.example.appointment.Management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.appointment.Adapter.VacationsListAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Interface.ITimeSlotLoadBarberListener;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.WorkingDays;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.sendNotification;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import dmax.dialog.SpotsDialog;

public class ManagerVacationFragment extends Fragment {

    Button acceptbtn, canacelVacbtn;
    ProgressDialog pdialog;
    Boolean pressed = false;
    AlertDialog dialog;
    Dialog mDialog;
    RecyclerView vacationRecycler;
    List<String> datesStringsList;
    MaterialSpinner materialSpinner;
    List<String> list = new ArrayList<>();
    List<Barber> barberList = new ArrayList<>();
    List<String> barberIdList = new ArrayList<>();
    String choosenBarberId;
    CalendarPickerView datePicker;
    LinearLayout btnLinearLayout;
    Calendar today = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
    private VacationsListAdapter vacationsListAdapter;

    public ManagerVacationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dialog.show();
        View itemView = inflater.inflate(R.layout.fragment_manager_vacation, container, false);
        materialSpinner = itemView.findViewById(R.id.Manager_vacation_material_spinner);
        btnLinearLayout = itemView.findViewById(R.id.Manager_vacation_btn_linear_id);
        acceptbtn = itemView.findViewById(R.id.Manager_vacation_btn_accept_id);
        materialSpinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.barbers_spinner_dropdown_background));
        canacelVacbtn = itemView.findViewById(R.id.Manager_cancelVacations_btn_id);
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
                            QuerySnapshot qs = task.getResult();
                            list.add("בחר ספר");
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot barberQDoc : qs) {
                                    Barber barber = barberQDoc.toObject(Barber.class);
                                    barberIdList.add(barberQDoc.getId());
                                    barberList.add(barber);
                                    list.add(barber.getName());
                                }
                            }
                            barbersLoadSuccess();
                        }
                    }
                });
    }

    public void barbersLoadSuccess() {
        materialSpinner.setItems(list);
        dialog.dismiss();
        materialSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0) {
                    if (datePicker.getVisibility() == View.VISIBLE)
                        datePicker.setVisibility(View.INVISIBLE);
                    Common.vacationList = new ArrayList<>();
                    datePicker.clearHighlightedDates();
                    choosenBarberId = barberIdList.get(position - 1);
                    highlightDatesCalculate();
                    acceptbtn.setEnabled(true);
                    canacelVacbtn.setEnabled(true);

                } else {

                    datePicker.setVisibility(View.INVISIBLE);
                    acceptbtn.setEnabled(false);
                    canacelVacbtn.setEnabled(false);

                }
            }
        });
    }

    private void highlightDatesCalculate() {

        pdialog = new ProgressDialog(getActivity());
        pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdialog.setTitle("קביעת חופשים");
        pdialog.setMessage("טוען לוח שנה אנא המתן...");
        pdialog.setCancelable(false);
        pdialog.setInverseBackgroundForced(false);
        pdialog.setIcon(R.mipmap.ic_launcher);
        pdialog.show();
        CollectionReference colRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(choosenBarberId)
                .collection("CustomDates");

        Query query = colRef.whereEqualTo("startHour", "-1").whereEqualTo("endHour", "-1");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {

                        for (DocumentSnapshot ds : task.getResult()) {
                            String DatesFromDb = ds.getId();
                            Calendar c = Calendar.getInstance();

                            try {

                                c.setTime(sdf.parse(DatesFromDb.toString()));
                                if (today.getTime().compareTo(c.getTime()) < 0 || today.getTime().compareTo(c.getTime()) == 0)
                                    Common.vacationList.add(c.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }

                        if (Common.vacationList.size() > 0) {
                            datePicker.highlightDates(Common.vacationList);
                            datePicker.setDateSelectableFilter(new CalendarPickerView.DateSelectableFilter() {
                                @Override
                                public boolean isDateSelectable(Date date) {
                                    String calDate = Common.simpleDateFormat.format(date);
                                    for (int i = 0; i < Common.vacationList.size(); i++) {
                                        String listDate = Common.simpleDateFormat.format(Common.vacationList.get(i));
                                        if (calDate.equals(listDate)) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            });

                            datePicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
                                @Override
                                public void onDateSelected(Date date) {
                                    for (int i = 0; i < Common.vacationList.size(); i++) {
                                        String listDate = Common.simpleDateFormat.format(Common.vacationList.get(i));
                                        String datestr = Common.simpleDateFormat.format(date);
                                        if (datestr.equals(listDate)) {
                                            Toast.makeText(getActivity(), "NOT GOOOOD!!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onDateUnselected(Date date) {

                                }
                            });
                        }
                        pdialog.dismiss();
                        datePicker.setVisibility(View.VISIBLE);
                    } else {
                        pdialog.dismiss();
                        datePicker.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        datePicker.setOnInvalidDateSelectedListener(new CalendarPickerView.OnInvalidDateSelectedListener() {
            @Override
            public void onInvalidDateSelected(Date date) {
                Toast.makeText(getContext(), "לא ניתן לבחור תאריך זה.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnLinearLayout.setBackgroundResource(0);
        acceptbtn.setEnabled(false);
        canacelVacbtn.setEnabled(false);
        Date today = new Date();
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);
        datePicker = view.findViewById(R.id.Manager_calendar_view_grid);
        datePicker.setBackgroundResource(0);


        datePicker.init(today, nextYear.getTime())
                .inMode(CalendarPickerView.SelectionMode.RANGE);


        acceptbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Date> dateList = datePicker.getSelectedDates();
                if (dateList.size() > 0) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle("קביעת חופשים").setMessage("האם הינך רוצה לקבוע חופשים בתאריכים שנבחרו?").setCancelable(false);
                    dialog.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            List<String> vacList = new ArrayList<>();
                            if (Common.vacationList.size() > 0) {
                                for (Date dates : dateList) {
                                    for (int i = 0; i < Common.vacationList.size(); i++) {
                                        if (dates.equals(Common.vacationList.get(i))) {
                                            Toast.makeText(getActivity(), "קיימת חופשה עם אחד או יותר מהתאריכים שנבחרו!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }
                                }
                            }

                            for (Date date : dateList) {
                                String strDate = Common.simpleDateFormat.format(date);
                                Log.d("SELECTED DATE:  ", strDate);
                                vacList.add(strDate);

                                DocumentReference barberVacRef = FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.currentUser.getSalonCity())
                                        .collection("Branch")
                                        .document(Common.currentUser.getSalonId())
                                        .collection("Barbers")
                                        .document(choosenBarberId)
                                        .collection("CustomDates")
                                        .document(strDate);


                                barberVacRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            WorkingDays workingDays = new WorkingDays();
                                            workingDays.setStartHour("-1");
                                            workingDays.setEndHour("-1");
                                            barberVacRef.set(workingDays);


                                            CollectionReference bookdate = FirebaseFirestore.getInstance()
                                                    .collection("AllSalon")
                                                    .document(Common.currentUser.getSalonCity())
                                                    .collection("Branch")
                                                    .document(Common.currentUser.getSalonId())
                                                    .collection("Barbers")
                                                    .document(choosenBarberId)
                                                    .collection("Bookings")
                                                    .document("FutureBookings").collection(strDate);

                                            bookdate.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        QuerySnapshot qs = task.getResult();
                                                        if (!qs.isEmpty()) {

                                                            for (QueryDocumentSnapshot ds : qs) {
                                                                String baberDocId = ds.getId();
                                                                BookingInformation bInfo = ds.toObject(BookingInformation.class);
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
                                                                            message += "אצל הספר ";
                                                                            message += bInfo.getBarberName() + " ";
                                                                            message += "בוטל. \nלפרטים נוספים אנא פנה למספרה! \nתודה!.";
                                                                            sendNotification.sendNotifications(userToken, "Appointment-הודעה חדשה מ", message);
                                                                        }
                                                                    }
                                                                });
                                                                CollectionReference userRef = FirebaseFirestore.getInstance()
                                                                        .collection("User")
                                                                        .document(bInfo.getCustomerUid())
                                                                        .collection("Booking");

                                                                Query query = userRef.whereEqualTo("date", bInfo.getDate())
                                                                        .whereEqualTo("hour", bInfo.getHour()).whereEqualTo("barberId", bInfo.getBarberId());

                                                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            QuerySnapshot qs = task.getResult();
                                                                            if (!qs.isEmpty()) {
                                                                                for (QueryDocumentSnapshot dr : qs) {
                                                                                    String docId = dr.getId();
                                                                                    DocumentReference useDocRef = FirebaseFirestore.getInstance()
                                                                                            .collection("User")
                                                                                            .document(bInfo.getCustomerUid())
                                                                                            .collection("Booking").document(docId);
                                                                                    useDocRef.delete();
                                                                                }
                                                                            }

                                                                        }
                                                                    }
                                                                });

                                                                DocumentReference baberdocRef = FirebaseFirestore.getInstance()
                                                                        .collection("AllSalon")
                                                                        .document(Common.currentUser.getSalonCity())
                                                                        .collection("Branch")
                                                                        .document(Common.currentUser.getSalonId())
                                                                        .collection("Barbers")
                                                                        .document(choosenBarberId)
                                                                        .collection("Bookings")
                                                                        .document("FutureBookings").collection(strDate).document(baberDocId);
                                                                baberdocRef.delete();
                                                                ;
                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }

                            String vacTitle = vacList.get(0) + " - " + vacList.get(vacList.size() - 1);

                            DocumentReference vacationRef = FirebaseFirestore.getInstance()
                                    .collection("AllSalon")
                                    .document(Common.currentUser.getSalonCity())
                                    .collection("Branch")
                                    .document(Common.currentUser.getSalonId())
                                    .collection("Barbers")
                                    .document(choosenBarberId)
                                    .collection("Vacations")
                                    .document(vacTitle);

                            Long timeInMillis = dateList.get(0).getTime();
                            vacationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        int i = 1;
                                        for (String date : vacList) {
                                            HashMap<String, Object> dayMap = new HashMap<>();
                                            dayMap.put("day" + i, date);
                                            if (i == 1) vacationRef.set(dayMap);
                                            else vacationRef.update(dayMap);
                                            i++;
                                        }
                                        String times = timeInMillis.toString();
                                        HashMap<String, Object> timeMap = new HashMap<>();
                                        timeMap.put("time", times);
                                        vacationRef.update(timeMap);
                                    }
                                }
                            });
                            pressed = true;
                        }
                    }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    });
                    final AlertDialog alert = dialog.create();
                    alert.show();
                    alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (pressed == true) {
                                Toast.makeText(getContext(), "תאריכים נבחרו בהצלחה!", Toast.LENGTH_SHORT).show();
                                getActivity().onBackPressed();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getActivity(), "לא נבחרו תאריכים!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });


        canacelVacbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.vacationList.size() != 0) {

                    datesStringsList = new ArrayList<>();
                    mDialog = new Dialog(getActivity());
                    mDialog.setContentView(R.layout.dialog_vacations);
                    mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    vacationRecycler = mDialog.findViewById(R.id.vacations_recycler_view_id);
                    init();

                    vacationsListAdapter.setOnItemClickListener(new VacationsListAdapter.OnItemClickListener() {
                        @Override
                        public void onDelete(int position) {
                            dialog.show();
                            String date = datesStringsList.get(position).replace("/", "_");
                            DocumentReference vacDateRef = FirebaseFirestore.getInstance()
                                    .collection("AllSalon")
                                    .document(Common.currentUser.getSalonCity())
                                    .collection("Branch")
                                    .document(Common.currentUser.getSalonId())
                                    .collection("Barbers")
                                    .document(choosenBarberId)
                                    .collection("Vacations")
                                    .document(date);

                            vacDateRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {

                                            List<String> list = new ArrayList<>();
                                            Map<String, Object> map = document.getData();
                                            if (map != null) {
                                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                                    list.add(entry.getValue().toString());
                                                }
                                            }

                                            for (String s : list) {
                                                DocumentReference customDateRef = FirebaseFirestore.getInstance()
                                                        .collection("AllSalon")
                                                        .document(Common.currentUser.getSalonCity())
                                                        .collection("Branch")
                                                        .document(Common.currentUser.getSalonId())
                                                        .collection("Barbers")
                                                        .document(choosenBarberId)
                                                        .collection("CustomDates")
                                                        .document(s);
                                                customDateRef.delete();
                                            }
                                            List<Date> highLightDates = new ArrayList<>();
                                            for (String date : list) {
                                                Calendar d = Calendar.getInstance();
                                                try {
                                                    d.setTime(Common.simpleDateFormat.parse(date));
                                                    highLightDates.add(d.getTime());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            vacDateRef.delete();
                                            dialog.dismiss();
                                            mDialog.dismiss();
                                            Common.vacationList = new ArrayList<>();
                                            Toast.makeText(getActivity(), "חופשה בוטלה בהצלחה!", Toast.LENGTH_SHORT).show();
                                            getActivity().onBackPressed();

                                        }
                                    }
                                }
                            });


                        }
                    });
                    mDialog.show();
                    dialog = new SpotsDialog.Builder().setContext(mDialog.getContext()).setCancelable(false).build();
                    dialog.show();
                } else Toast.makeText(getActivity(), "לא נקבעו חופשות!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void init() {

        CollectionReference mCollection = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(choosenBarberId)
                .collection("Vacations");


        mCollection.orderBy("time", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot abc = task.getResult();

                    if (abc.size() > 0) {
                        for (DocumentSnapshot shem : abc) {
                            String[] vacRangeStr = shem.getId().split(" - ");
                            Calendar c = Calendar.getInstance();
                            try {
                                c.setTime(sdf.parse(vacRangeStr[1]));
                                if (today.getTime().compareTo(c.getTime()) <= 0)
                                    datesStringsList.add(shem.getId());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }

                    }

                    vacationsListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });


        vacationRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        vacationsListAdapter = new VacationsListAdapter(datesStringsList);
        vacationRecycler.setAdapter(vacationsListAdapter);
        vacationRecycler.setHasFixedSize(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
    }
}