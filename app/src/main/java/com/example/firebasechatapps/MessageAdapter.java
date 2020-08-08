package com.example.firebasechatapps;


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

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sun.bob.mcalendarview.vo.DateData;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String last_date = "";
    private Calendar today;
    private String sToday = "";

    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, sndTimeText, receiverMessageText, rcvTimeText, sndPicTime, rcvPicTime, dataText;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

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


            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            sndTimeText = (TextView) itemView.findViewById(R.id.snd_time);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            rcvTimeText = (TextView) itemView.findViewById(R.id.rcv_time);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
            sndPicTime = (TextView) itemView.findViewById(R.id.snd_time_pic);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            rcvPicTime = (TextView) itemView.findViewById(R.id.rcv_time_pic);
            dataText = (TextView) itemView.findViewById(R.id.date_text);

        }
    }




    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.rcvTimeText.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.sndTimeText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
        messageViewHolder.sndPicTime.setVisibility(View.GONE);
        messageViewHolder.rcvPicTime.setVisibility(View.GONE);
        messageViewHolder.dataText.setVisibility(View.GONE);

        /*
        if ( !messages.getDate().equals(last_date) ) {
            messageViewHolder.dataText.setVisibility(View.VISIBLE);
            messageViewHolder.dataText.setText(messages.getDate());
            last_date = messages.getDate();
        }*/
        if (i>1) {
            Messages pm = userMessagesList.get(i-1);
            if ( !messages.getDate().equals(pm.getDate()) ) {
                messageViewHolder.dataText.setVisibility(View.VISIBLE);
                if ( messages.getDate().equals(sToday) ) {
                    messageViewHolder.dataText.setText("Today");
                } else {
                    messageViewHolder.dataText.setText(messages.getDate());
                }
            }
        } else if (i==0)  { //display date view whenever this is the first message
            messageViewHolder.dataText.setVisibility(View.VISIBLE);
            if ( messages.getDate().equals(sToday) ) {
                messageViewHolder.dataText.setText("Today");
            } else {
                messageViewHolder.dataText.setText(messages.getDate());
            }
        }


        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderId))
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

                //messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.rcvTimeText.setText(messages.getTime());
            }
        } else if (fromMessageType.equals("image")) {
            if (fromUserID.equals(messageSenderId)) {
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
        }
    }




    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
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
