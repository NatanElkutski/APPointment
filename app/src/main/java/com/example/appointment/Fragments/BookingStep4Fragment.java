package com.example.appointment.Fragments;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.BookingInformation;
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
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class BookingStep4Fragment extends Fragment {

    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    Unbinder unbinder;
    RelativeLayout step4_ll;
    AlertDialog dialog;
    TextView txt_booking_barber_text;
    TextView txt_booking_time_text;
    TextView txt_salon_address;
    TextView txt_salon_name;
    TextView txt_salon_open_hours;
    TextView txt_salon_phone;
    TextView txt_salon_website;


    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    private void setData() {
        txt_booking_barber_text.setText(Common.currentBarber.getName());
        String[] newstring = Common.timeCards.get(Common.bookingTimeSlotNumber).split(" - ");
        StringBuilder time = new StringBuilder();
        time.append(newstring[1]).append(" - ").append(newstring[0]).toString();
        StringBuilder sb = new StringBuilder();
        sb.append(" ב- ");
        sb.append(simpleDateFormat.format(Common.bookingDate.getTime()));
        sb.append(" בשעה: ");
        sb.append(time);
        txt_booking_time_text.setText(sb.toString());
        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());

        Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);// Put index of time slot we have selected
        intent.putExtra(Common.KEY_STEP, 4);
        localBroadcastManager.sendBroadcast(intent);
    }

    static BookingStep4Fragment instance;

    public static BookingStep4Fragment getInstance() {
        instance = new BookingStep4Fragment();
        return instance;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Apply format for date display on Confirm
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();


    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_four, container, false);

        step4_ll = itemView.findViewById(R.id.booking_step4_confirm_id_ll);
        step4_ll.setRotationY(180);

        unbinder = ButterKnife.bind(this, itemView);
        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
        txt_booking_barber_text = itemView.findViewById(R.id.txt_booking_barber_text);
        txt_booking_time_text = (TextView) itemView.findViewById(R.id.txt_booking_time_text);
        txt_salon_address = (TextView) itemView.findViewById(R.id.txt_salon_address);
        txt_salon_name = (TextView) itemView.findViewById(R.id.txt_salon_name);
        txt_salon_open_hours = (TextView) itemView.findViewById(R.id.txt_salon_open_hours);
        txt_salon_phone = (TextView) itemView.findViewById(R.id.txt_salon_phone);
        txt_salon_website = (TextView) itemView.findViewById(R.id.txt_salon_website);

        return itemView;
    }

    @Override
    public void onDestroyView() {
        Common.step = 0;
        Common.bookingTimeSlotNumber = -1;
        Common.currentBarber = null;
        Common.currentSalon = null;
        Common.bookingDate = Common.currentDate;
        Common.bookingInformation = null;
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
