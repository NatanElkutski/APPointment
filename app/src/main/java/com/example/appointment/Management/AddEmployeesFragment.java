package com.example.appointment.Management;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.Barber;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


public class AddEmployeesFragment extends Fragment {

    EditText employeeName, employeeEmail, employeeMinWork;
    Button addEmployeeBtn;
    String previousPermission;

    public AddEmployeesFragment() {
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
        View itemView = inflater.inflate(R.layout.fragment_add_employees, container, false);
        employeeEmail = itemView.findViewById(R.id.employee_email);
        employeeName = itemView.findViewById(R.id.employee_name);
        employeeMinWork = itemView.findViewById(R.id.employee_minwork);
        addEmployeeBtn = itemView.findViewById(R.id.add_employee_accept_btn);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addEmployeeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String employeeName_txt, employeeEmail_txt, employeeMinWork_txt;
                employeeEmail_txt = employeeEmail.getText().toString().trim();
                employeeName_txt = employeeName.getText().toString().trim();
                employeeMinWork_txt = employeeMinWork.getText().toString().trim();


                if (TextUtils.isEmpty(employeeEmail_txt)) {
                    employeeEmail.setError("שדה זה אינו יכול להיות ריק!");
                    return;
                }

                if (TextUtils.isEmpty(employeeName_txt)) {
                    employeeName.setError("שדה זה אינו יכול להיות ריק!");
                    return;
                }

                if (TextUtils.isEmpty(employeeMinWork_txt)) {
                    employeeMinWork.setError("שדה זה אינו יכול להיות ריק!");
                    return;
                }
                int minWork = Integer.parseInt(employeeMinWork_txt);
                if(minWork>120)
                {
                    employeeMinWork.setError("זמן העבודה אינו יכול לעלות על 120 דק");
                    return;
                }
                // Add Barber to Salon
                CollectionReference employeeEmailRef = FirebaseFirestore.getInstance()
                        .collection("User");

                Query query = employeeEmailRef.whereEqualTo("email", employeeEmail_txt);

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
                                                .document(Common.currentUser.getSalonCity())
                                                .collection("Branch")
                                                .document(Common.currentUser.getSalonId())
                                                .collection("Barbers");

                                        Barber barber = new Barber(employeeName_txt, minWork, employeeEmail_txt, userDocId);

                                        salonIdRef.add(barber).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    Query query1 = salonIdRef.whereEqualTo("email", employeeEmail_txt);

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
                                                                            userDocRef.update("salonId", Common.currentUser.getSalonId());
                                                                            userDocRef.update("barberId", barberIdInSalon);
                                                                            userDocRef.update("salonCity", Common.currentUser.getSalonCity());
                                                                            userDocRef.update("salonName", Common.currentUser.getSalonName());
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

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity) getActivity()).changeNav();
    }
}