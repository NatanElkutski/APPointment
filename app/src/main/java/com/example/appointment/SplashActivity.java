package com.example.appointment;

import android.content.Intent;
import android.os.Bundle;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.User;
import com.example.appointment.Model.profileImage;
import com.example.appointment.Service.LoginActivity;
import com.example.appointment.Service.MainActivity;
import com.example.appointment.Service.RegActivity;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;


public class SplashActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    DocumentReference useref;
    private static final int splashTimeOut = 2000;
    DocumentReference userTokenRef;
    LinearLayout splashLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        ProgressBar progressBar = findViewById(R.id.spin_kit);
        splashLL = findViewById(R.id.appointmentLL_splash);
        Sprite fadingCircle = new FadingCircle();
        progressBar.setIndeterminateDrawable(fadingCircle);
        progressBar.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                progressBar.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        LoginProccess();
                    }
                }, 2000);
            }
        }, splashTimeOut);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.splash_animation);
        splashLL.startAnimation(anim);
    }

    public void LoginProccess() {
        if (mAuth.getCurrentUser() != null) {
            Common.fuser = mAuth.getCurrentUser();
            useref = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid());
            useref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot ds = task.getResult();
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            Boolean isLogged = ds.getBoolean("isLogged");
                            String token = FirebaseInstanceId.getInstance().getToken();
                            DocumentReference TokenForCheck = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid());
                            TokenForCheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot docToken = task.getResult();
                                        if (docToken.exists()) {
                                            String userPrevToken = docToken.get("token").toString();
                                            if (!isLogged || userPrevToken.equals(token)) {
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
                                                                    userTokenRef = FirebaseFirestore.getInstance().collection("Tokens").document(Common.fuser.getUid());
                                                                    userTokenRef.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            if (Common.currentUser.getRegStep() == 1) {
                                                                                Intent intent = new Intent(SplashActivity.this, RegActivity.class);
                                                                                intent.putExtra(Common.IS_LOGIN, true);
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
                                            } else {
                                                mAuth.signOut();
                                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }

                                        }
                                    }
                                }
                            });
                        }
                    }
                }
            });
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
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
                        Common.profilePicture = image.getImageUrl();
                    }
                    useref.update("isLogged", true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            intent.putExtra(Common.IS_LOGIN, true);
                            startActivity(intent);
                            finish();
                        }
                    });

                } else {
                    Toast.makeText(SplashActivity.this, "Load Profile Picture Task Error", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra(Common.IS_LOGIN, true);
                    startActivity(intent);
                    finish();

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
                    Toast.makeText(SplashActivity.this, "User Message to Database Task Error", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
}


/*


 */