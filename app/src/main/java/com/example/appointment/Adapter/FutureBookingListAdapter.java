package com.example.appointment.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.FutureBooking;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class FutureBookingListAdapter extends RecyclerView.Adapter<FutureBookingListAdapter.ViewHolder> {

    private final List<FutureBooking> futureBookingList;
    private OnItemClickListener mListener;
    private final Context context;
    AlertDialog alertDialog;

    public interface OnItemClickListener {
        void onNavigateClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public FutureBookingListAdapter(Context context, List<FutureBooking> futureBookingList) {
        this.futureBookingList = futureBookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public FutureBookingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_list_item, parent, false);
        ViewHolder v = new ViewHolder(view, mListener);
        alertDialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();

        return v;
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.barberName_txt.setText(futureBookingList.get(position).getBarberName());
        holder.salonName_txt.setText(futureBookingList.get(position).getSalonName());
        holder.salonAddress_txt.setText(futureBookingList.get(position).getSalonAddress());
        holder.appointmentTime_txt.setText(futureBookingList.get(position).getTime());
        holder.activeNavigate.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("white")));

    }

    @Override
    public int getItemCount() {
        if (futureBookingList == null)
            return 0;
        else
            return futureBookingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public TextView barberName_txt;
        public TextView salonName_txt;
        public TextView salonAddress_txt;
        public TextView appointmentTime_txt;
        public RelativeLayout viewF, viewB;
        public ImageView activeNavigate;
        Layer layers;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            mview = itemView;

            barberName_txt = mview.findViewById(R.id.barbenName_id);
            salonName_txt = mview.findViewById(R.id.salonName_id);
            salonAddress_txt = mview.findViewById(R.id.salonAddress_id);
            appointmentTime_txt = mview.findViewById(R.id.appointmentTime_id);
            activeNavigate = mview.findViewById(R.id.navigation_icon_active);

            viewF = itemView.findViewById(R.id.rl);
            viewB = itemView.findViewById(R.id.view_background);

            activeNavigate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onNavigateClick(position);
                        }

                    }
                }
            });
        }
    }

    public void removeItem(int position) {

        Common.ActiveHour = futureBookingList.get(position).getHour();
        Common.ActiveBarberId = futureBookingList.get(position).getBarberId();
        Common.ActiveSalonid = futureBookingList.get(position).getSalonId();
        Common.ActiveDate = futureBookingList.get(position).getDate();
        Common.ActiveSalonCity = futureBookingList.get(position).getSalonCity();

        futureBookingList.remove(position);
        // this will update recyclerview means refresh it
        notifyItemRemoved(position);
    }

    public void restoreItem(FutureBooking futureBooking, int position) {
        futureBookingList.add(position, futureBooking);

        notifyItemInserted(position);
    }

    public void removeFromDb() {
        //BARBER DATABASE
        alertDialog.show();
        DocumentReference barberBooking = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.ActiveSalonCity).collection("Branch")
                .document(Common.ActiveSalonid)
                .collection("Barbers")
                .document(Common.ActiveBarberId)
                .collection("Bookings")
                .document("FutureBookings")
                .collection(Common.ActiveDate)
                .document(Common.ActiveHour);
        barberBooking.delete();

        //USER DATABASE
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.fuser.getUid()).collection("Booking");

        userBooking.whereEqualTo("hour", Common.ActiveHour)
                .whereEqualTo("date", Common.ActiveDate)
                .whereEqualTo("barberId", Common.ActiveBarberId)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String s = document.getId();
                        DocumentReference dc = userBooking.document(s);
                        dc.delete();
                        alertDialog.dismiss();
                    }
                }

            }
        });
    }
}
