package com.example.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

import com.example.arrayadapter.DownloadedExpandableListAdapter;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.DownloadedFileModel;

public class DownloadedFragment extends Fragment {

	public static ExpandableListView mListViExpandableListView;
	public static DownloadedExpandableListAdapter mAdapter;
	public static List<String> title;
	public static HashMap<String, List<DownloadedFileModel>> dataDownloaded;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		initialize();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		rootView = inflater.inflate(R.layout.fragment_downloaded_main,
				container, false);

		return rootView;
	}

	private void initialize() {

		title = new ArrayList<String>();
		dataDownloaded = new HashMap<String, List<DownloadedFileModel>>();

		title.add("Today");
		title.add("Yesterday");
		title.add("This week");
		title.add("Last week");
		title.add("This month");
		title.add("Older");

		dataDownloaded.put(title.get(0), new ArrayList<DownloadedFileModel>());
		dataDownloaded.put(title.get(1), new ArrayList<DownloadedFileModel>());
		dataDownloaded.put(title.get(2), new ArrayList<DownloadedFileModel>());
		dataDownloaded.put(title.get(3), new ArrayList<DownloadedFileModel>());
		dataDownloaded.put(title.get(4), new ArrayList<DownloadedFileModel>());
		dataDownloaded.put(title.get(5), new ArrayList<DownloadedFileModel>());

		Calendar today = Calendar.getInstance();
		today.setTimeInMillis(System.currentTimeMillis()); // today

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, -1); // yesterday
		Calendar lw = Calendar.getInstance();
		lw.add(Calendar.WEEK_OF_YEAR, -1); // last week

		for (DownloadedFileModel f : MainDownloadManager.downloaded) {
			Date date = new Date(f.getfTimestamp());
			Calendar day = Calendar.getInstance();
			day.setTime(date);
			int d = day.get(Calendar.DAY_OF_YEAR);
			int w = day.get(Calendar.WEEK_OF_YEAR);
			int m = day.get(Calendar.MONTH);
			int y = day.get(Calendar.YEAR);

			if (d == today.get(Calendar.DAY_OF_YEAR)
					&& y == today.get(Calendar.YEAR)) {
				dataDownloaded.get(title.get(0)).add(f);
			}// today
			else if (d == c.get(Calendar.DAY_OF_YEAR)
					&& y == c.get(Calendar.YEAR)) {
				dataDownloaded.get(title.get(1)).add(f);
			}// yesterday
			else if (w == today.get(Calendar.WEEK_OF_YEAR)
					&& y == today.get(Calendar.YEAR)) {
				dataDownloaded.get(title.get(2)).add(f);
			}// this week
			else if (y == lw.get(Calendar.YEAR)
					&& w == lw.get(Calendar.WEEK_OF_YEAR)) {
				dataDownloaded.get(title.get(3)).add(f);
			}// last week
			else if (y == today.get(Calendar.YEAR)
					&& m == today.get(Calendar.MONTH)) {
				dataDownloaded.get(title.get(4)).add(f);
			}// this month
			else {
				dataDownloaded.get(title.get(5)).add(f);
			}// older
		}

		mListViExpandableListView = (ExpandableListView) getView()
				.findViewById(R.id.ex_listview_downloaded);
		mAdapter = new DownloadedExpandableListAdapter(getActivity(), title,
				dataDownloaded);
		mListViExpandableListView.setAdapter(mAdapter);
		setItemListViewClick();
	}

	private void setItemListViewClick() {
		mListViExpandableListView
				.setOnChildClickListener(new OnChildClickListener() {

					@Override
					public boolean onChildClick(ExpandableListView arg0,
							View arg1, int arg2, int arg3, long arg4) {
						final DownloadedFileModel file = (DownloadedFileModel) mAdapter
								.getChild(arg2, arg3);

						String extension = file
								.getfName()
								.substring(file.getfName().lastIndexOf(".") + 1)
								.toLowerCase();
						String application = null;
						if (ConstantsVars.listApplication
								.containsKey(extension)) {
							application = ConstantsVars.listApplication.get(extension);

							Intent openFile = new Intent(Intent.ACTION_VIEW);
							File f = new File(file.getPath());
							openFile.setDataAndType(Uri.fromFile(f),
									application);
							startActivity(openFile);
						} else {
							Toast.makeText(getActivity(),
									"Can't open this file", Toast.LENGTH_LONG)
									.show();
						}

						return false;
					}
				});
	}
}
