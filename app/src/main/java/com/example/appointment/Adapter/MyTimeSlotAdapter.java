package com.example.appointment.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
import com.example.appointment.Model.TimeSlot;
import com.example.appointment.Model.TimeSlotBarber;
import com.example.appointment.R;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static android.R.color.white;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlot> timeSlotList;
    List<TimeSlotBarber> timeSlotBarberList;
    List<CardView> cardViewList;
    List<MyTimeSlotAdapter.MyViewHolder> myViewHolderList;
    LocalBroadcastManager localBroadcastManager;
    private int selectedPos = RecyclerView.NO_POSITION;
    int selected = -1;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    int count;
    int numberOfColumns;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        this.timeSlotBarberList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.myViewHolderList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<TimeSlot> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        this.myViewHolderList = new ArrayList<>();
        this.localBroadcastManager = LocalBroadcastManager.getInstance(context);
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_times_slot, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        count += i;

            holder.txt_time_slot.setText(Common.timeCards.get(count));
            if (timeSlotList.size() == 0) // if all posotion is available , just show list
            {
                holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                holder.timeslot_ll.setBackgroundResource(R.drawable.available_cardview_bookingfragment);
                holder.txt_time_slot_description.setText("פנוי");
                holder.card_time_slot.setClickable(true);
                holder.txt_time_slot_description.setTextColor(context.getColor(android.R.color.white));
                holder.txt_time_slot.setTextColor(context.getColor(android.R.color.white));
            } else // if have position is full (booked)
            {

                holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                holder.timeslot_ll.setBackgroundResource(R.drawable.available_cardview_bookingfragment);
                holder.txt_time_slot_description.setText("פנוי");
                holder.card_time_slot.setClickable(true);
                holder.txt_time_slot_description.setTextColor(context.getColor(android.R.color.white));
                holder.txt_time_slot.setTextColor(context.getColor(android.R.color.white));

                for (TimeSlot slotValue : timeSlotList) {
                    //So base on tag, we can set all remain card background without change full time slot
                    if (slotValue.getHour().equals(holder.txt_time_slot.getText())) {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                        holder.timeslot_ll.setBackgroundResource(R.drawable.not_available_timeslot_cardview);
                        holder.card_time_slot.setClickable(false);
                        holder.txt_time_slot_description.setText("תפוס");
                        holder.txt_time_slot_description.setTextColor(Color.parseColor("#4dffffff"));
                        holder.txt_time_slot.setTextColor(Color.parseColor("#4dffffff"));
                    }

                }
            }

        if(i == selectedPos)
        {
            holder.card_time_slot.setCardBackgroundColor(context.getColor(white));
            holder.txt_time_slot.setTextColor(context.getColor(R.color.AppMainColor));
            holder.txt_time_slot_description.setTextColor(context.getColor(R.color.AppMainColor));
            holder.card_time_slot.setSelected(!holder.card_time_slot.isSelected());
            holder.card_time_slot.setTag("SELECTED");
        }

        //Add all card to list
        //No add card already in cardViewList
        if (!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);

        if (!myViewHolderList.contains(holder))
            myViewHolderList.add(holder);

        //Check if card time slot if avilable
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectListener(View view, int pos) {
                //Loop all card in card List

                for (MyTimeSlotAdapter.MyViewHolder timeHolder : myViewHolderList) {
                    if (timeHolder.card_time_slot != holder.card_time_slot) {
                        Boolean busy = false;
                        for (TimeSlot ts : timeSlotList) {
                            if (timeHolder.txt_time_slot.getText().equals(ts.getHour())) {
                                busy = true;
                                break;
                            }
                        }
                        if (busy == false) {
                            timeHolder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                            timeHolder.timeslot_ll.setBackgroundResource(R.drawable.available_cardview_bookingfragment);
                            timeHolder.txt_time_slot.setTextColor(Color.WHITE);
                            holder.card_time_slot.setClickable(true);
                            timeHolder.txt_time_slot_description.setTextColor(Color.WHITE);
                        }
                    }
                }
                //Our selected card will be change color

                holder.card_time_slot.setCardBackgroundColor(context.getColor(white));
                holder.txt_time_slot.setTextColor(context.getColor(R.color.AppMainColor));
                holder.txt_time_slot_description.setTextColor(context.getColor(R.color.AppMainColor));
                holder.card_time_slot.setSelected(!holder.card_time_slot.isSelected());
                holder.card_time_slot.setTag("SELECTED");

                selectedPos = pos;


                //After that, send broadcast to enable button NEXT
                Common.bookingTimeSlotDate = holder.txt_time_slot.getText().toString();
                for (int k = 0; k < Common.TIME_SLOT_TOTAL; k++) {
                    if (Common.timeCards.get(k).equals(Common.bookingTimeSlotDate))
                        Common.bookingTimeSlotNumber = k;
                }
                Intent intent = new Intent(Common.KEY_ENABLE_BUTTON_NEXT);
                intent.putExtra(Common.KEY_TIME_SLOT, Common.bookingTimeSlotNumber);// Put index of time slot we have selected
                intent.putExtra(Common.KEY_STEP, 3); // Go to step 3
                localBroadcastManager.sendBroadcast(intent);
            }
        });

    }

    @Override
    public int getItemCount() {

        count = 0;
        Calendar todatDate = Calendar.getInstance();
        todatDate.add(Calendar.DATE, 0);

        if (Common.simpleDateFormat.format(Common.bookingDate.getTime()).equals(Common.simpleDateFormat.format(todatDate.getTime()))) {
            for (int k = 0; k < Common.TIME_SLOT_TOTAL; k++) {
                String temp = "";
                DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                String curTime = timeFormat.format(new Date());
                String currentTimeStr = "";
                String n = Common.timeCards.get(k);
                for (int j = 0; j < 4; j++) {
                    char c = n.charAt(j);
                    char t = curTime.charAt(j);
                    if (c != '-' && temp.length() < 4) {
                        if (c == ':') temp += '.';
                        else temp += c;
                    }
                    if (t != '-' && currentTimeStr.length() < 4) {
                        if (t == ':') currentTimeStr += '.';
                        else currentTimeStr += t;
                    }

                }
                Double slotTime = Double.parseDouble(temp);
                Double currentTime = Double.parseDouble(currentTimeStr);

                if (slotTime <= currentTime) {
                    count++;
                }

            }
        }

        //else count = 0;
        numberOfColumns = count;
        numberOfColumns = Common.TIME_SLOT_TOTAL - numberOfColumns;
        return numberOfColumns;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_time_slot, txt_time_slot_description;
        CardView card_time_slot;
        LinearLayout timeslot_ll;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            card_time_slot = (CardView) itemView.findViewById(R.id.card_time_slot);
            txt_time_slot = (TextView) itemView.findViewById(R.id.txt_time_slot);
            txt_time_slot_description = (TextView) itemView.findViewById(R.id.txt_time_slot_description);
            timeslot_ll = (LinearLayout) itemView.findViewById(R.id.booking_timeslot_card_ll);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectListener(view, getAdapterPosition());
        }
    }

}
