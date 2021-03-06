package com.threebeebox.firebasechatapps;

import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.annotation.Nullable;
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
public class CalendarFragment extends Fragment{
    private static final String TAG = "CalendarFragment";
    private ArrayList<String> groupsList = new ArrayList<String>();
    private ArrayList<DelayMsg> delayMsgList = new ArrayList<DelayMsg>();
    private ArrayList<DateData> dateList = new ArrayList<DateData>();
    private Calendar today;
    private DateData selectedDate;
    boolean expSeleted = true;
    private View calendarFragmentView;
    private ExpCalendarView mCalendarView;
    private TextView mDateTextView, mMonthTextView;
    private ImageButton mExpBtn;
    private RecyclerView DelyMessageRecyclerView;

    private String currentUserID;
    private String groupName = "";
    private FirebaseAuth mAuth;
    private DatabaseReference GroupsRef, GroupNameRef, DelayMsgRef, CurrentUserRef, RootRef;
    FirebaseRecyclerAdapter<DelayMessageRef, DelayMsgViewHolder> adapter;
    private Query query;

    public CalendarFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.i(TAG, "onCreateView");
        calendarFragmentView = inflater.inflate(R.layout.fragment_calendar, container, false);

        mCalendarView = (ExpCalendarView) calendarFragmentView.findViewById(R.id.calendarView);

        mDateTextView = (TextView) calendarFragmentView.findViewById(R.id.dateView);
        mMonthTextView = (TextView) calendarFragmentView.findViewById(R.id.monthView);
        // mTodayBtn = (Button) calendarFragmentView.findViewById(R.id.today_button);
        mExpBtn = (ImageButton) calendarFragmentView.findViewById(R.id.exp_button);
        DelyMessageRecyclerView = (RecyclerView) calendarFragmentView.findViewById(R.id.msgView);
        DelyMessageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DelyMessageRecyclerView.setAdapter(new EmptyAdapter());

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        GroupsRef = RootRef.child("Groups");
        CurrentUserRef = RootRef.child("Users").child(currentUserID);

        today = Calendar.getInstance();
        int dd = today.get(Calendar.DAY_OF_MONTH);
        int mm = today.get(Calendar.MONTH);
        int yyyy = today.get(Calendar.YEAR);
        selectedDate = new DateData(yyyy, mm, dd);
        int month = selectedDate.getMonth() + 1;
        String sDate = TransformMonth(mm + 1) + " " + TransformDay(dd) + ", " + yyyy;
        mDateTextView.setText(sDate);
        mMonthTextView.setText(Integer.toString(yyyy) + "-" + Integer.toString(++(mm)));
        mCalendarView.travelTo(new DateData(yyyy, mm, dd));

        return calendarFragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        RetrieveAndMarkDelayDate();
        expSeleted = true;
        mExpBtn.setImageResource(R.drawable.up_grey);
        mCalendarView.expand();

        mCalendarView.travelTo(new DateData(selectedDate.getYear(), selectedDate.getMonth() + 1, selectedDate.getDay()));
        selectedDate.setMonth(selectedDate.getMonth());
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        mCalendarView.travelTo(new DateData(selectedDate.getYear(), selectedDate.getMonth() + 1, selectedDate.getDay()));
        selectedDate.setMonth(selectedDate.getMonth());
        RetrieveAndMarkDelayDate();

        expSeleted = true;
        mExpBtn.setImageResource(R.drawable.up_grey);
        mExpBtn = (ImageButton) getActivity().findViewById(R.id.exp_button);
        mExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expSeleted) {
                    System.out.println("isSelected");
                    CellConfig.Month2WeekPos = CellConfig.middlePosition;
                    CellConfig.ifMonth = false;
                    mCalendarView.shrink();
                    mExpBtn.setImageResource(R.drawable.down);
                    expSeleted = false;
                } else {
                    System.out.println("isNOTSelected");
                    CellConfig.Week2MonthPos = CellConfig.middlePosition;
                    CellConfig.ifMonth = true;
                    mCalendarView.expand();
                    mExpBtn.setImageResource(R.drawable.up_grey);
                    expSeleted = true;
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
                final String selectedDate = TransformMonth(date.getMonth()) + " " + TransformDay(date.getDay()) + ", " + date.getYear();
                mDateTextView.setText(selectedDate);
                MarkedDates markedDates = mCalendarView.getMarkedDates();
                ArrayList markDataList = markedDates.getAll();
                RetrieveAndDisplayDelayMsg(selectedDate);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void RetrieveAndDisplayDelayMsg(final String selectedDate) {
        query = CurrentUserRef.child("DelayMessage").orderByChild("displayDate").equalTo(selectedDate);
        FirebaseRecyclerOptions<DelayMessageRef> options = new FirebaseRecyclerOptions.Builder<DelayMessageRef>().setQuery(query, DelayMessageRef.class).build();

        adapter = new FirebaseRecyclerAdapter<DelayMessageRef, DelayMsgViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final DelayMsgViewHolder holder, final int position, @NonNull final DelayMessageRef delayMessageRef) {
                final String messageID = getRef(position).getKey();
                System.out.println("messageID:" + messageID);

                final String ref = delayMessageRef.getRef(); //can be groupID or UserID of individual messages
                String type = delayMessageRef.getType();

                if (type.equals("group")) {
                    GroupsRef.child(ref).child("DelayMessage").child(messageID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            System.out.println("DelayMessage");
//                            Log.i(TAG, dataSnapshot.getKey());
                            holder.delayMsg.setText((String) dataSnapshot.child("message").getValue());
                            holder.displayTime.setText((String) dataSnapshot.child("displayTime").getValue());

                            holder.editBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String sender = dataSnapshot.child("message").getValue().toString();
                                    EditDelayMsgDialog dialog = new EditDelayMsgDialog(true, "group", sender, null, ref, messageID);
                                    dialog.show(getChildFragmentManager() , "edit dialog");
                                }
                            });
                             holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                                    dialog.setMessage("Are you sure?");
                                    dialog.setTitle("Delete delay message");
                                    dialog.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    GroupsRef.child(ref).child("DelayMessage").child(messageID).removeValue();
                                                    CurrentUserRef.child("DelayMessage").child(messageID).removeValue();
                                                    Toast.makeText(getContext(), "Delay message deleted!", Toast.LENGTH_LONG).show();
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

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    //individual
                    DatabaseReference messageRef = RootRef.child("Messages").child(currentUserID).child(ref);
                    messageRef.child("Delay").child(messageID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            System.out.println("DelayMessage");
//                            Log.i(TAG, dataSnapshot.getKey());
                            holder.delayMsg.setText((String) dataSnapshot.child("message").getValue());
                            holder.displayTime.setText((String) dataSnapshot.child("displayTime").getValue());

                            holder.editBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String sender = dataSnapshot.child("message").getValue().toString();
                                    EditDelayMsgDialog dialog = new EditDelayMsgDialog(true, "group", sender, ref, ref, messageID);
                                    dialog.show(getChildFragmentManager() , "edit dialog");
                                }
                            });
                            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                                    dialog.setMessage("Are you sure?");
                                    dialog.setTitle("Delete delay message");
                                    dialog.setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    RootRef.child("Message").child(currentUserID).child(ref).child("Delay").child(messageID).removeValue();
                                                    CurrentUserRef.child("DelayMessage").child(messageID).removeValue();
                                                    Toast.makeText(getContext(), "Delay message deleted!", Toast.LENGTH_LONG).show();
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

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

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

    private void RetrieveAndMarkDelayDate() {
        unMarkDate();
        CurrentUserRef.child("DelayMessage").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MarkDate(snapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MarkDate(snapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                MarkDate(snapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MarkDate(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void MarkDate(DataSnapshot messagesSnapshot) {
        if (messagesSnapshot.exists()) {
            try {
                Date date = new Date((long) messagesSnapshot.child("displayTimestamp").getValue());
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Hong_Kong"));
                cal.setTime(date);
                if (messagesSnapshot.child("type").getValue().equals("group")) {
                    mCalendarView.markDate(
                            new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)).setMarkStyle(
                                    new MarkStyle(MarkStyle.DOT, Color.BLUE))
                    );
                }else if(messagesSnapshot.child("type").getValue().equals("chat")){
                    mCalendarView.markDate(
                            new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)).setMarkStyle(
                                    new MarkStyle(MarkStyle.DOT, Color.GREEN))
                    );
                }else{
                    Log.i(TAG, "Unknow delay message Type!!!");
                }
            } catch (Exception e) {
                Log.i(TAG, messagesSnapshot.toString());
                Log.i(TAG, e.toString());
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

    private String TransformMonth(int month) {
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

    private String TransformDay(int day){
        if (day<10){
            return "0"+ Integer.toString(day);
        }else{
            return  Integer.toString(day);
        }
    }
}