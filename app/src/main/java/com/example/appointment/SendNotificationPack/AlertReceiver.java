package com.example.appointment.SendNotificationPack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.appointment.Model.BookingInformation;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class AlertReceiver extends BroadcastReceiver {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification();
        String barberId = intent.getStringExtra("barberId");
        String date = intent.getStringExtra("date");
        String bookingTime = intent.getStringExtra("bookingTime");

        CollectionReference ifAppointmentExistsRef = FirebaseFirestore.getInstance()
                .collection("User").document(mAuth.getCurrentUser().getUid())
                .collection("Booking");

        Log.d("TIME FROM ALERTRECEIVER :",bookingTime);

        ifAppointmentExistsRef
                .whereEqualTo("date", date)
                .whereEqualTo("hour",bookingTime)
                .whereEqualTo("barberId",barberId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        for(QueryDocumentSnapshot qds : qs)
                        {
                            BookingInformation Binfo = qds.toObject(BookingInformation.class);
                            Log.d("THE TIME OF NOT",Binfo.getHour());
                            nb.setContentTitle("תזכורת לתור!");
                            String [] hourFormatted = Binfo.getHour().split(" - ");
                            String newHour = hourFormatted[1]+" - "+hourFormatted[0];
                            nb.setContentText("תזכורת לתור אצל "+Binfo.getBarberName()+" בשעה: "+newHour);
                            nb.setSmallIcon(R.mipmap.ic_launcher);
                        }
                        notificationHelper.getManager().notify(1, nb.build());
                    }
                }
            }
        });

    }
}
