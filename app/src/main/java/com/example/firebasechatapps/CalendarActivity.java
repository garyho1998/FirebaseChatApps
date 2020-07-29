package com.example.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

public class CalendarActivity extends AppCompatActivity {


    private Calendar today;
    private DateData selectedDate;
    private Toolbar mToolbar;
    private MCalendarView mCalendarView;
    private TextView mdateView;
    private Button mMonthTextView, mPrevBtn, mNextBtn;
    private String currentGroupName;

    private RecyclerView mDelayMsgRecyclerList;


    private DatabaseReference GroupNameRef, DelayMsgRef;
    private Query query;

    final String TAG = "CalendarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendarView = (MCalendarView) findViewById(R.id.calendarView);

        mdateView = (TextView) findViewById(R.id.dateView);
        mMonthTextView = (Button) findViewById(R.id.monthView);
        mPrevBtn = (Button) findViewById(R.id.prev_button);
        mNextBtn = (Button) findViewById(R.id.next_button);


        today = Calendar.getInstance();
        int dd = today.get(Calendar.DAY_OF_MONTH);
        int mm = today.get(Calendar.MONTH);
        int yyyy = today.get(Calendar.YEAR);
        selectedDate = new DateData(yyyy, mm, dd);
        mdateView.setText(selectedDate.toString());
        mMonthTextView.setText(Integer.toString(yyyy) + "-" + Integer.toString(++(mm)));
        mCalendarView.travelTo(new DateData(yyyy, mm, dd));

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);
        DelayMsgRef = GroupNameRef.child("DelayMessage");

        mToolbar = (Toolbar) findViewById(R.id.calendar_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Calendar of " + currentGroupName);

        mDelayMsgRecyclerList = (RecyclerView) findViewById(R.id.msgView);
        mDelayMsgRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k=0; k<markData.size();k++){
            mCalendarView.unMarkDate((DateData)markData.get(k));
        }

    }
    @Override
    protected void onStart()
    {
        super.onStart();
        RetrieveAndMarkDelayDate();

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
                mdateView.setText(selectedDate);

                query = DelayMsgRef.orderByChild("displayDate").equalTo(selectedDate);
                FirebaseRecyclerOptions<DelayMsg> options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();

                FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter =
                        new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull DelayMsgViewHolder holder, final int position, @NonNull DelayMsg model)
                            {
                                holder.delayMsg.setText(model.getMessage());
                                holder.displayTime.setText("Scheduled to send at " + model.getDisplayTime());

                            }

                            @NonNull
                            @Override
                            public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                            {
                                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.delay_msg_display_layout, viewGroup, false);
                                // create a new layout
                                DelayMsgViewHolder viewHolder = new DelayMsgViewHolder(view);
                                return viewHolder;
                            }
                        };
                mDelayMsgRecyclerList.setAdapter(adapter);
                adapter.startListening();
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

    private void RetrieveAndMarkDelayDate() {
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k=0; k<markData.size();k++){
            mCalendarView.unMarkDate((DateData)markData.get(k));
        }

        mCalendarView.invalidate();

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

                        mCalendarView.markDate(new DateData(year, month, day).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED)));

                    } catch (Exception e) {
                        //error handling code
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                Log.e("!_@@@_ERROR_>>", "onCancelled", firebaseError.toException());
            }

        });
        markedDates = mCalendarView.getMarkedDates();
        markData = markedDates.getAll();
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