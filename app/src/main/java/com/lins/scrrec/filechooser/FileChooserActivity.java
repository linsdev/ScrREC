package com.lins.scrrec.filechooser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lins.scrrec.R;

public class FileChooserActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

	//public static final String EXTRA_IS_DIR_SELECTING = "IS_DIR_SELECTING";
	public static final String EXTRA_PATH = "PATH";

	public static final String FOLDER = "Folder";
	public static final String PARENT_FOLDER = "ParentDirectory";

	private static final String EXT_STORAGE_DIR = Environment.getExternalStorageDirectory().getName();
	//private boolean isSelectingFolder;
	private File currentFolder;
	private FileArrayAdapter fileArrayListAdapter;
	private FileFilter fileFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			//isSelectingFolder = extras.getBoolean(EXTRA_IS_DIR_SELECTING);
			currentFolder = new File(extras.getString(EXTRA_PATH));
		}
		fileFilter = path -> {
            // || path.getName().endsWith(".mp4");
            return path.isDirectory() && path.canRead() && path.canWrite() && path.canExecute();
        };

		currentFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsoluteFile();
		fill(currentFolder);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			final File parent_file = currentFolder.getParentFile();
			if ((!currentFolder.getName().equals(EXT_STORAGE_DIR)) && parent_file != null) {
				currentFolder = parent_file;
				fill(currentFolder);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void fill(File f) {
		File[] folders = null;
		if (fileFilter != null)
			folders = f.listFiles(fileFilter);
		else
			folders = f.listFiles();

		this.setTitle(getString(R.string.current_dir) + ": " + f.getName());
		List<FileInfo> dirs = new ArrayList<FileInfo>();
		List<FileInfo> files = new ArrayList<FileInfo>();
		try {
			for (File file : folders) {
				if (!file.isHidden()) {
					if (file.isDirectory())
						dirs.add(new FileInfo(file.getName(),
								FOLDER,
								file.getAbsolutePath(), true, false));
					else
						files.add(new FileInfo(file.getName(),
								getString(R.string.file_size) + ": " + file.length(),
								file.getAbsolutePath(), false, false));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(dirs);
		Collections.sort(files);
		dirs.addAll(files);
		if (!f.getName().equalsIgnoreCase(EXT_STORAGE_DIR) && f.getParentFile() != null) {
			dirs.add(0, new FileInfo("..", PARENT_FOLDER, f.getParent(), false, true));
		}
		fileArrayListAdapter = new FileArrayAdapter(FileChooserActivity.this, R.layout.file_row, dirs);
		this.setListAdapter(fileArrayListAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FileInfo fileDescriptor = fileArrayListAdapter.getItem(position);
		File fileSelected = new File(fileDescriptor.getPath());
		/*if (fileDescriptor.isFolder() || fileDescriptor.isParent()) {
			if (isSelectingFolder) {
				returnResult(fileSelected.getAbsolutePath());
			}
			else {
				currentFolder = fileSelected;
				fill(currentFolder);
			}
		} else {
			returnResult(fileSelected.getAbsolutePath());
		}*/
		currentFolder = fileSelected;
		fill(currentFolder);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
		FileInfo fileDescriptor = fileArrayListAdapter.getItem(position);
		if (fileDescriptor.isParent())
			return false;
		returnResult(fileDescriptor.getName());
		return true;
	}

	private void returnResult(final String path) {
		Intent intent = new Intent()
				.putExtra(EXTRA_PATH, path);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}
}