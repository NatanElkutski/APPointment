package com.example.appointment.Service;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;

import com.example.appointment.Adapter.FutureBookingListAdapter;
import com.example.appointment.Adapter.MyItemTouchHelperCallBack;
import com.example.appointment.Common.Common;
import com.example.appointment.Interface.ITouchHelper;
import com.example.appointment.Model.FutureBooking;
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

import dmax.dialog.SpotsDialog;


public class FutureBookingFragment extends Fragment implements ITouchHelper {

    private static final String TAG = "FireLog";
    private RecyclerView recyclerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FutureBookingListAdapter futureBookingListAdapter;
    private List<FutureBooking> futureBookingList;
    private TextView activeEmpty;
    private LinearLayout emptyLinear;
    android.location.Address address1;
    ProgressDialog dialog;

    AlertDialog alertDialog;

    boolean deleteKey = true;

    RelativeLayout layout;


    public FutureBookingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("תורים עתידיים");
        dialog.setMessage("אנא המתן...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.show();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_futurebooking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alertDialog = new SpotsDialog.Builder().setContext(getActivity()).setCancelable(false).build();

        layout = view.findViewById(R.id.RLM_active);
        recyclerView = view.findViewById(R.id.active_recycler_view_id);
        futureBookingList = new ArrayList<>();
        activeEmpty = view.findViewById(R.id.txt_active_empty);
        emptyLinear = view.findViewById(R.id.ll_active_empty);
        initList();

    }

    private void initList() {

        CollectionReference mCollection = db.collection("User").document(Common.fuser.getUid())
                .collection("Booking");

        Query query = mCollection.whereEqualTo("done", false).orderBy("timestamp");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    if (task.getResult().getDocuments().size() == 0)
                        emptyLinear.setVisibility(View.VISIBLE);
                    else {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FutureBooking futureBooking = doc.toObject(FutureBooking.class);
                            futureBookingList.add(futureBooking);

                        }
                        futureBookingListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        futureBookingListAdapter = new FutureBookingListAdapter(getContext(), futureBookingList);
        recyclerView.setAdapter(futureBookingListAdapter);
        recyclerView.setHasFixedSize(true);
        ItemTouchHelper.Callback callback = new MyItemTouchHelperCallBack(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        futureBookingListAdapter.setOnItemClickListener(new FutureBookingListAdapter.OnItemClickListener() {
            @Override
            public void onNavigateClick(int position) {
                String cityAndAddress = futureBookingList.get(position).getSalonAddress()+" "+ futureBookingList.get(position).getSalonCity();
                String encodedAddress = cityAndAddress.replace(" ","+");
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + encodedAddress));
                startActivity(intent);
            }
        });

    }

    // 1st method for Address Coordinates (OPTIONAL)
    /*
    private void geoLocate(String address) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            List<android.location.Address> addressList = geocoder.getFromLocationName(address, 1);

            if (addressList.size() > 0) {
                address1 = addressList.get(0);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        futureBookingList.add(newPosition, futureBookingList.remove(oldPosition));
        futureBookingListAdapter.notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int position) {
        // we will delete and also want to undo
        String name = futureBookingList.get(viewHolder.getAdapterPosition()).getBarberName();

        // backup of removed item for undo
        final FutureBooking deletedHistory = futureBookingList.get(viewHolder.getAdapterPosition());
        final int deletedIndex = viewHolder.getAdapterPosition();

        //remove the item from recyclerview
        futureBookingListAdapter.removeItem(viewHolder.getAdapterPosition());

        //showing snackbar for undo
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity()).setTitle("ביטול פגישה עתידית").setMessage("אתה בטוח שאתה רוצה לבטל את הפגישה?").setCancelable(false);
        dialog.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                futureBookingListAdapter.removeFromDb();
                if (futureBookingList.size() == 0) emptyLinear.setVisibility(View.VISIBLE);
            }
        }).setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                futureBookingListAdapter.restoreItem(deletedHistory, deletedIndex);
            }
        });
        final AlertDialog alert = dialog.create();
        alert.show();

    }

    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 3;
        // how to use activity method from fragment
        ((MainActivity)getActivity()).changeNav();
    }
}