package com.example.arrayadapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dlmanager.R;
import com.example.model.DownloadedFileModel;

public class DownloadedExpandableListAdapter extends BaseExpandableListAdapter {
	private Context mContext;
	private List<String> title;
	private HashMap<String, List<DownloadedFileModel>> listData;

	public DownloadedExpandableListAdapter(Context context,
			List<String> header, HashMap<String, List<DownloadedFileModel>> data) {
		this.mContext = context;
		this.title = header;
		this.listData = data;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {

		return (DownloadedFileModel) this.listData.get(
				this.title.get(groupPosition)).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		final DownloadedFileModel file = (DownloadedFileModel) getChild(
				groupPosition, childPosition);
		Holder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.listview_downloaded_item, null);

			holder = new Holder();

			holder.imgv_filetype_downloaded = (ImageView) convertView
					.findViewById(R.id.filetype_downloaded);

			holder.tv_file_downloaded_link = (TextView) convertView
					.findViewById(R.id.file_downloaded_link);
			holder.tv_file_downloaded_infos = (TextView) convertView
					.findViewById(R.id.file_downloaded_infos);
			holder.tv_filename_dowloaded = (TextView) convertView
					.findViewById(R.id.filename_downloaded);
			holder.tv_date_downloaded = (TextView) convertView
					.findViewById(R.id.file_downloaded_date);
			holder.cb_file_downloaded_selected = (CheckBox) convertView
					.findViewById(R.id.downloaded_file_selected);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.tv_filename_dowloaded.setText(file.getfName());

		long fileSizeinBytes = file.getfSize();

		double sizeInKbOrMb;
		if (fileSizeinBytes < 1024 * 1024) {
			sizeInKbOrMb = (double) fileSizeinBytes / (double) 1024;
		} else {
			sizeInKbOrMb = (double) fileSizeinBytes / (double) 1048576;
		}
		sizeInKbOrMb = Math.round(sizeInKbOrMb * 100.00) / 100.00;
		if (fileSizeinBytes < 1024 * 1024) {
			holder.tv_file_downloaded_infos.setText(Double
					.toString(sizeInKbOrMb) + " Kb");
		} else {
			holder.tv_file_downloaded_infos.setText(Double
					.toString(sizeInKbOrMb) + " Mb");
		}

		URL url;
		try {
			url = new URL(file.getfLink());
			holder.tv_file_downloaded_link.setText(url.getHost());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Date date = new Date(file.getfTimestamp());
		holder.tv_date_downloaded.setText(date.toString());

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		return this.listData.get(this.title.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.title.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.title.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		String title = (String) this.title.get(groupPosition);
		if (convertView == null) {
			LayoutInflater inflate = (LayoutInflater) this.mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflate.inflate(R.layout.header, null);
		}

		TextView header = (TextView) convertView.findViewById(R.id.ex_header);
		TextView nbFiles = (TextView) convertView
				.findViewById(R.id.ex_nb_files);
		int numberFiles = getChildrenCount(groupPosition);
		String nbf;
		if (numberFiles <= 1) {
			nbf = numberFiles + " file";
		} else {
			nbf = numberFiles + " files";
		}
		header.setText(title);
		nbFiles.setText(nbf);

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	private class Holder {
		public ImageView imgv_filetype_downloaded;
		public TextView tv_file_downloaded_link;
		public TextView tv_file_downloaded_infos;
		public TextView tv_filename_dowloaded;
		public TextView tv_date_downloaded;
		public CheckBox cb_file_downloaded_selected;
	}

}
