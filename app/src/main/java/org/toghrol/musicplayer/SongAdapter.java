package org.toghrol.musicplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> implements Filterable {

    private List<Song> songList;
    private List<Song> songListFull;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongAdapter(List<Song> songList, OnSongClickListener listener) {
        this.songList = songList;
        this.songListFull = new ArrayList<>(songList);
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
        holder.bind(song);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    @Override
    public Filter getFilter() {
        return songFilter;
    }

    private Filter songFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Song> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(songListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Song song : songListFull) {
                    if (song.getTitle().toLowerCase().contains(filterPattern) ||
                            song.getArtist().toLowerCase().contains(filterPattern)) {
                        filteredList.add(song);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            songList.clear();
            songList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateList(List<Song> newSongs) {
        songListFull = new ArrayList<>(newSongs);
        songList = new ArrayList<>(newSongs);
        notifyDataSetChanged();
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

        public void bind(Song song) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());
            durationTextView.setText(song.getFormattedDuration());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSongClick(song);
                }
            });
        }
    }
}
