package com.example.appointment.Management;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.Salon;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;
import com.example.appointment.Service.UploadActivity;
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
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.toptoche.searchablespinnerlibrary.SearchableListDialog;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AdminAddSalonFragment extends Fragment {

    EditText salonNameEt, salonManagerEt, salonAddressEt, salonPhoneEt, salonWebsiteEt;
    TextView salonWorkingHoursEt1, salonWorkingHoursEt2;
    String nameSalon, managerSalon, addressSalon, workingHoursSalon1, workingHoursSalon2, phoneSalon, websiteSalon;
    MaterialSpinner spinner;
    SearchableSpinner s_spinner;
    List<String> list;
    Button addSalonBtn;
    String choosenSalonId;
    CollectionReference salonManagerRef;
    Dialog mDialog;
    AlertDialog dialog;
    TimePickerDialog timePickerDialog;
    List<String> newList = new ArrayList<>();
    List<User> usersList = new ArrayList<>();
    String choosenEmail;

    public AdminAddSalonFragment() {
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

        View itemView = inflater.inflate(R.layout.fragment_admin_add_salon, container, false);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        spinner = itemView.findViewById(R.id.admin_add_salon_spinner);
        addSalonBtn = itemView.findViewById(R.id.admin_add_salon_add_salon_btn);
        salonNameEt = itemView.findViewById(R.id.admin_add_salon_salonName);
        s_spinner = itemView.findViewById(R.id.searchable_spinner);
        s_spinner.setPopupBackgroundResource(R.drawable.barbers_spinner_dropdown_background);
        //salonManagerEt = itemView.findViewById(R.id.admin_add_salon_salonManagerUid);
        salonAddressEt = itemView.findViewById(R.id.admin_add_salon_salonAddress);
        salonWorkingHoursEt1 = itemView.findViewById(R.id.admin_add_salon_salonWorkingHours1);
        salonWorkingHoursEt2 = itemView.findViewById(R.id.admin_add_salon_salonWorkingHours2);
        salonPhoneEt = itemView.findViewById(R.id.admin_add_salon_salonPhone);
        salonWebsiteEt = itemView.findViewById(R.id.admin_add_salon_salonWebsite);
        spinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.barbers_spinner_dropdown_background));
        addSalonBtn.setEnabled(false);
        dialog.show();
        setSearchableSpinner();
        loadSalonBarbers();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        Calendar calendar = Calendar.getInstance();

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);



        salonWorkingHoursEt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10) {
                            salonWorkingHoursEt2.setText("0" + hourOfDay + ":" + "0" + minute);
                        } else if (minute < 10) {
                            salonWorkingHoursEt2.setText(hourOfDay + ":0" + minute);
                            getView().clearFocus();
                        } else if (hourOfDay < 10) {
                            salonWorkingHoursEt2.setText("0" + hourOfDay + ":" + minute);
                            getView().clearFocus();
                        } else {
                            salonWorkingHoursEt2.setText(hourOfDay + ":" + minute);
                            getView().clearFocus();
                        }
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });

        salonWorkingHoursEt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (hourOfDay < 10 && minute < 10)
                            salonWorkingHoursEt1.setText("0" + hourOfDay + ":" + "0" + minute);
                        else if (minute < 10)
                            salonWorkingHoursEt1.setText(hourOfDay + ":0" + minute);
                        else if (hourOfDay < 10)
                            salonWorkingHoursEt1.setText("0" + hourOfDay + ":" + minute);
                        else salonWorkingHoursEt1.setText(hourOfDay + ":" + minute);
                        salonWorkingHoursEt2.callOnClick();
                    }
                }, hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });


        addSalonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameSalon = salonNameEt.getText().toString().trim();
                managerSalon = "";//salonManagerEt.getText().toString().trim();
                addressSalon = salonAddressEt.getText().toString().trim();
                workingHoursSalon1 = salonWorkingHoursEt1.getText().toString().trim();
                workingHoursSalon2 = salonWorkingHoursEt2.getText().toString().trim();
                phoneSalon = salonPhoneEt.getText().toString().trim();
                websiteSalon = salonWebsiteEt.getText().toString().trim();
                String OpenHours = workingHoursSalon1 + " - " + workingHoursSalon2;


                if (TextUtils.isEmpty(nameSalon)) {
                    salonNameEt.setError("שדה זה לא יכול להיות ריק!");
                    return;
                }
                if (TextUtils.isEmpty(addressSalon)) {
                    salonAddressEt.setError("שדה זה לא יכול להיות ריק!");
                    return;
                }
                if (TextUtils.isEmpty(workingHoursSalon1)) {
                    salonWorkingHoursEt1.setError("שדה זה לא יכול להיות ריק!");
                    return;
                }
                if (TextUtils.isEmpty(workingHoursSalon2)) {
                    salonWorkingHoursEt2.setError("שדה זה לא יכול להיות ריק!");
                    return;
                }
                String[] s1 = workingHoursSalon1.split(":");
                String[] s2 = workingHoursSalon2.split(":");
                int temp1 = Integer.parseInt(s1[0] + s1[1]);
                int temp2 = Integer.parseInt(s2[0] + s2[1]);
                if (temp1 > temp2) {
                    salonWorkingHoursEt1.setError("שעת פתיחה לא יכולה להיות יותר גדולה משעת סגירה!");
                    return;
                }
                if (TextUtils.isEmpty(phoneSalon)) {
                    salonPhoneEt.setError("שדה זה לא יכול להיות ריק");
                }
                if (TextUtils.isEmpty(websiteSalon)) {
                    websiteSalon = "";
                }
                if(TextUtils.isEmpty(choosenEmail))
                {
                    Toast.makeText(context, "לא נבחר מנהל המספרה.", Toast.LENGTH_SHORT).show();
                    return;
                }

                CollectionReference salonIdCol = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(choosenSalonId)
                        .collection("Branch");

                salonIdCol.whereEqualTo("name", nameSalon).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            if (qs.isEmpty()) {
                                salonManagerRef = FirebaseFirestore.getInstance()
                                        .collection("User");

                                salonManagerRef.whereEqualTo("email", choosenEmail).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot managerQs = task.getResult();
                                            if (!managerQs.isEmpty()) {
                                                String managerUID = "";
                                                for (QueryDocumentSnapshot managerQDS : managerQs) {
                                                    managerUID = managerQDS.getId();
                                                }
                                                HashMap<String, Object> salonInfo = new HashMap<>();
                                                salonInfo.put("name", nameSalon);
                                                salonInfo.put("address", addressSalon);
                                                salonInfo.put("managerUid", managerUID);
                                                salonInfo.put("openHours", OpenHours);
                                                salonInfo.put("phone", phoneSalon);
                                                salonInfo.put("website", websiteSalon);

                                                String finalManagerUID = managerUID;
                                                salonIdCol.add(salonInfo).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        salonIdCol.whereEqualTo("name", nameSalon).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    QuerySnapshot salonQs = task.getResult();
                                                                    String salonDocId = "";
                                                                    if (!salonQs.isEmpty()) {
                                                                        for (QueryDocumentSnapshot salonQds : salonQs) {
                                                                            salonDocId = salonQds.getId();
                                                                        }
                                                                        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("User").document(finalManagerUID);
                                                                        userDocRef.update("salonId", salonDocId);
                                                                        userDocRef.update("salonCity", choosenSalonId);
                                                                        userDocRef.update("permission", "manager");
                                                                        userDocRef.update("salonName", nameSalon);
                                                                        Toast.makeText(getActivity(), "הסלון נוצר בהצלחה!", Toast.LENGTH_SHORT).show();
                                                                        getActivity().onBackPressed();
                                                                    }
                                                                }
                                                            }
                                                        });


                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                salonManagerEt.setError("לא קיים משתמש עם הדוא''ל הזה. אנא הכנס דואל תקין ונסה שוב.");
                                                return;
                                            }
                                        }
                                    }
                                });


                            } else {
                                Toast.makeText(getActivity(), "בעיר שנבחרה כבר קיים סלון עם השם הזה.\nאנא בחר שם אחר ונסה שוב.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                });

            }
        });

        s_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>0)
                {
                    choosenEmail = newList.get(position);
                }
                else
                {
                    choosenEmail = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                choosenEmail = "";
            }
        });

    }

    private void setSearchableSpinner() {
        CollectionReference AllUsers = FirebaseFirestore.getInstance().collection("User");
        AllUsers.whereEqualTo("permission", "user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    s_spinner.setTitle("בחר את המנהל הסלון");

                    newList.add("בחר משתמש");
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        for (QueryDocumentSnapshot qds : qs) {
                            User user = qds.toObject(User.class);
                            usersList.add(user);
                            newList.add(user.getEmail());
                        }
                    }
                    s_spinner.setPositiveButton("סגור");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.search_item, newList);
                    s_spinner.setAdapter(adapter);
                    dialog.dismiss();
                }
            }
        });


    }

    private void loadSalonBarbers() {

        CollectionReference salonRef = FirebaseFirestore.getInstance()
                .collection("AllSalon");

        salonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            list = new ArrayList<>();
                            QuerySnapshot qs = task.getResult();
                            list.add("בחר עיר");
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot salonQDoc : qs) {
                                    list.add(salonQDoc.getId());
                                }
                            }
                            list.add("הוסף סלון");
                            SalonLoadSuccess();
                        }
                    }
                });
    }

    public void SalonLoadSuccess() {
        spinner.setItems(list);
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0) {
                    if (item.equals("הוסף סלון")) {
                        Toast.makeText(getActivity(), "הוסף סלון חדש", Toast.LENGTH_SHORT).show();
                        view.setSelectedIndex(0);
                        mDialog = new Dialog(getContext());
                        mDialog.setContentView(R.layout.dialog_add_new_city);
                        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Button dialogAccept = mDialog.findViewById(R.id.dialog_add_new_city_btn_accept_id);
                        Button dialogCancel = mDialog.findViewById(R.id.dialog_add_new_city_btn_cancel_id);
                        EditText dialogCityEt = mDialog.findViewById(R.id.dialog_add_new_city_cityNameid);
                        dialogAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String cityName = dialogCityEt.getText().toString().trim();
                                if (TextUtils.isEmpty(cityName)) {
                                    dialogCityEt.setError("שדה זה לא יכול להיות ריק.\nאנא הכנס עיר");
                                    return;
                                }
                                CollectionReference citiesRef = FirebaseFirestore.getInstance().collection("AllSalon");

                                citiesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            QuerySnapshot cityQS = task.getResult();
                                            if (!cityQS.isEmpty()) {
                                                for (QueryDocumentSnapshot cityQDS : cityQS) {
                                                    if (cityQDS.getId().equals(cityName)) {
                                                        Toast.makeText(getContext(), "העיר שהכנסת כבר קיימת", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                }
                                            }
                                            HashMap<String, Object> cityMap = new HashMap<>();
                                            cityMap.put("name", cityName);

                                            DocumentReference citiesDocRef = FirebaseFirestore.getInstance().collection("AllSalon").document(cityName);
                                            citiesDocRef.set(cityMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mDialog.dismiss();
                                                    Toast.makeText(getContext(), "העיר נוספה בהצלחה", Toast.LENGTH_SHORT).show();
                                                    loadSalonBarbers();
                                                    return;
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mDialog.dismiss();
                                                    loadSalonBarbers();
                                                    return;
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                        dialogCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadSalonBarbers();
                                mDialog.dismiss();
                                return;
                            }
                        });
                        mDialog.show();
                    } else {
                        choosenSalonId = list.get(position);
                        addSalonBtn.setEnabled(true);
                    }
                } else {
                    addSalonBtn.setEnabled(false);
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