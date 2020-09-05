package com.threebeebox.firebasechatapps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class GroupFinishCreateActivity extends AppCompatActivity implements AddMemberRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    FloatingActionButton edit, finish;
    TextView groupNameTV;
    private CircleImageView userProfileImage;
    private RecyclerView myContactsList;
    private StorageReference UserProfileImagesRef;
    private DatabaseReference RootRef, UsersRef, ContactRef, GroupNameRef;
    private String currentUserID, currentGroupName, currentGroupID;
    private AddMemberFirebaseRecyclerAdapter adapter;
    private static final int GalleryPick = 1;
    private ProgressDialog loadingBar;
    final String TAG = "GroupCreateActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_finish);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = RootRef.child("Users");
        ContactRef = RootRef.child("Contacts").child(currentUserID);
        currentGroupID = getIntent().getExtras().getString("groupID");
        currentGroupName = getIntent().getExtras().getString("groupName");
        GroupNameRef = RootRef.child("Groups").child(currentGroupID);
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initView();
        RetrieveGroupInfo();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new AddMemberRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(myContactsList);
    }
    public void initView(){
        loadingBar = new ProgressDialog(this);
        groupNameTV = (TextView) findViewById(R.id.group_name);
        groupNameTV.setText(currentGroupName);
        finish = (FloatingActionButton) findViewById(R.id.finish);
        myContactsList = (RecyclerView) findViewById(R.id.member_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(this));
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        edit = (FloatingActionButton) findViewById(R.id.edit);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Group");

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent groupChatIntent = new Intent(GroupFinishCreateActivity.this, GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                groupChatIntent.putExtra("groupID", currentGroupID);
                startActivity(groupChatIntent);
                finish();
            }
        });
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(GroupFinishCreateActivity.this);
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(GroupFinishCreateActivity.this);
            }
        });
    }
    public void onStart() {
        super.onStart();
        SetRecyclerAdapter();
    }

    public void SetRecyclerAdapter(){
        GroupSingleton.getInstance().isAdmin = true;

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(ContactRef, String.class)
                        .build();

        adapter = new AddMemberFirebaseRecyclerAdapter(options, UsersRef, GroupNameRef, currentGroupName, currentGroupID);
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        adapter.AddMember(viewHolder.getAdapterPosition());
        adapter.notifyItemChanged(viewHolder.getAdapterPosition());
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(GroupFinishCreateActivity.this, MainActivity.class));
        finish();

    }

    //change image
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
                                                    Toast.makeText(GroupFinishCreateActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(GroupFinishCreateActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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

    private void RetrieveGroupInfo() {
        RootRef.child("Groups").child(currentGroupID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                            }
                        } else {
                            Toast.makeText(GroupFinishCreateActivity.this, "Unknow error in RetrieveGroupInfo", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

}
