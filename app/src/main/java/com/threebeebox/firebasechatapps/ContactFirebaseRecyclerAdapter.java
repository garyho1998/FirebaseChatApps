package com.threebeebox.firebasechatapps;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactFirebaseRecyclerAdapter extends FirebaseRecyclerAdapter<String, ContactsViewHolder> {
    private DatabaseReference UsersRef;
    private Context mCon;

    public ContactFirebaseRecyclerAdapter(@NonNull FirebaseRecyclerOptions options, DatabaseReference UsersRef, Context mCon) {
        super(options);
        this.UsersRef = UsersRef;
        this.mCon = mCon;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout_with_delete, viewGroup, false);
        return new ContactsViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull String value) {
        final String userIDs = getRef(position).getKey();

        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("userState").hasChild("state"))
                    {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                        if (state.equals("online"))
                        {
                            holder.onlineIcon.setVisibility(View.VISIBLE);
                            holder.minorInfo.setVisibility(View.INVISIBLE);
                        }
                        else if (state.equals("offline"))
                        {
                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
                            String today = currentDate.format(calendar.getTime());
                            if (date.equals(today)) {
                                holder.minorInfo.setText( dataSnapshot.child("userState").child("time").getValue().toString() );
                            } else {
                                holder.minorInfo.setText(date);
                            }
                        }
                    }
                    else
                    {
                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                    }


                    if (dataSnapshot.hasChild("image"))
                    {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        String profileName = dataSnapshot.child("name").getValue().toString();
                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(phoneNumber);
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                    }
                    else
                    {
                        String profileName = dataSnapshot.child("name").getValue().toString();
                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            Intent chatIntent = new Intent(mCon, ChatActivity.class);
                            chatIntent.putExtra("visit_user_id", userIDs);
                            chatIntent.putExtra("visit_user_name", dataSnapshot.child("name").getValue().toString());
                            chatIntent.putExtra("visit_image", dataSnapshot.child("image").getValue().toString());
                            mCon.startActivity(chatIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void deleteItem(int position) {
        getSnapshots().getSnapshot(position).getRef().removeValue();
    }
}
