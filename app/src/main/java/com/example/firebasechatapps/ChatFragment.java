package com.example.firebasechatapps;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View ChatFragmentView;
    private FloatingActionButton mcalendarButton;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ChatFragmentView = inflater.inflate(R.layout.fragment_chat, container, false);

        mcalendarButton = (FloatingActionButton) ChatFragmentView.findViewById(R.id.calendarButton);

        mcalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(getContext(), CalendarActivity.class);
//                calendarIntent.putExtra("type" , valueToPass);
                startActivity(calendarIntent);
            }
        });

        // Inflate the layout for this fragment
        return ChatFragmentView;
    }

}
