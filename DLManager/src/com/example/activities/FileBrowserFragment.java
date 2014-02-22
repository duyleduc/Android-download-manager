package com.example.activities;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.arrayadapter.DirLVAdapter;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.Files;

public class FileBrowserFragment extends Fragment {

	public static ListView mListView;
	public static DirLVAdapter mAdapter;
	private static Context mContext;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		initialize();

	}

	private void initialize() {
		mListView = (ListView) getView().findViewById(R.id.list_directory);
		mContext = getActivity();
		getFile(ConstantsVars.DLMDIR.getPath());
		mListView.setAdapter(mAdapter);
		setItemClick();
		getActivity().invalidateOptionsMenu();
	}

	public static void getFile(String path) {
		MainDownloadManager.mMenu.findItem(R.id.more).setVisible(false);
		ArrayList<Files> list = new ArrayList<Files>();
		File f = new File(path);
		for (File fi : f.listFiles()) {
			if (!fi.isHidden() && fi.canRead() && !fi.getName().equals("cache")) {
				Files one = new Files(fi, false);
				list.add(one);
			}
		}
		mAdapter = new DirLVAdapter(mContext, R.layout.listview_dir_item, list);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		rootView = inflater.inflate(R.layout.fragment_directory, container,
				false);

		return rootView;
	}

	private void setItemClick() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				final Files file = (Files) mListView
						.getItemAtPosition(position);

				if (file.getFile().isDirectory()
						&& file.getFile().listFiles().length > 0) {

					getFile(file.getFile().getPath());

				} else {
					if (file.getFile().isDirectory()) {// toast something
						Toast.makeText(mContext, "Directory is empty",
								Toast.LENGTH_LONG).show();
					} else {// a file
						String name = file.getFile().getName();
						String extension = name.substring(
								name.lastIndexOf(".") + 1).toLowerCase();
						String type = null;
						if (ConstantsVars.listApplication
								.containsKey(extension)) {
							type = ConstantsVars.listApplication.get(extension);
							Intent openFile = new Intent(Intent.ACTION_VIEW);
							openFile.setDataAndType(
									Uri.fromFile(file.getFile()), type);
							startActivity(openFile);
						} else {
							Toast.makeText(mContext, "Can't open this file",
									Toast.LENGTH_LONG).show();
						}
					}
				}

			}
		});
	}

	public static boolean onBackPressed() {
		Files f = (Files) mListView.getItemAtPosition(0);
		String pathParent = f.getFile().getParent();
		if (pathParent != null) {
			String path = pathParent.substring(0, pathParent.lastIndexOf("/"));
			if (!path.equals("/storage")) {
				getFile(path);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
