package com.example.appointment.Service;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.appointment.Adapter.MessageListAdapter;
import com.example.appointment.Common.Common;
import com.example.appointment.Model.Message;
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

public class MessagesFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    RecyclerView recyclerView;
    MessageListAdapter messageListAdapter;
    List<Message> messageList;
    ProgressDialog dialog;
    LinearLayoutManager linearLayoutManager;


    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Common.seenMessages = 0;
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("הודעות");
        dialog.setMessage("אנא המתן...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.show();
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        initList();

        messageListAdapter = new MessageListAdapter(getContext(), recyclerView, messageList,getView());

        recyclerView = view.findViewById(R.id.messages_recycler_view_id);


        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(messageListAdapter);
        recyclerView.setHasFixedSize(true);


        messageListAdapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(int position) {
                messageListAdapter.removeItem(position);
            }
        });

    }

    private void initList() {

        messageList = new ArrayList<>();

        CollectionReference mCollection = db.collection("User").document(Common.fuser.getUid())
                .collection("Messages");

        Query query = mCollection.orderBy("timestamp", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Message message = doc.toObject(Message.class);

                        messageList.add(message);
                    }
                    messageListAdapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Common.fragmentPage = 2;
        ((MainActivity)getActivity()).changeNav();
    }

}