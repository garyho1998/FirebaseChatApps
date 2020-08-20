package com.threebeebox.firebasechatapps;
import android.graphics.Canvas;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ravi on 29/09/17.
 */

public class AddMemberRecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;

    public AddMemberRecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            final View foregroundView = ((ContactsViewHolder) viewHolder).view_foreground;

            getDefaultUIUtil().onSelected(foregroundView);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
        System.out.println("onChildDrawOver");
        final View foregroundView = ((ContactsViewHolder) viewHolder).view_foreground;
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final View foregroundView = ((ContactsViewHolder) viewHolder).view_foreground;
        getDefaultUIUtil().clearView(foregroundView);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        final View foregroundView = ((ContactsViewHolder) viewHolder).view_foreground;

        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                actionState, isCurrentlyActive);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    //Not Working
    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        System.out.println("getSwipeDirs");
        if (((TextView) viewHolder.itemView).getText().equals("Contact already in group")) {
            return 0;
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    //Not Working
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        System.out.println("getMovementFlags");

        int swipeFlags = ItemTouchHelper.START;

        ContactsViewHolder holder = (ContactsViewHolder) viewHolder;
        System.out.println("((TextView) holder.userStatus).getText(): "+ ((TextView) holder.userStatus).getText());
        if (((TextView) holder.userStatus).getText().equals("Contact already in group")) {
            return makeMovementFlags(0, 0);
        }
        System.out.println("SwipeSingleton.getInstance().isAdmin: "+ GroupSingleton.getInstance().isAdmin);
        if(!GroupSingleton.getInstance().isAdmin){
            return makeMovementFlags(0, 0);
        }
        return makeMovementFlags(0, swipeFlags);
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}