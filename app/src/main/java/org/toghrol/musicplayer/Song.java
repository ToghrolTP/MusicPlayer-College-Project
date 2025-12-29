package org.toghrol.musicplayer;

public class Song {
    private String title;
    private String artist;
    private String path;
    private long duration;

    public Song(String title, String artist, String path, long duration) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
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

    public String getFormattedDuration() {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (1000 * 60)) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
