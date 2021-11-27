package com.example.appointment.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.HistoryBooking;
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

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.ViewHolder> {

    private final List<HistoryBooking> historyBookingList;
    private final Context context;
    AlertDialog alertDialog;

    public HistoryListAdapter(Context context, List<HistoryBooking> historyBookingList) {
        this.historyBookingList = historyBookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_list_item, parent, false);
        alertDialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.barberName_txt.setText(historyBookingList.get(position).getBarberName());
        holder.salonName_txt.setText(historyBookingList.get(position).getSalonName());
        holder.salonAddress_txt.setText(historyBookingList.get(position).getSalonAddress());
        holder.appointmentTime_txt.setText(historyBookingList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        if (historyBookingList == null)
            return 0;
        else
            return historyBookingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View mview;

        public TextView barberName_txt;
        public TextView salonName_txt;
        public TextView salonAddress_txt;
        public TextView appointmentTime_txt;
        public RelativeLayout viewF, viewB;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mview = itemView;

            barberName_txt = mview.findViewById(R.id.barbenName_id);
            salonName_txt = mview.findViewById(R.id.salonName_id);
            salonAddress_txt = mview.findViewById(R.id.salonAddress_id);
            appointmentTime_txt = mview.findViewById(R.id.appointmentTime_id);
            viewF = itemView.findViewById(R.id.rl);
            viewB = itemView.findViewById(R.id.view_background);
        }
    }

    public void removeItem(int position) {
        Common.HistoryHour = historyBookingList.get(position).getHour();
        Common.HistoryBarberId = historyBookingList.get(position).getBarberId();

        historyBookingList.remove(position);
        // this will update recyclerview means refresh it
        notifyItemRemoved(position);
    }

    public void restoreItem(HistoryBooking historyBooking, int position) {
        historyBookingList.add(position, historyBooking);

        notifyItemInserted(position);
    }

    public void removeFromDb() {

        alertDialog.show();
        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.fuser.getUid()).collection("Booking");

        userBooking.whereEqualTo("hour", Common.HistoryHour).whereEqualTo("barberId", Common.HistoryBarberId)
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
