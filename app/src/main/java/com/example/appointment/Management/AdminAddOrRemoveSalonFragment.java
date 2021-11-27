package com.example.appointment.Management;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.appointment.Common.Common;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;

public class AdminAddOrRemoveSalonFragment extends Fragment {

    Button addSalonBtn,removeSalonBtn,cancelBtn;

    public AdminAddOrRemoveSalonFragment() {
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
        View itemView =  inflater.inflate(R.layout.fragment_admin_add_or_remove_salon, container, false);
        addSalonBtn = itemView.findViewById(R.id.Admin_addOrRemove_add_button);
        removeSalonBtn = itemView.findViewById(R.id.Admin_addOrRemove_remove_button);

        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addSalonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.adminAddSalonFragment);
            }
        });

        removeSalonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.adminRemoveSalonFragment);
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