package com.lins.scrrec.filechooser;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lins.scrrec.R;

@SuppressLint("DefaultLocale")
public class FileArrayAdapter extends ArrayAdapter<FileInfo> {

	private Context context;
	private int resourceID;
	private List<FileInfo> items;

	public FileArrayAdapter(Context context, int textViewResourceId, List<FileInfo> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.resourceID = textViewResourceId;
		this.items = objects;
	}

	public FileInfo getItem(int i) {
		return items.get(i);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(resourceID, null);
			viewHolder = new ViewHolder();
			viewHolder.icon = (ImageView) convertView.findViewById(android.R.id.icon);
			viewHolder.name = (TextView) convertView.findViewById(R.id.name);
			viewHolder.details = (TextView) convertView.findViewById(R.id.details);
			convertView.setTag(viewHolder);
		} else
			viewHolder = (ViewHolder) convertView.getTag();

		FileInfo option = items.get(position);
		if (option != null) {
			if (option.getData().equalsIgnoreCase(FileChooserActivity.FOLDER))
				viewHolder.icon.setImageResource(R.drawable.ic_folder);
			else if (option.getData().equalsIgnoreCase(FileChooserActivity.PARENT_FOLDER))
				viewHolder.icon.setImageResource(R.drawable.ic_back);
			/*else {
				String name = option.getName().toLowerCase();
				if (name.endsWith(".mp4"))
					viewHolder.icon.setImageResource(R.drawable.mp4);
			}*/
			viewHolder.name.setText(option.getName());
			viewHolder.details.setText(option.getData());
		}
		return convertView;
	}

	class ViewHolder {
		ImageView icon;
		TextView name;
		TextView details;
	}
}
