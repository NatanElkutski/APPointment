package com.example.appointment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.User;
import com.example.appointment.Service.RegActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;

public class SignActivity extends AppCompatActivity {

    String verificationCodeBySystem,userPhoneNumber,userEmail;
    EditText mEmail, mPassword, mPassword1;
    Button mRegisterBtn,have_acc;
    Dialog dialog;
    ProgressDialog pDialog;
    AlertDialog aDialog;
    PinView pinView;
    private FirebaseAuth mAuth;
    InputMethodManager imm;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mPassword1 = findViewById(R.id.password1);
        mRegisterBtn = findViewById(R.id.submit_now);
        have_acc = findViewById(R.id.have_account);
        mAuth = FirebaseAuth.getInstance();
        aDialog = new SpotsDialog.Builder().setContext(SignActivity.this).setCancelable(false).build();
        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("התחברות");
        pDialog.setMessage("אנא המתן...");
        pDialog.setCancelable(false);
        pDialog.setInverseBackgroundForced(false);
        pDialog.setIcon(R.mipmap.ic_launcher);


        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.show();
                String email,password,password1;
                userEmail = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                password1 = mPassword1.getText().toString().trim();

                if(TextUtils.isEmpty(userEmail))
                {
                    pDialog.dismiss();
                    mEmail.setError("Email is required!");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    pDialog.dismiss();
                    mPassword.setError("Password is required and must have at least 6 digits!");
                    return;
                }
                if(TextUtils.isEmpty(password1))
                {
                    pDialog.dismiss();
                    mPassword1.setError("Password is required and must have at least 6 digits!");
                    return;
                }
                if(!password.equals(password1))
                {
                    pDialog.dismiss();
                    mPassword.setError("Passwords do not match!!");
                    return;
                }


                mAuth.createUserWithEmailAndPassword(userEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            signInWithEmail(userEmail, password);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Toast.makeText(SignActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        have_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(id, forceResendingToken);
                verificationCodeBySystem = id;
                Log.d("VERIFICATION ID: ", "onCodeSent:" + id);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(SignActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void signInWithEmail(String email,String password)
    {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    dialog = new Dialog(SignActivity.this);
                    dialog.setContentView(R.layout.dialog_register);
                    dialog.setCancelable(false);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    EditText dialogPhone = dialog.findViewById(R.id.dialog_phoneReg_id);
                    pinView = dialog.findViewById(R.id.pinview_verify);
                    Button btnAccept = dialog.findViewById(R.id.dialog_btn_accept_id);
                    Button btnCancel = dialog.findViewById(R.id.dialog_btn_cancel_id);
                    btnAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(dialog.getContext().INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), 0);
                            userPhoneNumber = dialogPhone.getText().toString().trim();
                            if(TextUtils.isEmpty(userPhoneNumber) || userPhoneNumber.length() != 10)
                            {

                                dialogPhone.setError("Phone can not be Empty and must contain 10 digits!");
                                return;
                            }
                            sendVerificationCodeToUser(userPhoneNumber);
                        }
                    });

                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(dialog.getContext().INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(), 0);
                            dialog.dismiss();
                        }
                    });


                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            pinView.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    
                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    if(charSequence.length() == 6 && pinView!=null)
                                    {
                                        Log.d("Verify by System ",""+verificationCodeBySystem);
                                        Log.d("Verify by USER ",""+pinView.getText());

                                        String codeByUser = Objects.requireNonNull(pinView.getText()).toString();
                                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
                                        linkUserAuth(credential);

                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                        }
                    });

                    pDialog.dismiss();
                    dialogPhone.requestFocus();
                    imm = (InputMethodManager)getSystemService(dialog.getContext().INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                    dialog.show();
                }
            }
        });
    }

    public void sendVerificationCodeToUser(String phone) {
        String area = "+972";
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(area+phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    private void linkUserAuth(AuthCredential credential){
        Objects.requireNonNull(mAuth.getCurrentUser()).linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d("LINK AUTHENTICATION Success!!!","linkWithCredential:success");
                    dialog.dismiss();
                    Common.fuser = task.getResult().getUser();
                    DocumentReference userRef = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                Common.regStep ++;
                                User user = new User();
                                user.setPhone(userPhoneNumber);
                                user.setEmail(userEmail);
                                user.setPermission("user");
                                user.setRegStep(Common.regStep);
                                Calendar regTime = Calendar.getInstance();
                                regTime.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                                Timestamp timestamp = new Timestamp(regTime.getTime());
                                user.setTimestamp(timestamp);
                                userRef.set(user);
                                userRef.update("isLogged",true);
                                Intent intent = new Intent(SignActivity.this, RegActivity.class);
                                startActivity(intent);
                                finish();

                            }
                        }
                    });
                }else{
                    Toast.makeText(SignActivity.this, "הקוד שהוקש שגוי", Toast.LENGTH_SHORT).show();
                    pinView.setError("הקוד שהוקש שגוי.");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(mAuth.getCurrentUser()!=null)
        {
            if(Common.regStep < 1 )mAuth.getCurrentUser().delete();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if(mAuth.getCurrentUser()!=null)
        {
            if(Common.regStep < 1 )mAuth.getCurrentUser().delete();
        }
        super.onStop();
    }






}


