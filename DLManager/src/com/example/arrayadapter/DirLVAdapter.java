package com.example.arrayadapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.activities.MainDownloadManager;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.Files;

@SuppressLint("NewApi")
public class DirLVAdapter extends ArrayAdapter<Files> {

	private List<Files> listFiles;
	private int resource;
	private Holder holder;

	public DirLVAdapter(Context context, int resource, List<Files> listFiles) {
		super(context, resource, listFiles);
		this.listFiles = listFiles;
		this.resource = resource;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final Files file = (Files) getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					this.resource, null);
			holder = new Holder();

			holder.ivFileType = (ImageView) convertView
					.findViewById(R.id.fileimage);

			holder.tvFileName = (TextView) convertView
					.findViewById(R.id.filename);
			holder.cbCheck = (CheckBox) convertView.findViewById(R.id.choose);
			convertView.setTag(holder);

		} else {
			holder = (Holder) convertView.getTag();

		}
		if (file.getFile().isDirectory()) {
			CheckBox cb = (CheckBox) convertView.findViewById(R.id.choose);
			cb.setVisibility(View.INVISIBLE);
		}

		String extension = file.getFile().getName()
				.substring(file.getFile().getName().lastIndexOf(".") + 1)
				.toUpperCase();

		if (file.getFile().isDirectory()) {
			extension = "folder";
		}

		if (ConstantsVars.IMAGE_TYPE.contains(extension)) {
			holder.ivFileType.setImageBitmap(file.getAvatar());
		} else {
			holder.ivFileType
					.setImageResource(ConstantsVars.setIcon(extension));
		}
		holder.tvFileName.setText(file.getFile().getName());
		holder.cbCheck.setChecked(file.isChecked());
		holder.cbCheck.setTag(file);

		holder.cbCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				CheckBox v = (CheckBox) arg0;
				Files f = (Files) arg0.getTag();
				f.setChecked(v.isChecked());
			}
		});

		return convertView;
	}

	private int nbrChecked() {
		int nbr = 0;
		for (Files f : listFiles) {
			if (f.isChecked()) {
				nbr++;
			}
		}
		return nbr;
	}

	private class Holder {
		public ImageView ivFileType;
		public TextView tvFileName;
		public CheckBox cbCheck;

	}
}
