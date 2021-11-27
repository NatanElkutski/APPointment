package com.example.appointment.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Common.Common;
import com.example.appointment.Interface.IRecyclerItemSelectedListener;
import com.example.appointment.Model.Salon;
import com.example.appointment.R;

import java.util.ArrayList;
import java.util.List;

import static android.R.color.white;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;
    LocalBroadcastManager localBroadcastManager;
    List<MyViewHolder> myViewHolderList;

    public MySalonAdapter(Context context, List<Salon> salonList) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        myViewHolderList = new ArrayList<>();
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

        if(!cardViewList.contains(myViewHolder.card_salon))
            cardViewList.add(myViewHolder.card_salon);

        if(!myViewHolderList.contains(myViewHolder))
            myViewHolderList.add(myViewHolder);

            myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectListener(View view, int pos) {

                    for(MyViewHolder holder : myViewHolderList)
                    {
                        if(holder.card_salon != myViewHolder.card_salon)
                        {
                            holder.card_salon.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                            holder.contact_ll.setBackgroundResource(R.drawable.schedule_booking_salon_card_view_default);
                            holder.txt_salon_name.setTextColor(Color.WHITE);
                            holder.txt_salon_address.setTextColor(Color.WHITE);
                        }
                    }

                    myViewHolder.card_salon.setCardBackgroundColor(context.getColor(white));
                    myViewHolder.txt_salon_name.setTextColor(context.getColor(R.color.AppMainColor));
                    myViewHolder.txt_salon_address.setTextColor(context.getColor(R.color.AppMainColor));

                    Intent intent = new Intent (Common.KEY_ENABLE_BUTTON_NEXT);
                    intent.putExtra(Common.KEY_SALON_STORE,salonList.get(pos));
                    intent.putExtra(Common.KEY_STEP,1);
                    localBroadcastManager.sendBroadcast(intent);
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
