package com.example.activities;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.arrayadapter.DownloadingLVAdapter;
import com.example.dlmanager.R;
import com.example.model.ChunkFileModel;
import com.example.model.ConstantsVars;
import com.example.model.FileModel;

public class DownloadingFragment extends Fragment {

	public static ListView mListViewDownloading;
	public static ListView mListViewQueue;
	public static DownloadingLVAdapter mAdapterQ;
	public static DownloadingLVAdapter mAdapterD;

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
		rootView = inflater.inflate(R.layout.fragment_downloading, container,
				false);
		return rootView;
	}

	private void initialize() {
		DownloadingFragment.mListViewDownloading = (ListView) getView()
				.findViewById(R.id.listview_downloading);
		DownloadingFragment.mAdapterD = new DownloadingLVAdapter(getActivity(),
				R.layout.listview_downloading_item, MainDownloadManager.files);
		cancelDownloadingFile();
		DownloadingFragment.mListViewDownloading.setAdapter(mAdapterD);

		DownloadingFragment.mListViewQueue = (ListView) getView().findViewById(
				R.id.listview_queue);
		DownloadingFragment.mAdapterQ = new DownloadingLVAdapter(getActivity(),
				R.layout.listview_downloading_item, MainDownloadManager.queues);
		DownloadingFragment.mListViewQueue.setAdapter(mAdapterQ);
	}

	private void cancelDownloadingFile() {
		DownloadingFragment.mListViewDownloading
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							final int position, long arg3) {

						AlertDialog.Builder cancelOrNot = new AlertDialog.Builder(
								getActivity());
						cancelOrNot.setTitle(R.string.cancelornot);
						cancelOrNot.setCancelable(false).setNegativeButton(
								"No", new OnClickListener() {

									public void onClick(DialogInterface dialog,
											int arg1) {
										dialog.cancel();

									}
								});
						cancelOrNot.setPositiveButton("Yes",
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										FileModel file = (FileModel) mListViewDownloading
												.getItemAtPosition(position);
										file.dlService.stopThread();
										file.dlService.stopSelf();
										MainDownloadManager.files.remove(file);
										mAdapterD.notifyDataSetChanged();
										try {
											file.mRAF.close();
											File f = new File(file.path);
											f.delete();
											for (ChunkFileModel c : file
													.getParts()) {
												f = new File(
														ConstantsVars.DLMDIR
																.getPath()
																+ ConstantsVars.CACHDIR
																+ "/"
																+ c.getId());
												if (f != null)
													f.delete();
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									}
								});

						AlertDialog dialog = cancelOrNot.create();
						dialog.show();

					}

				});
	}

}
