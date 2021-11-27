package com.example.appointment.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appointment.Common.Common;
import com.example.appointment.Interface.IRecyclerItemSelectedListener;
import com.example.appointment.Management.ManagerTimeFragment;
import com.example.appointment.Management.TimeFragment;
import com.example.appointment.Model.BookingInformation;
import com.example.appointment.Model.Message;
import com.example.appointment.Model.TimeSlot;
import com.example.appointment.Model.TimeSlotBarber;
import com.example.appointment.Model.profileImage;
import com.example.appointment.R;
import com.example.appointment.SendNotificationPack.sendNotification;
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
import com.makeramen.roundedimageview.RoundedImageView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static android.R.color.white;

public class BarberTimeSlotAdapter extends RecyclerView.Adapter<BarberTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<TimeSlotBarber> timeSlotBarberList;
    List<CardView> cardViewList;
    Dialog mDialog;
    AlertDialog adialog;
    List<BarberTimeSlotAdapter.MyViewHolder> myViewHolderList;
    profileImage profileImage = new profileImage();
    Button callbtn, smsbtn, cancelbtn;
    private int selectedPos = RecyclerView.NO_POSITION;

    private final int REQUEST_PHONE_CALL = 1;


    TimeFragment mFragment;
    ManagerTimeFragment managerTimeFragment;

// While passing the fragment into your adapter, do it this way.


    public BarberTimeSlotAdapter(Context context) {
        this.context = context;
        this.timeSlotBarberList = new ArrayList<>();
        this.myViewHolderList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public BarberTimeSlotAdapter(Context context, List<TimeSlotBarber> timeSlotBarberList, TimeFragment mFragment) {
        this.context = context;
        this.timeSlotBarberList = timeSlotBarberList;
        this.mFragment = mFragment;
        this.myViewHolderList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public BarberTimeSlotAdapter(Context context, List<TimeSlotBarber> timeSlotBarberList, ManagerTimeFragment managerTimeFragment) {
        this.context = context;
        this.timeSlotBarberList = timeSlotBarberList;
        this.managerTimeFragment = managerTimeFragment;
        this.myViewHolderList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_times_slot, parent, false);
        adialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();
        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.txt_time_slot.setText(Common.timeCards.get(i));
        if (timeSlotBarberList.size() == 0) // if all posotion is available , just show list
        {
            holder.txt_time_slot_description.setText("פנוי");

            holder.card_time_slot.setClickable(false);
            holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
            holder.txt_time_slot_description.setTextColor(Color.parseColor("#4d0362E1"));
            holder.txt_time_slot.setTextColor(Color.parseColor("#4d0362E1"));
            holder.timeslot_ll.setBackgroundResource(R.drawable.barber_timeslot_cardview_empty);
        } else // if have position is full (booked)
        {
            holder.txt_time_slot_description.setText("פנוי");

            holder.card_time_slot.setClickable(false);
            holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
            holder.txt_time_slot_description.setTextColor(Color.parseColor("#4d0362E1"));
            holder.txt_time_slot.setTextColor(Color.parseColor("#4d0362E1"));
            holder.timeslot_ll.setBackgroundResource(R.drawable.barber_timeslot_cardview_empty);

            for (TimeSlotBarber slotValue : timeSlotBarberList) {
                //So base on tag, we can set all remain card background without change full time slot
                if (slotValue.getHour().equals(holder.txt_time_slot.getText())) {
                    holder.card_time_slot.setTag(Common.DISABLE_TAG);
                    holder.card_time_slot.setClickable(true);
                    holder.txt_time_slot_description.setText(slotValue.getCustomerName());
                    holder.card_time_slot.setCardBackgroundColor(context.getColor(android.R.color.transparent));
                    holder.timeslot_ll.setBackgroundResource(R.drawable.barber_timeslot_customer_cardview_selector);
                    holder.txt_time_slot_description.setTextColor(context.getColor(R.color.AppMainColor));
                    holder.txt_time_slot.setTextColor(context.getColor(R.color.AppMainColor));

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

        //Check if card time slot if avilable
        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectListener(View view, int pos) {
                //Loop all card in card List
                adialog.show();

                //Our selected card will be change color
                mDialog = new Dialog(context);
                mDialog.setContentView(R.layout.dialog_contact);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                callbtn = (Button) mDialog.findViewById(R.id.dialog_btn_call_id);
                smsbtn = (Button) mDialog.findViewById(R.id.dialog_btn_message_id);
                cancelbtn = (Button) mDialog.findViewById(R.id.dialog_btn_cancelApp_id);
                TextView dialog_name_tv = (TextView) mDialog.findViewById(R.id.dialog_name_id);
                TextView dialog_phone_tv = (TextView) mDialog.findViewById(R.id.dialog_phone_id);
                RoundedImageView dialog_contact_img = (RoundedImageView) mDialog.findViewById(R.id.dialog_image_id);
                for (TimeSlotBarber slotValue : timeSlotBarberList) {
                    if (slotValue.getHour().equals(holder.txt_time_slot.getText())) {
                        dialog_name_tv.setText(slotValue.getCustomerName());
                        dialog_phone_tv.setText(slotValue.getCustomerPhone());
                        String barberId = slotValue.getBarberId();
                        DocumentReference imgRef = FirebaseFirestore.getInstance().collection("User").document(slotValue.getCustomerUid()).collection("Uploads").document("profileImage");
                        imgRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().exists()) {
                                        adialog.dismiss();
                                        DocumentSnapshot ds = task.getResult();
                                        profileImage profileImg = ds.toObject(profileImage.class);
                                        profileImage.setImageUrl(profileImg.getImageUrl());
                                        //Picasso.get().load(profileImage.getImageUrl()).into(dialog_contact_img);
                                        Glide.with(context).load(profileImage.getImageUrl()).into(dialog_contact_img);
                                    } else {
                                        adialog.dismiss();
                                    }
                                    callbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                                            phoneIntent.setData(Uri.parse("tel:" + dialog_phone_tv.getText().toString()));
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                                            } else {
                                                context.startActivity(phoneIntent);
                                                if (mDialog.isShowing()) mDialog.dismiss();
                                            }
                                        }
                                    });
                                    smsbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
                                            String phoneNumber = dialog_phone_tv.getText().toString().trim();
                                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                                myMessage(phoneNumber);
                                            } else {
                                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.SEND_SMS}, 0);
                                            }

                                        }
                                    });
                                    cancelbtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String CustomerUid = slotValue.getCustomerUid();
                                            final AlertDialog.Builder dialog = new AlertDialog.Builder(context).setTitle("ביטול פגישה עתידית").setMessage("אתה בטוח שאתה רוצה לבטל את הפגישה?").setCancelable(false);
                                            dialog.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    adialog.show();
                                                    DocumentReference barberBooking = FirebaseFirestore.getInstance()
                                                            .collection("AllSalon")
                                                            .document(Common.currentUser.getSalonCity())
                                                            .collection("Branch")
                                                            .document(Common.currentUser.getSalonId())
                                                            .collection("Barbers")
                                                            .document(barberId)
                                                            .collection("Bookings")
                                                            .document("FutureBookings")
                                                            .collection(slotValue.getDate())
                                                            .document(slotValue.getHour());
                                                    barberBooking.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                barberBooking.delete();
                                                                //USER DATABASE
                                                                CollectionReference userBooking = FirebaseFirestore.getInstance()
                                                                        .collection("User")
                                                                        .document(slotValue.getCustomerUid()).collection("Booking");

                                                                userBooking.whereEqualTo("hour", slotValue.getHour()).whereEqualTo("date", slotValue.getDate()).whereEqualTo("barberId", barberId)
                                                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                        if (task.isSuccessful()) {
                                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                                String s = document.getId();
                                                                                BookingInformation bInfo = document.toObject(BookingInformation.class);
                                                                                DocumentReference dc = userBooking.document(s);
                                                                                dc.delete();

                                                                                String message = "שלום ";
                                                                                message += bInfo.getCustomerName() + "." + "\n";
                                                                                message += "תורך בתאריך : ";
                                                                                String userdate = bInfo.getDate().replace("_", "/");
                                                                                message += userdate + "\n";
                                                                                message += "בשעה : ";
                                                                                String[] hour = bInfo.getHour().split(" - ");
                                                                                String newHour = hour[1] + " - " + hour[0];
                                                                                message += newHour;
                                                                                message += "\n";
                                                                                message += "אצל ";
                                                                                message += bInfo.getBarberName() + " ";
                                                                                message += "בוטל. \nלפרטים נוספים אנא פנה למספרה. \nתודה.";

                                                                                String subject = "הודעה על ביטול תור קיים";

                                                                                Timestamp timestamp = null;
                                                                                try {
                                                                                    timestamp = Common.makeTimeStampMessage(Common.currentTimeWithDate());
                                                                                } catch (ParseException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                                Message messageToUserDb = new Message(bInfo.getSalonName(), subject, message, Common.currentTimeWithDate(), timestamp);

                                                                                CollectionReference userMessageRef = FirebaseFirestore.getInstance().collection("User")
                                                                                        .document(bInfo.getCustomerUid()).collection("Messages");

                                                                                String finalMessage = message;
                                                                                userMessageRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                        userMessageRef.document().set(messageToUserDb).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                            @Override
                                                                                            public void onSuccess(Void aVoid) {
                                                                                                String newDoc = userMessageRef.document().getId();
                                                                                                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Tokens").document(slotValue.getCustomerUid());
                                                                                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            DocumentSnapshot ds = task.getResult();
                                                                                                            String userToken = ds.get("token").toString().trim();
                                                                                                            sendNotification.sendNotifications(userToken, "Appointment-הודעה חדשה מ", finalMessage);
                                                                                                            adialog.dismiss();
                                                                                                            mDialog.dismiss();
                                                                                                            if(Common.currentUser.getPermission().equals("barber")) mFragment.cancelDate();
                                                                                                            else if (Common.currentUser.getPermission().equals("manager")) managerTimeFragment.cancelDate();
                                                                                                            Toast.makeText(context, "פגישה בוטלה בהצלחה!", Toast.LENGTH_SHORT).show();
                                                                                                        }

                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        });

                                                                                    }
                                                                                });
                                                                            }
                                                                        } else adialog.dismiss();

                                                                    }
                                                                });
                                                            } else adialog.dismiss();
                                                        }
                                                    });
                                                }
                                            }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    adialog.dismiss();
                                                }
                                            });
                                            final AlertDialog alert = dialog.create();
                                            alert.show();
                                        }
                                    });

                                    mDialog.show();
                                }
                            }
                        });

                    }

                }
            }
        });

    }


    private void myMessage(String phone) {
        String message = "תורך בוטל אנא פנה למספרה.";
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phone, null, message, null, null);
        Toast.makeText(context, "הודעה נשלחה בהצלחה!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
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
