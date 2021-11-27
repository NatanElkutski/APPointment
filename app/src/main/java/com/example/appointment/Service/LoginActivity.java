package com.example.appointment.Service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.chaos.view.PinView;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.User;
import com.example.appointment.Model.profileImage;
import com.example.appointment.R;

import com.example.appointment.SignActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    // Variables

    LinearLayout passwordll;
    LottieAnimationView lottieAnimationView;
    TextView forgotPass;
    EditText mEmail, mPassword;
    Button mLoginBtn, signWithPhonBtn;
    Dialog mdialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ProgressDialog dialog;
    PinView pinView;
    String verificationCodeBySystem;
    InputMethodManager imm;
    boolean logged = true;
    profileImage profileImage;
    DocumentReference useref;
    float password_animation_progress = 0.5f;

    public void adjustFontScale(Configuration configuration) {
        configuration.fontScale = (float) 1.0;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getBaseContext().getSystemService(getBaseContext().WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(getResources().getConfiguration());
        setContentView(R.layout.activity_login);
        profileImage = new profileImage();
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("התחברות");
        dialog.setMessage("אנא המתן...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setIcon(R.mipmap.ic_launcher);
        forgotPass = findViewById(R.id.forgot_password);
        signWithPhonBtn = findViewById(R.id.sign_in_with_phone_btn);
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_PHONE_STATE);

        mEmail = findViewById(R.id.username);
        mEmail.setTextColor(Color.parseColor("white"));
        mPassword = findViewById(R.id.password);
        mPassword.setTextColor(Color.parseColor("white"));
        mLoginBtn = findViewById(R.id.login_now);
        passwordll = findViewById(R.id.login_password_ll);
        lottieAnimationView = findViewById(R.id.password_eye_animation);

        lottieAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password_animation_progress == 0.5f) {
                    lottieAnimationView.setMinAndMaxProgress(0.5f, 1f);
                    password_animation_progress = 1f;
                    mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                } else {
                    lottieAnimationView.setMinAndMaxProgress(0f, 0.5f);
                    password_animation_progress = 0.5f;
                    mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                mPassword.setSelection(mPassword.length());
                lottieAnimationView.playAnimation();
            }
        });

        mEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEmail.setBackgroundResource(R.drawable.edittext_selected_stroke);
                } else {
                    mEmail.setBackgroundResource(R.drawable.et_bg);
                }
            }
        });
        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    passwordll.setBackgroundResource(R.drawable.linear_layout_edittext_selected);
                } else {
                    passwordll.setBackgroundResource(R.drawable.linear_layout_edit_text_bg);
                }
            }
        });


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String edemail = mEmail.getText().toString().trim();
                String edpassword = mPassword.getText().toString().trim();
                if (TextUtils.isEmpty(edemail)) {
                    mEmail.setError("Email is required.");
                    return;
                }
                if (TextUtils.isEmpty(edpassword)) {
                    mPassword.setError("Password is required.");
                    return;
                }

                if (edpassword.length() < 6) {
                    mPassword.setError("Password must be >= 6 characters");
                    return;
                }
                dialog.show();

                mAuth.signInWithEmailAndPassword(edemail, edpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Common.fuser = mAuth.getCurrentUser();

                            DocumentReference registeredTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid()); // Current

                            registeredTokenRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        DocumentSnapshot regTokenSS = task.getResult();
                                        String registeredToken = regTokenSS.get("token").toString();

                                        FirebaseInstanceId.getInstance().getInstanceId()
                                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                        if (task.isSuccessful()) {
                                                            String currentToken = task.getResult().getToken();
                                                            AlertDialog dAlert = null;
                                                            dialog.dismiss();
                                                            if (!registeredToken.equals(currentToken)) {
                                                                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this, R.style.MyDialogTheme);
                                                                // add some style motherfucker
                                                                alert.setTitle("קיים חיבור נוסף");
                                                                alert.setMessage("נמצא מכשיר נוסף שמחובר למשתמש, האם לנתק אותו?");
                                                                alert.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        HashMap<String, Object> map = new HashMap<>();
                                                                        map.put("token", currentToken);
                                                                        DocumentReference userTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid());
                                                                        userTokenRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                userTokenRef.update(map);
                                                                                loginMethod();
                                                                            }
                                                                        });
                                                                    }
                                                                }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        mAuth.signOut();
                                                                        dialog.dismiss();
                                                                        logged = false;
                                                                    }
                                                                });
                                                                dAlert = alert.create();
                                                                dAlert.show();
                                                            }
                                                            else
                                                            {
                                                                loginMethod();
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                        } else {
                            dialog.dismiss();
                            mEmail.setError("Email Or Password is not correct!");
                            Toast.makeText(LoginActivity.this, "User Does not Exists or User Info is not correct!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });



        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mdialog = new Dialog(LoginActivity.this);
                mdialog.setContentView(R.layout.dialog_add_new_city);
                mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                Button dialogAccept = mdialog.findViewById(R.id.dialog_add_new_city_btn_accept_id);
                dialogAccept.setText("אישור");
                Button dialogCancel = mdialog.findViewById(R.id.dialog_add_new_city_btn_cancel_id);
                EditText dialogCityEt = mdialog.findViewById(R.id.dialog_add_new_city_cityNameid);
                TextView dialogTv = mdialog.findViewById(R.id.dialog_add_new_city_cityTextview);
                dialogTv.setText("הכנס כתובת מייל:");
                dialogCityEt.setHint("example@gmail.com");
                dialogCityEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


                dialogAccept.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String emailInput = dialogCityEt.getText().toString().trim();
                        // Empty email input leaves out.
                        if (TextUtils.isEmpty(emailInput)) {
                            Toast.makeText(LoginActivity.this, "שדה זה לא יכול להיות ריק", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference searchAfterUsers = db.collection("User");


                        // Check if user exists in database.
                        Task<QuerySnapshot> findIfUserExists_query = searchAfterUsers.whereEqualTo("email", emailInput)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if (document.get("email").equals(emailInput)) {
                                                    // Toast.makeText(LoginActivity.this, "Email was found!", Toast.LENGTH_SHORT).show();
                                                    firebaseAuth.sendPasswordResetEmail(emailInput).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(LoginActivity.this, "Password Reset was sent to email!", Toast.LENGTH_SHORT).show();
                                                            mdialog.dismiss();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(LoginActivity.this, "Failed to send Password Reset!", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    return;
                                                }
                                            }
                                            Toast.makeText(LoginActivity.this, "Email does NOT exist!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Failed to complete task", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LoginActivity.this, "Failed to check email in database!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

                dialogCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mdialog.dismiss();
                    }
                });
                mdialog.show();


            }
        });

        signWithPhonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdialog = new Dialog(LoginActivity.this);
                mdialog.setContentView(R.layout.dialog_register);
                mdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mdialog.setCancelable(false);
                EditText dialogPhone = mdialog.findViewById(R.id.dialog_phoneReg_id);
                dialogPhone.setTextColor(Color.parseColor("white"));
                pinView = mdialog.findViewById(R.id.pinview_verify);
                Button btnAccept = mdialog.findViewById(R.id.dialog_btn_accept_id);
                Button btnCancel = mdialog.findViewById(R.id.dialog_btn_cancel_id);

                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(mdialog.getContext().INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mdialog.getCurrentFocus().getWindowToken(), 0);
                        String userPhoneNumber = dialogPhone.getText().toString().trim();
                        if (TextUtils.isEmpty(userPhoneNumber) || userPhoneNumber.length() != 10) {
                            dialogPhone.requestFocus();
                            imm = (InputMethodManager) getSystemService(mdialog.getContext().INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                            dialogPhone.setError("שגיאה - נא להכניס 10 ספרות");
                            return;
                        }
                        CollectionReference cl = FirebaseFirestore.getInstance().collection("User");
                        Query query = cl.whereEqualTo("phone", userPhoneNumber);
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size() > 0) {
                                        sendVerificationCodeToUser(userPhoneNumber);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "מספר זה לא נמצא במערכת", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogPhone.requestFocus();
                        imm = (InputMethodManager) getSystemService(mdialog.getContext().INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        mdialog.dismiss();
                    }
                });
                dialogPhone.requestFocus();
                imm = (InputMethodManager) getSystemService(mdialog.getContext().INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                mdialog.setOnShowListener(new DialogInterface.OnShowListener() {
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

                                    verifyCode(codeByUser);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });
                    }
                });

                mdialog.show();
            }
        });

    }

    public void loadProfilePicture() {
        Common.profilePicture = "";
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Uploads").document("profileImage");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        profileImage image = document.toObject(profileImage.class);
                        profileImage.setImageUrl(image.getImageUrl());
                        Common.profilePicture = profileImage.getImageUrl();
                    }
                    useref.update("isLogged", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra(Common.IS_LOGIN, true);
                            if (dialog.isShowing()) dialog.dismiss();
                            startActivity(intent);
                            finish();
                        }
                    });

                } else {
                    if (dialog.isShowing()) dialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Load Profile Picture Task Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loginMethod()
    {
        useref = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
        useref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userSnapShot = task.getResult();
                    User user = userSnapShot.toObject(User.class);
                    Common.currentUser = user;
                    if (Common.currentUser.getRegStep() == 1) {
                        Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                        finish();
                    } else {
                        userMessagesFromDb(Common.fuser.getUid());
                    }
                }
            }
        });
    }

    public void userMessagesFromDb(String userUid) {
        CollectionReference colMessagesRef = FirebaseFirestore.getInstance()
                .collection("User")
                .document(userUid)
                .collection("Messages");

        Query query = colMessagesRef.whereEqualTo("seen", false);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    if (!qs.isEmpty()) {
                        Common.seenMessages = qs.size();
                    }
                    loadProfilePicture();
                } else {
                    Toast.makeText(LoginActivity.this, "User Message to Database Task Error", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    public void sendVerificationCodeToUser(String phone) {
        String area = "+972";
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(area+phone)       // Phone number to verify
                        .setTimeout(120L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallback)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    public PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
        }

        @Override
        public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(id, forceResendingToken);
            verificationCodeBySystem = id;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                pinView.setText(code);
                verifyCode(pinView.getText().toString());
                Toast.makeText(getApplicationContext(), "" + code, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void verifyCode(String codeByUser) {


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);

    }

    public void signInTheUserByCredentials(PhoneAuthCredential credential) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Common.fuser = mAuth.getCurrentUser();
                            useref = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
                            useref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot ds = task.getResult();
                                        if (ds.exists()) {
                                            User user = ds.toObject(User.class);
                                                Common.currentUser = user;
                                                FirebaseInstanceId.getInstance().getInstanceId()
                                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                                if (task.isSuccessful()) {
                                                                    // Get new Instance ID token
                                                                    String token = task.getResult().getToken();
                                                                    HashMap<String, Object> map = new HashMap<>();
                                                                    map.put("token", token);
                                                                    DocumentReference userTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid());
                                                                    userTokenRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            if (Common.currentUser.getRegStep() == 1) {
                                                                                Intent intent = new Intent(LoginActivity.this, RegActivity.class);
                                                                                startActivity(intent);
                                                                                finish();

                                                                            } else {
                                                                                userMessagesFromDb(Common.fuser.getUid());
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    Log.w("TAG", "getInstanceId failed", task.getException());
                                                                    return;
                                                                }
                                                            }
                                                        });

                                        }
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pinView.setError(""+e);
                Log.d("Error", ""+e);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void SignUp(View v) {
        Intent i = new Intent(LoginActivity.this, SignActivity.class);
        startActivity(i);
    }



}