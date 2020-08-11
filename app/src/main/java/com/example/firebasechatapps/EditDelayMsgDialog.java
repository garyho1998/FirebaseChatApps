package com.example.firebasechatapps;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditDelayMsgDialog extends AppCompatDialogFragment {

    private EditText editMsg, editDate, editTime;
    private TextView textID;
    private String groupID, msgID;
    private Boolean isAct;
    private EditMsgDialogListener listener;

    private DatabaseReference GroupRef;

    public EditDelayMsgDialog(Boolean isAct, String groupID, String msgID) {
        this.isAct = isAct;
        this.groupID = groupID;
        this.msgID = msgID;
    }
//    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialog);

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_delay_msg_layout, null);

        editMsg = view.findViewById(R.id.edit_msg);
        editDate = view.findViewById(R.id.edit_date);
        editTime = view.findViewById(R.id.edit_time);
        textID = view.findViewById(R.id.text_id);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);
        DatabaseReference msgRef = GroupRef.child("DelayMessage").child(msgID);
        msgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DelayMsg msg = dataSnapshot.getValue(DelayMsg.class);
                editMsg.setText(msg.getMessage());
                editDate.setText(msg.getDisplayDate());
                editTime.setText(msg.getDisplayTime());
                textID.setText(msg.messageID);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        builder.setView(view)
                .setTitle("Edit delay message")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String msg = editMsg.getText().toString();
                        String date = editDate.getText().toString();
                        String time = editTime.getText().toString();
                        String id = textID.getText().toString();
                        listener.applyEdit(groupID, msgID, msg, date, time);
                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (isAct) {
            try {
                listener = (EditMsgDialogListener) context;
            } catch (ClassCastException e) {
                throw  new ClassCastException(context.toString() + " must implement EditMsgDialogListener");
            }
        } else {
            try {
                listener = (EditMsgDialogListener) getTargetFragment();
            } catch (ClassCastException e) {
                Log.e("Dialog", "onAttach: ClassCastException : " + e.getMessage());
            }
        }


    }


    public interface EditMsgDialogListener{
        void applyEdit(String groupID, String msgID, String msg, String date, String time);
    }

}
