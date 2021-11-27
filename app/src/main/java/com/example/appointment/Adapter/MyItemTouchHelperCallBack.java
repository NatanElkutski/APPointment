package com.example.appointment.Adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointment.Interface.ITouchHelper;

public class MyItemTouchHelperCallBack extends ItemTouchHelper.Callback {

    ITouchHelper iTouchHelper;

    public MyItemTouchHelperCallBack(ITouchHelper iTouchHelper) {
        this.iTouchHelper = iTouchHelper;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // in this method we will get movement of the recyclerview history item
        final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        final int swipeFlags = ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // in this method when we move our item then what we do we just get position and send in interface

        iTouchHelper.itemTouchOnMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        iTouchHelper.onSwiped(viewHolder, viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        } else {
            if (viewHolder instanceof HistoryListAdapter.ViewHolder) {
                final View forigroundView = ((HistoryListAdapter.ViewHolder) viewHolder).viewB;
                getDefaultUIUtil().onDrawOver(c, recyclerView, forigroundView, dX, dY, actionState, isCurrentlyActive);
            }
            if (viewHolder instanceof FutureBookingListAdapter.ViewHolder) {
                final View forigroundView = ((FutureBookingListAdapter.ViewHolder) viewHolder).viewB;
                getDefaultUIUtil().onDrawOver(c, recyclerView, forigroundView, dX, dY, actionState, isCurrentlyActive);
            }
        }
        // in this we will show delete button when we swipe
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        if (actionState != ItemTouchHelper.ACTION_STATE_DRAG) {
            if (viewHolder instanceof HistoryListAdapter.ViewHolder) {
                final View forigroundView = ((HistoryListAdapter.ViewHolder) viewHolder).viewF;
                getDefaultUIUtil().onDraw(c, recyclerView, forigroundView, dX, dY, actionState, isCurrentlyActive);
            }
            if (viewHolder instanceof FutureBookingListAdapter.ViewHolder) {
                final View forigroundView = ((FutureBookingListAdapter.ViewHolder) viewHolder).viewF;
                getDefaultUIUtil().onDraw(c, recyclerView, forigroundView, dX, dY, actionState, isCurrentlyActive);
            }
        }
        // when we swipe so with smooth animation
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        //super.clearView(recyclerView, viewHolder);
        if (viewHolder instanceof HistoryListAdapter.ViewHolder) {
            final View forigroundView = ((HistoryListAdapter.ViewHolder) viewHolder).viewF;
            //this will set color of item when we drag and leave any position so we want do it original color
            //forigroundView.setBackgroundColor(ContextCompat.getColor(((HistoryListAdapter.ViewHolder)viewHolder).viewF.getContext(), R.color.colorWhite));
            getDefaultUIUtil().clearView(forigroundView);
        }
        if (viewHolder instanceof FutureBookingListAdapter.ViewHolder) {
            final View forigroundView = ((FutureBookingListAdapter.ViewHolder) viewHolder).viewF;
            //this will set color of item when we drag and leave any position so we want do it original color
            //forigroundView.setBackgroundColor(ContextCompat.getColor(((HistoryListAdapter.ViewHolder)viewHolder).viewF.getContext(), R.color.colorWhite));
            getDefaultUIUtil().clearView(forigroundView);
        }

        //this will clear view when we swipe and drag
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        //in this we change color then item selected ok
        if (viewHolder != null) {
            if (viewHolder instanceof HistoryListAdapter.ViewHolder) {
                final View foregroundView = ((HistoryListAdapter.ViewHolder) viewHolder).viewF;
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    foregroundView.setBackgroundColor(Color.LTGRAY);
                }
                getDefaultUIUtil().onSelected(foregroundView);
            }
            if (viewHolder instanceof FutureBookingListAdapter.ViewHolder) {
                final View foregroundView = ((FutureBookingListAdapter.ViewHolder) viewHolder).viewF;
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    foregroundView.setBackgroundColor(Color.LTGRAY);
                }
                getDefaultUIUtil().onSelected(foregroundView);
            }
        }
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

}
