package com.lins.scrrec.recorder;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.lins.scrrec.MainActivity;
import com.lins.scrrec.R;

import java.util.HashSet;
import java.util.Set;

public class RecorderService extends Service {

    public static final String EXTRA_FILE_NAME = "FILE_NAME";
    public static final String EXTRA_QUALITY = "QUALITY";
    public static final String ACTION_START = "START";
    public static final String ACTION_STOP = "STOP";
    private static final int NOTIFICATION_ID = 314;

    public static DisplayMetrics display;
    private static PowerManager.WakeLock wakeLock;
    private static boolean is_recording = false;
    private static RecordQuality recordQuality = RecordQuality.Medium;
    private static String fileName = "";

    private RecorderBase recorder;
    private static RecorderService self = null;

    public static boolean isRecording() {
        return is_recording;
    }

    public static RecordQuality getRecordQuality() {
        return recordQuality;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void Init(Context context, String file_name, int radioButtonID) {
        context.startService(new Intent(context, RecorderService.class)
                .putExtra(EXTRA_FILE_NAME, file_name)
                .putExtra(EXTRA_QUALITY, radioButtonID));
    }

    public static void Start(Context context) {
        context.startService(new Intent(context, RecorderService.class)
                .setAction(ACTION_START));
        MainActivity.self.unlockBtnRec();
    }

    public static void Stop(Context context) {
        context.stopService(new Intent(context, RecorderService.class));
    }

    public static void UpdateRecFileList(String file_name) {
        new Thread(() -> {
            SharedPreferences pref = self.getSharedPreferences(MainActivity.PREF, Activity.MODE_PRIVATE);
            Set<String> rec_files = pref.getStringSet(MainActivity.PREF_REC_FILES, new HashSet<>());
            rec_files.add(file_name);
            SharedPreferences.Editor pref_editor = pref.edit();
            pref_editor.putStringSet(MainActivity.PREF_REC_FILES, rec_files);
            pref_editor.apply();
        }).run();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;

        recorder = (Build.VERSION.SDK_INT >= 21) ? new RecorderV21() : new Recorder();

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "ScrREC");

        display = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(display);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_START.equals(intent.getAction())) {
            startRecording();
            return Service.START_STICKY;
        }
        /** This action may be come only from Notification */
        else if (ACTION_STOP.equals(intent.getAction())) {
            stopRecording();
            /** Finish {@link MainActivity} */
            if (MainActivity.self != null)
                MainActivity.self.finish();
        }
        /** By default, init recording */
        else if (!is_recording) {
            fileName = intent.getStringExtra(EXTRA_FILE_NAME);
            recordQuality = RecordQuality.fromID(intent.getIntExtra(EXTRA_QUALITY, 0));
            recorder.init(fileName, recordQuality);
            return Service.START_STICKY;
        }
        return Service.START_NOT_STICKY;
    }

    private void startRecording() {
        recorder.start();
        is_recording = true;
        showNotification();
        wakeLock.acquire();
    }

    private void stopRecording() {
        recorder.stop();
        is_recording = false;
        wakeLock.release();
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        if (is_recording)
            stopRecording();
        self = null;
        super.onDestroy();
    }

    private void showNotification() {
        PendingIntent startMainActivityIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentRS = new Intent(this, RecorderService.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .setAction(ACTION_STOP);
        PendingIntent stopThisServiceIntent = PendingIntent.getService(this, 0, intentRS, 0);

        startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this)
                .setTicker("Start recording")
                .setContentTitle("ScrREC")
                .setContentText("Recording...")
                .setSmallIcon(R.mipmap.ic_circle)
                .setColor(ContextCompat.getColor(this, R.color.colorRec))
                .setContentIntent(startMainActivityIntent)
                .addAction(R.mipmap.ic_square, "STOP", stopThisServiceIntent)
                .build());
    }
}
