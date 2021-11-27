package com.example.appointment.Service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import android.util.DisplayMetrics;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.WindowManager;
import android.widget.ImageView;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.MaterialMenuView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.example.appointment.Common.Common;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.MyFireBaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Menu menu, menuNav;
    MenuItem logoutItem;
    DocumentReference userRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ProgressDialog pDialog;
    MaterialMenuView materialMenu;
    DrawerLayout drawerLayout;
    MenuItem timeTable;
    ActionBarDrawerToggle mDrawerToggle;
    RelativeLayout navLogoRL,navBackRL;
    ImageView navBackButton;
    RoundedImageView usrImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Intent intent = new Intent(this, MyFireBaseMessagingService.class);
        startService(intent);
        String languageToLoad = "he"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.activity_main);


        pDialog = new ProgressDialog(this);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setTitle("מתנתק");
        pDialog.setMessage("אנא המתן...");
        pDialog.setCancelable(false);
        pDialog.setInverseBackgroundForced(false);
        pDialog.setIcon(R.mipmap.ic_launcher);
        mAuth.getCurrentUser();

        materialMenu = findViewById(R.id.imageMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        navLogoRL = findViewById(R.id.logo_rl_nav);
        navBackRL = findViewById(R.id.nav_back_button);
        navBackButton = findViewById(R.id.navback_img);
        drawerLayout.setMinimumWidth(400);
        drawerLayout.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);
        drawerLayout.setStatusBarBackgroundColor(Color.TRANSPARENT);

        View mainView = findViewById(R.id.navHostFragment);

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                mainView.setTranslationX(-slideOffset * drawerView.getWidth());
                drawerLayout.bringChildToFront(drawerView);
                drawerView.requestLayout();
            }
        };
        drawerLayout.setDrawerListener(mDrawerToggle);
        findViewById(R.id.imageMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawerLayout.isOpen()) {
                    userMessagesFromDb(Common.fuser.getUid());
                    drawerLayout.setScrimColor(Color.TRANSPARENT);
                    drawerLayout.openDrawer(GravityCompat.START);
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                    materialMenu.animateIconState(MaterialMenuDrawable.IconState.X);

                } else {
                    materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                    drawerLayout.closeDrawer(GravityCompat.START);


                }
            }
        });

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);
        navigationView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_RIGHT);



        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
        params.width = metrics.widthPixels;
        navigationView.setLayoutParams(params);
        ViewCompat.setElevation(drawerLayout, 20);

        menuNav = navigationView.getMenu();
        logoutItem = menuNav.findItem(R.id.messagesFragment).setActionView(R.layout.notification_badge);
        userMessagesFromDb(Common.fuser.getUid());

        //avigationView.getMenu().getItem(6).setActionView(R.layout.notification_badge);

        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {

                materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        MenuItem item = menuNav.findItem(R.id.menuLogout);


        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
                drawerLayout.closeDrawer(GravityCompat.START);
                pDialog.show();
                userRef = db.collection("User").document(Common.fuser.getUid());
                userRef.update("isLogged", false).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Common.currentUser = null;
                        Common.fuser = null;
                        FirebaseAuth.getInstance().signOut();
                        Intent intent1 = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        pDialog.dismiss();
                    }
                });
                return true;
            }
        });

        Common.header = navigationView.getHeaderView(0);

        TextView usrName = Common.header.findViewById(R.id.nameProfile4);
        String[] fname = Common.currentUser.getName().split(" ");
        usrName.setText(fname[0]);

        usrImg = Common.header.findViewById(R.id.imageProfile4);

        menu = findViewById(R.id.Menu_id);
        //setNavigationViewListener();

        //Toast.makeText(this, ""+Common.currentUser.getPermission(), Toast.LENGTH_SHORT).show();
        if (!Common.fuser.isEmailVerified()) {
            TextView verified = Common.header.findViewById(R.id.verified_txt_id);
            ImageView verifiedIcon = Common.header.findViewById(R.id.verified_icon);
            verifiedIcon.setVisibility(View.GONE);
            verified.setText("not verified");
            verified.setTextColor(getColor(R.color.darker_gray));
        }

        if (Common.currentUser != null) {
            if (!Common.currentUser.getPermission().equals("user")) {
                Menu menu = navigationView.getMenu();
                MenuItem management = menu.findItem(R.id.managementMenu);
                timeTable = menu.findItem(R.id.TimeFragment);
                management.setVisible(true);
                if(!Common.currentUser.getPermission().equals("admin"))
                timeTable.setVisible(true);

                if (!Common.currentUser.getPermission().equals("barber")) {
                    timeTable.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Navigation.findNavController(MainActivity.this, R.id.navHostFragment).navigate(R.id.managerTimeFragment);
                            drawerLayout.closeDrawer(GravityCompat.START);
                            return true;
                        }
                    });
                }
            }

            if (!Common.profilePicture.equals("")) {
                Glide.with(MainActivity.this)
                        .load(Common.profilePicture)
                        .circleCrop()
                        .into(usrImg);
            }


            usrImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, UploadActivity.class));
                }
            });
        }

        navBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
                    {
                        if (!qs.isEmpty()) {
                            Common.seenMessages = qs.size();
                        } else {
                            Common.seenMessages = 0;
                        }
                    }
                    if (Common.seenMessages > 0) {
                        TextView hola = logoutItem.getActionView().findViewById(R.id.notification_badge_counter);
                        String notCounter = "" + Common.seenMessages;
                        hola.setText(notCounter);
                        RelativeLayout notBadgeRL = logoutItem.getActionView().findViewById(R.id.notification_badge_RL);
                        notBadgeRL.setVisibility(View.VISIBLE);
                    } else {
                        RelativeLayout notBadgeRL = logoutItem.getActionView().findViewById(R.id.notification_badge_RL);
                        notBadgeRL.setVisibility(View.GONE);
                    }

                }
            }
        });
    }

    public void changeNav()
    {
        if(Common.fragmentPage!=1) {
            navLogoRL.setVisibility(View.GONE);
            navBackRL.setVisibility(View.VISIBLE);
        }
        else
        {
            navLogoRL.setVisibility(View.VISIBLE);
            navBackRL.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {

        if (Common.fragmentPage == 1) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage("האם הינך בטוח שאת/ה רוצה לצאת?");
            builder.setPositiveButton("לצאת", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Common.step = 0;
            Common.bookingTimeSlotNumber = -1;
            Common.currentBarber = null;
            Common.currentSalon = null;
            Common.bookingDate = Common.currentDate;
            Common.bookingInformation = null;
            super.onBackPressed();

        }

    }

    @Override
    protected void onDestroy() {
        finish();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Common.profilePicture.equals("")) {
            Glide.with(MainActivity.this)
                    .load(Common.profilePicture)
                    .circleCrop()
                    .into(usrImg);
        }
    }
}