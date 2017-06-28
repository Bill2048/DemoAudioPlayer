package com.chaoxing.demo.audioplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by HuWei on 2017/6/22.
 */

public class PlaylistAdapter extends ArrayAdapter<Audio> {

    private PlaylistCallbacks mPlaylistCallbacks;

    public PlaylistAdapter(@NonNull Context context, List<Audio> audioList) {
        super(context, R.layout.item_playlist, audioList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_playlist, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Audio audio = getItem(position);
        holder.tvTitle.setText(audio.getTitle());
        if (mPlaylistCallbacks != null && mPlaylistCallbacks.getActiveIndex() == position) {
            holder.tvTitle.setTextColor(0xFF0099FF);
        } else {
            holder.tvTitle.setTextColor(getContext().getResources().getColor(android.R.color.black));
        }
        return convertView;
    }

    static class ViewHolder {
        View itemView;
        TextView tvTitle;

        ViewHolder(View itemView) {
            this.itemView = itemView;
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
        }
    }

    public interface PlaylistCallbacks {
        int getActiveIndex();

        Audio getActiveAudio();
    }

    public void setPlaylistCallbacks(PlaylistCallbacks playlistCallbacks) {
        this.mPlaylistCallbacks = playlistCallbacks;
    }
}
