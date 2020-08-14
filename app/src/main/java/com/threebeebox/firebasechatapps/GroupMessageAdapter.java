package com.threebeebox.firebasechatapps;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import sun.bob.mcalendarview.vo.DateData;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.MessageViewHolder>
{
    private Context mcon;
    private String groupID, currentUserID;
    private List<ChatMessage> userChatMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference msgRef;

    private Calendar today;
    private String sToday = "";
    private static final int GroupChatType = 1;

    public GroupMessageAdapter (Context con, String groupID, List<ChatMessage> userChatMessageList, String currentUserID)
    {
        this.mcon = con;
        this.groupID = groupID;
        this.userChatMessageList = userChatMessageList;
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

            today = Calendar.getInstance();
            int dd = today.get(Calendar.DAY_OF_MONTH);
            int mm = today.get(Calendar.MONTH);
            int yyyy = today.get(Calendar.YEAR);
            DateData todayDate = new DateData(yyyy, mm, dd);
            int month = todayDate.getMonth()+1;
            if (todayDate.getDay()<10) {
                sToday = TransferMonth(month) + " 0" + todayDate.getDay() + ", " + todayDate.getYear();
            } else {
                sToday = TransferMonth(month) + " " + todayDate.getDay() + ", " + todayDate.getYear();
            }

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
        final ChatMessage chatMessage = userChatMessageList.get(i);

        String from = chatMessage.getFrom();
        String fromMessageType ="";
        if (chatMessage.getType()!=null) {
            fromMessageType = chatMessage.getType();
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

        if (i>1) {
            ChatMessage pm = userChatMessageList.get(i-1);
            if ( !chatMessage.getDate().equals(pm.getDate()) ) {
                messageViewHolder.dataText.setVisibility(View.VISIBLE);
                if ( chatMessage.getDate().equals(sToday) ) {
                    messageViewHolder.dataText.setText("Today");
                } else {
                    messageViewHolder.dataText.setText(chatMessage.getDate());
                }
                //besides a new date view, also add a new receiver view if not from current user
                if (!from.equals(currentUserID)) {
                    messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
                    messageViewHolder.rcvNameText.setText(chatMessage.getName());
                }
            }
        } else if (i==0)  {
            messageViewHolder.dataText.setVisibility(View.VISIBLE);
            if ( chatMessage.getDate().equals(sToday) ) {
                messageViewHolder.dataText.setText("Today");
            } else {
                messageViewHolder.dataText.setText(chatMessage.getDate());
            }
        }

        if (i>1) {
            ChatMessage pm = userChatMessageList.get(i-1);
            if ( !chatMessage.getName().equals(pm.getName()) && !from.equals(currentUserID) ) {
                messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
                messageViewHolder.rcvNameText.setText(chatMessage.getName());
            }
        } else if (i==0)  {
            messageViewHolder.rcvNameText.setVisibility(View.VISIBLE);
            messageViewHolder.rcvNameText.setText(chatMessage.getName());
        }



        if (fromMessageType.equals("image")) {
            if (from.equals(currentUserID)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.sndPicTime.setVisibility(View.VISIBLE);

                messageViewHolder.sndPicTime.setText(chatMessage.getTime());
                Picasso.get().load(chatMessage.getMessage()).into(messageViewHolder.messageSenderPicture);
            } else {
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.rcvPicTime.setVisibility(View.VISIBLE);

                messageViewHolder.rcvPicTime.setText(chatMessage.getTime());
                Picasso.get().load(chatMessage.getMessage()).into(messageViewHolder.messageReceiverPicture);
            }

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imageIntent = new Intent(mcon, ImageViewActivity.class);
                    imageIntent.putExtra("imageID", chatMessage.getMessage());
                    mcon.startActivity(imageIntent);

                }
            });

        } else { //text message
            if (from.equals(currentUserID))
            {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.sndTimeText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(chatMessage.getMessage());
                messageViewHolder.sndTimeText.setText(chatMessage.getTime());
            }
            else
            {
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.rcvTimeText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);

                messageViewHolder.receiverMessageText.setText(chatMessage.getMessage());
                messageViewHolder.rcvTimeText.setText(chatMessage.getTime());
            }
        }




    }




    @Override
    public int getItemCount()
    {
        return userChatMessageList.size();
    }

    private String TransferMonth(int month) {
        switch (month){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return null;
        }

    }

}
