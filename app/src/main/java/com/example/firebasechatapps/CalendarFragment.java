package com.example.firebasechatapps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.vo.DateData;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";

    private View calendarFragmentView;
    private MCalendarView mCalendarView;
    private TextView mDateTextView;
    private RecyclerView mDelayMsgRecyclerList;

    private DatabaseReference GroupRef, GroupNameRef, DelayMsgRef;
    private Query query;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        calendarFragmentView = inflater.inflate(R.layout.fragment_calendar, container, false);

        mCalendarView = (MCalendarView) calendarFragmentView.findViewById(R.id.calendarView);
        mDateTextView = (TextView) calendarFragmentView.findViewById(R.id.dateView);
        mDelayMsgRecyclerList = (RecyclerView) calendarFragmentView.findViewById(R.id.msgView);
        mDelayMsgRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));


        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        return calendarFragmentView;
    }


    @Override
    public void onStart()
    {
        super.onStart();

        mCalendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {

                final String selectedDate = TransferMonth(date.getMonth()) + " " + date.getDay() + ", " + date.getYear();
                mDateTextView.setText(selectedDate);

                GroupRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                        while (items.hasNext()) {
                            DataSnapshot item = items.next();
                            final String itemKey = item.getKey();
                            GroupNameRef = item.getRef();
//                            GroupNameRef = GroupRef.child("Group2");
                            GroupNameRef = GroupRef.child(itemKey);
                            DelayMsgRef = GroupNameRef.child("DelayMessage");

                            //--
                            Toast.makeText(getContext(), "itemKey: " + itemKey, Toast.LENGTH_SHORT).show();

                            //---
                            Toast.makeText(getContext(), "Retrieving  " + DelayMsgRef.toString(), Toast.LENGTH_SHORT).show();

                            query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);
                            FirebaseRecyclerOptions<DelayMsg> options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();

                            FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter =
                                    new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                                        @Override
                                        protected void onBindViewHolder(@NonNull final DelayMsgViewHolder delayMsgViewHolder, int i, @NonNull final DelayMsg delayMsg) {
                                            delayMsgViewHolder.delayMsg.setText(delayMsg.getMessage());
                                            delayMsgViewHolder.displayTime.setText("Scheduled to send at " + delayMsg.getDisplayTime() + " to " + DelayMsgRef.getParent().getKey());
                                        }

                                        @NonNull
                                        @Override
                                        public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delay_msg_display_layout, parent, false);
                                            // create a new layout
                                            DelayMsgViewHolder viewHolder = new DelayMsgViewHolder(view);
                                            return viewHolder;
                                        }
                                    };

                            mDelayMsgRecyclerList.setAdapter(adapter);
                            adapter.startListening();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
                    }
                });



                /*
                GroupNameRef = GroupRef.child("Group2");
                DelayMsgRef = GroupNameRef.child("DelayMessage");
                query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);
                FirebaseRecyclerOptions<DelayMsg> options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();

                FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter =
                        new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull final DelayMsgViewHolder delayMsgViewHolder, int i, @NonNull final DelayMsg delayMsg) {
                                delayMsgViewHolder.delayMsg.setText(delayMsg.getMessage());
                                delayMsgViewHolder.displayTime.setText("Scheduled to send at " + delayMsg.getDisplayTime());
                            }

                            @NonNull
                            @Override
                            public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delay_msg_display_layout, parent, false);
                                // create a new layout
                                DelayMsgViewHolder viewHolder = new DelayMsgViewHolder(view);
                                return viewHolder;
                            }
                        };

                mDelayMsgRecyclerList.setAdapter(adapter);
                adapter.startListening();*/
            }
        });
    }

    public static class DelayMsgViewHolder extends RecyclerView.ViewHolder
    {
        TextView delayMsg, displayTime;

        public DelayMsgViewHolder(@NonNull View itemView)
        {
            super(itemView);
            delayMsg = itemView.findViewById(R.id.delay_msg);
            displayTime = itemView.findViewById(R.id.display_time);
        }
    }

    private void RetrieveAndMarkDelayMsg() {

        GroupNameRef.child("DelayMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                while (items.hasNext()) {
                    DataSnapshot item = items.next();
                    String sDate = item.child("displayDate").getValue().toString();
                    try {
                        Date dDate=new SimpleDateFormat("MMM dd, yyyy").parse(sDate);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dDate);
                        int month = cal.get(Calendar.MONTH);
                        month++;
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        int year = cal.get(Calendar.YEAR);

                        // to be deleted...
//                        mDelayView.setText(Integer.toString(year) + Integer.toString(month) + Integer.toString(day));
                        mCalendarView.markDate(new DateData(year, month, day).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED)));
//                        delay_msg_date_list.add(dDate);

                    }
                    catch (Exception e)
                    {
                        //error handling code
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
            }

        });

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