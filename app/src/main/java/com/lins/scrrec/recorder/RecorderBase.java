package com.lins.scrrec.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.lins.scrrec.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

abstract class RecorderBase {
    public abstract void init(String file_name, RecordQuality quality);
    public abstract void start();
    public abstract void stop();

    static String generateFileName(String dir_path) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        String filename = sdf.format(c.getTime());
        String file_name = String.format("%s/%s.mp4", dir_path, filename);
        RecorderService.UpdateRecFileList(file_name);
        return file_name;
    }
}