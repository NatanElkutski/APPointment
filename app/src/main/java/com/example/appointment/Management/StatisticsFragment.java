package com.example.appointment.Management;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.DateForStatistics;
import com.example.appointment.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.timessquare.CalendarPickerView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class StatisticsFragment extends Fragment {

    BarChart barChart;
    Button chooseDatesBtn;
    Dialog mDialog;
    TextView newClientsTv, totalAppointmentsTv, datesRangeTv;
    List<String> datesList;
    List<Date> dateList;
    Calendar s, e;
    List<DateForStatistics> dateForStatisticsList;
    ArrayList<BarEntry> data;
    List<String> vacList;
    int cnt = 0, totalsize = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View itemView = inflater.inflate(R.layout.fragment_statistics, container, false);
        barChart = itemView.findViewById(R.id.barChartWeek);
        barChart.setTouchEnabled(false);
        barChart.setDragEnabled(false);
//        barChart.setScaleEnabled(false);
//        barChart.setScaleXEnabled(false);
//        barChart.setScaleYEnabled(false);
        barChart.setPinchZoom(false);
        chooseDatesBtn = itemView.findViewById(R.id.chooseDatesBtn);
        newClientsTv = itemView.findViewById(R.id.statistics_week_new_clients_cnt_tv);
        totalAppointmentsTv = itemView.findViewById(R.id.statistics_week_number_of_appointments_tv);
        datesRangeTv = itemView.findViewById(R.id.statistics_week_dates_range_tv);
        initGraph();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chooseDatesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = new Dialog(getContext());
                mDialog.setCancelable(false);
                mDialog.setContentView(R.layout.dialog_date_picker);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button acceptBtn = mDialog.findViewById(R.id.dialog_date_picker_btn_accept_id);
                Button cancelBtn = mDialog.findViewById(R.id.dialog_date_picker_btn_cancel_id);

                Date today = new Date();

                Calendar tommorowDate = Calendar.getInstance();
                tommorowDate.add(Calendar.DATE, 1);

                Calendar startingDate = Calendar.getInstance();
                int CurrentYear = startingDate.get(Calendar.YEAR) - 1;
                startingDate.set(Calendar.YEAR, CurrentYear);
                startingDate.set(Calendar.DAY_OF_MONTH, 1);

                CalendarPickerView datePicker = mDialog.findViewById(R.id.dialog_date_picker_calendar_view_grid);
                datePicker.setBackgroundResource(0);

                datePicker.init(startingDate.getTime(), tommorowDate.getTime()).withHighlightedDate(today)
                        .inMode(CalendarPickerView.SelectionMode.RANGE);

                datePicker.scrollToDate(today);

                acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        dateList = new ArrayList<>();
                        dateList = datePicker.getSelectedDates();
                        vacList = new ArrayList<>();
                        for (Date date : dateList) {
                            String strDate = Common.simpleDateFormat.format(date);
                            vacList.add(strDate);
                        }
                        receiveNewClients();
                    }
                });
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

                mDialog.show();
            }
        });
    }

    public void receiveNewClients() {

        DocumentReference countDoneMeetings = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("Bookings")
                .document("PastBookings");

        dateForStatisticsList = new ArrayList<>();

        s = Calendar.getInstance();
        s.setTime(dateList.get(0));
        s.set(Calendar.HOUR_OF_DAY, 0);
        s.set(Calendar.MINUTE, 0);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);
        s.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));

        e = Calendar.getInstance();
        e.setTime(dateList.get(dateList.size() - 1));
        e.set(Calendar.HOUR_OF_DAY, 23);
        e.set(Calendar.MINUTE, 59);
        e.set(Calendar.SECOND, 0);
        e.set(Calendar.MILLISECOND, 0);
        e.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));

        countDoneMeetings.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    datesList = new ArrayList<>();
                    for (int j = 0; j < vacList.size(); j++) {
                        String[] dstr = vacList.get(j).split("_");
                        String dateStr = dstr[0] + "." + dstr[1];
                        float floatDate = Float.parseFloat(dateStr);
                        DateForStatistics dateForStatistics = new DateForStatistics();
                        dateForStatistics.setDate(floatDate);
                        dateForStatistics.setValues(0);
                        dateForStatisticsList.add(dateForStatistics);
                    }
                    if (task.getResult().exists()) {
                        Map<String, Object> map = task.getResult().getData();
                        for (Map.Entry<String, Object> entry : map.entrySet()) { // Receive FutureBookings Document Fields to get all the existing dates till today (Including Today)
                            try {
                                Date date = Common.simpleDateFormat.parse(entry.getKey());
                                Calendar listDate = Calendar.getInstance();
                                listDate.setTime(date);
                                if (listDate.getTimeInMillis() >= s.getTimeInMillis() && listDate.getTimeInMillis() <= e.getTimeInMillis()) {
                                    datesList.add(entry.getKey());
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        totalsize = datesList.size();
                        if (totalsize > 0) {
                            for (int i=0;i<vacList.size();i++) {
                                String strDate = vacList.get(i);
                                int finalI = i;
                                countDoneMeetings.collection(strDate).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (!task.getResult().isEmpty()) {
                                                int totalSum = dateForStatisticsList.get(finalI).getValues()+task.getResult().size();
                                                float tempFDate = dateForStatisticsList.get(finalI).getDate();
                                                DateForStatistics dateForStatistics = new DateForStatistics();
                                                dateForStatistics.setDate(tempFDate);
                                                dateForStatistics.setValues(totalSum);
                                                dateForStatisticsList.set(finalI,dateForStatistics);
                                            }
                                            cnt++;
                                            if (cnt == totalsize) {
                                                cnt = 0;
                                                totalsize=0;
                                                loadMonthly();
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            loadMonthly();
                        }
                    }
                }
            }

        });

    }


    public void loadMonthly() {
        Timestamp startTimestamp = new Timestamp(s.getTime());
        Timestamp endTimestamp = new Timestamp(e.getTime());

        CollectionReference newCustomersRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Clients");


        // Counter for new clients (Yearly)
        Query query = newCustomersRef.whereGreaterThan("timestamp", startTimestamp).whereLessThan("timestamp", endTimestamp);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!task.getResult().isEmpty()) {
                        newClientsTv.setText("" + querySnapshot.size());
                    } else {
                        newClientsTv.setText("0");
                    }

                    data = new ArrayList<>();
                    int totalSum = 0;
                    for (int i = 0; i < dateForStatisticsList.size(); i++) {
                        data.add(new BarEntry(dateForStatisticsList.get(i).getDate(), dateForStatisticsList.get(i).getValues()));
                        totalSum += dateForStatisticsList.get(i).getValues();
                    }

                    totalAppointmentsTv.setText("" + totalSum);
                    String DateRangStr = vacList.get(vacList.size() - 1).replace("_", "/") + " - " + vacList.get(0).replace("_", "/");
                    datesRangeTv.setText(DateRangStr);

                    BarDataSet barDataSet = new BarDataSet(data, "ימים");

                    barDataSet.setColors(ColorTemplate.rgb("0362E1"));
                    barDataSet.setValueTextColor(Color.BLACK);
                    barDataSet.setValueTextColor(16);

                    BarData barData = new BarData(barDataSet);

                    barChart.setFitBars(true);
                    barChart.setData(barData);
                    barChart.getDescription().setText("חודשי");
                    barChart.animateY(2000);

                }
            }
        });
    }

    public void initGraph() {
        data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            float f = i;
            data.add(i, new BarEntry(f, 0));
        }

        BarDataSet barDataSet = new BarDataSet(data, "ימים");

        barDataSet.setColors(ColorTemplate.rgb("0362E1"));
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextColor(16);
        barDataSet.setFormLineWidth(0.1f);

        BarData barData = new BarData(barDataSet);

        barChart.setFitBars(true);
        barChart.setData(barData);
        barChart.getDescription().setText("חודשי");
        barChart.animateY(2000);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}