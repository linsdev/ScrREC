package com.lins.scrrec.player;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.lins.scrrec.R;

import org.freedesktop.gstreamer.Player;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PlayerActivity extends FullScreenActivity
                            implements Player.PositionUpdatedListener,
                                       Player.DurationChangedListener,
                                       Player.VideoDimensionsChangedListener {

    private static final String BUNDLE_IS_PLAYING = "IS_PLAYING";
    private static final String BUNDLE_POSITION = "POSITION";
    private static final long SEEK_BAR_CONST = 1_000_000L;

    private boolean is_playing = true;
    private int position = 0;    /** Current position from seekBar in seconds */
    private Player player;
    private SeekBar seekBar;

    public static void OpenVideo(Context context, String file_name) {
        android.net.Uri uri = android.net.Uri.fromFile(new File(file_name));
        context.startActivity(new Intent(context, PlayerActivity.class).setData(uri));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Player.init(this);
        } catch (Exception e) {
            finish();
            return;
        }
        if (savedInstanceState != null) {
            is_playing = savedInstanceState.getBoolean(BUNDLE_IS_PLAYING);
            position = savedInstanceState.getInt(BUNDLE_POSITION);
        }

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar sb) {}
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                updateTimeText();
            }
            public void onStopTrackingTouch(SeekBar sb) {
                player.seek(sb.getProgress() * SEEK_BAR_CONST);
            }
        });

        ((GStreamerSurfaceView) findViewById(R.id.video_surface))
                .getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder sh) {}
            public void surfaceChanged(SurfaceHolder sh, int format, int width, int height) {
                player.setSurface(sh.getSurface());
            }
            public void surfaceDestroyed(SurfaceHolder sh) {
                player.setSurface(null);
            }
        });

        player = new Player();
        player.setPositionUpdatedListener(this);
        player.setDurationChangedListener(this);
        player.setVideoDimensionsChangedListener(this);
        player.setUri(getIntent().getDataString());
        player.seek(position * SEEK_BAR_CONST);
        updateTimeText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (is_playing) player.play();
    }

    @Override
    protected void onPause() {
        player.pause();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_IS_PLAYING, is_playing);
        outState.putInt(BUNDLE_POSITION, position);
    }

    @Override
    protected void onDestroy() {
        player.close();
        super.onDestroy();
    }

    @Override
    protected void onContentViewTap() {
        super.onContentViewTap();
        if (is_playing) {
            player.pause();
            is_playing = false;
            showControls();
        } else {
            player.play();
            is_playing = true;
            if (isFullScreen) hideControls();
        }
    }

    @Override
    protected void onContentViewDoubleTap() {
        super.onContentViewDoubleTap();
        updateVideoSurface();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateVideoSurface();
    }

    private void updateVideoSurface() {
        final long actual_position = position;
        final int duration = seekBar.getMax();
        player.seek(((position + duration / 2) % duration) * SEEK_BAR_CONST);
        player.seek(actual_position * SEEK_BAR_CONST);
    }

    private void updateTimeText() {
        SeekBar seekBar = (SeekBar) this.findViewById(R.id.seek_bar);
        position = seekBar.getProgress();
        final int duration = seekBar.getMax();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        setTitle(df.format(new Date(position)) + "  /  " + df.format(new Date(duration)));
    }

    @Override
    public void positionUpdated(Player player, final long position) {
        runOnUiThread (() -> seekBar.setProgress((int) (position / SEEK_BAR_CONST)));
    }

    @Override
    public void durationChanged(Player player, final long duration) {
        runOnUiThread (() -> {
            seekBar.setMax((int) (duration / SEEK_BAR_CONST));
            updateTimeText();
        });
    }

    @Override
    public void videoDimensionsChanged(Player player, final int width, final int height) {
        runOnUiThread (() -> {
            GStreamerSurfaceView videoSurface = (GStreamerSurfaceView) findViewById(R.id.video_surface);
            videoSurface.media_width = width;
            videoSurface.media_height = height;
            videoSurface.requestLayout();
        });
    }
}
