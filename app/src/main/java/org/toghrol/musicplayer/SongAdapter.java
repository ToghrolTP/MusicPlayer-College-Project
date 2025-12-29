package org.toghrol.musicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, position);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView artistTextView;
        private TextView durationTextView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.songItemTitle);
            artistTextView = itemView.findViewById(R.id.songItemArtist);
            durationTextView = itemView.findViewById(R.id.songItemDuration);
        }

        public void bind(Song song, int position) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());
            durationTextView.setText(song.getFormattedDuration());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(position);
                }
            });
        }
    }
}
