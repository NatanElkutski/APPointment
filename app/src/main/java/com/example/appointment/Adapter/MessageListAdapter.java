package com.example.appointment.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.SmoothScroller;

import com.example.appointment.Common.Common;
import com.example.appointment.Model.Message;
import com.example.appointment.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import dmax.dialog.SpotsDialog;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {

    List<Message> messageList;
    AlertDialog alertDialog;
    RecyclerView recyclerView;
    Context context;
    View v;
    LinearLayoutManager linearLayoutManager;

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public MessageListAdapter(Context context, RecyclerView recyclerView, List<Message> messageList, View v) {
        this.messageList = messageList;
        this.recyclerView = recyclerView;
        this.context = context;
        this.v = v;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);
        ViewHolder v = new ViewHolder(view, mListener);
        alertDialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();
        return v;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageListAdapter.ViewHolder holder, int position) {

        Message message = messageList.get(position);
        String sender = "מאת : " + message.getSenderName();
        holder.SalonName_txt.setText(sender);
        if (message.getSeen()) holder.notification_icon.setImageBitmap(null);
        holder.messageBody_txt.setText(message.getMessage());
        String dateReplace = message.getTime().replace("_", "/");
        String[] dateTimeApart = dateReplace.split(" ");
        String[] time = dateTimeApart[1].split(":");

        String date = "תאריך : " + dateTimeApart[0] + " " + time[0] + ":" + time[1];
        holder.MessageSentTime_txt.setText(date);
        String subject = "נושא : " + message.getSubject();
        holder.messageSubject_txt.setText(subject);


        Boolean isExpanded = message.getExpandable();
        if (isExpanded) {


            holder.relativeLayout.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            final int actualheight = holder.relativeLayout.getMeasuredHeight();
            holder.arroy_icon.animate().setDuration((long) (actualheight / holder.relativeLayout.getContext().getResources().getDisplayMetrics().density)).rotation(180);
            holder.relativeLayout.getLayoutParams().height = 0;
            holder.relativeLayout.setVisibility(View.VISIBLE);

            Animation animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {

                    holder.relativeLayout.getLayoutParams().height = (interpolatedTime == 1) ? ViewGroup.LayoutParams.WRAP_CONTENT : (int) (actualheight * interpolatedTime);
                    holder.relativeLayout.requestLayout();
                }
            };


            animation.setDuration((long) (actualheight /  holder.relativeLayout.getContext().getResources().getDisplayMetrics().density));

            holder.relativeLayout.startAnimation(animation);

        } else {

            if (holder.relativeLayout.getVisibility() == View.VISIBLE) {

                final int actualHeight = holder.relativeLayout.getMeasuredHeight();
                holder.arroy_icon.animate().setDuration((long) (actualHeight / holder.relativeLayout.getContext().getResources().getDisplayMetrics().density)).rotation(0);
                Animation animation = new Animation() {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {

                        if (interpolatedTime == 1) {
                            holder.relativeLayout.setVisibility(View.GONE);
                            holder.relativeLayout.getLayoutParams().height = actualHeight;
                        } else {
                            holder.relativeLayout.getLayoutParams().height = actualHeight - (int) (actualHeight * interpolatedTime);
                            holder.relativeLayout.requestLayout();

                        }
                    }
                };

                animation.setDuration((long) (actualHeight / holder.relativeLayout.getContext().getResources().getDisplayMetrics().density));
                holder.relativeLayout.startAnimation(animation);

            }
        }

    }

    @Override
    public int getItemCount() {
        if (messageList == null)
            return 0;
        else
            return messageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView SalonName_txt, MessageSentTime_txt, messageBody_txt, messageSubject_txt;
        LinearLayout linearLayout;
        RelativeLayout relativeLayout;
        Button messageDeleteBtn;
        ImageView notification_icon,arroy_icon;
        RelativeLayout mainLayout;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);

            SalonName_txt = itemView.findViewById(R.id.Message_sender_tv_id);
            MessageSentTime_txt = itemView.findViewById(R.id.MessageTime_id);
            messageBody_txt = itemView.findViewById(R.id.Message_body_tv);
            messageSubject_txt = itemView.findViewById(R.id.MessageSubject_id);
            messageDeleteBtn = itemView.findViewById(R.id.Message_item_delete_btn);
            notification_icon = itemView.findViewById(R.id.Message_notification_icon_id);
            arroy_icon = itemView.findViewById(R.id.Message_arroy_icon_id);
            mainLayout = itemView.findViewById(R.id.RLM_Messages);
            linearLayout = itemView.findViewById(R.id.linear_layout_message);
            relativeLayout = itemView.findViewById(R.id.expandable_layout);

            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    CollectionReference Reference = FirebaseFirestore.getInstance().collection("User").document(Common.fuser.getUid())
                            .collection("Messages");

                    Query query = Reference
                            .whereEqualTo("message", messageList.get(getAdapterPosition()).getMessage())
                            .whereEqualTo("timestamp", messageList.get(getAdapterPosition()).getTimestamp())
                            .whereEqualTo("seen", messageList.get(getAdapterPosition()).getSeen())
                            .whereEqualTo("time", messageList.get(getAdapterPosition()).getTime())
                            .whereEqualTo("senderName", messageList.get(getAdapterPosition()).getSenderName());

                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot qs = task.getResult();
                                if (!qs.isEmpty()) {
                                    for (QueryDocumentSnapshot qDS : qs) {
                                        Message message = qDS.toObject(Message.class);
                                        if (!message.getSeen()) {
                                            String docId = qDS.getId();
                                            DocumentReference docRef = FirebaseFirestore.getInstance()
                                                    .collection("User").document(Common.fuser.getUid()).collection("Messages").document(docId);

                                            docRef.update("seen", true);
                                            Common.seenMessages -= 1;
                                        }
                                    }
                                }
                            }
                        }
                    });

                    Boolean message = messageList.get(getAdapterPosition()).getExpandable();
                    messageList.get(getAdapterPosition()).setExpandable(!message);
                    notification_icon.setImageBitmap(null);
                    notifyItemChanged(getAdapterPosition());
                }
            });

            messageDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }

                    }
                }
            });

        }
    }

    public void removeItem(int position) {
            alertDialog.show();
            CollectionReference userBooking = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.fuser.getUid()).collection("Messages");

            userBooking.whereEqualTo("timestamp", messageList.get(position).getTimestamp()).whereEqualTo("senderName", messageList.get(position).getSenderName()).whereEqualTo("subject", messageList.get(position).getSubject()).whereEqualTo("message", messageList.get(position).getMessage())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String s = document.getId();
                            DocumentReference dc = userBooking.document(s);
                            dc.delete();
                            messageList.remove(position);
                            notifyItemRemoved(position);
                            notifyDataSetChanged();
                            alertDialog.dismiss();
                        }
                    }

                }
            });
    }

    public static void collapse(final View view) {

        final int actualHeight = view.getMeasuredHeight();

        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                if (interpolatedTime == 1) {
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = actualHeight - (int) (actualHeight * interpolatedTime);
                    view.requestLayout();

                }
            }
        };

        animation.setDuration((long) (actualHeight / view.getContext().getResources().getDisplayMetrics().density));
        view.startAnimation(animation);
    }
}
