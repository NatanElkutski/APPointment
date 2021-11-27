package com.example.appointment.Service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;

public class HomeFragment extends Fragment {

    CardView bookingCV, historyCV, futureMeetingsCV, watchMeetingsCV, userInfoCV, contactCV, managementCv;
    AlertDialog dialog;
    LinearLayout barberLinear, ll_appointments, ll_manganment;
    TextView notVerifiedTv, helloUsernameTV;
    ProgressDialog pDialog;
    NavigationView navigationView;
    LottieAnimationView lottieAnimationView;
    TextClock tc;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public HomeFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        adjustFontScale(getResources().getConfiguration());
        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
        View itemView = inflater.inflate(R.layout.fragment_home, container, false);
        navigationView = itemView.findViewById(R.id.navigationView);
        tc = itemView.findViewById(R.id.textClock_id);
        return itemView;
    }

    public void adjustFontScale(Configuration configuration) {
        configuration.fontScale = (float) 1.0;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pDialog = new ProgressDialog(getActivity(), R.style.MyDialogTheme);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("טוען נתונים מחדש");
        pDialog.setMessage("אנא המתן...");
        pDialog.setCancelable(false);
        pDialog.setInverseBackgroundForced(false);
        pDialog.setIcon(R.mipmap.ic_launcher);
        notVerifiedTv = view.findViewById(R.id.not_verified_tv_id);
        barberLinear = view.findViewById(R.id.barber_linearLayout);
        ll_appointments = view.findViewById(R.id.linearLayout_appointments);
        ll_manganment = view.findViewById(R.id.main_managment_ll);
        ll_manganment.setVisibility(View.GONE);
        tc.setTimeZone("Asia/Jerusalem");
        tc.setFormat12Hour(null);
        tc.setFormat24Hour("EEEE, d MMMM yyyy, HH:mm");

        bookingCV = view.findViewById(R.id.card_view_booking);
        historyCV = view.findViewById(R.id.card_view_history);
        futureMeetingsCV = view.findViewById(R.id.card_view_future);
        futureMeetingsCV.setAlpha(1);
        watchMeetingsCV = view.findViewById(R.id.schedule_cardView);
        userInfoCV = view.findViewById(R.id.account_box);
        contactCV = view.findViewById(R.id.contact_cardView);
        helloUsernameTV = view.findViewById(R.id.hello_username_tv);
        lottieAnimationView = view.findViewById(R.id.hello_user_animation);


        // Barber Linearlayout:
        managementCv = view.findViewById(R.id.management_cardView);

        if(Common.currentUser.getPermission().equals("admin"))
        {
            watchMeetingsCV.setVisibility(View.GONE);
        }

        if (!Common.fuser.isEmailVerified()) {
            notVerifiedTv.setVisibility(View.VISIBLE);
            ll_appointments.setVisibility(View.GONE);
            contactCV.setVisibility(View.GONE);
        }

        if (Common.currentUser != null) {
            if (!Common.currentUser.getPermission().equals("user")) {
                ll_manganment.setVisibility(View.VISIBLE);
            }
        }


        bookingCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.bookingFragment);
            }
        });

        historyCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.historyFragment);

            }
        });


        futureMeetingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.activeFragment);
            }
        });
        watchMeetingsCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.currentUser.getPermission().equals("barber"))
                    Navigation.findNavController(view).navigate(R.id.TimeFragment);
                else if (Common.currentUser.getPermission().equals("manager") || Common.currentUser.getPermission().equals("manager+barber"))
                    Navigation.findNavController(view).navigate(R.id.managerTimeFragment);
            }
        });
        userInfoCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.UserInformationFragment);
            }
        });
        contactCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.contactFragment);
            }
        });

        managementCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.management);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        greetingsUser();

        pDialog.show();
        Common.fragmentPage = 1;
        ((MainActivity) getActivity()).changeNav();
        if (!Common.fuser.isEmailVerified()) {
            Common.fuser.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        if (Common.fuser.isEmailVerified()) {
                            pDialog.dismiss();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                        }
                    }
                }
            });
        }


        Common.fuser.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DocumentReference registeredTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid()); // Current

                registeredTokenRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot regTokSS = task.getResult();
                            String regTok = regTokSS.get("token").toString();

                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (task.isSuccessful()) {
                                                String curTok = task.getResult().getToken();
                                                if (!regTok.equals(curTok)) {
                                                    pDialog.dismiss();
                                                    mAuth.signOut();
                                                    startActivity(new Intent(getContext(), LoginActivity.class));
                                                } else {
                                                    //Check if user changed email, if user did change update on FB
                                                    DocumentReference userDoc = FirebaseFirestore.getInstance()
                                                            .collection("User").document(Common.fuser.getUid());
                                                    if (!Common.fuser.getEmail().equals(Common.currentUser.getEmail())) {
                                                        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    userDoc.update("email", Common.fuser.getEmail()).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                    DocumentSnapshot ds = task.getResult();
                                                                    User user = ds.toObject(User.class);
                                                                    Common.currentUser = user;
                                                                    try {
                                                                        updateBookings();
                                                                    } catch (ParseException e) {
                                                                        pDialog.dismiss();
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot ds = task.getResult();
                                                                    User user = ds.toObject(User.class);
                                                                    Common.currentUser = user;
                                                                    try {
                                                                        updateBookings();
                                                                    } catch (ParseException e) {
                                                                        pDialog.dismiss();
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });
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

    private void updateBookings() throws ParseException {

        Timestamp timestamp = Common.makeTimeStampMessage(Common.currentTimeWithDate());
        Log.d("TIME STAMP ", timestamp.toDate().toString());
        if (Common.currentUser.getPermission().equals("manager"))
            updateHistoryList(timestamp);
        else if (Common.currentUser.getPermission().equals("barber") || Common.currentUser.getPermission().equals("manager+barber"))
            updateBarberBookingsDoneState(timestamp);
        else updateHistoryList(timestamp);
    }

    public void updateHistoryList(Timestamp timestamp) {

        CollectionReference userBookingRef = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Booking");

        Query query = userBookingRef.whereLessThan("timestamp", timestamp);
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) ;
                else {
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        String Docid = doc.getDocument().getId();
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Booking").document(Docid);
                        ref.update("done", true);
                    }
                }
                if (pDialog.isShowing()) pDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pDialog.dismiss();
            }
        });
    }

    public void updateBarberBookingsDoneState(Timestamp timestamp) throws ParseException {

        DocumentReference futureBookingsUpdateRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.currentUser.getSalonCity())
                .collection("Branch")
                .document(Common.currentUser.getSalonId())
                .collection("Barbers")
                .document(Common.currentUser.getBarberId())
                .collection("Bookings").document("FutureBookings");

        futureBookingsUpdateRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot ds = task.getResult();
                    if (ds.exists()) { // If FutureBookings Document Exists
                        if (timestamp != null) {
                            List<String> list = new ArrayList<>();
                            Calendar todayDate = Calendar.getInstance();
                            todayDate.set(Calendar.MINUTE, 0);
                            todayDate.set(Calendar.HOUR, 0);
                            Map<String, Object> map = task.getResult().getData();
                            for (Map.Entry<String, Object> entry : map.entrySet()) { // Receive FutureBookings Document Fields to get all the existing dates till today (Including Today)
                                try {
                                    Date date = Common.simpleDateFormat.parse(entry.getKey());
                                    Calendar listDate = Calendar.getInstance();
                                    listDate.setTime(date);
                                    if (listDate.getTimeInMillis() <= todayDate.getTimeInMillis()) {
                                        list.add(entry.getKey());
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (list.size() > 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    int finalI = i;
                                    CollectionReference futureBookingsDateColRef = FirebaseFirestore.getInstance()
                                            .collection("AllSalon")
                                            .document(Common.currentUser.getSalonCity())
                                            .collection("Branch")
                                            .document(Common.currentUser.getSalonId())
                                            .collection("Barbers")
                                            .document(Common.currentUser.getBarberId())
                                            .collection("Bookings").document("FutureBookings").collection(list.get(finalI));

                                    futureBookingsDateColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {

                                                    QuerySnapshot qs = task.getResult();
                                                    if (!qs.isEmpty()) {
                                                        final int size = qs.size(); // save the collection size to check if its the last document in the date collection to remove the date from FutureBookings document field
                                                        Query query = futureBookingsDateColRef.whereLessThan("timestamp", timestamp);
                                                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    QuerySnapshot qs = task.getResult();
                                                                    if (!qs.isEmpty()) {

                                                                        DocumentReference PastBookingExtRef = FirebaseFirestore.getInstance()
                                                                                .collection("AllSalon")
                                                                                .document(Common.currentUser.getSalonCity())
                                                                                .collection("Branch")
                                                                                .document(Common.currentUser.getSalonId())
                                                                                .collection("Barbers")
                                                                                .document(Common.currentUser.getBarberId())
                                                                                .collection("Bookings")
                                                                                .document("PastBookings");

                                                                        PastBookingExtRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    DocumentSnapshot PastBookDS = task.getResult();
                                                                                    if (PastBookDS.exists()) { // If PastBookings Document Exists so update
                                                                                        PastBookingExtRef.update(list.get(finalI), "");
                                                                                    } else { // If PastBookings Document Not Exists set
                                                                                        HashMap<String, Object> dayMap = new HashMap<>();
                                                                                        dayMap.put(list.get(finalI), "");
                                                                                        PastBookingExtRef.set(dayMap);
                                                                                    }
                                                                                    for (QueryDocumentSnapshot qDs : qs) {

                                                                                        String DocId = qDs.getId();
                                                                                        BookingInformation bk = qDs.toObject(BookingInformation.class);

                                                                                        DocumentReference BookingPastDoc = FirebaseFirestore.getInstance()
                                                                                                .collection("AllSalon")
                                                                                                .document(Common.currentUser.getSalonCity())
                                                                                                .collection("Branch")
                                                                                                .document(Common.currentUser.getSalonId())
                                                                                                .collection("Barbers")
                                                                                                .document(Common.currentUser.getBarberId())
                                                                                                .collection("Bookings").document("PastBookings").collection(list.get(finalI)).document(DocId);

                                                                                        bk.setDone(true);
                                                                                        BookingPastDoc.set(bk).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    if (size == 1)
                                                                                                        futureBookingsUpdateRef.update(list.get(finalI), FieldValue.delete());

                                                                                                    DocumentReference BookingDateDoc = FirebaseFirestore.getInstance()
                                                                                                            .collection("AllSalon")
                                                                                                            .document(Common.currentUser.getSalonCity())
                                                                                                            .collection("Branch")
                                                                                                            .document(Common.currentUser.getSalonId())
                                                                                                            .collection("Barbers")
                                                                                                            .document(Common.currentUser.getBarberId())
                                                                                                            .collection("Bookings").document("FutureBookings").collection(list.get(finalI)).document(DocId);
                                                                                                    BookingDateDoc.delete();
                                                                                                    updateHistoryList(timestamp);
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        updateHistoryList(timestamp);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        updateHistoryList(timestamp);
                                                        futureBookingsUpdateRef.update(list.get(finalI), FieldValue.delete());
                                                    }
                                            }

                                        }
                                    });
                                }
                            } else {
                                updateHistoryList(timestamp);
                            }
                        } else {
                            pDialog.dismiss();
                            Toast.makeText(getActivity(), "TimeStamp is Null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        updateHistoryList(timestamp);
                    }
                    updateHistoryList(timestamp);
                }
            }
        });
    }

    public void greetingsUser() {

        Calendar todayDate = Calendar.getInstance();
        SimpleDateFormat sd = new SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm");
        String today = tc.getText().toString().trim();
        try {
            todayDate.setTime(sd.parse(today));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 4);
        morning.set(Calendar.MINUTE, 59);
        morning.set(Calendar.SECOND, 59);

        Calendar day = Calendar.getInstance();
        day.set(Calendar.HOUR_OF_DAY, 11);
        day.set(Calendar.MINUTE, 59);
        day.set(Calendar.SECOND, 59);

        Calendar evening = Calendar.getInstance();
        evening.set(Calendar.HOUR_OF_DAY, 15);
        evening.set(Calendar.MINUTE, 59);
        evening.set(Calendar.SECOND, 59);

        Calendar night = Calendar.getInstance();
        night.set(Calendar.HOUR_OF_DAY, 20);
        night.set(Calendar.MINUTE, 59);
        night.set(Calendar.SECOND, 59);

        String[] fullname = Common.currentUser.getName().split(" ");
        String firstname = fullname[0];


        if (todayDate.getTime().after(morning.getTime()) && todayDate.getTime().before(day.getTime())) {
            helloUsernameTV.setText("בוקר טוב " + firstname);
            lottieAnimationView.setAnimation(R.raw.clearsky);
        } else if (todayDate.getTime().after(day.getTime()) && todayDate.getTime().before(evening.getTime())) {
            helloUsernameTV.setText("צהריים טובים " + firstname);
            lottieAnimationView.setAnimation(R.raw.clearsky);
        } else if (todayDate.getTime().after(evening.getTime()) && todayDate.getTime().before(night.getTime())) {
            helloUsernameTV.setText("ערב טוב " + firstname);
            lottieAnimationView.setAnimation(R.raw.fewclouds);
        } else {
            helloUsernameTV.setText("לילה טוב " + firstname);
            lottieAnimationView.setAnimation(R.raw.nightclearsky);
        }
    }

}