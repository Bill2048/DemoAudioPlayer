package com.chaoxing.demo.audioplayer;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by HUWEI on 2017/6/13.
 */

public interface OnRecyclerViewItemClickListener {

    void onItemClick(RecyclerView rv, View childView, int position);

    void onItemLongClick(RecyclerView rv, View childView, int position);

}
