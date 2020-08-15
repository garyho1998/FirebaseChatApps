package com.threebeebox.firebasechatapps;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragmentRecyclerAdapter extends RecyclerView.Adapter<ChatsFragmentRecyclerAdapter.ChatsViewHolder> {
    private static final String TAG = "NotesRecyclerAdapter";
    private ArrayList<User> UserList = new ArrayList<>();
    private OnChatListener mOnChatListener;

    public ChatsFragmentRecyclerAdapter(ArrayList<User> UserList, OnChatListener mOnChatListener) {
        this.UserList = UserList;
        this.mOnChatListener = mOnChatListener;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
        return new ChatsViewHolder(view, mOnChatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        User target = UserList.get(position);
        Picasso.get().load(target.image).into(holder.profileImage);

        holder.userName.setText(target.name);

        if (!target.state.isEmpty()) {
            if (target.state.equals("online")) {
                holder.userStatus.setText("online");
            } else if (target.state.equals("offline")) {
                holder.userStatus.setText("Last Seen: " + target.date + " " + target.time);
            }
        } else {
            holder.userStatus.setText("offline");
        }
    }

    @Override
    public int getItemCount() {
        return UserList.size();
    }

    public interface OnChatListener{
        void onChatClick(int position);
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView profileImage;
        TextView userStatus, userName;
        OnChatListener mOnChatListener;

        public ChatsViewHolder(@NonNull View itemView, OnChatListener onChatListener) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_status);
            userName = itemView.findViewById(R.id.user_profile_name);
            mOnChatListener = onChatListener;
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            Log.d(TAG, "onClick: " + getAdapterPosition());
            mOnChatListener.onChatClick(getAdapterPosition());
        }
    }
}
