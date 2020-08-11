package com.example.firebasechatapps;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<groupObject> arrayAdapter;
    private ArrayList<groupObject> list_of_groups = new ArrayList<>();

    private DatabaseReference GroupRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupFragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        IntializeFields();
        RetrieveAndDisplayGroups();

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                String currentGroupName = ((groupObject)adapterView.getItemAtPosition(position)).getName();
                String currentGroupID = ((groupObject)adapterView.getItemAtPosition(position)).getID();
                
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName" , currentGroupName);
                groupChatIntent.putExtra("groupID" , currentGroupID);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }

    private void IntializeFields()
    {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<groupObject>(getContext(), android.R.layout.simple_list_item_1, list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups()
    {
        UserRef.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Set<groupObject> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    DataSnapshot dataSnapshotTemp = ((DataSnapshot)iterator.next());
                    groupObject tempGroup = new groupObject(dataSnapshotTemp.getKey(), (String) dataSnapshotTemp.getValue());
                    set.add(tempGroup);
                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class groupObject {
        private String id;
        private String name;

        public groupObject(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getID() {
            return id;
        }
        public String getName() {
            return name;
        }
        @Override
        public String toString() {
            return name;
        }
    }
}


