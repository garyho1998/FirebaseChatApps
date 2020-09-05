package com.threebeebox.firebasechatapps;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import sun.bob.mcalendarview.vo.DateData;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder>
{
    private Context mcon;
    private String groupID, currentUserID;
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference msgRef;

    private Calendar today;
    private String sToday = "";
    private static final int GroupChatType = 1;

    public GroupMessageAdapter (Context con, String groupID, List<Messages> userMessagesList, String currentUserID)
    {
        this.mcon = con;
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

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
            sToday = currentDate.format(calendar.getTime());

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
        final Messages messages = userMessagesList.get(i);

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

        // display date view
        if (i>0) {
            Messages pm = userMessagesList.get(i-1);
            if ( !messages.getDate().equals(pm.getDate()) ) {
                messageViewHolder.dataText.setVisibility(View.VISIBLE);
                if ( messages.getDate().equals(sToday) ) {
                    messageViewHolder.dataText.setText("Today");
                } else {
                    messageViewHolder.dataText.setText(messages.getDate());
                }
                //besides a new date view, also add a new receiver view if not from current user
                if (!from.equals(currentUserID)) {
                    messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
                    messageViewHolder.rcvNameText.setText( messages.getName() );
                }
            }
        } else if (i==0)  {
            messageViewHolder.dataText.setVisibility(View.VISIBLE);
            if ( messages.getDate().equals(sToday) ) {
                messageViewHolder.dataText.setText("Today");
            } else {
                messageViewHolder.dataText.setText(messages.getDate());
            }
        }

        //display user name view
        if (i>0) {
            Messages pm = userMessagesList.get(i-1);
            if ( !messages.getFrom().equals(pm.getFrom()) && !messages.getFrom().equals(currentUserID) ) {
                messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
                messageViewHolder.rcvNameText.setText( messages.getName() );
            }
        } else if (i==0)  {
            if (!from.equals(currentUserID)) {
                messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
                messageViewHolder.rcvNameText.setText( messages.getName() );
            }
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

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imageIntent = new Intent(mcon, ImageViewActivity.class);
                    imageIntent.putExtra("imageID", messages.getMessage());
                    mcon.startActivity(imageIntent);

                }
            });

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

    private String FindUserName(String from) {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(from);
        Log.d("findUserName", "ref: " + UserRef.toString());
        final String[] name = {""};
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("name")){
                    name[0] = dataSnapshot.child("name").getValue().toString();
                }else{
                    name[0] = dataSnapshot.child("phoneNumber").getValue().toString();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Log.d("findUserName", "return: " + name[0]);
        return name[0];
    }


    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

}
