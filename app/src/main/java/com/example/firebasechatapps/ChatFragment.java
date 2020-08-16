//package com.threebeebox.firebasechatapps;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.firebase.ui.database.FirebaseRecyclerAdapter;
//import com.firebase.ui.database.FirebaseRecyclerOptions;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.Query;
//import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Picasso;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//import sun.bob.mcalendarview.vo.DateData;
//
//
///**
// * A simple {@link Fragment} subclass.
// */
//public class ChatFragment extends Fragment
//{
//    private View PrivateChatsView;
//    private RecyclerView chatsList;
//
//    private DatabaseReference ChatsRef, UsersRef;
//    private FirebaseAuth mAuth;
//    private String currentUserID="";
//    private String sToday = "";
//
//
//    public ChatFragment() {
//        // Required empty public constructor
//    }
//
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        PrivateChatsView = inflater.inflate(R.layout.fragment_chat, container, false);
//
//        mAuth = FirebaseAuth.getInstance();
//        currentUserID = mAuth.getCurrentUser().getUid();
//        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUserID);
//        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
//
//        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
//        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat currentDate = new SimpleDateFormat(("MMM dd, yyyy"));
//        sToday = currentDate.format(calendar.getTime());
//
//        return PrivateChatsView;
//    }
//
//
//    @Override
//    public void onStart()
//    {
//        super.onStart();
//
//
//        FirebaseRecyclerOptions<Contacts> options =
//                new FirebaseRecyclerOptions.Builder<Contacts>()
//                        .setQuery(ChatsRef, Contacts.class)
//                        .build();
//
//
//        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
//                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
//                    @Override
//                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
//                    {
//                        final String usersIDs = getRef(position).getKey();
//                        final String[] retImage = {"default_image"};
//
//                        UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot)
//                            {
//                                if (dataSnapshot.exists())
//                                {
//                                    if (dataSnapshot.hasChild("image"))
//                                    {
//                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
//                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
//                                    }
//
//                                    final String retName = dataSnapshot.child("name").getValue().toString();
//                                    final String retStatus = dataSnapshot.child("status").getValue().toString();
//
//                                    holder.userName.setText(retName);
//
//                                    // get last update time
//                                    final Query lastQuery = ChatsRef.child(usersIDs).orderByKey().limitToLast(1);
//                                    lastQuery.addChildEventListener(new ChildEventListener() {
//                                        @Override
//                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                                            String last_time = dataSnapshot.child("time").getValue().toString();
//                                            String last_date = dataSnapshot.child("date").getValue().toString();
//                                            if (last_date.equals(sToday)) {
//                                                holder.lastSend.setText(last_time);
//                                            } else {
//                                                holder.lastSend.setText(last_date);
//                                            }
//
//                                            String type = dataSnapshot.child("type").getValue().toString();
//                                            if (type.equals("text")) {
//                                                String text_msg = dataSnapshot.child("message").getValue().toString();
//                                                holder.userMsg.setText(text_msg);
//                                            } else if (type.equals("image")) {
//                                                holder.userMsg.setText("[Image]");
//                                            }
//                                        }
//                                        @Override
//                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//                                        }
//                                        @Override
//                                        public void onChildRemoved(DataSnapshot dataSnapshot) {
//                                        }
//                                        @Override
//                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                                        }
//                                        @Override
//                                        public void onCancelled(DatabaseError databaseError) {
//                                        }
//                                    });
//
//                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
//                                        @Override
//                                        public void onClick(View view)
//                                        {
//                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
//                                            chatIntent.putExtra("visit_user_id", usersIDs);
//                                            chatIntent.putExtra("visit_user_name", retName);
//                                            chatIntent.putExtra("visit_image", retImage[0]);
//                                            startActivity(chatIntent);
//                                        }
//                                    });
//                                }
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//                    }
//
//                    @NonNull
//                    @Override
//                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
//                    {
//                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
//                        return new ChatsViewHolder(view);
//                    }
//                };
//
//        chatsList.setAdapter(adapter);
//        adapter.startListening();
//    }
//
//
//
//
//    public static class  ChatsViewHolder extends RecyclerView.ViewHolder
//    {
//        CircleImageView profileImage;
//        TextView userMsg, userName, lastSend;
//
//
//        public ChatsViewHolder(@NonNull View itemView)
//        {
//            super(itemView);
//
//            profileImage = itemView.findViewById(R.id.users_profile_image);
//            userMsg = itemView.findViewById(R.id.user_msg);
//            userName = itemView.findViewById(R.id.user_profile_name);
//            lastSend = itemView.findViewById(R.id.minor_info);
//        }
//    }
//}
