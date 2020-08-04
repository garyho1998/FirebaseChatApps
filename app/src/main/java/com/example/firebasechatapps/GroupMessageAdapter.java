package com.example.firebasechatapps;


import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder>
{
    private String groupID, currentUserID;
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference msgRef;

    private String last_date = "";
    private String last_sender = "";

    public GroupMessageAdapter (String groupID, List<Messages> userMessagesList, String currentUserID)
    {
        this.groupID = groupID;
        this.userMessagesList = userMessagesList;
        this.currentUserID = currentUserID;

    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, sndTimeText, receiverMessageText, rcvTimeText, sndPicTime, rcvPicTime, dataText;
        public TextView rcvNameText;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            rcvNameText = (TextView) itemView.findViewById(R.id.rcv_name);
            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            sndTimeText = (TextView) itemView.findViewById(R.id.snd_time);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            rcvTimeText = (TextView) itemView.findViewById(R.id.rcv_time);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            sndPicTime = (TextView) itemView.findViewById(R.id.snd_time_pic);
            rcvPicTime = (TextView) itemView.findViewById(R.id.rcv_time_pic);
            dataText = (TextView) itemView.findViewById(R.id.date_text);

        }
    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.group_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        Messages messages = userMessagesList.get(i);

        String from = messages.getFrom();
        String fromMessageType ="";
        if (messages.getType()!=null) {
            fromMessageType = messages.getType();
        }


        msgRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID).child("Message");


        messageViewHolder.rcvNameText.setVisibility(View.GONE);
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.rcvTimeText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.sndTimeText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.sndPicTime.setVisibility(View.GONE);
        messageViewHolder.rcvPicTime.setVisibility(View.GONE);
        messageViewHolder.dataText.setVisibility(View.GONE);

        if ( !messages.getDate().equals(last_date) ) {
            messageViewHolder.dataText.setVisibility(View.VISIBLE);
            messageViewHolder.dataText.setText(messages.getDate());
            last_date = messages.getDate();
            last_sender = "";
        }

        if ( !messages.getName().equals(last_sender) && !from.equals(currentUserID)) {
            messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
            messageViewHolder.rcvNameText.setText(messages.getName());
            last_sender = messages.getName();
        }

        if (fromMessageType.equals("image")) {
            if (from.equals(currentUserID)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.sndPicTime.setVisibility(View.VISIBLE);

                messageViewHolder.sndPicTime.setText(messages.getTime());
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.rcvPicTime.setVisibility(View.VISIBLE);

                messageViewHolder.rcvPicTime.setText(messages.getTime());
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }
        } else { //text message
            if (from.equals(currentUserID))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.sndTimeText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.sndTimeText.setText(messages.getTime());
            }
            else
            {
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.rcvTimeText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);

                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.rcvTimeText.setText(messages.getTime());
            }
        }



    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

}
