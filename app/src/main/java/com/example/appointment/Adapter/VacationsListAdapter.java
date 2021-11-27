package com.example.appointment.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appointment.R;

import java.util.List;

public class VacationsListAdapter extends RecyclerView.Adapter<VacationsListAdapter.ViewHolder>{
    private List<String> datesList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onDelete(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mListener = listener;
    }

    public VacationsListAdapter(List<String>datesList)
    {
        this.datesList = datesList;
    }

    @NonNull
    @Override
    public VacationsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vacations_list_item,parent,false);
        ViewHolder v = new ViewHolder(view,mListener);
        return v;
    }

    @Override
    public void onBindViewHolder(@NonNull VacationsListAdapter.ViewHolder holder, int position) {

        String newDateFormat = datesList.get(position).replace("_", "/");
        holder.dates_txt.setText(newDateFormat);
    }

    @Override
    public int getItemCount() {
        if(datesList == null)
            return 0;
        else
            return datesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        View mview;
        Button cancelBtn;
        public TextView dates_txt;

        public RelativeLayout viewF,viewB;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            mview = itemView;
            cancelBtn = (Button)itemView.findViewById(R.id.vacations_list_item_btn);
            dates_txt = (TextView) mview.findViewById(R.id.dates_id);
            viewF = itemView.findViewById(R.id.rl_vacations);

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDelete(position);
                        }

                    }
                }
            });
        }
    }

}
