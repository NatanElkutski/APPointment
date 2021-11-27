package com.example.appointment.Service;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.User;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Pattern;

public class RegActivity extends AppCompatActivity {

    Button submitBtn;
    DatePicker datePicker;
    EditText fname,lname;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_info);

        Common.fuser = mAuth.getCurrentUser();
        submitBtn = findViewById(R.id.userInfo_submit);
        datePicker = findViewById(R.id.userInfo_datepicker_id);
        fname = findViewById(R.id.userInfo_fname_id);
        lname = findViewById(R.id.userInfo_lname_id);

        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("התחברות");
        pDialog.setMessage("אנא המתן...");
        pDialog.setCancelable(false);
        pDialog.setInverseBackgroundForced(false);
        pDialog.setIcon(R.mipmap.ic_launcher);


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                String name = fname.getText().toString().trim();
                String lastname = lname.getText().toString().trim();
                if(TextUtils.isEmpty(name))
                {
                    pDialog.dismiss();
                    fname.setError("שדה זה אינו יכול להיות ריק");
                    return;
                }
                if(TextUtils.isEmpty(lastname))
                {
                    pDialog.dismiss();
                    lname.setError("שדה זה אינו יכול להיות ריק");
                    return;
                }
                if(!Pattern.matches("[a-zA-Zא-ת]+",name))
                {
                    pDialog.dismiss();
                    fname.setError("שדה זה חייב לכלול רק אותיות");
                    return;
                }
                if(!Pattern.matches("[a-zA-Zא-ת]+",lastname))
                {
                    pDialog.dismiss();
                    lname.setError("שדה זה חייב לכלול רק אותיות");
                    return;
                }
                String fullname = name+" "+lastname;
                String BirthDate;
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year =  datePicker.getYear();
                Calendar calendar = Calendar.getInstance();
                if(calendar.get(Calendar.YEAR) - year < 13)
                {
                    pDialog.dismiss();
                    Toast.makeText(RegActivity.this, "הינך חייב/ת להיות לפחות בן/ת 13", Toast.LENGTH_SHORT).show();
                    return;
                }
                calendar.set(year, month, day);
                BirthDate = Common.simpleBirthFormat.format(calendar.getTime());
                DocumentReference userRef = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            userRef.update("birthdate",BirthDate);
                            userRef.update("name",fullname);
                            userRef.update("regStep",2);
                            DocumentSnapshot ds = task.getResult();
                            User user = ds.toObject(User.class);
                            Common.currentUser = user;
                            Common.regStep++;
                            FirebaseInstanceId.getInstance().getInstanceId()
                                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w("TAG", "getInstanceId failed", task.getException());
                                                return;
                                            }

                                            // Get new Instance ID token
                                            String token = task.getResult().getToken();
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("token", token);
                                            DocumentReference userTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid());
                                            userTokenRef.set(map);
                                            Common.fuser.sendEmailVerification();
                                            pDialog.dismiss();
                                            startActivity(new Intent(RegActivity.this,MainActivity.class));
                                            finish();
                                        }
                                    });



                        }
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot Back-Press from this page! must Complete Registration First!", Toast.LENGTH_SHORT).show();
    }
}