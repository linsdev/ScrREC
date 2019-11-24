package com.lins.scrrec.recorder;

import android.media.MediaRecorder;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import com.lins.scrrec.MainActivity;

import java.io.IOException;

@RequiresApi(api = 21)
class RecorderV21 extends RecorderBase {

    private MediaRecorder mediaRecorder;

    @Override
    public void init(String dir_path, RecordQuality quality) {
        mediaRecorder = new MediaRecorder();
        if (initRecorder(dir_path, quality))
            MainActivity.self.gettingPermissionToScreenCapture(mediaRecorder);
    }

    @Override
    public void start() {
        mediaRecorder.start();
    }

    @Override
    public void stop() {
        mediaRecorder.stop();
    }

    private boolean initRecorder(String dir_path, RecordQuality quality) {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(generateFileName(dir_path));
        mediaRecorder.setVideoSize(RecorderService.display.widthPixels, RecorderService.display.heightPixels);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.VORBIS);

        switch (quality) {
            case Low:
                mediaRecorder.setVideoEncodingBitRate(5000 * 1000);
                break;
            case Medium:
                mediaRecorder.setVideoEncodingBitRate(12000 * 1000);
                break;
            case High:
                mediaRecorder.setVideoEncodingBitRate(25000 * 1000);
                break;
        }

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(MainActivity.self,"Error start recording", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}