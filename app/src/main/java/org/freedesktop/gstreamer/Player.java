package org.freedesktop.gstreamer;

import java.io.Closeable;
import android.view.Surface;
import android.content.Context;

public class Player implements Closeable {

    private static native void nativeClassInit();
    private native void nativeNew();
    private native void nativeFree();
    private native void nativePlay();
    private native void nativePause();
    private native void nativeStop();
    private native void nativeSeek(long position);
    private native String nativeGetUri();
    private native void nativeSetUri(String uri);
    private native long nativeGetPosition();
    private native long nativeGetDuration();
    private native double nativeGetVolume();
    private native void nativeSetVolume(double volume);
    private native boolean nativeGetMute();
    private native void nativeSetMute(boolean mute);
    private native void nativeSetSurface(Surface surface);
    private long native_player;

    public static interface PositionUpdatedListener {
        abstract void positionUpdated(Player player, long position);
    }
    public static interface DurationChangedListener {
        abstract void durationChanged(Player player, long duration);
    }
    public static interface BufferingListener {
        abstract void buffering(Player player, int percent);
    }
    public static interface StateChangedListener {
        abstract void stateChanged(Player player, State state);
    }
    public static interface EndOfStreamListener {
        abstract void endOfStream(Player player);
    }
    public static interface ErrorListener {
        abstract void error(Player player, Error error, String errorMessage);
    }
    public static interface VideoDimensionsChangedListener {
        abstract void videoDimensionsChanged(Player player, int width, int height);
    }

    public enum State { STOPPED, BUFFERING, PAUSED, PLAYING }
    public enum Error { FAILED }

    private static final State[] stateMap = { State.STOPPED, State.BUFFERING, State.PAUSED, State.PLAYING };
    private static final Error[] errorMap = { Error.FAILED };

    private Surface surface;
    private PositionUpdatedListener positionUpdatedListener;
    private DurationChangedListener durationChangedListener;
    private StateChangedListener stateChangedListener;
    private BufferingListener bufferingListener;
    private ErrorListener errorListener;
    private VideoDimensionsChangedListener videoDimensionsChangedListener;
    private EndOfStreamListener endOfStreamListener;

    public static void init(Context context) throws Exception {
        System.loadLibrary("gstreamer_android");
        GStreamer.init(context);
        System.loadLibrary("player");
        nativeClassInit();
    }

    public Player() {
        nativeNew();
    }

    @Override
    public void close() {
        nativeFree();
    }

    public void play() {
        nativePlay();
    }

    public void pause() {
        nativePause();
    }

    public void stop() {
        nativeStop();
    }

    public void seek(long position) {
        nativeSeek(position);
    }

    public String getUri() {
        return nativeGetUri();
    }

    public void setUri(String uri) {
        nativeSetUri(uri);
    }

    public long getPosition() {
        return nativeGetPosition();
    }

    public long getDuration() {
        return nativeGetDuration();
    }

    public double getVolume() {
        return nativeGetVolume();
    }

    public void setVolume(double volume) {
        nativeSetVolume(volume);
    }

    public boolean getMute() {
        return nativeGetMute();
    }

    public void setMute(boolean mute) {
        nativeSetMute(mute);
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
        nativeSetSurface(surface);
    }

    public Surface getSurface() {
        return surface;
    }

    public void setPositionUpdatedListener(PositionUpdatedListener listener) {
        positionUpdatedListener = listener;
    }

    private void onPositionUpdated(long position) {
        if (positionUpdatedListener != null)
            positionUpdatedListener.positionUpdated(this, position);
    }

    public void setDurationChangedListener(DurationChangedListener listener) {
        durationChangedListener = listener;
    }

    private void onDurationChanged(long duration) {
        if (durationChangedListener != null)
            durationChangedListener.durationChanged(this, duration);
    }

    public void setStateChangedListener(StateChangedListener listener) {
        stateChangedListener = listener;
    }

    private void onStateChanged(int stateIdx) {
        if (stateChangedListener != null) {
            State state = stateMap[stateIdx];
            stateChangedListener.stateChanged(this, state);
        }
    }

    public void setBufferingListener(BufferingListener listener) {
        bufferingListener = listener;
    }

    private void onBuffering(int percent) {
        if (bufferingListener != null)
            bufferingListener.buffering(this, percent);
    }

    public void setEndOfStreamListener(EndOfStreamListener listener) {
        endOfStreamListener = listener;
    }

    private void onEndOfStream() {
        if (endOfStreamListener != null)
            endOfStreamListener.endOfStream(this);
    }

    public void setErrorListener(ErrorListener listener) {
        errorListener = listener;
    }

    private void onError(int errorCode, String errorMessage) {
        if (errorListener != null) {
            Error error = errorMap[errorCode];
            errorListener.error(this, error, errorMessage);
        }
    }

    public void setVideoDimensionsChangedListener(VideoDimensionsChangedListener listener) {
        videoDimensionsChangedListener = listener;
    }

    private void onVideoDimensionsChanged(int width, int height) {
        if (videoDimensionsChangedListener != null)
            videoDimensionsChangedListener.videoDimensionsChanged(this, width, height);
    }
}
