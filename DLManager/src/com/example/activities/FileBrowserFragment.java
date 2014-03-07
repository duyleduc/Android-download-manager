package com.example.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.example.arrayadapter.DirLVAdapter;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.Files;

public class FileBrowserFragment extends Fragment {

	public static ListView mListView;
	public static DirLVAdapter mAdapter;
	public static ArrayList<Parcelable> listViewPos;

	private static Context mContext;
	private static List<Files> listFiles;

	private static String parentPath;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		initialize();
	}

	private void initialize() {

		listViewPos = new ArrayList<Parcelable>();
		mListView = (ListView) getView().findViewById(R.id.list_directory);
		mContext = getActivity();
		getFile(ConstantsVars.DLMDIR.getPath());

		mListView.setAdapter(mAdapter);
		setItemClick();
		getActivity().invalidateOptionsMenu();
	}

	public static void getFile(String path) {
		listFiles = new ArrayList<Files>();
		File f = new File(path);
		for (final File fi : f.listFiles()) {
			if (!fi.isHidden() && fi.canRead() && !fi.getName().equals("cache")) {
				final Files one = new Files(fi, false);
				String name = fi.getName();
				if (ConstantsVars.IMAGE_TYPE.contains((name.substring(name
						.lastIndexOf(".") + 1).toUpperCase()))) {
					new Thread(new Runnable() {

						@Override
						public void run() {
							one.setAvatar(resizeBitmap(fi, 100, 100));
						}
					}).start();
				}
				listFiles.add(one);
			}
		}

		if (listFiles.size() > 0) {
			// sort by file name
			Collections.sort(listFiles, new Comparator<Files>() {

				@Override
				public int compare(Files a, Files b) {
					return a.getFile().getName()
							.compareTo(b.getFile().getName());
				}
			});
			parentPath = listFiles.get(0).getFile().getParent();
			mAdapter = new DirLVAdapter(mContext, R.layout.listview_dir_item,
					listFiles);
			mListView.setAdapter(mAdapter);
		} else {
			Toast.makeText(mContext, "Directory is empty", Toast.LENGTH_LONG)
					.show();
		}
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

					// save listview position
					Parcelable state = mListView.onSaveInstanceState();

					listViewPos.add(state);
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
						Intent openFile = new Intent(Intent.ACTION_VIEW);
						if (ConstantsVars.listApplication
								.containsKey(extension)) {
							type = ConstantsVars.listApplication.get(extension);
						} else {
							type = "*/*";
						}
						openFile.setDataAndType(Uri.fromFile(file.getFile()),
								type);

						startActivity(openFile);
					}
				}

			}
		});
	}

	public static boolean onBackPressed() {

		if (parentPath != null) {
			String path = parentPath.substring(0, parentPath.lastIndexOf("/"));
			if (!path.equals("/storage")) {

				getFile(path);
				// restore listview position
				if (listViewPos.size() > 0) {
					Parcelable state = listViewPos
							.remove(listViewPos.size() - 1);
					mListView.onRestoreInstanceState(state);
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private static Bitmap resizeBitmap(File source, int height, int width) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(source.getPath(), bmOptions);

		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = Math.min(photoW / width, photoH / height);

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(source.getPath(), bmOptions);
		return bitmap;
	}

	public static void deleteSelectedFile() {
		Iterator<Files> i = listFiles.iterator();
		while (i.hasNext()) {
			Files f = i.next();
			if (f.isChecked()) {
				f.getFile().delete();
				i.remove();
			}
		}

		mAdapter.notifyDataSetChanged();
	}

}
