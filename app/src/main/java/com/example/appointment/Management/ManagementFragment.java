package com.example.appointment.Management;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.appointment.Common.Common;
import com.example.appointment.R;
import com.example.appointment.Service.MainActivity;

public class ManagementFragment extends Fragment {

    CardView hours_set, vacation_set, add_employee, add_salon, statistics_cv;
    LinearLayout manager_ll, business_ll;

    public ManagementFragment() {
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
        adjustFontScale(getResources().getConfiguration());
        return inflater.inflate(R.layout.fragment_management, container, false);
    }

    public void adjustFontScale(Configuration configuration) {
        configuration.fontScale = (float) 1.0;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getActivity().getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        manager_ll = view.findViewById(R.id.management_manager_ll);
        hours_set = view.findViewById(R.id.hours_set_button);
        vacation_set = view.findViewById(R.id.vacation);
        add_employee = view.findViewById(R.id.add_employee_cardView);
        add_salon = view.findViewById(R.id.management_admin_add_or_remove_salon_cd);
        statistics_cv = view.findViewById(R.id.statistics_cardView);
        business_ll = view.findViewById(R.id.business_layout);

        if (Common.currentUser.getPermission().equals("admin")) {
            business_ll.setVisibility(View.GONE);
        }

        if (!Common.currentUser.getPermission().equals("admin")) {
            add_salon.setVisibility(View.GONE);
        }

        if (!Common.currentUser.getPermission().equals("barber"))
            manager_ll.setVisibility(View.VISIBLE);
        else
            manager_ll.setVisibility(View.GONE);


        hours_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.currentUser.getPermission().equals("barber"))
                    Navigation.findNavController(view).navigate(R.id.selectHoursFragment);
                else if (Common.currentUser.getPermission().equals("manager") || Common.currentUser.getPermission().equals("manager+barber"))
                    Navigation.findNavController(view).navigate(R.id.managerSelectHoursFragment);
            }
        });

        vacation_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.currentUser.getPermission().equals("barber"))
                    Navigation.findNavController(view).navigate(R.id.vacationFragment);
                else if (Common.currentUser.getPermission().equals("manager") || Common.currentUser.getPermission().equals("manager+barber"))
                    Navigation.findNavController(view).navigate(R.id.managerVacationFragment);
            }
        });

        add_employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.addOrRemoveEmployeeFragment);
            }
        });

        add_salon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.adminAddOrRemoveSalonFragment);
            }
        });

        statistics_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.currentUser.getPermission().equals("manager"))
                    Navigation.findNavController(view).navigate(R.id.statisticsFragment);
                else
                    Navigation.findNavController(view).navigate(R.id.managerStatisticsFragment);
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