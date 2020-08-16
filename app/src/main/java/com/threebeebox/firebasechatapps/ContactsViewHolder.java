package com.threebeebox.firebasechatapps;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsViewHolder extends RecyclerView.ViewHolder
{
    TextView userName, userStatus,minorInfo;
    CircleImageView profileImage;
    ImageView onlineIcon;
    boolean swipable = true;
    public RelativeLayout view_background, view_foreground;

    public ContactsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        view_background = itemView.findViewById(R.id.view_background);
        view_foreground = itemView.findViewById(R.id.view_foreground);
        userName = itemView.findViewById(R.id.user_profile_name);
        userStatus = itemView.findViewById(R.id.user_status);
        profileImage = itemView.findViewById(R.id.users_profile_image);
        onlineIcon = itemView.findViewById(R.id.user_online_status);
        minorInfo = itemView.findViewById(R.id.minor_info);

    }
}