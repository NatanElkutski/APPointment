package com.example.appointment.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Common.Common;
import com.example.appointment.Interface.IRecyclerItemSelectedListener;
import com.example.appointment.Model.Salon;
import com.example.appointment.R;

import java.util.ArrayList;
import java.util.List;

import static android.R.color.white;

public class MyContactAdapter extends RecyclerView.Adapter<MyContactAdapter.MyViewHolder> {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;
    String city;
    Dialog mDialog;
    LocalBroadcastManager localBroadcastManager;
    List<MyViewHolder> myViewHolderList;
    private final int REQUEST_PHONE_CALL = 1;

    public MyContactAdapter(Context context, List<Salon> salonList,String city) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        myViewHolderList = new ArrayList<>();
        this.city = city;
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_salon,viewGroup,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.txt_salon_name.setText(salonList.get(i).getName());
        myViewHolder.txt_salon_address.setText(salonList.get(i).getAddress());

        myViewHolder.card_salon.setClickable(true);
        myViewHolder.card_salon.setCardBackgroundColor(context.getColor(android.R.color.transparent));
        myViewHolder.contact_ll.setBackgroundResource(R.drawable.barber_timeslot_customer_cardview_selector);
        myViewHolder.txt_salon_name.setTextColor(context.getColor(R.color.AppMainColor));
        myViewHolder.txt_salon_address.setTextColor(context.getColor(R.color.AppMainColor));

        if(!cardViewList.contains(myViewHolder.card_salon))
            cardViewList.add(myViewHolder.card_salon);

        if(!myViewHolderList.contains(myViewHolder))
            myViewHolderList.add(myViewHolder);

            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectListener(View view, int pos) {
                    mDialog = new Dialog(context);
                    mDialog.setCancelable(false);
                    mDialog.setContentView(R.layout.dialog_contact_business);
                    mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView salonName,salonAddress,salonPhone;
                    Button callBtn, navBtn;
                    ImageView exitBtn;
                    salonName = mDialog.findViewById(R.id.contact_business_SalonName);
                    salonAddress = mDialog.findViewById(R.id.contact_business_SalonAddress);
                    salonPhone = mDialog.findViewById(R.id.contact_business_SalonPhone);
                    callBtn = mDialog.findViewById(R.id.contact_business_call_btn);
                    navBtn = mDialog.findViewById(R.id.contact_business_navigate_btn);
                    exitBtn = mDialog.findViewById(R.id.contact_business_exit_btn);

                    salonName.setText(salonList.get(pos).getName());
                    String cityAndAddress = salonList.get(pos).getAddress()+", "+city;
                    salonAddress.setText(cityAndAddress);
                    salonPhone.setText(salonList.get(pos).getPhone());

                    callBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                            phoneIntent.setData(Uri.parse("tel:" + salonPhone.getText().toString()));

                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                            } else {
                                context.startActivity(phoneIntent);
                                if (mDialog.isShowing()) mDialog.dismiss();
                            }
                            mDialog.dismiss();
                        }
                    });

                    navBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String navAddress = city+" "+salonList.get(pos).getAddress();
                            String FormatAdr = navAddress.replace(" ","+");
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("google.navigation:q=" + FormatAdr));
                            context.startActivity(intent);
                            mDialog.dismiss();
                        }
                    });
                    exitBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });

                    mDialog.show();

                }
            });
    }


    @Override
    public int getItemCount() {
        return salonList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_salon_name,txt_salon_address;
        CardView card_salon;
        LinearLayout contact_ll;
        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            contact_ll = itemView.findViewById(R.id.contact_ll_id);
            card_salon = itemView.findViewById(R.id.card_salon);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectListener(view, getAdapterPosition());
        }
    }


}
