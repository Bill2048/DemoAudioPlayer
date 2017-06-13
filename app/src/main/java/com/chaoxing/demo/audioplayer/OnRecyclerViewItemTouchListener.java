package com.chaoxing.demo.audioplayer;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by HUWEI on 2017/6/13.
 */

public class OnRecyclerViewItemTouchListener implements RecyclerView.OnItemTouchListener {

    private GestureDetector gestureDetector;
    private OnRecyclerViewItemClickListener onItemClickListener;

    public OnRecyclerViewItemTouchListener(final RecyclerView rv, OnRecyclerViewItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        gestureDetector = new GestureDetector(rv.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (OnRecyclerViewItemTouchListener.this.onItemClickListener != null) {
                    View childView = rv.findChildViewUnder(e.getX(), e.getY());
                    if (childView != null) {
                        OnRecyclerViewItemTouchListener.this.onItemClickListener.onItemClick(rv, childView, rv.getChildLayoutPosition(childView));
                    }
                }
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (gestureDetector != null) {
            return gestureDetector.onTouchEvent(e);
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public void setGestureDetector(GestureDetector gestureDetector) {
        this.gestureDetector = gestureDetector;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
