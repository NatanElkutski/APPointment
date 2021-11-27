package com.example.appointment.Service;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;


import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;


import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import dmax.dialog.SpotsDialog;


public class UserInformationFragment extends Fragment {

    TextView userFullNametv, userEmailtv, userPhonetv, userBDatetv;
    Button userVerifyEmailBtn, changeNameBtn, changeEmailBtn, changePhoneBtn, changeBirthBtn, cancleAccount, changePassword;
    Dialog mDialog;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    PinView pinView;
    ProgressDialog dialogP;
    InputMethodManager imm;
    String changePhoneNumber;
    String verificationCodeBySystem;
    DocumentReference userInfoDoc = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
    AlertDialog DialogWait;

    public UserInformationFragment() {
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
        return inflater.inflate(R.layout.fragment_user_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DialogWait = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();

        dialogP = new ProgressDialog(getContext());
        dialogP.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialogP.setTitle("הודעה נשלחה");
        dialogP.setMessage("אנא המתן...");
        dialogP.setCancelable(false);
        dialogP.setInverseBackgroundForced(false);
        dialogP.setIcon(R.mipmap.ic_launcher);

        userFullNametv = view.findViewById(R.id.userInformation_fullname_id);
        userEmailtv = view.findViewById(R.id.userInformation_email_id);
        userPhonetv = view.findViewById(R.id.userInformation_phone_id);
        userBDatetv = view.findViewById(R.id.userInformation_Birthdate_id);
        userVerifyEmailBtn = view.findViewById(R.id.userInformation_verifyEmail_btn_id);
        changeNameBtn = view.findViewById(R.id.userInformation_ChangeName_btn_id);
        changeEmailBtn = view.findViewById(R.id.userInformation_ChangeEmail_btn_id);
        changeBirthBtn = view.findViewById(R.id.userInformation_ChangeBirth_btn_id);
        changePhoneBtn = view.findViewById(R.id.userInformation_ChangePhone_btn_id);
        changePassword = view.findViewById(R.id.userInformation_Password_btn_id);
        cancleAccount = view.findViewById(R.id.cancel_account);
        userFullNametv.setText(Common.currentUser.getName());
        userEmailtv.setText(Common.fuser.getEmail());
        userPhonetv.setText(Common.currentUser.getPhone());
        userBDatetv.setText(Common.currentUser.getBirthdate());


        // Change password
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme).setTitle("החלפת סיסמה")
                        .setMessage("האם הינך בטוח שאתה רוצה להחליף את הסיסמה שלך?").setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog = new Dialog(getContext());
                        mDialog.setCancelable(false);
                        mDialog.setContentView(R.layout.dialog_change_password);
                        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        EditText oldPass = mDialog.findViewById(R.id.dialog_change_password_et1);
                        EditText newPass1 = mDialog.findViewById(R.id.dialog_change_password_et2);
                        EditText newPass2 = mDialog.findViewById(R.id.dialog_change_password_et3);
                        Button cancelBtn = mDialog.findViewById(R.id.dialog_change_password_btn_cancel_id);
                        Button acceptBtn = mDialog.findViewById(R.id.dialog_change_password_btn_accept_id);
                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
                        acceptBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DialogWait.show();

                                Pattern UpperCasePattern = Pattern.compile("[A-Z ]");
                                Pattern lowerCasePattern = Pattern.compile("[a-z ]");
                                Pattern numberCasePattern = Pattern.compile("[0-9]");
                                String currentP = oldPass.getText().toString().trim();
                                String newP1 = newPass1.getText().toString().trim();
                                String newP2 = newPass2.getText().toString().trim();
                                if (TextUtils.isEmpty(currentP)) {
                                    DialogWait.dismiss();
                                    oldPass.setError("שדה זה אינו יכול להיות ריק");
                                    return;
                                }
                                if (TextUtils.isEmpty(newP1)) {
                                    DialogWait.dismiss();
                                    newPass1.setError("שדה זה אינו יכול להיות ריק");
                                    return;
                                }
                                if (TextUtils.isEmpty(newP2)) {
                                    DialogWait.dismiss();
                                    newPass2.setError("שדה זה אינו יכול להיות ריק");
                                    return;
                                }
                                if (!newP1.equals(newP2)) {
                                    DialogWait.dismiss();
                                    newPass1.setError("הסיסמה החדשה אינה תואמת אנא וודא שהסיסמה תקינה");
                                    return;
                                }
                                if (!UpperCasePattern.matcher(newP1).find() || !lowerCasePattern.matcher(newP1).find() || !numberCasePattern.matcher(newP1).find()) {
                                    DialogWait.dismiss();
                                    newPass1.setError("הסיסמה צריכה להכיל שילוב של a-z,A-z,0-9");
                                    return;
                                }


                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(Common.fuser.getEmail(), currentP);
                                Common.fuser.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (newP1.equals(currentP)) {
                                            DialogWait.dismiss();
                                            newPass1.setError("הסיסמה החדשה זהה לסיסמה הישנה!");
                                            return;
                                        } else {
                                            Common.fuser.updatePassword(newP1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    DialogWait.dismiss();
                                                    Toast.makeText(getContext(), "סיסמתך שונתה בהצלחה!", Toast.LENGTH_SHORT).show();
                                                    mDialog.dismiss();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    DialogWait.dismiss();
                                                    newPass1.setError("לפחות 6 תווים!");
                                                }
                                            });
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        DialogWait.dismiss();
                                        oldPass.setError("סיסמה שגויה אנא נסה שנית.");
                                    }
                                });
                            }
                        });
                        mDialog.show();
                    }
                }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertush = builder.create();
                alertush.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.alert_dialog_background));
                alertush.show();

            }
        });

        // Change date of birth
        changeBirthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        DialogWait.show();
                        Calendar birthCal = Calendar.getInstance();
                        birthCal.set(Calendar.YEAR, year);
                        birthCal.set(Calendar.MONTH, month);
                        birthCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        long age13 = 410240376000L;
                        Calendar today = Calendar.getInstance();

                        if (today.getTimeInMillis() - birthCal.getTimeInMillis() > age13) {
                            month += 1;
                            String monthStr = "";
                            String dayStr = "";
                            if (month < 10) monthStr = "0" + month;
                            else monthStr = "" + month;

                            if (dayOfMonth < 10) dayStr = "0" + dayOfMonth;
                            else dayStr = "" + dayOfMonth;
                            String birthDate = "" + dayStr + "/" + monthStr + "/" + year;

                            userInfoDoc.update("birthdate", birthDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DialogWait.dismiss();
                                    userBDatetv.setText(birthDate);
                                    Common.currentUser.setBirthdate(birthDate);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    DialogWait.dismiss();
                                    Toast.makeText(getContext(), "הפעולה לא הושלמה בהצלחה אנא נסה שוב " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            DialogWait.dismiss();
                            Toast.makeText(getContext(), "הינך חייב/ת להיות לפחות בן/ת 13", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener,
                        year,
                        month,
                        day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        // Delete account button.
        cancleAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle("מחיקת חשבון")
                        .setMessage("האם הינך בטוח שאתה רוצה למחוק את החשבון שלך?").setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Common.currentUser.getPermission().equals("user")) {
                            mDialog = new Dialog(getContext());
                            mDialog.setContentView(R.layout.dialog_add_new_city);
                            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            Button dialogAccept = mDialog.findViewById(R.id.dialog_add_new_city_btn_accept_id);
                            dialogAccept.setText("אישור");
                            Button dialogCancel = mDialog.findViewById(R.id.dialog_add_new_city_btn_cancel_id);
                            EditText dialogCityEt = mDialog.findViewById(R.id.dialog_add_new_city_cityNameid);
                            TextView dialogTv = mDialog.findViewById(R.id.dialog_add_new_city_cityTextview);
                            dialogTv.setText("הכנס סיסמה נוכחית:");
                            dialogCityEt.setHint("הכנס סיסמה");
                            dialogCityEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                            dialogAccept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String password = dialogCityEt.getText().toString().trim();
                                    if (TextUtils.isEmpty(password)) {
                                        dialogCityEt.setError("שדה זה לא יכול להיות ריק");
                                        return;
                                    }
                                    AuthCredential credential = EmailAuthProvider
                                            .getCredential(Common.fuser.getEmail(), password);
                                    Common.fuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                alert.setMessage("האם אתה בטוח שאתה רוצה למחוק את המשתמש?");
                                                alert.setTitle("מחיקת משתמש");
                                                alert.setIcon(R.mipmap.ic_launcher);
                                                alert.setCancelable(false);
                                                alert.setPositiveButton("אישור", null);
                                                alert.setNegativeButton("בטל", null);

                                                final AlertDialog mAlertDialog = alert.create();
                                                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialog) {

                                                        Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                        b.setOnClickListener(new View.OnClickListener() {

                                                            @Override
                                                            public void onClick(View v) {
                                                                DialogWait.show();
                                                                // Here we search all meetings of user, deleting and than removing account.
                                                                CollectionReference all_meetings_ref = FirebaseFirestore.getInstance()
                                                                        .collection("User")
                                                                        .document(Common.fuser.getUid())
                                                                        .collection("Booking");

                                                                all_meetings_ref.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {

                                                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                                BookingInformation bInfo = queryDocumentSnapshot.toObject(BookingInformation.class);
                                                                                DocumentReference deleteFromFutureRef = FirebaseFirestore.getInstance()
                                                                                        .collection("AllSalon")
                                                                                        .document(bInfo.getSalonCity())
                                                                                        .collection("Branch")
                                                                                        .document(bInfo.getSalonId())
                                                                                        .collection("Barbers")
                                                                                        .document(bInfo.getBarberId())
                                                                                        .collection("Bookings")
                                                                                        .document("FutureBookings")
                                                                                        .collection(bInfo.getDate())
                                                                                        .document(bInfo.getHour());

                                                                                if (!bInfo.getDone()) {
                                                                                    deleteFromFutureRef.delete();
                                                                                }
                                                                                all_meetings_ref.document(queryDocumentSnapshot.getId()).delete();
                                                                            }
                                                                            DocumentReference userUploadDoc = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Uploads").document("profileImage");
                                                                            userUploadDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        DocumentSnapshot ds = task.getResult();
                                                                                        if (ds.exists()) {
                                                                                            userUploadDoc.delete();
                                                                                        }
                                                                                        CollectionReference userMessagesCol = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid()).collection("Messages");
                                                                                        userMessagesCol.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    QuerySnapshot messagesSnapshot = task.getResult();
                                                                                                    if (!messagesSnapshot.isEmpty()) {
                                                                                                        for (QueryDocumentSnapshot qds : task.getResult()) {
                                                                                                            userMessagesCol.document(qds.getId()).delete();
                                                                                                        }
                                                                                                    }

                                                                                                    // HERE WE DELETE USER <-------------
                                                                                                    DocumentReference del_user_from_db = FirebaseFirestore.getInstance()
                                                                                                            .collection("User").document(Common.fuser.getUid());


                                                                                                    del_user_from_db.delete().
                                                                                                            addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void aVoid) {
                                                                                                                    DocumentReference removeToken = FirebaseFirestore.getInstance().collection("Token").document(Common.fuser.getUid());

                                                                                                                    removeToken.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onSuccess(Void aVoid) {
                                                                                                                            // Remove user's token.
                                                                                                                            Common.fuser.delete().
                                                                                                                                    addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                                        @Override
                                                                                                                                        public void onSuccess(Void aVoid) {
                                                                                                                                            // Remove user totally.
                                                                                                                                            DialogWait.dismiss();
                                                                                                                                            mDialog.dismiss();
                                                                                                                                            mAlertDialog.dismiss();
                                                                                                                                            mAuth.signOut();
                                                                                                                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                                                                                                                            getActivity().finish();
                                                                                                                                            Log.d("User Delete", "User account deleted.");
                                                                                                                                        }
                                                                                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                @Override
                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                    DialogWait.dismiss();
                                                                                                                                    mDialog.dismiss();
                                                                                                                                    Log.d("User Delete Error", "User account failed!: " + e);
                                                                                                                                }
                                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                                                @Override
                                                                                                                                public void onFailure(@NonNull Exception e) {
                                                                                                                                    DialogWait.dismiss();
                                                                                                                                    mDialog.dismiss();
                                                                                                                                    Log.d("Delete Token Error", "Token error: " + e);
                                                                                                                                }
                                                                                                                            });
                                                                                                                        }
                                                                                                                    });




                                                                                                                }
                                                                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                                                        @Override
                                                                                                        public void onFailure(@NonNull Exception e) {
                                                                                                            DialogWait.dismiss();
                                                                                                            Log.d("User Delete Error", "User account failed!: " + e);
                                                                                                        }
                                                                                                    });

                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });

                                                                        } else {
                                                                            Log.d("Task Status: ", " failed! Check delete button");
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });

                                                        Button n = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                                        n.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                mAlertDialog.dismiss();
                                                                mDialog.dismiss();

                                                            }
                                                        });
                                                    }
                                                });
                                                mAlertDialog.show();
                                            } else {
                                                dialogCityEt.setError("סיסמה שגויה.");
                                                return;
                                            }

                                        }
                                    });
                                }
                            });

                            dialogCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDialog.dismiss();
                                }
                            });
                            mDialog.show();
                        } else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setMessage("לא ניתן להסיר משתמש זה!\nאנא פנה לממונה עליך.");
                            alert.setTitle("שגיאה בהסרת משתמש");
                            alert.setIcon(R.mipmap.ic_launcher);
                            alert.setCancelable(false);
                            alert.setPositiveButton("אישור", null);

                            final AlertDialog mAlertDialog = alert.create();

                            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialog) {
                                    Button ok = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                                    ok.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mAlertDialog.dismiss();
                                        }
                                    });
                                }
                            });

                            mAlertDialog.show();
                        }
                    }
                }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();

            }
        });

        // change email (only after verification)
        changeEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme).setTitle("החלפת מייל")
                        .setMessage("האם הינך בטוח שאתה רוצה להחליף את הדוא׳׳ל שלך?").setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog = new Dialog(getContext());
                        mDialog.setContentView(R.layout.dialog_add_new_city);
                        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Button dialogAccept = mDialog.findViewById(R.id.dialog_add_new_city_btn_accept_id);
                        dialogAccept.setText("אישור");
                        Button dialogCancel = mDialog.findViewById(R.id.dialog_add_new_city_btn_cancel_id);
                        EditText dialogCityEt = mDialog.findViewById(R.id.dialog_add_new_city_cityNameid);
                        TextView dialogTv = mDialog.findViewById(R.id.dialog_add_new_city_cityTextview);
                        dialogTv.setText("הכנס הסיסמה  נוכחית:");
                        dialogCityEt.setHint("הכנס סיסמה");
                        dialogCityEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        dialogAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String password = dialogCityEt.getText().toString().trim();
                                if (TextUtils.isEmpty(password)) {
                                    dialogCityEt.setError("שדה זה לא יכול להיות ריק");
                                    return;
                                }
                                AuthCredential credential = EmailAuthProvider
                                        .getCredential(Common.fuser.getEmail(), password);
                                Common.fuser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                            final EditText edittext = new EditText(getContext());
                                            alert.setMessage("הכנס את כתובת הדוא׳׳ל החדשה על מנת לעדכנה.");
                                            alert.setTitle("עדכון כתובת דוא׳׳ל");
                                            alert.setIcon(R.mipmap.ic_launcher);
                                            alert.setView(edittext);
                                            alert.setCancelable(false);
                                            alert.setPositiveButton("אישור", null);
                                            alert.setNegativeButton("בטל", null);

                                            final AlertDialog mAlertDialog = alert.create();
                                            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                @Override
                                                public void onShow(DialogInterface dialog) {

                                                    Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                    b.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            String YouEditTextValue = edittext.getText().toString();
                                                            if (!TextUtils.isEmpty(YouEditTextValue) && YouEditTextValue.contains("@") && YouEditTextValue.contains(".")) {
                                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                user.verifyBeforeUpdateEmail(YouEditTextValue).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(getActivity(), "כתובת הדוא׳׳ל עודכנה וקישור לאימות נשלח לכתובת החדשה.", Toast.LENGTH_SHORT).show();
                                                                        mAlertDialog.dismiss();
                                                                        mDialog.dismiss();
                                                                    }
                                                                });
                                                            } else {
                                                                edittext.setError("אנא וודא שכותבת הדוא׳׳ל החדשה תקינה.");
                                                            }
                                                        }
                                                    });

                                                    Button n = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                                    n.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            mAlertDialog.dismiss();
                                                            mDialog.dismiss();
                                                        }
                                                    });
                                                }
                                            });
                                            mAlertDialog.show();
                                        } else {
                                            dialogCityEt.setError("סיסמה שגויה.");
                                            return;
                                        }

                                    }
                                });
                            }
                        });

                        dialogCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
                        mDialog.show();
                    }
                }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();

            }
        });

        //change user name
        changeNameBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.MyDialogTheme);
                EditText edittext = new EditText(getContext());
                edittext.setBackground(getActivity().getDrawable(R.drawable.gray_et_bg_selector));
                edittext.setTextColor(Color.BLACK);
                edittext.setTextCursorDrawable(R.drawable.black_edit_text_cursor);
                alert.setMessage("הכנס את שמך רווח ואת שם המשפחה.");
                alert.setTitle("עדכון שם מלא");
                alert.setIcon(R.mipmap.ic_launcher);
                alert.setView(edittext);
                alert.setCancelable(false);
                alert.setPositiveButton("אישור", null);
                alert.setNegativeButton("בטל", null);

                final AlertDialog mAlertDialog = alert.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                String fullname = edittext.getText().toString().trim();
                                if (!TextUtils.isEmpty(fullname) && fullname.contains(" ")) {
                                    String[] fnameLname = fullname.split(" ");
                                    if (!Pattern.matches("[a-zA-Zא-ת]+", fnameLname[0]) || !Pattern.matches("[a-zA-Zא-ת]+", fnameLname[1])) {
                                        edittext.setError("חייב לכלול רק אותיות!");
                                        return;
                                    }
                                    userInfoDoc.update("name", fullname).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userFullNametv.setText(fullname);
                                            Common.currentUser.setName(fullname);
                                            TextView usrName = Common.header.findViewById(R.id.nameProfile4);
                                            String[] fname = fullname.split(" ");
                                            usrName.setText(fname[0]);
                                            Toast.makeText(getActivity(), "שם מלא עודכן בהצלחה.", Toast.LENGTH_SHORT).show();
                                            mAlertDialog.dismiss();
                                        }
                                    });
                                } else {
                                    edittext.setError("שדה זה לא יכול להיות ריק וחייב לכלול רווח בין השם הפרטי ושם המשפחה.");
                                }
                            }
                        });
                        Button n = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                        n.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mAlertDialog.dismiss();
                            }
                        });
                    }
                });
                mAlertDialog.show();
            }
        });

        // change user & auth phone number
        changePhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyDialogTheme).setTitle("החלפת מס' טלפון")
                        .setMessage("האם הינך בטוח שאתה רוצה להחליף את מס' הטלפון שלך?").setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDialog = new Dialog(getContext());
                        mDialog.setContentView(R.layout.dialog_register);
                        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        Button dialogAccept = mDialog.findViewById(R.id.dialog_btn_accept_id);
                        Button dialogCancel = mDialog.findViewById(R.id.dialog_btn_cancel_id);
                        EditText dialogPhone = mDialog.findViewById(R.id.dialog_phoneReg_id);
                        dialogPhone.setTextColor(Color.parseColor("white"));
                        pinView = mDialog.findViewById(R.id.pinview_verify);

                        dialogAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(mDialog.getContext().INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(mDialog.getCurrentFocus().getWindowToken(), 0);
                                changePhoneNumber = dialogPhone.getText().toString().trim();
                                if (TextUtils.isEmpty(changePhoneNumber) || changePhoneNumber.length() != 10) {
                                    dialogPhone.requestFocus();
                                    imm = (InputMethodManager) getActivity().getSystemService(mDialog.getContext().INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                    dialogPhone.setError("Phone can not be Empty and must contain 10 digits!");
                                    return;
                                }
                                dialogP.show();
                                CollectionReference cl = FirebaseFirestore.getInstance().collection("User");
                                Query query = cl.whereEqualTo("phone", changePhoneNumber);
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().size() > 0) {
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "User with this phone number Already Exists! Please enter a different number", Toast.LENGTH_SHORT).show();

                                            } else {
                                                sendVerificationCodeToUser(changePhoneNumber);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        dialogCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogPhone.requestFocus();
                                imm = (InputMethodManager) getActivity().getSystemService(mDialog.getContext().INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                mDialog.dismiss();
                            }
                        });
                        dialogPhone.requestFocus();
                        imm = (InputMethodManager) getActivity().getSystemService(mDialog.getContext().INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        mDialog.show();
                    }
                }).

                        setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.show();
            }
        });

        if (Common.fuser.isEmailVerified()) {
            userVerifyEmailBtn.setVisibility(View.GONE);
        }

        userVerifyEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.fuser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), "דוא''ל לאימות נשלח בהצלחה", Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }

    public void sendVerificationCodeToUser(String phone) {
        String area = "+972";
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                area + phone,
                10,
                TimeUnit.SECONDS,
                getActivity(),
                mCallback)
        ;
    }

    public PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            super.onCodeAutoRetrievalTimeOut(s);
            dialogP.dismiss();
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
                dialogP.dismiss();
                pinView.setText(code);
                verifyCode(pinView.getText().toString());
                Toast.makeText(getActivity().getApplicationContext(), "" + code, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void verifyCode(String codeByUser) {


        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        phoneAuthReauthenticate(credential);

    }

    private void phoneAuthReauthenticate(PhoneAuthCredential credential) {
        Common.fuser.updatePhoneNumber(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDialog.dismiss();
                userInfoDoc.update("phone", changePhoneNumber);
                Common.currentUser.setPhone(changePhoneNumber);
                userPhonetv.setText(changePhoneNumber);
                Toast.makeText(getActivity(), "מס הטלפון עודכן בהצלחה.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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