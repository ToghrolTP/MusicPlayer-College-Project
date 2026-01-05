package org.toghrol.musicplayer;

import android.content.ContentUris;
import android.net.Uri;

public class Song {
    private String title;
    private String artist;
    private String path;
    private long duration;
    private long albumId; // Field for Album ID

    // Constructor with 5 parameters
    public Song(String title, String artist, String path, long duration, long albumId) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    // Generates the URI for the album art using the albumId
    public Uri getAlbumArtUri() {
        return ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    public String getFormattedDuration() {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}