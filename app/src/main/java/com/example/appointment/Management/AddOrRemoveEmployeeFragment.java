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

public class AddOrRemoveEmployeeFragment extends Fragment {

    Button removeBarberBtn, addBarberBtn, cancelBtn;

    public AddOrRemoveEmployeeFragment() {
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
        View itemView = inflater.inflate(R.layout.fragment_add_or_remove_employee, container, false);
        removeBarberBtn = itemView.findViewById(R.id.AddOrRemove_remove_employee_btn);
        addBarberBtn = itemView.findViewById(R.id.AddOrRemove_add_employee_btn);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        removeBarberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.currentUser.getPermission().equals("admin"))
                    Navigation.findNavController(view).navigate(R.id.adminRemoveEmployeeFragment);
                else
                    Navigation.findNavController(view).navigate(R.id.removeEmployeesFragment);
            }
        });

        addBarberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.currentUser.getPermission().equals("admin"))
                    Navigation.findNavController(view).navigate(R.id.adminAddEmployeeFragment);
                else
                    Navigation.findNavController(view).navigate(R.id.addEmployeesFragment);
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