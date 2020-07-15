package com.example.firebasechatapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;

public class CalendarActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CalendarView mCalendarView;
    private TextView mdateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mToolbar = (Toolbar) findViewById(R.id.calendar_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Calendar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        mdateView = (TextView) findViewById(R.id.dateView);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView CalendarView, int year, int month, int dayOfMonth) {
                String date = year + "/" + (++month) + "/"+ dayOfMonth ;
                mdateView.setText(date);

//                Log.d(TAG, "onSelectedDayChange: yyyy/mm/dd:" + date);
//                Intent intent = new Intent(CalendarActivity.this,MainActivity.class);
//                intent.putExtra("date",date);
//                startActivity(intent);

            }
        });
    }
}