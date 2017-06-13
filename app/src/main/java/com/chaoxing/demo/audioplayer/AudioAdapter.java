package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Collections;
import java.util.List;

/**
 * Created by HUWEI on 2017/6/13.
 */

public class AudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Audio> audioList = Collections.emptyList();
    private OnItemClickListener onItemClickListener;

    public AudioAdapter(Context context, List<Audio> audioList) {
        this.context = context;
        this.audioList = audioList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        AudioViewHolder holder = new AudioViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final AudioViewHolder audioViewHolder = (AudioViewHolder) holder;
        Audio audio = (Audio) getItem(position);
        audioViewHolder.tvNumber.setText(position + "");
        audioViewHolder.tvTitle.setText(audio.getTitle());
        audioViewHolder.tvArtist.setText(audio.getArtist());
        audioViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(AudioAdapter.this, audioViewHolder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioList.size();
    }

    public Object getItem(int position) {
        return audioList.get(position);
    }

    static class AudioViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        ViewSwitcher vsLeft;
        TextView tvNumber;
        ImageView ivStatus;

        TextView tvTitle;
        TextView tvArtist;

        AudioViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            vsLeft = (ViewSwitcher) itemView.findViewById(R.id.vs_left);
            tvNumber = (TextView) itemView.findViewById(R.id.tv_number);
            ivStatus = (ImageView) itemView.findViewById(R.id.iv_status);

            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(RecyclerView.Adapter adapter, View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}

