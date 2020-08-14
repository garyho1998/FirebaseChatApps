package com.threebeebox.firebasechatapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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

import sun.bob.mcalendarview.CellConfig;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

public class CalendarActivity extends AppCompatActivity implements EditDelayMsgDialog.EditMsgDialogListener {


    private Calendar today;
    private DateData selectedDate;
    private Toolbar mToolbar;
    private ExpCalendarView mCalendarView;
    private TextView mdateView, mMonthTextView;
    private Button mTodayBtn, mExpBtn;
    private String currentGroupName, currentGroupID;
    private RecyclerView mDelayMsgRecyclerList;

    private DatabaseReference GroupsRef, GroupNameRef, DelayMsgRef;
    private Query query;

    final String TAG = "CalendarActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mCalendarView = (ExpCalendarView) findViewById(R.id.calendarView);

        mdateView = (TextView) findViewById(R.id.dateView);
        mMonthTextView = (TextView) findViewById(R.id.monthView);
        mTodayBtn = (Button) findViewById(R.id.today_button);
        mExpBtn = (Button) findViewById(R.id.exp_button);

        travelToToday();

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID);
        DelayMsgRef = GroupNameRef.child("DelayMessage");

        mToolbar = (Toolbar) findViewById(R.id.calendar_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Calendar of " + currentGroupName);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mDelayMsgRecyclerList = (RecyclerView) findViewById(R.id.msgView);
        mDelayMsgRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void travelToToday() {
        today = Calendar.getInstance();
        int dd = today.get(Calendar.DAY_OF_MONTH);
        int mm = today.get(Calendar.MONTH);
        int yyyy = today.get(Calendar.YEAR);
        selectedDate = new DateData(yyyy, mm, dd);
        int month = selectedDate.getMonth() + 1;
        String sDate = TransferMonth(month) + " " + selectedDate.getDay() + ", " + selectedDate.getYear();
        mdateView.setText(sDate);
        mMonthTextView.setText(Integer.toString(yyyy) + "-" + Integer.toString(++(mm)));
        mCalendarView.travelTo(new DateData(yyyy, mm, dd));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k = 0; k < markData.size(); k++) {
            mCalendarView.unMarkDate((DateData) markData.get(k));
        }
        RetrieveAndMarkDelayDate();
        mCalendarView.expand();
        mExpBtn.setText("Shrink");
        travelToToday();
        System.out.println("onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        RetrieveAndMarkDelayDate();

        mTodayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int m = selectedDate.getMonth() + 1;
                mCalendarView.travelTo(new DateData(selectedDate.getYear(), m, selectedDate.getDay()));
                selectedDate.setMonth(--m);
            }
        });

        mExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mExpBtn.getText().equals("Shrink")) {
                    CellConfig.Month2WeekPos = CellConfig.middlePosition;
                    CellConfig.ifMonth = false;
                    mCalendarView.shrink();
                    mExpBtn.setText("Expand");
                } else if (mExpBtn.getText().equals("Expand")) {
                    CellConfig.Week2MonthPos = CellConfig.middlePosition;
                    CellConfig.ifMonth = true;
                    mCalendarView.expand();
                    mExpBtn.setText("Shrink");
                }
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

                final String sDate = TransferMonth(date.getMonth()) + " " + date.getDay() + ", " + date.getYear();
                mdateView.setText(sDate);

                query = DelayMsgRef.orderByChild("displayDate").equalTo(sDate);
                FirebaseRecyclerOptions<DelayMsg> options = new FirebaseRecyclerOptions.Builder<DelayMsg>().setQuery(query, DelayMsg.class).build();

                FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder> adapter =
                        new FirebaseRecyclerAdapter<DelayMsg, DelayMsgViewHolder>(options) {
                            @Override
                            protected void onBindViewHolder(@NonNull final DelayMsgViewHolder holder, final int position, @NonNull final DelayMsg model) {
                                holder.delayMsg.setText(model.getMessage());
                                holder.displayTime.setText(model.getDisplayTime());

                                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        EditDelayMsgDialog dialog = new EditDelayMsgDialog(true, currentGroupID, model.getMessageID());
                                        dialog.show(getSupportFragmentManager(), "edit dialog");
                                    }
                                });

                                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(CalendarActivity.this, R.style.AlertDialog);
                                        dialog.setMessage("Are you sure?");
                                        dialog.setTitle("Delete delay message");
                                        dialog.setPositiveButton("Yes",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        DelayMsgRef.child(model.getMessageID()).removeValue();
                                                        Toast.makeText(getApplicationContext(), "Delay message deleted!", Toast.LENGTH_LONG).show();

                                                        // check marked date
//                                                        onResume();
                                                        RetrieveAndMarkDelayDate();
                                                    }
                                                });
                                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });
                                        AlertDialog alertDialog = dialog.create();
                                        alertDialog.show();
                                    }
                                });
                            }

                            @NonNull
                            @Override
                            public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
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

    @Override
    public void applyEdit(String groupID, String msgID, String msg, String date, String time) {
        Toast.makeText(this, "Delay message edited" + msg, Toast.LENGTH_SHORT).show();
        //apply change to firebase...
        DatabaseReference msgRef = GroupNameRef.child("DelayMessage").child(msgID);
        msgRef.child("message").setValue(msg);
        msgRef.child("displayDate").setValue(date);
        msgRef.child("displayTime").setValue(time);

        onResume();
        RetrieveAndMarkDelayDate();
    }


    public static class DelayMsgViewHolder extends RecyclerView.ViewHolder {
        TextView delayMsg, displayTime;
        ImageButton editBtn, deleteBtn;

        public DelayMsgViewHolder(@NonNull View itemView) {
            super(itemView);

            delayMsg = itemView.findViewById(R.id.delay_msg);
            displayTime = itemView.findViewById(R.id.display_time);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            editBtn = itemView.findViewById(R.id.edit_btn);

        }

    }

    private void RetrieveAndMarkDelayDate() {
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k = 0; k < markData.size(); k++) {
            mCalendarView.unMarkDate((DateData) markData.get(k));
        }

        //mCalendarView.invalidate();

        GroupNameRef.child("DelayMessage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> items = dataSnapshot.getChildren().iterator();

                while (items.hasNext()) {
                    DataSnapshot item = items.next();
                    String sDate = item.child("displayDate").getValue().toString();
                    try {
                        Date dDate = new SimpleDateFormat("MMM dd, yyyy").parse(sDate);

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
        for (int k = 0; k < markData.size(); k++) {
            mCalendarView.unMarkDate((DateData) markData.get(k));
        }

    }

    private String TransferMonth(int month) {
        switch (month) {
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