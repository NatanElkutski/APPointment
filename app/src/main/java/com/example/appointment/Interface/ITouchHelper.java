package com.example.appointment.Interface;

import androidx.recyclerview.widget.RecyclerView;

public interface ITouchHelper {

    void itemTouchOnMove(int oldPosition, int newPosition);

    void onSwiped(RecyclerView.ViewHolder viewHolder, int position);
}
