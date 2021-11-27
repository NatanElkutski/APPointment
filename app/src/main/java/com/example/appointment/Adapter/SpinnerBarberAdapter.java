package com.example.appointment.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appointment.Model.Barber;
import com.example.appointment.R;

import java.util.ArrayList;

public class SpinnerBarberAdapter extends ArrayAdapter<Barber> {

    public SpinnerBarberAdapter(Context context, ArrayList<Barber>barbersList){
        super(context,0,barbersList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position,convertView,parent);
    }

    private View initView (int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.barber_spinner_item,parent,false
            );
        }
        TextView barber_name_txt = convertView.findViewById(R.id.barber_spinner_name);

        Barber barber = getItem(position);
        if(barber!=null)
        barber_name_txt.setText(barber.getName());

        return convertView;
    }
}
