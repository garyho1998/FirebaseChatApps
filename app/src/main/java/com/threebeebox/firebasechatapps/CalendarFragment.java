package com.threebeebox.firebasechatapps;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import sun.bob.mcalendarview.CellConfig;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthChangeListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.vo.DateData;
import sun.bob.mcalendarview.vo.MarkedDates;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment implements EditDelayMsgDialog.EditMsgDialogListener {
//    implements EditDelayMsgDialog.EditMsgDialogListener

    private static final String TAG = "CalendarFragment";
    private ArrayList<String> groupsList = new ArrayList<String>();
    private ArrayList<DelayMsg> delayMsgList = new ArrayList<DelayMsg>();
    private ArrayList<DateData> dateList = new ArrayList<DateData>();
    private Calendar today;
    private DateData selectedDate;
    private View calendarFragmentView;
    private ExpCalendarView mCalendarView;
    private TextView mDateTextView, mMonthTextView;
    private Button mTodayBtn, mExpBtn;
    private RecyclerView DelyMessageRecyclerView;

    private String currentUserID;
    private String groupName = "";
    private FirebaseAuth mAuth;
    private DatabaseReference GroupsRef, GroupNameRef, DelayMsgRef, CurrentUserRef;
    private Query query;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        calendarFragmentView = inflater.inflate(R.layout.fragment_calendar, container, false);

        mCalendarView = (ExpCalendarView) calendarFragmentView.findViewById(R.id.calendarView);

        mDateTextView = (TextView) calendarFragmentView.findViewById(R.id.dateView);
        mMonthTextView = (TextView) calendarFragmentView.findViewById(R.id.monthView);
        mTodayBtn = (Button) calendarFragmentView.findViewById(R.id.today_button);
        mExpBtn = (Button) calendarFragmentView.findViewById(R.id.exp_button);
        DelyMessageRecyclerView = (RecyclerView) calendarFragmentView.findViewById(R.id.msgView);
        DelyMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        CurrentUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        today = Calendar.getInstance();
        int dd = today.get(Calendar.DAY_OF_MONTH);
        int mm = today.get(Calendar.MONTH);
        int yyyy = today.get(Calendar.YEAR);
        selectedDate = new DateData(yyyy, mm, dd);
        int month = selectedDate.getMonth() + 1;
        String sDate = TransferMonth(mm + 1) + " " + dd + ", " + yyyy;
        mDateTextView.setText(sDate);
        mMonthTextView.setText(Integer.toString(yyyy) + "-" + Integer.toString(++(mm)));
        mCalendarView.travelTo(new DateData(yyyy, mm, dd));

        return calendarFragmentView;
    }


    @Override
    public void onResume() {
        super.onResume();
        RetrieveAndMarkDelayDate();
    }

    @Override
    public void onStart() {
        super.onStart();
        RetrieveAndMarkDelayDate();

        mTodayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int m = selectedDate.getMonth() + 1;
                mCalendarView.travelTo(new DateData(selectedDate.getYear(), m, selectedDate.getDay()));
                m--;
                selectedDate.setMonth(m);
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
                final String selectedDate = TransferMonth(date.getMonth()) + " " + date.getDay() + ", " + date.getYear();
                mDateTextView.setText(selectedDate);
                RetrieveAndDisplayDelayMsg(selectedDate);
            }
        });
    }

    private void RetrieveAndDisplayDelayMsg(final String selectedDate) {
        query = CurrentUserRef.child("DelayMessage").orderByChild("displayDate").equalTo(selectedDate);
        FirebaseRecyclerOptions<DelayMessageRef> options = new FirebaseRecyclerOptions.Builder<DelayMessageRef>().setQuery(query, DelayMessageRef.class).build();

        FirebaseRecyclerAdapter<DelayMessageRef, DelayMsgViewHolder> adapter = new FirebaseRecyclerAdapter<DelayMessageRef, DelayMsgViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final DelayMsgViewHolder holder, final int position, @NonNull final DelayMessageRef delayMessageRef) {
                final String messageID = getRef(position).getKey();
                System.out.println("messageID:" + messageID);

                String ref = delayMessageRef.getRef(); //can be groupID or UserID of individual messages
                String type = delayMessageRef.getType();

                if (type.equals("group")) {
                    GroupsRef.child(ref).child("DelayMessage").child(messageID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println("DelayMessage");
                            Log.i(TAG, dataSnapshot.getKey());
                            holder.delayMsg.setText((String) dataSnapshot.child("message").getValue());
                            holder.displayTime.setText((String) dataSnapshot.child("displayTime").getValue());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    //individual
                    //TODO
                }


                holder.editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                    }
                });

                holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO
                    }
                });
            }

            @NonNull
            @Override
            public DelayMsgViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delay_msg_display_layout, parent, false);
                return new DelayMsgViewHolder(view);
            }
        };

        DelyMessageRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void applyEdit(String groupID, String msgID, String msg, String date, String time) {
        Toast.makeText(getContext(), "Delay message edited" + msg, Toast.LENGTH_SHORT).show();

        DatabaseReference msgRef = GroupNameRef.child("DelayMessage").child(msgID);
        msgRef.child("message").setValue(msg);
        msgRef.child("displayDate").setValue(date);
        msgRef.child("displayTime").setValue(time);

        onResume();
    }


    private void RetrieveAndMarkDelayDate() {
        unMarkDate();
        CurrentUserRef.child("groups").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot userGroupsSnapshot, String s) {
                LoadGroupsListAndMarkLater(userGroupsSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot userGroupsSnapshot, String s) {
                LoadGroupsListAndMarkLater(userGroupsSnapshot);
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
    }

    private void LoadGroupsListAndMarkLater(DataSnapshot userGroupsSnapshot) {
        if (userGroupsSnapshot.exists()) {
            final String currentGroupID = userGroupsSnapshot.getKey();
            GroupsRef.child(currentGroupID).child("DelayMessage").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot messagesSnapshot, String s) {
                    MarkDate(messagesSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot messagesSnapshot, String s) {
                    MarkDate(messagesSnapshot);
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
        }
    }

    private void MarkDate(DataSnapshot messagesSnapshot) {
        if (messagesSnapshot.exists()) {
            try {
                if (messagesSnapshot.child("from").getValue().equals(currentUserID)) {
                    Date date = new Date((long) messagesSnapshot.child("displayTimestamp").getValue());
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Hong_Kong"));
                    cal.setTime(date);
                    mCalendarView.markDate(
                            new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)).setMarkStyle(
                                    new MarkStyle(MarkStyle.DOT, Color.RED))
                    );
                }
            } catch (Exception e) {
//                Log.i(TAG, messagesSnapshot.toString());
//                Log.i(TAG, e.toString());
            }
        }
    }

    public void unMarkDate() {
        MarkedDates markedDates = mCalendarView.getMarkedDates();
        ArrayList markData = markedDates.getAll();
        for (int k = 0; k < markData.size(); k++) {
            mCalendarView.unMarkDate((DateData) markData.get(k));
        }
    }

    private static class DelayMsgViewHolder extends RecyclerView.ViewHolder {
        TextView delayMsg, displayTime;
        ImageButton editBtn, deleteBtn;

        public DelayMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            delayMsg = itemView.findViewById(R.id.delay_msg);
            displayTime = itemView.findViewById(R.id.display_time);
            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
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