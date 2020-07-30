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
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 *
 */
public class CalendarFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private ArrayList<String> GroupsList = new ArrayList<String>();
    private Calendar today;
    private DateData selectedDate;
    private View calendarFragmentView;
    private MCalendarView mCalendarView;
    private TextView mDateTextView;
    private Button mMonthTextView, mPrevBtn, mNextBtn;
    private RecyclerView mDelayMsgRecyclerList;

    private String userID;
    private FirebaseAuth mAuth;
    private DatabaseReference GroupRef, GroupNameRef, DelayMsgRef, UserRef;
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
        mMonthTextView = (Button) calendarFragmentView.findViewById(R.id.monthView);
        mPrevBtn = (Button) calendarFragmentView.findViewById(R.id.prev_button);
        mNextBtn = (Button) calendarFragmentView.findViewById(R.id.next_button);
        mDelayMsgRecyclerList = (RecyclerView) calendarFragmentView.findViewById(R.id.msgView);
        mDelayMsgRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        today = Calendar.getInstance();
        int dd = today.get(Calendar.DAY_OF_MONTH);
        int mm = today.get(Calendar.MONTH);
        int yyyy = today.get(Calendar.YEAR);
        selectedDate = new DateData(yyyy, mm, dd);
        mDateTextView.setText(selectedDate.toString());
        mMonthTextView.setText(Integer.toString(yyyy) + "-" + Integer.toString(++(mm)));
        mCalendarView.travelTo(new DateData(yyyy, mm, dd));

        RetrieveGroupsList();
        return calendarFragmentView;
    }

    /*
    @Override
    public void onResume() {
        super.onResume();

        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k=0; k<markData.size();k++){
            mCalendarView.unMarkDate((DateData)markData.get(k));
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k=0; k<markData.size();k++){
            mCalendarView.unMarkDate((DateData)markData.get(k));
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        RetrieveAndMarkDelayMsg();

        mPrevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCalendarView.travelTo(new DateData(selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDay()));
                int m = selectedDate.getMonth()-1;
                selectedDate.setMonth(m);
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int m = selectedDate.getMonth()+2;
                mCalendarView.travelTo(new DateData(selectedDate.getYear(), m, selectedDate.getDay()));
                m--;
                selectedDate.setMonth(m);
            }
        });

        mCalendarView.setOnMonthChangeListener(new OnMonthChangeListener() {
            @Override
            public void onMonthChange(int year, int month) {
                mMonthTextView.setText(Integer.toString(year) + "-" + Integer.toString(month));
            }
        });

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

                        FirebaseRecyclerOptions<DelayMsg> options;
                        FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter = null;

                        while (items.hasNext()) {
                            DataSnapshot item = items.next();
                            GroupNameRef = item.getRef();
                            DelayMsgRef = GroupNameRef.child("DelayMessage");

                            query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);
                            options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();
                            //---
                            Toast.makeText(getContext(), "Querying: the options are" + options.toString(), Toast.LENGTH_SHORT).show();

                            adapter =
                                    new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                                        @Override
                                        protected void onBindViewHolder(@NonNull final DelayMsgViewHolder delayMsgViewHolder, int i, @NonNull final DelayMsg delayMsg) {
                                            delayMsgViewHolder.delayMsg.setText(delayMsg.getMessage());
                                            delayMsgViewHolder.displayTime.setText("Scheduled to send at " + delayMsg.getDisplayTime() + " to " + DelayMsgRef.getParent().getKey());
                                            //--
                                            Toast.makeText(getContext(), "Retrieving and binding " + delayMsg.getMessage() + "\n==========" + selectedDate, Toast.LENGTH_SHORT).show();

                                        }

                                        @NonNull
                                        @Override
                                        public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delay_msg_display_layout, parent, false);
                                            // create a new layout
                                            DelayMsgViewHolder viewHolder = new DelayMsgViewHolder(view);
                                            //---
                                            Toast.makeText(getContext(), "CreatingViewHolder: ", Toast.LENGTH_SHORT).show();
                                            return viewHolder;
                                        }
                                    };

                            //---
                            Toast.makeText(getContext(), "Adapter: " + adapter.toString(), Toast.LENGTH_SHORT).show();
                        }

                        mDelayMsgRecyclerList.setAdapter(adapter);
                        adapter.startListening();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
//                        Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
                    }
                });
                RetrieveAndDisplayDelayMsg(selectedDate);

            }
        });
    }
    private void RetrieveAndDisplayDelayMsg(final String selectedDate) {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                FirebaseRecyclerOptions<DelayMsg> options;
                FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter = null;
                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                while (items.hasNext()) {
                    DataSnapshot item = items.next();
                    GroupNameRef = item.getRef();
                    String GroupName = GroupNameRef.getKey();

                    if (GroupsList.contains(GroupName)) {
                        Log.d("myTag", "Retrieving from " + GroupName);
                        DelayMsgRef = GroupNameRef.child("DelayMessage");


                        query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);
//                        query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);

//                        FirebaseRecyclerOptions<DelayMsg> options;
//                        FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter = null;
                        options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();

                        Log.d("myTag", "Querying: the options are" + options.toString());

                        adapter =
                                new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                                    @Override
                                    protected void onBindViewHolder(@NonNull final DelayMsgViewHolder delayMsgViewHolder, int i, @NonNull final DelayMsg delayMsg) {
                                        delayMsgViewHolder.delayMsg.setText(delayMsg.getMessage());
                                        delayMsgViewHolder.displayTime.setText("Scheduled to send at " + delayMsg.getDisplayTime() + " to " + DelayMsgRef.getParent().getKey());

                                        Log.d("myTag", "Binding " + delayMsg.getMessage() + "\n==========" + selectedDate);
                                    }

                                    @NonNull
                                    @Override
                                    public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delay_msg_display_layout, parent, false);
                                        // create a new layout
                                        DelayMsgViewHolder viewHolder = new DelayMsgViewHolder(view);

                                        Log.d("myTag", "CreatingViewHolder: ");
                                        return viewHolder;
                                    }
                                };
                        Log.d("myTag", "Adapter: " + adapter.toString());

                        mDelayMsgRecyclerList.setAdapter(adapter);
//                        adapter.startListening();
                    }

                }
//                mDelayMsgRecyclerList.setAdapter(adapter);
                adapter.startListening();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                        Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
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

    private void RetrieveGroupsList() {
//        ArrayList<String> groupsList = new ArrayList<String>();
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        DatabaseReference ChatGpRef = UserRef.child("groups");

        ChatGpRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                while (items.hasNext()) {
                    DataSnapshot item = items.next();
                    GroupNameRef = item.getRef();
                    String name = GroupNameRef.getKey();
                    GroupsList.add(name);
//                    groupsList.add(name);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void RetrieveAndMarkDelayMsg() {

        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k=0; k<markData.size();k++){
            mCalendarView.unMarkDate((DateData)markData.get(k));
        }
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