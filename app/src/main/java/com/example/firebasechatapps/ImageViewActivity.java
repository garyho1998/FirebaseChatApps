package com.example.firebasechatapps;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private String imageID;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imageID = getIntent().getExtras().get("imageID").toString();

        imageView = (ImageView) findViewById(R.id.image_view);

    }

    @Override
    protected void onStart() {
        super.onStart();

        Picasso.get().load(imageID).into(imageView);
    }
}