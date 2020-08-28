package com.threebeebox.firebasechatapps;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragmentRecyclerAdapter extends RecyclerView.Adapter<ChatsFragmentRecyclerAdapter.ChatsViewHolder> {
    private static final String TAG = "ChatsFRecyclerAdapter";
    private ArrayList<User> UserList = new ArrayList<>();
    private OnChatListener mOnChatListener;
    private DatabaseReference ChatsRef;
    private String sToday = "";
    private String currentUserId;

    public ChatsFragmentRecyclerAdapter(ArrayList<User> UserList, OnChatListener mOnChatListener, String currentUserID) {
        this.UserList = UserList;
        this.mOnChatListener = mOnChatListener;
        this.currentUserId = currentUserID;

        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
        sToday = currentDate.format(calendar.getTime());
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
        return new ChatsViewHolder(view, mOnChatListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {
        User target = UserList.get(position);
        Picasso.get().load(target.image).into(holder.profileImage);

        holder.userName.setText(target.name);

        if (!target.state.isEmpty()) {
            if (target.state.equals("online")) {
                holder.minorInfo.setText("online");
            } else if (target.state.equals("offline")) {
                if (target.date.equals(sToday)) {
                    holder.minorInfo.setText(target.time);
                } else {
                    holder.minorInfo.setText(target.date);
                }
            }
        } else {
            holder.minorInfo.setText("offline");
        }

        final Query lastQuery = ChatsRef.child(currentUserId).child(target.userId).child("Chat").orderByKey().limitToLast(1);
        lastQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    setUserMsg(dataSnapshot, holder);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    setUserMsg(dataSnapshot, holder);
                }
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public int getItemCount() {
        return UserList.size();
    }

    public interface OnChatListener {
        void onChatClick(int position);
    }

    public class ChatsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CircleImageView profileImage;
        TextView minorInfo, userName, userMsg;
        OnChatListener mOnChatListener;

        public ChatsViewHolder(@NonNull View itemView, OnChatListener onChatListener) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            minorInfo = itemView.findViewById(R.id.minor_info);
            userName = itemView.findViewById(R.id.user_profile_name);
            userMsg = itemView.findViewById(R.id.user_status);
            mOnChatListener = onChatListener;
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            Log.i(TAG, "onClick: " + getAdapterPosition());
            mOnChatListener.onChatClick(getAdapterPosition());
        }
    }

    private void setUserMsg(DataSnapshot dataSnapshot, ChatsViewHolder holder){
        String type = (String) dataSnapshot.child("type").getValue();
        if (type!=null) {
            if (type.equals("text")) {
                String text_msg = dataSnapshot.child("message").getValue().toString();
                holder.userMsg.setText(text_msg);
            } else if (type.equals("image")) {
                holder.userMsg.setText("[Image]");
            }
        }
    }
}
