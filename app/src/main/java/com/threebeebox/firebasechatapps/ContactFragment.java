package com.threebeebox.firebasechatapps;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment implements ContactRecyclerItemTouchHelper.RecyclerItemTouchHelperListener{
    private static final String TAG = "CalendarFragment";
    private View ContactsView;
    private RecyclerView myContactsList;
    private DatabaseReference ContactsRef, UsersRef, GroupNameRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private ContactFirebaseRecyclerAdapter adapter;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contact, container, false);

        myContactsList = (RecyclerView) ContactsView.findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ContactRecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(myContactsList);

        return ContactsView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.i(TAG, "onStart");
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<String>().setQuery(ContactsRef, String.class).build();

        adapter = new ContactFirebaseRecyclerAdapter(options, UsersRef, getContext());

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ContactsViewHolder) {
            new AlertDialog.Builder(getContext())
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
    }
}
