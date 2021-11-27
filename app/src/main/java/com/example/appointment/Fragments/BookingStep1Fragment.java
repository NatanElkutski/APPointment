package com.example.appointment.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Adapter.AutoCompleteCityAdapter;
import com.example.appointment.Adapter.MySalonAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.SpacesItemDecoration;
import com.example.appointment.Interface.IAllSalonLoadListener;
import com.example.appointment.Interface.IBranchLoadListener;
import com.example.appointment.Model.Salon;
import com.example.appointment.R;
import com.example.appointment.Service.BookingFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;


public class BookingStep1Fragment extends Fragment implements IAllSalonLoadListener, IBranchLoadListener {


    CollectionReference allSalonRef;
    CollectionReference branchRef;
    AutoCompleteTextView autocompleteTv;
    RecyclerView recycler_salon;
    IAllSalonLoadListener iAllSalonLoadListener;
    IBranchLoadListener iBranchLoadListener;
    LocalBroadcastManager localBroadcastManager;
    Unbinder unbinder;
    List<String> citiesList;
    AlertDialog dialog;
    String choosenCity = "";

    static BookingStep1Fragment instance;

    public static BookingStep1Fragment getInstance() {
        instance = new BookingStep1Fragment();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        allSalonRef = FirebaseFirestore.getInstance().collection("AllSalon");
        iAllSalonLoadListener = this;
        iBranchLoadListener = this;

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View itemView = inflater.inflate(R.layout.fragment_booking_step_one, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        autocompleteTv = itemView.findViewById(R.id.auto_complete_tv_booking1);
        autocompleteTv.setDropDownBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.autocomplete_dropdown));
        recycler_salon = itemView.findViewById(R.id.recycler_salon);
        recycler_salon.setRotationY(180);
        autocompleteTv.setRotationY(180);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        Common.step = 0;
        Common.bookingTimeSlotNumber = -1;
        Common.currentBarber = null;
        Common.currentSalon = null;
        Common.bookingDate = Common.currentDate;
        Common.bookingInformation = null;
        initView();

        loadAllSalon();
        return itemView;

    }

    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));

    }

    private void loadAllSalon() {

        allSalonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            citiesList = new ArrayList<>();
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult())
                                citiesList.add(documentSnapshot.getId());
                            iAllSalonLoadListener.onAllSalonLoadSuccess(citiesList);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllSalonLoadListener.onAllSalonLoadFailed(e.getMessage());
            }
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onAllSalonLoadSuccess(List<String> areaNameList) {
        AutoCompleteCityAdapter adapter = new AutoCompleteCityAdapter(getContext(), citiesList);
        autocompleteTv.setAdapter(adapter);

        autocompleteTv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (autocompleteTv.getText().toString().equals("")) {
                    autocompleteTv.showDropDown();
                }
                return false;
            }
        });


        autocompleteTv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choosenCity = parent.getItemAtPosition(position).toString();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                loadBranchOfCity(choosenCity);


                //
            }
        });
        autocompleteTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (!s.equals(choosenCity)) {
                    recycler_salon.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                    intent.putExtra(Common.KEY_STEP, 0);
                    localBroadcastManager.sendBroadcast(intent);
                } else {
                    autocompleteTv.showDropDown();
                    recycler_salon.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    autocompleteTv.showDropDown();
                }
            }
        });


    }

    private void loadBranchOfCity(String cityName) {
        dialog.show();

        Common.city = cityName;

        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(cityName)
                .collection("Branch");

        branchRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Salon> list = new ArrayList<>();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Salon salon = documentSnapshot.toObject(Salon.class);
                        salon.setSalonId(documentSnapshot.getId());
                        list.add(salon);
                    }
                    iBranchLoadListener.onBranchLoadSuccess(list);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iBranchLoadListener.onBranchLoadFailed(e.getMessage());
            }
        });
    }

    @Override
    public void onAllSalonLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> salonList) {
        MySalonAdapter adapter = new MySalonAdapter(getActivity(), salonList);
        recycler_salon.setAdapter(adapter);
        recycler_salon.setVisibility(View.VISIBLE);

        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}
