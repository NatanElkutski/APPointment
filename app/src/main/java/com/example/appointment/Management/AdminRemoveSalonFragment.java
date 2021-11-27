package com.example.appointment.Management;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.Barber;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.Message;
import com.example.appointment.Model.Salon;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.sendNotification;
import com.example.appointment.Service.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;


public class AdminRemoveSalonFragment extends Fragment {

    LinearLayout cardView_linear;
    MaterialSpinner citySpinner, salonSpinner;
    List<String> cityList = new ArrayList<>();
    List<Salon> salonList;
    List<String> salonIdList;
    List<String> salonNamesList;
    TextView SalonName, SalonAddress;
    Button removeSalonBtn;
    String choosenCity;
    CollectionReference salonBarbersCol;
    DocumentReference SalonRef;

    String choosenSalonId, choosenManagerUid;
    AlertDialog alertDialog;


    public AdminRemoveSalonFragment() {
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
        View itemView = inflater.inflate(R.layout.fragment_admin_remove_salon, container, false);
        alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
        citySpinner = itemView.findViewById(R.id.admin_remove_salon_city_spinner);
        salonSpinner = itemView.findViewById(R.id.admin_remove_salon_salon_spinner);
        salonSpinner.setEnabled(false);
        cardView_linear = itemView.findViewById(R.id.admin_remove_salon_cardView_linear_layout);
        SalonName = itemView.findViewById(R.id.admin_remove_salon_cardView_salon_name);
        removeSalonBtn = itemView.findViewById(R.id.admin_remove_salon__accept_btn);
        removeSalonBtn.setEnabled(false);
        SalonAddress = itemView.findViewById(R.id.admin_remove_salon_cardView_salon_address);

        citySpinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.barbers_spinner_dropdown_background));
        salonSpinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.barbers_spinner_dropdown_background));

        loadCity();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            Field popup = MaterialSpinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(salonSpinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(20);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }


    }

    private void loadCity() {
        String n = "בחר סלון";
        salonSpinner.setItems(n);

        CollectionReference salonBarbersRef = FirebaseFirestore.getInstance()
                .collection("AllSalon");

        salonBarbersRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot qs = task.getResult();
                            cityList.add("בחר עיר");
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot cityQds : qs) {
                                    cityList.add(cityQds.getId());
                                }
                            }
                            cityLoadSuccess();
                        }
                    }
                });
    }

    public void cityLoadSuccess() {
        citySpinner.setItems(cityList);
        citySpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0) {
                    choosenCity = cityList.get(position);
                    salonSpinner.setEnabled(true);
                    salonLoad();
                } else {
                    salonSpinner.setEnabled(false);
                    cardView_linear.setVisibility(View.GONE);
                    removeSalonBtn.setEnabled(false);
                    String n = "בחר סלון";
                    salonSpinner.setItems(n);
                }
            }
        });
    }

    private void salonLoad() {

        CollectionReference salonRef = FirebaseFirestore.getInstance()
                .collection("AllSalon").document(choosenCity).collection("Branch");

        salonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            salonList = new ArrayList<>();
                            salonIdList = new ArrayList<>();
                            salonNamesList = new ArrayList<>();
                            QuerySnapshot qs = task.getResult();
                            salonNamesList.add("בחר סלון");
                            if (!qs.isEmpty()) {
                                for (QueryDocumentSnapshot salonQds : qs) {
                                    Salon salon = salonQds.toObject(Salon.class);
                                    salonList.add(salon);
                                    salonNamesList.add(salon.getName());
                                    salonIdList.add(salonQds.getId());
                                }
                            }
                            salonLoadSuccess();
                        }
                    }
                });
    }

    public void salonLoadSuccess() {
        salonSpinner.setItems(salonNamesList);
        salonSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if (position > 0) {
                    SalonName.setText(salonList.get(position - 1).getName());
                    SalonAddress.setText(salonList.get(position - 1).getAddress());
                    cardView_linear.setVisibility(View.VISIBLE);
                    choosenSalonId = salonIdList.get(position - 1);
                    choosenManagerUid = salonList.get(position - 1).getManagerUid();
                    removeSalonBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext()).setTitle("הסרת סלון").setMessage("האם הינך בטוח שברצונך להסיר את הסלון " + SalonName.getText().toString().trim() + "?");
                            alertBuilder.setPositiveButton("הסר", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                    salonBarbersCol = FirebaseFirestore.getInstance()
                                            .collection("AllSalon")
                                            .document(choosenCity)
                                            .collection("Branch")
                                            .document(choosenSalonId).collection("Barbers");

                                    salonBarbersCol.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                QuerySnapshot salonBarbersQS = task.getResult();
                                                if (!salonBarbersQS.isEmpty()) {
                                                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                    alert.setMessage("לא ניתן להסיר בית עסק זה!\nאנא וודא שהסלון ריק מעובדים.");
                                                    alert.setTitle("שגיאה בהסרת בית עסק");
                                                    alert.setIcon(R.mipmap.ic_launcher);
                                                    alert.setCancelable(false);
                                                    alert.setPositiveButton("אישור", null);


                                                    final AlertDialog mAlertDialog = alert.create();

                                                    mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                        @Override
                                                        public void onShow(DialogInterface dialog) {
                                                            Button pb = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                                            pb.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    mAlertDialog.dismiss();
                                                                }
                                                            });
                                                        }
                                                    });
                                                    mAlertDialog.show();
                                                } else {
                                                    alertDialog.show();
                                                    SalonRef = FirebaseFirestore.getInstance().collection("AllSalon")
                                                            .document(choosenCity)
                                                            .collection("Branch")
                                                            .document(choosenSalonId);

                                                    SalonRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            Salon salon = task.getResult().toObject(Salon.class);

                                                            SalonRef.collection("Clients").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                    if(task.isSuccessful()){
                                                                        // Remove clients list
                                                                        if(!task.getResult().isEmpty()){
                                                                            for(QueryDocumentSnapshot qds : task.getResult()){
                                                                                SalonRef.collection("Clients").document(qds.getId()).delete();
                                                                            }
                                                                        }
                                                                        // Update fields of business' manager
                                                                        DocumentReference userRef = FirebaseFirestore.getInstance().collection("User").document(salon.getManagerUid());
                                                                        userRef.update("permission", "user").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                userRef.update("salonId", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        userRef.update("salonCity", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                userRef.update("salonName", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        SalonRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onSuccess(Void aVoid) {
                                                                                                                alertDialog.dismiss();
                                                                                                                Toast.makeText(getContext(), "בית העסק הוסר בהצלחה.", Toast.LENGTH_SHORT);
                                                                                                                getActivity().onBackPressed();
                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                });
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });




                                                        }
                                                    });
                                                }

                                            }
                                        }
                                    });
                                }
                            }).setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alertBuilder.show();


                        }
                    });
                    removeSalonBtn.setEnabled(true);
                } else {
                    cardView_linear.setVisibility(View.GONE);
                    removeSalonBtn.setEnabled(false);
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