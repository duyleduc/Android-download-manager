package com.example.arrayadapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dlmanager.R;
import com.example.model.FileModel;

public class DownloadingLVAdapter extends ArrayAdapter<FileModel> {

	private int resource;
	private Holder holder;

	public DownloadingLVAdapter(Context context, int resource,
			ArrayList<FileModel> listFiles) {
		super(context, resource, listFiles);

		this.resource = resource;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final FileModel aFile = (FileModel) getItem(position);
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					this.resource, null);
			holder = new Holder();

			holder.pbDowloading = (ProgressBar) convertView
					.findViewById(R.id.propressbar_download);
			holder.tvFileName = (TextView) convertView
					.findViewById(R.id.filename_downloading);
			holder.tvStatus = (TextView) convertView
					.findViewById(R.id.download_status);
			holder.tvFilesize = (TextView) convertView
					.findViewById(R.id.file_downloading_size);

			convertView.setTag(holder);

		} else {
			holder = (Holder) convertView.getTag();

		}

		String url = aFile.getfUrl();
		String fName = url.substring(url.lastIndexOf("/") + 1);
		holder.tvFileName.setText(fName);

		long fileSizeinBytes = aFile.getfSize();
		double sizeInKbOrMb;
		if (fileSizeinBytes < 1024 * 1024) {
			sizeInKbOrMb = (double) fileSizeinBytes / (double) 1024;
		} else {
			sizeInKbOrMb = (double) fileSizeinBytes / (double) 1048576;
		}
		sizeInKbOrMb = Math.round(sizeInKbOrMb * 100.00) / 100.00;
		if (fileSizeinBytes < 1024 * 1024) {
			holder.tvFilesize.setText(Double.toString(sizeInKbOrMb) + " Kb");
		} else {
			holder.tvFilesize.setText(Double.toString(sizeInKbOrMb) + " Mb");
		}

		// progressing bar
		holder.pbDowloading.setMax((int) aFile.getfSize());
		holder.pbDowloading.setProgress((int) aFile.getDownloadedLenght());

		holder.tvStatus.setText(aFile.getPercentDownloaded());

		return convertView;
	}

	private class Holder {
		public ProgressBar pbDowloading;
		public TextView tvFileName;
		public TextView tvStatus;
		public TextView tvFilesize;
	}

}
