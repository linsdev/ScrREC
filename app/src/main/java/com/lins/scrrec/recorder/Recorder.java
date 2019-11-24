package com.lins.scrrec.recorder;

import android.os.AsyncTask;
import android.widget.Toast;

import com.lins.scrrec.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Recorder extends RecorderBase {

    private static final String APP_FILE_NAME = "/system/bin/screenrecord";

    private String command;
    private SuTask suTask;
    private String dir_path;

    @Override
    public void init(String dir_path, RecordQuality quality) {
        if (isRootNotAvailable()) {
            RecorderService.Stop(MainActivity.self);
            Toast.makeText(MainActivity.self, "Root your device first!", Toast.LENGTH_LONG).show();
            return;
        }
        this.dir_path = dir_path;
        command = APP_FILE_NAME + " --bit-rate ";
        switch (quality) {
            case Low:     command += 1000 * 1000;  break;
            case Medium:  command += 2500 * 1000;  break;
            case High:    command += 5000 * 1000;  break;
        }
        RecorderService.Start(MainActivity.self);
    }

    private static boolean isRootNotAvailable(){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            return false;
        } catch (Exception e) {
            return true;
        } finally {
            if (process != null) {
                try { process.destroy(); }
                catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void start() {
        suTask = new SuTask();
        suTask.execute(null, null);
    }

    @Override
    public void stop() {
        if (suTask != null) {
            suTask.onCancelled();
            suTask.cancel(true);
            suTask = null;
        }
    }

    private class SuTask extends AsyncTask<Boolean, Void, Boolean> {

        private static final String ASCII = "ASCII";
        private boolean is_continue = true;

        @Override
        protected Boolean doInBackground(Boolean... booleans) {

            try {
                while (is_continue) {
                    Process su = Runtime.getRuntime().exec("su");
                    OutputStream output = su.getOutputStream();
                    String s = String.format("%s %s\nexit\n", command, generateFileName(dir_path));
                    output.write(s.getBytes(ASCII));
                    su.waitFor();
                }
                return true;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.self, "Error start recording", Toast.LENGTH_LONG).show();
            }
            return false;
        }

        @Override
        protected void onCancelled() {
            try {
                is_continue = false;

                Process su = Runtime.getRuntime().exec("su");
                OutputStream output = su.getOutputStream();
                //String cmd = String.format("r=`ps -t screenrecord | grep %s`\n", APP_FILE_NAME);
                output.write(("r=`ps -t screenrecord | grep " + APP_FILE_NAME + "`\n").getBytes(ASCII));
                output.write("echo ${r#* }\n".getBytes(ASCII));

                InputStream input = su.getInputStream();
                byte[] buffer = new byte[20];
                input.read(buffer, 0, 20);
                String PID = new String(buffer, 0, 20);
                PID = PID.substring(0, PID.indexOf(' '));

                output.write(("kill -INT " + PID + "\nexit\n").getBytes(ASCII));
                su.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}