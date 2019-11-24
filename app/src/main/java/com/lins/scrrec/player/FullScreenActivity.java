package com.lins.scrrec.player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.lins.scrrec.R;

/**
 * Full-screen activity that shows and hides the system UI
 * (i.e. status bar and navigation/system bar) with user interaction.
 */
public class FullScreenActivity extends AppCompatActivity {
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler hideHandler = new Handler();
    private View contentView;
    private final Runnable hidePartRunnable = new Runnable() {
        /**
         * Note that some of these constants are new as of API 16 (Jelly Bean)
         * and API 19 (KitKat). It is safe to use them, as they are inlined
         * at compile-time and do nothing on earlier devices.
         */
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            /** Delayed removal of status and navigation bar */
            contentView.setSystemUiVisibility(
                      View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable showPartRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.show();
        }
    };
    protected boolean isFullScreen = false;

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            onContentViewTap();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onContentViewDoubleTap();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        contentView = findViewById(R.id.video_surface);

        /** Set up the user interaction to manually show or hide the system UI. */
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureListener());
        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        fullScreen();
        hideControls();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {  // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fullScreen() {
        /** Hide UI first */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        isFullScreen = true;

        /** Schedule a runnable to remove the status and navigation bar after a delay */
        hideHandler.removeCallbacks(showPartRunnable);
        hideHandler.postDelayed(hidePartRunnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void normalScreen() {
        /** Show the system bar */
        contentView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        isFullScreen = false;

        /** Schedule a runnable to display UI elements after a delay */
        hideHandler.removeCallbacks(hidePartRunnable);
        hideHandler.postDelayed(showPartRunnable, UI_ANIMATION_DELAY);
    }

    protected void onContentViewTap() {}

    protected void onContentViewDoubleTap() {
        if (isFullScreen)
            normalScreen();
        else
            fullScreen();
    }

    protected void hideControls() {
        findViewById(R.id.fullscreen_content_controls).setVisibility(View.GONE);
    }

    protected void showControls() {
        findViewById(R.id.fullscreen_content_controls).setVisibility(View.VISIBLE);
    }
}
