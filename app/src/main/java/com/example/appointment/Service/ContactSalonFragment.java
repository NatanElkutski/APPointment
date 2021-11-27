package com.example.appointment.Service;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.appointment.Adapter.MyContactAdapter;
import com.example.appointment.Adapter.MySalonAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Common.SpacesItemDecoration;
import com.example.appointment.Interface.IAllSalonLoadListener;
import com.example.appointment.Interface.IBranchLoadListener;
import com.example.appointment.Model.Salon;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;


public class ContactSalonFragment extends Fragment implements IAllSalonLoadListener, IBranchLoadListener{

    CollectionReference allSalonRef;
    CollectionReference branchRef;
    MaterialSpinner spinner;
    RecyclerView recycler_salon;
    IAllSalonLoadListener iAllSalonLoadListener ;
    IBranchLoadListener iBranchLoadListener;
    Unbinder unbinder;
    AlertDialog dialog;
    String city;

    public ContactSalonFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allSalonRef = FirebaseFirestore.getInstance().collection("AllSalon");
        iAllSalonLoadListener = this;
        iBranchLoadListener = this;

        dialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View itemView =  inflater.inflate(R.layout.fragment_contact,container,false);
        unbinder = ButterKnife.bind(this,itemView);
        spinner = itemView.findViewById(R.id.contact_spinner);
        spinner.getPopupWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.barbers_spinner_dropdown_background));
        recycler_salon = itemView.findViewById(R.id.contact_recyclerView);
        dialog.show();
        initView();

        loadAllSalon();

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    private void initView() {
        recycler_salon.setHasFixedSize(true);
        recycler_salon.setLayoutManager(new GridLayoutManager(getActivity(),2));
        recycler_salon.addItemDecoration(new SpacesItemDecoration(8));

    }

    private void loadAllSalon() {

        allSalonRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            List<String> list = new ArrayList<>();
                            list.add("בחר עיר");
                            for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                                list.add(documentSnapshot.getId());
                            iAllSalonLoadListener.onAllSalonLoadSuccess(list);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllSalonLoadListener.onAllSalonLoadFailed(e.getMessage());
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onAllSalonLoadSuccess(List <String> areaNameList) {
        spinner.setItems(areaNameList);
        dialog.dismiss();
        spinner.setOnItemSelectedListener( new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                if(position > 0)
                {
                    city = item.toString();
                    loadBranchOfCity();
                }
                else {
                    city = "";
                    recycler_salon.setVisibility(View.GONE);
                }


            }
        });
    }

    private void loadBranchOfCity() {
        dialog.show();


        branchRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(city)
                .collection("Branch");

        branchRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Salon> list = new ArrayList<>();
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot documentSnapshot:task.getResult())
                    {
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
        dialog.dismiss();
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBranchLoadSuccess(List<Salon> salonList) {
        MyContactAdapter adapter = new MyContactAdapter(getActivity(),salonList,city);
        recycler_salon.setAdapter(adapter);
        recycler_salon.setVisibility(View.VISIBLE);

        dialog.dismiss();
    }

    @Override
    public void onBranchLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity)getActivity()).changeNav();
    }
}