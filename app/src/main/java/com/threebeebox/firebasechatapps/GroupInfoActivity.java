package com.threebeebox.firebasechatapps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class GroupInfoActivity extends AppCompatActivity implements GroupInfoRecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private RecyclerView MemberList;
    private DatabaseReference RootRef, UsersRef, ContactRef, GroupNameRef;
    private String currentUserID, currentGroupName, currentGroupID;
    public boolean isAdmin;
    private GroupInfoFirebaseRecyclerAdapter adapter;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private CircleImageView userProfileImage;
    private RelativeLayout Add_member_relativeLayout;
    private CardView exitGroup;
    private ProgressDialog loadingBar;
    private TextView InfoName;

    final String TAG = "GroupInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactRef = RootRef.child("Contacts").child(currentUserID);
        GroupNameRef = RootRef.child("Groups").child(currentGroupID);

        MemberList = (RecyclerView) findViewById(R.id.member_list);
        MemberList.setLayoutManager(new LinearLayoutManager(this));
        exitGroup = (CardView) findViewById(R.id.exitGroup);
        exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeUser();
            }
        });
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        Add_member_relativeLayout = (RelativeLayout) findViewById(R.id.add_member_relativeLayout);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Group Info");

        InfoName = (TextView) findViewById(R.id.infoName);
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(GroupInfoActivity.this);
            }
        });
        loadingBar = new ProgressDialog(this);

        RetrieveGroupInfo();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new GroupInfoRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(MemberList);
    }

    public void onStart() {
        super.onStart();
        GroupNameRef.child("Member").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue().toString().equals("Admin")){
                    GroupSingleton.getInstance().isAdmin = true;
                }else{
                    GroupSingleton.getInstance().isAdmin = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(GroupNameRef.child("Member"), String.class)
                        .build();

        adapter = new GroupInfoFirebaseRecyclerAdapter(options, UsersRef, GroupNameRef, currentGroupName, currentGroupID, currentUserID);
        MemberList.setAdapter(adapter);
        adapter.startListening();

        Add_member_relativeLayout.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(GroupInfoActivity.this, AddMemberActivity.class);
                        intent.putExtra("groupName", currentGroupName);
                        intent.putExtra("groupID", currentGroupID);
                        startActivityForResult(intent, 0);
                    }
                });
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Do you really want to remove this user?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        adapter.deleteItem(viewHolder.getAdapterPosition());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    }
                }).show();
    }

    private void RetrieveGroupInfo() {
        RootRef.child("Groups").child(currentGroupID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("GroupName")) {
                                String retrieveGroupName = dataSnapshot.child("GroupName").getValue().toString();
                                InfoName.setText(retrieveGroupName);
                            }
                            if (dataSnapshot.hasChild("image")) {
                                String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }
                        } else {
                            Toast.makeText(GroupInfoActivity.this, "Unknow error in RetrieveGroupInfo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your image is uploading...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                File filePathUri = new File(resultUri.getPath());
                Bitmap bitmap = null;
                try {
                    bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(filePathUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                final byte[] bytes = byteArrayOutputStream.toByteArray();

                final StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putBytes(bytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                RootRef.child("Groups").child(currentGroupID).child("image")
                                        .setValue(uri.toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(GroupInfoActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(GroupInfoActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }
    }

    public void removeUser() {
        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Do you really want to remove this user?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isAdmin) { //assign other user as admin if no other admin
                            //TODO
                        }
                        GroupNameRef.child("Member").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    userSnapshot.getRef().removeValue();
                                } else {
                                    System.out.println(currentUserID + " not exist when deleteItem");
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                        UsersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userSnapshot) {
                                if (userSnapshot.exists()) {
                                    userSnapshot.child("groups").child(currentGroupID).getRef().removeValue();
                                } else {
                                    System.out.println(currentUserID + " not exist when deleteItem");
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();

    }
}
