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
    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull String value) {
        final String userIDs = getRef(position).getKey();

        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.child("userState").hasChild("state")) {
                        String state = dataSnapshot.child("userState").child("state").getValue().toString();

                        if (state.equals("online")) {
                            holder.minorInfo.setText("online");
                        } else if (state.equals("offline")) {
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
                            String today = currentDate.format(calendar.getTime());
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            if (date.equals(today)) {
                                holder.minorInfo.setText(dataSnapshot.child("userState").child("time").getValue().toString());
                            } else {
                                holder.minorInfo.setText(date);
                            }
                        }
                    }

                    final Intent chatIntent = new Intent(mCon, ChatActivity.class);
                    if (dataSnapshot.hasChild("name")) {
                        String profileName = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        holder.userName.setText(profileName);
                        holder.userStatus.setText(status);
                        chatIntent.putExtra("visit_user_name", dataSnapshot.child("name").getValue().toString());
                    } else {
                        if (dataSnapshot.hasChild("phoneNumber")) {
                            holder.userName.setText(dataSnapshot.child("phoneNumber").getValue().toString());
                            chatIntent.putExtra("visit_user_name", dataSnapshot.child("phoneNumber").getValue().toString());
                        }
                    }
                    if (dataSnapshot.hasChild("image")) {
                        String userImage = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.user_icon).into(holder.profileImage);
                    }
                }else{
                    getRef(position).removeValue();
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
