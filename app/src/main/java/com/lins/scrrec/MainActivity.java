package com.lins.scrrec;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.lins.scrrec.filechooser.FileChooserActivity;
import com.lins.scrrec.player.PlayerActivity;
import com.lins.scrrec.recorder.RecorderService;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {

    public static MainActivity self;

    public static final String PREF = "PREF";
    public static final String PREF_REC_FILES = "REC_FILES";
    private static final String PREF_PATH_REC = "PATH_REC";
    private static final String PREF_REC_QUALITY = "REC_QUALITY";
    private static final String PREF_PATH_PLAYER = "PATH_PLAYER";
    private static final String DEFAULT_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();

    private static final int REQUEST_CODE_GETTING_PERMISSION = 1000;
    private static final int REQUEST_CODE_FILE_CHOOSER_ACTIVITY = 3333;

    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;

        setContentView(R.layout.activity_main);

        Button btnRecChooseFile = (Button) findViewById(R.id.btnRecChooseFile);
        Button btnPlayerChooseFile = (Button) findViewById(R.id.btnPlayerChooseFile);
        final ToggleButton toggleBtnRec = (ToggleButton) findViewById(R.id.toggleBtnRec);
        final RadioGroup radioGroupQuality = (RadioGroup) findViewById(R.id.radioGroupQuality);

        final EditText editTextRecPath = (EditText) findViewById(R.id.editRecPath);
        editTextRecPath.setKeyListener(null);
        EditText editTextPlayerPath = (EditText) findViewById(R.id.editPlayerPath);
        editTextPlayerPath.setKeyListener(null);

        btnRecChooseFile.setOnLongClickListener(this);
        btnPlayerChooseFile.setOnLongClickListener(this);
        toggleBtnRec.setOnLongClickListener(this);

        toggleBtnRec.setOnCheckedChangeListener((btn, isChecked) -> {
            toggleBtnRec.setEnabled(false);
            if (isChecked) {
                final int qualityRadioBtnID = radioGroupQuality.getCheckedRadioButtonId();
                final String dir_path = editTextRecPath.getText().toString();
                RecorderService.Init(MainActivity.this, dir_path, qualityRadioBtnID);
                saveRecData(dir_path, qualityRadioBtnID);
            }
            else {
                RecorderService.Stop(MainActivity.this);
                unlockBtnRec();
            }
        });

        findViewById(R.id.btnPlay).setOnClickListener(view -> {
            String filename = ((EditText) findViewById(R.id.editPlayerPath)).getText().toString();
            PlayerActivity.OpenVideo(MainActivity.this, filename);
            savePref(PREF_PATH_PLAYER, filename);
        });

        btnRecChooseFile.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, FileChooserActivity.class)
                    .putExtra(FileChooserActivity.EXTRA_PATH, editTextRecPath.getText().toString());
            startActivityForResult(i, REQUEST_CODE_FILE_CHOOSER_ACTIVITY);
        });

        btnPlayerChooseFile.setOnClickListener(view -> {
            //OpenFileDialog(FileDialog.Mode.CHOOSE_FILE,
            //        ((EditText) findViewById(R.id.editTextPlayerPath)).getText().toString());
        });
    }

    private void savePref(final String key, final String value) {
        SharedPreferences.Editor pref = getSharedPreferences(PREF, Activity.MODE_PRIVATE).edit();
        pref.putString(key, value);
        pref.apply();
    }

    private void saveRecData(final String dir_path, final int qualityID) {
        SharedPreferences.Editor pref = getSharedPreferences(PREF, Activity.MODE_PRIVATE).edit();
        pref.putString(PREF_PATH_REC, dir_path);
        pref.putInt(PREF_REC_QUALITY, qualityID);
        pref.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences pref = getSharedPreferences(PREF, Activity.MODE_PRIVATE);
        ((RadioButton) findViewById(pref.getInt(PREF_REC_QUALITY, R.id.radioBtnMedium))).setChecked(true);
        /** Setup UI state based on data from {@link RecorderService} */
        if (RecorderService.isRecording()) {
            ((ToggleButton) findViewById(R.id.toggleBtnRec)).setChecked(RecorderService.isRecording());
            ((RadioButton) findViewById(RecorderService.getRecordQuality().getID())).setChecked(true);
        }
        unlockBtnRec();
        ((EditText) findViewById(R.id.editRecPath)).setText(pref.getString(PREF_PATH_REC, DEFAULT_DIR));
        ((EditText) findViewById(R.id.editPlayerPath)).setText(pref.getString(PREF_PATH_PLAYER, DEFAULT_DIR));
    }

    @Override
    protected void onStop() {
        if(!RecorderService.isRecording())
            RecorderService.Stop(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        self = null;
        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View view) {
        Toast.makeText(this, view.getContentDescription(), Toast.LENGTH_SHORT).show();
        return false;
    }

    public void unlockBtnRec() {
        findViewById(R.id.toggleBtnRec).setEnabled(true);
    }

    @RequiresApi(api = 21)
    public void gettingPermissionToScreenCapture(MediaRecorder mr) {
        mediaRecorder = mr;
        MediaProjectionManager m = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(m.createScreenCaptureIntent(), REQUEST_CODE_GETTING_PERMISSION);
    }

    @TargetApi(21)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GETTING_PERMISSION)
            onActivityResultGettingPermission(resultCode, data);
        else if (requestCode == REQUEST_CODE_FILE_CHOOSER_ACTIVITY)
            onActivityResultFileChooserActivity(resultCode, data);
    }

    @RequiresApi(api = 21)
    private void onActivityResultGettingPermission(int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            RecorderService.Stop(this);
            Toast.makeText(this, "Screen cast permission denied", Toast.LENGTH_SHORT).show();
            ((ToggleButton) findViewById(R.id.toggleBtnRec)).setChecked(false);
            unlockBtnRec();
            return;
        }
        /** Setting permission to screen capture for mediaRecorder */
        ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(resultCode, data)
                .createVirtualDisplay("ScrREC",
                        RecorderService.display.widthPixels, RecorderService.display.heightPixels,
                        RecorderService.display.densityDpi,
                        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        mediaRecorder.getSurface(), null, null);
        /** User allows screen capture, so starting is safe */
        RecorderService.Start(this);
        mediaRecorder = null;
    }

    private void onActivityResultFileChooserActivity(int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            ((EditText) findViewById(R.id.editRecPath))
                    .setText(data.getStringExtra(FileChooserActivity.EXTRA_PATH));
    }
}
