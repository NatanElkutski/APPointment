package com.example.appointment.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Common.Common;
import com.example.appointment.Interface.IRecyclerItemSelectedListener;
import com.example.appointment.Model.Barber;
import com.example.appointment.R;

import java.util.ArrayList;
import java.util.List;

import static android.R.color.holo_orange_dark;
import static android.R.color.white;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {


    Context context;
    List <Barber> barberList;
    List <CardView>cardViewList;
    List<MyBarberAdapter.MyViewHolder> myViewHolderList;
    LocalBroadcastManager localBroadcastManager;

    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
        myViewHolderList = new ArrayList<>();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_barber,viewGroup,false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.txt_barber_name.setText(barberList.get(i).getName());

        if(!cardViewList.contains(myViewHolder.card_barber))
            cardViewList.add(myViewHolder.card_barber);

        if(!myViewHolderList.contains(myViewHolder))
            myViewHolderList.add(myViewHolder);

        myViewHolder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectListener(View view, int pos) {
                // set background for all item not choice
                for(MyBarberAdapter.MyViewHolder holder : myViewHolderList)
                {
                    if(holder.card_barber != myViewHolder.card_barber)
                    {
                        holder.card_barber.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                        holder.barbercard_ll.setBackgroundResource(R.drawable.available_cardview_bookingfragment);
                        holder.txt_barber_name.setTextColor(Color.WHITE);
                        holder.roundacc.setImageResource(R.drawable.ic_roundaccount_white);
                    }
                }
                // set background for choice
                myViewHolder.card_barber.setCardBackgroundColor(context.getColor(white));
                myViewHolder.txt_barber_name.setTextColor(context.getColor(R.color.AppMainColor));
                myViewHolder.roundacc.setImageResource(R.drawable.ic_roundaccount_maincolor);

                // send local broadcast to enable button next
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_BARBER_SELECTED,barberList.get(pos));
                intent.putExtra(Common.KEY_STEP,2);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_barber_name;
        CardView card_barber;
        ImageView roundacc;
        LinearLayout barbercard_ll;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            card_barber = itemView.findViewById(R.id.card_barber);
            txt_barber_name = itemView.findViewById(R.id.txt_barber_name);
            barbercard_ll = itemView.findViewById(R.id.booking_barber_card_ll);
            roundacc = itemView.findViewById(R.id.booking_barber_roundacc);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectListener(v,getAdapterPosition());
        }
    }


}
