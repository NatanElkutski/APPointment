package com.example.appointment.Management;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.Message;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.sendNotification;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class RemoveEmployeesFragment extends Fragment {

    LinearLayout cardView_linear;
    MaterialSpinner spinner;
    List<String> list = new ArrayList<>();
    List<Barber> barberList = new ArrayList<>();
    List<String> barberIdList = new ArrayList<>();
    TextView barberName;
    Button removeBarberBtn, cancelBtn;
    AlertDialog alertDialog;

    public RemoveEmployeesFragment() {
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
        View itemview = inflater.inflate(R.layout.fragment_remove_employees, container, false);
        alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
        spinner = itemview.findViewById(R.id.remove_employee_spinner);
        cardView_linear = itemview.findViewById(R.id.remove_employee_barber_cardview_linear_layout);
        barberName = itemview.findViewById(R.id.remove_employee_cardview_barber_name);
        removeBarberBtn = itemview.findViewById(R.id.remove_employees_accept_btn);
        removeBarberBtn.setEnabled(false);
        spinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.barbers_spinner_dropdown_background));

        loadSalonBarbers();
        return itemview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


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
                                barbersLoadSuccess();
                            } else {
                                barbersLoadSuccess();
                            }
                        }
                    }
                });
    }

    public void barbersLoadSuccess() {
        spinner.setItems(list);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0) {
                    removeBarberBtn.setEnabled(true);
                    barberName.setText(list.get(position));
                    cardView_linear.setVisibility(View.VISIBLE);
                    String choosenBarberId = barberIdList.get(position - 1);
                    String choosenbarberUid = barberList.get(position - 1).getUid();
                    removeBarberBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext()).setTitle("הסרת עובד").setMessage("האם הינך בטוח שברצונך להסיר את העובד " + barberName.getText().toString().trim() + "?");
                            alertBuilder.setPositiveButton("הסר", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.show();
                                    DocumentReference barberFutureBookings = FirebaseFirestore.getInstance()
                                            .collection("AllSalon")
                                            .document(Common.currentUser.getSalonCity())
                                            .collection("Branch")
                                            .document(Common.currentUser.getSalonId())
                                            .collection("Barbers").document(choosenBarberId)
                                            .collection("Bookings")
                                            .document("FutureBookings");

                                    barberFutureBookings.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot ds = task.getResult();
                                                if (ds.exists()) {
                                                    Map<String, Object> map = ds.getData();
                                                    for (Map.Entry<String, Object> entry : map.entrySet()) { // Receive FutureBookings Document Fields to get all the existing dates till today (Including Today)
                                                        CollectionReference barberFutureDates = FirebaseFirestore.getInstance()
                                                                .collection("AllSalon")
                                                                .document(Common.currentUser.getSalonCity())
                                                                .collection("Branch")
                                                                .document(Common.currentUser.getSalonId())
                                                                .collection("Barbers")
                                                                .document(choosenBarberId).collection("Bookings")
                                                                .document("FutureBookings")
                                                                .collection(entry.getKey());
                                                        barberFutureDates.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    QuerySnapshot futureQS = task.getResult();
                                                                    if (!futureQS.isEmpty()) {
                                                                        for (QueryDocumentSnapshot futureDateQDS : futureQS) {

                                                                            BookingInformation Binfo = futureDateQDS.toObject(BookingInformation.class);

                                                                            String message = "שלום ";
                                                                            message += Binfo.getCustomerName() + "." + "\n";
                                                                            message += "תורך בתאריך : ";
                                                                            String userdate = Binfo.getDate().replace("_", "/");
                                                                            message += userdate + "\n";
                                                                            message += "בשעה : ";
                                                                            String[] hour = Binfo.getHour().split(" - ");
                                                                            String newHour = hour[1] + " - " + hour[0];
                                                                            message += newHour;
                                                                            message += "\n";
                                                                            message += "אצל ";
                                                                            message += Binfo.getBarberName() + " ";
                                                                            message += "בוטל. \nלפרטים נוספים אנא פנה למספרה. \nתודה.";

                                                                            String subject = "הודעה על ביטול תור קיים";

                                                                            Timestamp timestamp = null;
                                                                            try {
                                                                                timestamp = Common.makeTimeStampMessage(Common.currentTimeWithDate());
                                                                            } catch (ParseException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            Message messageToUserDb = new Message(Binfo.getSalonName(), subject, message, Common.currentTimeWithDate(), timestamp);

                                                                            CollectionReference userMessageRef = FirebaseFirestore.getInstance().collection("User")
                                                                                    .document(Binfo.getCustomerUid()).collection("Messages");

                                                                            String finalMessage = message;
                                                                            userMessageRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                    userMessageRef.document().set(messageToUserDb).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                        @Override
                                                                                        public void onSuccess(Void aVoid) {
                                                                                            DocumentReference userRef = FirebaseFirestore.getInstance().collection("Tokens").document(Binfo.getCustomerUid());
                                                                                            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                    if (task.isSuccessful()) {
                                                                                                        DocumentSnapshot ds = task.getResult();
                                                                                                        String userToken = ds.get("token").toString().trim();
                                                                                                        sendNotification.sendNotifications(userToken, "Appointment-הודעה חדשה מ", finalMessage);

                                                                                                        CollectionReference userBookingRef = FirebaseFirestore.getInstance().collection("User")
                                                                                                                .document(Binfo.getCustomerUid()).collection("Booking");

                                                                                                        userBookingRef.whereEqualTo("barberId", choosenBarberId)
                                                                                                                .whereEqualTo("date", Binfo.getDate())
                                                                                                                .whereEqualTo("hour", Binfo.getHour()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    QuerySnapshot userBookingQs = task.getResult();
                                                                                                                    if (!userBookingQs.isEmpty()) {
                                                                                                                        for (QueryDocumentSnapshot userBookingQDS : userBookingQs) {
                                                                                                                            DocumentReference userBookingDocRef = FirebaseFirestore.getInstance().collection("User")
                                                                                                                                    .document(Binfo.getCustomerUid())
                                                                                                                                    .collection("Booking")
                                                                                                                                    .document(userBookingQDS.getId());
                                                                                                                            userBookingDocRef.delete();
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });


                                                                            DocumentReference FutureDate = FirebaseFirestore.getInstance()
                                                                                    .collection("AllSalon")
                                                                                    .document(Common.currentUser.getSalonCity())
                                                                                    .collection("Branch")
                                                                                    .document(Common.currentUser.getSalonId())
                                                                                    .collection("Barbers")
                                                                                    .document(choosenBarberId).collection("Bookings")
                                                                                    .document("FutureBookings")
                                                                                    .collection(entry.getKey())
                                                                                    .document(futureDateQDS.getId());

                                                                            FutureDate.delete();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                    barberFutureBookings.delete();
                                                }
                                                DocumentReference barberPastBookings = FirebaseFirestore.getInstance()
                                                        .collection("AllSalon")
                                                        .document(Common.currentUser.getSalonCity())
                                                        .collection("Branch")
                                                        .document(Common.currentUser.getSalonId())
                                                        .collection("Barbers").document(choosenBarberId)
                                                        .collection("Bookings")
                                                        .document("PastBookings");

                                                barberPastBookings.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot pastDS = task.getResult();
                                                            if (pastDS.exists()) {
                                                                Map<String, Object> map = pastDS.getData();
                                                                for (Map.Entry<String, Object> pastEntry : map.entrySet()) { // Receive FutureBookings Document Fields to get all the existing dates till today (Including Today)
                                                                    CollectionReference barberPastDates = FirebaseFirestore.getInstance()
                                                                            .collection("AllSalon")
                                                                            .document(Common.currentUser.getSalonCity())
                                                                            .collection("Branch")
                                                                            .document(Common.currentUser.getSalonId())
                                                                            .collection("Barbers")
                                                                            .document(choosenBarberId).collection("Bookings")
                                                                            .document("PastBookings")
                                                                            .collection(pastEntry.getKey());

                                                                    barberPastDates.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                QuerySnapshot pastQS = task.getResult();
                                                                                if (!pastQS.isEmpty()) {
                                                                                    for (QueryDocumentSnapshot pastQDS : pastQS) {
                                                                                        DocumentReference PastDate = FirebaseFirestore.getInstance()
                                                                                                .collection("AllSalon")
                                                                                                .document(Common.currentUser.getSalonCity())
                                                                                                .collection("Branch")
                                                                                                .document(Common.currentUser.getSalonId())
                                                                                                .collection("Barbers")
                                                                                                .document(choosenBarberId).collection("Bookings")
                                                                                                .document("PastBookings")
                                                                                                .collection(pastEntry.getKey())
                                                                                                .document(pastQDS.getId());

                                                                                        PastDate.delete();
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                                barberPastBookings.delete();
                                                            }
                                                            CollectionReference barberCustomDates = FirebaseFirestore.getInstance()
                                                                    .collection("AllSalon")
                                                                    .document(Common.currentUser.getSalonCity())
                                                                    .collection("Branch")
                                                                    .document(Common.currentUser.getSalonId())
                                                                    .collection("Barbers").document(choosenBarberId)
                                                                    .collection("CustomDates");

                                                            barberCustomDates.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        QuerySnapshot customDatesQS = task.getResult();
                                                                        if (!customDatesQS.isEmpty()) {
                                                                            for (QueryDocumentSnapshot customDatesQDS : customDatesQS) {
                                                                                DocumentReference CustomDate = FirebaseFirestore.getInstance()
                                                                                        .collection("AllSalon")
                                                                                        .document(Common.currentUser.getSalonCity())
                                                                                        .collection("Branch")
                                                                                        .document(Common.currentUser.getSalonId())
                                                                                        .collection("Barbers").document(choosenBarberId)
                                                                                        .collection("CustomDates")
                                                                                        .document(customDatesQDS.getId());
                                                                                CustomDate.delete();
                                                                            }

                                                                        }
                                                                        CollectionReference barberVacations = FirebaseFirestore.getInstance()
                                                                                .collection("AllSalon")
                                                                                .document(Common.currentUser.getSalonCity())
                                                                                .collection("Branch")
                                                                                .document(Common.currentUser.getSalonId())
                                                                                .collection("Barbers").document(choosenBarberId)
                                                                                .collection("Vacations");

                                                                        barberVacations.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    QuerySnapshot vacationQS = task.getResult();
                                                                                    if (!vacationQS.isEmpty()) {
                                                                                        for (QueryDocumentSnapshot vacationQDS : vacationQS) {
                                                                                            DocumentReference vacationDate = FirebaseFirestore.getInstance()
                                                                                                    .collection("AllSalon")
                                                                                                    .document(Common.currentUser.getSalonCity())
                                                                                                    .collection("Branch")
                                                                                                    .document(Common.currentUser.getSalonId())
                                                                                                    .collection("Barbers").document(choosenBarberId)
                                                                                                    .collection("Vacations")
                                                                                                    .document(vacationQDS.getId());

                                                                                            vacationDate.delete();
                                                                                        }
                                                                                    }

                                                                                    CollectionReference barberWorkingDays = FirebaseFirestore.getInstance()
                                                                                            .collection("AllSalon")
                                                                                            .document(Common.currentUser.getSalonCity())
                                                                                            .collection("Branch")
                                                                                            .document(Common.currentUser.getSalonId())
                                                                                            .collection("Barbers").document(choosenBarberId)
                                                                                            .collection("WorkingDays");

                                                                                    barberWorkingDays.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                QuerySnapshot workingdaysQS = task.getResult();
                                                                                                if (!workingdaysQS.isEmpty()) {
                                                                                                    for (QueryDocumentSnapshot workingdayQDS : workingdaysQS) {
                                                                                                        DocumentReference workingDay = FirebaseFirestore.getInstance()
                                                                                                                .collection("AllSalon")
                                                                                                                .document(Common.currentUser.getSalonCity())
                                                                                                                .collection("Branch")
                                                                                                                .document(Common.currentUser.getSalonId())
                                                                                                                .collection("Barbers").document(choosenBarberId)
                                                                                                                .collection("WorkingDays")
                                                                                                                .document(workingdayQDS.getId());

                                                                                                        workingDay.delete();
                                                                                                    }
                                                                                                }
                                                                                                DocumentReference salonBarberDoc = FirebaseFirestore.getInstance()
                                                                                                        .collection("AllSalon")
                                                                                                        .document(Common.currentUser.getSalonCity())
                                                                                                        .collection("Branch")
                                                                                                        .document(Common.currentUser.getSalonId())
                                                                                                        .collection("Barbers").document(choosenBarberId);
                                                                                                salonBarberDoc.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        DocumentReference barberUserDoc = FirebaseFirestore.getInstance()
                                                                                                                .collection("User").document(choosenbarberUid);

                                                                                                        barberUserDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                                if(task.isSuccessful())
                                                                                                                {
                                                                                                                    DocumentSnapshot ds = task.getResult();
                                                                                                                    User user = ds.toObject(User.class);
                                                                                                                    if(user.getPermission().equals("barber"))
                                                                                                                    {
                                                                                                                        barberUserDoc.update("permission", "user");
                                                                                                                        barberUserDoc.update("salonId", "");
                                                                                                                        barberUserDoc.update("salonCity", "");
                                                                                                                        barberUserDoc.update("barberId", "");
                                                                                                                        barberUserDoc.update("salonName", "");
                                                                                                                    }
                                                                                                                    else if(user.getPermission().equals("manager+barber"))
                                                                                                                    {
                                                                                                                        barberUserDoc.update("permission", "manager");
                                                                                                                        barberUserDoc.update("barberId", FieldValue.delete());
                                                                                                                    }
                                                                                                                    alertDialog.dismiss();
                                                                                                                    Toast.makeText(getActivity(), "הספר הוסר בהצלחה!", Toast.LENGTH_SHORT).show();
                                                                                                                    getActivity().onBackPressed();
                                                                                                                }
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alertBuilder.show();
                        }
                    });
                } else {
                    removeBarberBtn.setEnabled(false);
                    cardView_linear.setVisibility(View.GONE);
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