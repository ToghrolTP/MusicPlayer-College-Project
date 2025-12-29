package org.toghrol.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen; // Added Import
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private TextView songTitle, artistName, currentTime, totalTime;
    private ImageButton btnPlayPause, btnNext, btnPrevious;
    private SeekBar seekBar;

    // Player
    private SongAdapter songAdapter;
    private List<Song> songList;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;

    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        songList = new ArrayList<>();

        mediaPlayer = new MediaPlayer();

        setupButtonListeners();

        setupSeekBarListener();

        if (checkPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        songTitle = findViewById(R.id.songTitle);
        artistName = findViewById(R.id.artistName);
        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);

        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);

        seekBar = findViewById(R.id.seekBar);
    }

    private void setupButtonListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
    }

    private void setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Permission denied. Cannot access music files.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSongs() {
        songList.clear();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        Cursor cursor = getContentResolver().query(uri, projection, selection,
                null, MediaStore.Audio.Media.TITLE + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);
                long duration = cursor.getLong(3);

                if (artist == null || artist.equals("<unknown>")) {
                    artist = "Unknown Artist";
                }

                Song song = new Song(title, artist, path, duration);
                songList.add(song);
            }
            cursor.close();
        }

        songAdapter = new SongAdapter(songList, position -> {
            playSong(position);
        });

        recyclerView.setAdapter(songAdapter);

        if (songList.isEmpty()) {
            Toast.makeText(this, "No music files found on device",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void playSong(int position) {
        if (position < 0 || position >= songList.size()) {
            return;
        }

        currentSongIndex = position;
        Song song = songList.get(position);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            updateNowPlayingUI(song);
            updatePlayPauseButton(true);

            mediaPlayer.setOnCompletionListener(mp -> playNextSong());

            startSeekBarUpdate();

        } catch (IOException e) {
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null || currentSongIndex == -1) {
            if (!songList.isEmpty()) {
                playSong(0);
            }
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            updatePlayPauseButton(false);
        } else {
            mediaPlayer.start();
            updatePlayPauseButton(true);
            startSeekBarUpdate();
        }
    }

    private void playNextSong() {
        if (songList.isEmpty()) return;

        int nextIndex = (currentSongIndex + 1) % songList.size();
        playSong(nextIndex);
    }

    private void playPreviousSong() {
        if (songList.isEmpty()) return;

        int prevIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
        playSong(prevIndex);
    }

    private void updateNowPlayingUI(Song song) {
        songTitle.setText(song.getTitle());
        artistName.setText(song.getArtist());
        totalTime.setText(song.getFormattedDuration());

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setProgress(0);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    private void startSeekBarUpdate() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);

                    long seconds = (currentPosition / 1000) % 60;
                    long minutes = (currentPosition / (1000 * 60)) % 60;
                    currentTime.setText(String.format("%d:%02d", minutes, seconds));

                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBar);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBar);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if (mediaPlayer != null && mediaPlayer.isPlaying()) {
        //     mediaPlayer.pause();
        //     updatePlayPauseButton(false);
        // }
    }
}