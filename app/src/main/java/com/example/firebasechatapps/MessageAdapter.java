package com.example.firebasechatapps;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sun.bob.mcalendarview.vo.DateData;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private Context mcon;
    private String rcvID;
    private Boolean inContact = false;

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String last_date = "";
    private Calendar today;
    private String sToday = "";

    public MessageAdapter (Context con,String rcvID, List<Messages> userMessagesList)
    {
        this.mcon = con;
        this.rcvID = rcvID;
        this.userMessagesList = userMessagesList;
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, sndTimeText, receiverMessageText, rcvTimeText, sndPicTime, rcvPicTime, dataText, inContactText;
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
            inContactText = (TextView) itemView.findViewById(R.id.in_contact_text);

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

    /*
    private Boolean checkIfInContact(final String rcvID) {
        final Boolean[] in = {false};
        final int[] changed = {0};
        final DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        Log.d("checkContact", "contactsRef: " + contactsRef.toString());

        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Iterator<DataSnapshot> contactItems = dataSnapshot.getChildren().iterator();

                while (contactItems.hasNext()) {
                    DataSnapshot item = contactItems.next();
                    Log.d("checkContact", "checking contact: " + item.getKey() + "   with " + rcvID + "\nresult = " + Boolean.toString(item.getKey().equals(rcvID)));
                    if (item.getKey().equals(rcvID)) {
//                        inContact = true;
                        in[0] = true;
                        changed[0] = 1;
                        Log.d("checkContact", "in[0] after checking equal: " + Boolean.toString(in[0]));

                        break;
                    }
                }
                in[0] = false;
                changed[0] = 1;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();

            }
        }, 3000);   //5 seconds
        Log.d("checkContact", "in[0] RETURN in func: " + Boolean.toString(in[0]));
        return in[0];
    }*/


    private Boolean checkIfInContact(final String rcvID) {
        final Boolean[] exist = {null};
        final DatabaseReference contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        /*
        Log.d("checkContact", "hash code = " + Integer.toString(contactsRef.child(rcvID).hashCode()));
        Log.d("checkContact", "hash code = " + "");
        contactsRef.child(rcvID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("checkContact", "exist? " + Boolean.toString(dataSnapshot.exists()));
                if (dataSnapshot.exists()) {
                    exist[0] = true;
                    inContact = true;
                    Log.d("checkContact", "inContact? " + Boolean.toString(inContact));
                } else {
                    exist[0] = false;
                    inContact = false;
                    Log.d("checkContact", "inContact? " + Boolean.toString(inContact));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        contactsRef.orderByKey().equalTo(rcvID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()){
                            inContact = true;
                            Log.d("checkContact", "inContact = " + Boolean.toString(inContact));
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.exists()){
                            inContact = true;
                            Log.d("checkContact", "inContact = " + Boolean.toString(inContact));
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        Log.d("checkContact", "inContact returned in func " + Boolean.toString(inContact));
        return inContact;
    }





    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(i);

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
        messageViewHolder.inContactText.setVisibility(View.GONE);


        if (i==0) { //display reminder if sender not in contact
//            Log.d("checkContact", "func inContact return " + Boolean.toString(checkIfInContact(rcvID)));

            if (!checkIfInContact(rcvID)) {
//                messageViewHolder.inContactText.setVisibility(View.VISIBLE);
                messageViewHolder.inContactText.setText("NO, not in your contact");
                Toast.makeText(mcon, "user NOT in your contact!", Toast.LENGTH_SHORT).show();
            } else {
//                messageViewHolder.inContactText.setVisibility(View.VISIBLE);
                messageViewHolder.inContactText.setText("YES, in your contact");
                Toast.makeText(mcon, "user IN your contact!", Toast.LENGTH_SHORT).show();
            }
        }



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

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imageIntent = new Intent(mcon, ImageViewActivity.class);
                    imageIntent.putExtra("imageID", messages.getMessage());
                    mcon.startActivity(imageIntent);

                }
            });
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
