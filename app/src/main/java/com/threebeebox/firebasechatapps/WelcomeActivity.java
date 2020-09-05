package com.threebeebox.firebasechatapps;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hololo.tutorial.library.PermissionStep;
import com.hololo.tutorial.library.Step;
import com.hololo.tutorial.library.TutorialActivity;

public class WelcomeActivity extends TutorialActivity {
    SharedPreferences sharedPreferences;
    Boolean firstTime;
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_welcome);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        firstTime = sharedPreferences.getBoolean("firstTime", true);
        addFragment(new Step.Builder().setTitle("This is header")
                .setContent("This is content")
                .setBackgroundColor(Color.parseColor("#FF0957")) // int background color
                .setDrawable(R.drawable.welcome1) // int top drawable
                .setSummary("This is summary")
                .build());
        // Permission Step
        addFragment(new PermissionStep.Builder().setTitle("This is header2")
                .setContent("This is content2")
                .setBackgroundColor(Color.parseColor("#FF0957"))
                .setDrawable(R.drawable.welcome2)
                .setSummary("This is summary2")
                .setPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .build());

        if(firstTime){
            firstTime = false;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTime", firstTime);
        }else{
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void currentFragmentPosition(int position) {

    }
}
