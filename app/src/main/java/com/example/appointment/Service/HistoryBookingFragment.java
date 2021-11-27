package com.example.appointment.Service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.appointment.Adapter.HistoryListAdapter;
import com.example.appointment.Adapter.MyItemTouchHelperCallBack;
import com.example.appointment.Common.Common;
import com.example.appointment.Interface.ITouchHelper;
import com.example.appointment.Model.HistoryBooking;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;


public class HistoryBookingFragment extends Fragment implements ITouchHelper {

    private RecyclerView recyclerView;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private HistoryListAdapter historyListAdapter;
    private List<HistoryBooking> historyBookingList;
    private LinearLayout emptyLinear;
    ProgressDialog dialog;
    RelativeLayout layout;

    public HistoryBookingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("היסטוריה");
        dialog.setMessage("אנא המתן...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.show();
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layout = view.findViewById(R.id.RLM_history);
        recyclerView = view.findViewById(R.id.history_recycler_view_id);
        historyBookingList = new ArrayList<>();
        emptyLinear = view.findViewById(R.id.empty_history_ll);
        initList();
    }

    private void initList() {
        CollectionReference mCollection = db.collection("User").document(Common.fuser.getUid())
                .collection("Booking");

        Query query = mCollection.whereEqualTo("done", true).orderBy("timestamp");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    if (task.getResult().getDocuments().size() == 0)
                        emptyLinear.setVisibility(View.VISIBLE);
                    else {
                        emptyLinear.setVisibility(View.GONE);
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            HistoryBooking historyBooking = doc.toObject(HistoryBooking.class);
                            historyBookingList.add(historyBooking);
                        }
                        historyListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        historyListAdapter = new HistoryListAdapter(getContext(), historyBookingList);
        recyclerView.setAdapter(historyListAdapter);
        recyclerView.setHasFixedSize(true);
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallBack(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        historyBookingList.add(newPosition, historyBookingList.remove(oldPosition));
        historyListAdapter.notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int position) {
        // we will delete and also want to undo
        String name = historyBookingList.get(viewHolder.getAdapterPosition()).getBarberName();

        // backup of removed item for undo
        final HistoryBooking deletedHistoryBooking = historyBookingList.get(viewHolder.getAdapterPosition());
        final int deletedIndex = viewHolder.getAdapterPosition();

        //remove the item from recyclerview
        historyListAdapter.removeItem(viewHolder.getAdapterPosition());

        //showing snackbar for undo
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle("מחיקת פגישה").setMessage("אתה בטוח שאתה רוצה למחוק את הפגישה?");
        dialog.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                historyListAdapter.removeFromDb();
                if (historyBookingList.size() == 0) emptyLinear.setVisibility(View.VISIBLE);
            }
        }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                historyListAdapter.restoreItem(deletedHistoryBooking, deletedIndex);
            }
        });
        final AlertDialog alert = dialog.create();
        alert.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity)getActivity()).changeNav();
    }
}