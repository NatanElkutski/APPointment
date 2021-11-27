package com.example.appointment.Management;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.appointment.Model.Barber;
import com.example.appointment.Model.Salon;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class AdminAddEmployeeFragment extends Fragment {

    EditText employeeName, employeeMinWork;
    Button addEmployeeBtn;
    String previousPermission,chosenEmail;

    SearchableSpinner employeeEmail;

    List<String> salonList = new ArrayList<>();
    List<String> salonIdList = new ArrayList<>();

    List<String> cityList = new ArrayList<>();

    List<String> newList = new ArrayList<>();
    List<User> usersList = new ArrayList<>();

    String chosenSalonId, chosenCity,chosenSalonName;

    MaterialSpinner spinnerCity, spinnerSalon;

    LinearLayout employeeFormLL;

    AlertDialog dialog;

    public AdminAddEmployeeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_admin_add_employee, container, false);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        employeeEmail = itemView.findViewById(R.id.admin_add_employee_searchable_spinner);
        employeeEmail.setPopupBackgroundResource(R.drawable.barbers_spinner_dropdown_background);
        employeeName = itemView.findViewById(R.id.admin_employee_name);
        employeeMinWork = itemView.findViewById(R.id.admin_employee_minwork);
        addEmployeeBtn = itemView.findViewById(R.id.admin_add_employee_accept_btn);
        employeeFormLL = itemView.findViewById(R.id.admin_add_employee_form_ll);
        spinnerCity = itemView.findViewById(R.id.admin_add_employee_city_spinner);
        spinnerSalon = itemView.findViewById(R.id.admin_add_employee_business_spinner);
        addEmployeeBtn.setEnabled(false);
        dialog.show();
        loadCity();
        setSearchableSpinner();

        employeeEmail.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position>0)
                {
                    chosenEmail = newList.get(position);
                    addEmployeeBtn.setEnabled(true);
                }
                else
                {
                    chosenEmail = "";
                    addEmployeeBtn.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                chosenEmail = "";
            }
        });

        return itemView;
    }

    private void setSearchableSpinner() {
        CollectionReference AllUsers = FirebaseFirestore.getInstance().collection("User");
        AllUsers.whereEqualTo("permission", "user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    employeeEmail.setTitle("בחר את המנהל הסלון");

                    newList.add("בחר משתמש");
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        for (QueryDocumentSnapshot qds : qs) {
                            User user = qds.toObject(User.class);
                            usersList.add(user);
                            newList.add(user.getEmail());
                        }
                    }
                    employeeEmail.setPositiveButton("סגור");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.search_item, newList);
                    employeeEmail.setAdapter(adapter);
                    dialog.dismiss();
                }
            }
        });


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addEmployeeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String employeeName_txt, employeeEmail_txt, employeeMinWork_txt;
                employeeName_txt = employeeName.getText().toString().trim();
                employeeMinWork_txt = employeeMinWork.getText().toString().trim();

                if (TextUtils.isEmpty(employeeName_txt)) {
                    employeeName.setError("שדה זה אינו יכול להיות ריק!");
                    return;
                }

                if (TextUtils.isEmpty(employeeMinWork_txt)) {
                    employeeMinWork.setError("שדה זה אינו יכול להיות ריק!");
                    return;
                }
                int minWork = Integer.parseInt(employeeMinWork_txt);
                if(minWork>120 || minWork<=0)
                {
                    employeeMinWork.setError("זמן העבודה אינו יכול לעלות על 120 דק או להיות קטן או שווה ל0");
                    return;
                }
                // Add Barber to Salon
                CollectionReference employeeEmailRef = FirebaseFirestore.getInstance()
                        .collection("User");

                Query query = employeeEmailRef.whereEqualTo("email", chosenEmail);

                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot qSD : qs) {
                                    String userDocId = qSD.getId();
                                    String pemission = qSD.get("permission").toString();
                                    if (pemission.equals("user") || pemission.equals("manager")) {

                                        previousPermission = pemission;

                                        CollectionReference salonIdRef = FirebaseFirestore.getInstance()
                                                .collection("AllSalon")
                                                .document(chosenCity)
                                                .collection("Branch")
                                                .document(chosenSalonId)
                                                .collection("Barbers");

                                        Barber barber = new Barber(employeeName_txt,  minWork, chosenEmail, userDocId);

                                        salonIdRef.add(barber).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    Query query1 = salonIdRef.whereEqualTo("email", chosenEmail);

                                                    query1.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                QuerySnapshot barberEmailQS = task.getResult();
                                                                if (!barberEmailQS.isEmpty()) {
                                                                    for (QueryDocumentSnapshot baberEmailDOC : barberEmailQS) {
                                                                        String barberIdInSalon = baberEmailDOC.getId();
                                                                        DocumentReference userDocRef = FirebaseFirestore.getInstance()
                                                                                .collection("User").document(userDocId);

                                                                        if(pemission.equals("user")) {
                                                                            userDocRef.update("permission", "barber");
                                                                            userDocRef.update("salonId", chosenSalonId);
                                                                            userDocRef.update("barberId", barberIdInSalon); // <------------- ?? NOPE
                                                                            userDocRef.update("salonCity", chosenCity);
                                                                            userDocRef.update("salonName", chosenSalonName);
                                                                        }
                                                                        else
                                                                        {
                                                                            userDocRef.update("barberId",barberIdInSalon);
                                                                            userDocRef.update("permission","manager+barber");
                                                                        }
                                                                        Toast.makeText(getActivity(), "המשתמש הוסף כספר חדש במספרה בהצלחה!", Toast.LENGTH_SHORT).show();
                                                                        getActivity().onBackPressed();
                                                                    }


                                                                }
                                                            }
                                                        }
                                                    });
                                                }


                                            }
                                        });


                                    } else {
                                        Toast.makeText(getActivity(), "המשתמש כבר רשום במערכת כעובד!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "כתובת הדוא''ל אינה קיימת.\n אנא בדוק את כתובת הדוא''ל ונסה שוב.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

            }
        });
    }


    private void loadCity() {

        CollectionReference cityRef = FirebaseFirestore.getInstance()
                .collection("AllSalon");

        cityRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            cityList = new ArrayList<>();
                            QuerySnapshot qs = task.getResult();
                            cityList.add("בחר עיר");
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot cityQDoc : qs) {
                                    cityList.add(cityQDoc.getId());
                                }

                            }
                        }
                        cityLoadSuccess();
                    }
                });
    }

    private void cityLoadSuccess() {
        spinnerCity.setItems(cityList);

        spinnerCity.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if(position>0){
                    spinnerSalon.setEnabled(true);
                    chosenCity = cityList.get(position);
                    loadSalon();

                }
                else{
                    employeeFormLL.setVisibility(View.GONE);
                    addEmployeeBtn.setEnabled(false);
                    spinnerSalon.setEnabled(false);
                    spinnerSalon.setItems("בחר סלון");
                }
            }
        });

    }

    private void loadSalon(){
        Log.d("STAM KOTERET", chosenCity+"");
        CollectionReference salonRef = FirebaseFirestore.getInstance()
                .collection("AllSalon").document(chosenCity).collection("Branch");

        salonRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    salonList = new ArrayList<>();
                    salonIdList = new ArrayList<>();
                    salonList.add("בחר סלון");
                    if (!qs.isEmpty()) {
                        for (QueryDocumentSnapshot salonQDoc : qs) {
                            Salon salon = salonQDoc.toObject(Salon.class);
                            salonList.add(salon.getName());
                            salonIdList.add(salonQDoc.getId());
                        }

                    }
                    loadSalonSuccess();
                }
            }
        });


    }

    private void loadSalonSuccess(){
        spinnerSalon.setItems(salonList);

        spinnerSalon.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if(position>0){
                    employeeFormLL.setVisibility(View.VISIBLE);
                    chosenSalonId = salonIdList.get(position-1);
                    chosenSalonName = salonList.get(position);
                }

                else{
                    employeeFormLL.setVisibility(View.GONE);
                    addEmployeeBtn.setEnabled(false);
                }
            }
        });

    }
}