package com.example.activities;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.example.arrayadapter.DownloadingLVAdapter;
import com.example.control.DownloadService;
import com.example.database.DownloadedDB;
import com.example.dlmanager.R;
import com.example.model.ChunkFileModel;
import com.example.model.ConstantsVars;
import com.example.model.DownloadedFileModel;
import com.example.model.EnumStateFile;
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
		pauseDownloadingFile();
		DownloadingFragment.mListViewDownloading.setAdapter(mAdapterD);

		DownloadingFragment.mListViewQueue = (ListView) getView().findViewById(
				R.id.listview_queue);
		DownloadingFragment.mAdapterQ = new DownloadingLVAdapter(getActivity(),
				R.layout.listview_downloading_item, MainDownloadManager.queues);
		DownloadingFragment.mListViewQueue.setAdapter(mAdapterQ);
	}

	private void cancelDownloadingFile() {
		DownloadingFragment.mListViewDownloading
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int position, long arg3) {
						final FileModel file = (FileModel) mListViewDownloading
								.getItemAtPosition(position);

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

										file.dlService.stopThread(false);
										file.dlService.stopSelf();
										MainDownloadManager.files.remove(file);
										mAdapterD.notifyDataSetChanged();

										File f = new File(file.path);
										f.delete();
										for (ChunkFileModel c : file.getParts()) {
											f = new File(
													ConstantsVars.DLMDIR
															.getPath()
															+ MainDownloadManager.CACHDIR
															+ "/" + c.getId());
											if (f != null)
												f.delete();
										}
									}
								});

						AlertDialog dialog = cancelOrNot.create();
						dialog.show();
						return false;
					}

				});
	}

	private void pauseDownloadingFile() {
		DownloadingFragment.mListViewDownloading
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						final FileModel file = (FileModel) DownloadingFragment.mListViewDownloading
								.getItemAtPosition(position);
						if (file.getState() == EnumStateFile.DOWNLOADING)
							pause(file, getActivity());
						else
							resume(file, getActivity());
					}
				});

	}

	public static void pause(final FileModel file, Context context) {

		file.dlService.stopThread(true);
		file.dlService.stopSelf();
		file.setState(EnumStateFile.PAUSED);

		String path = file.getfUrl();
		DownloadedFileModel dl = new DownloadedFileModel(path.substring(path
				.lastIndexOf("/") + 1), file.getfUrl(), file.getfSize(), 1,
				file.path, file.getfTimestamp());

		long id = DownloadedDB.getInstance(context).updateOrCreate(dl);

		if (id != 0)
			file.setId(id);
		
		for (ChunkFileModel c : file.getParts()) {
			if (file.getState() == EnumStateFile.NORANGE) {
				c.setBegin(0);
				c.setDownloadedSize(0);
				new File(MainDownloadManager.CACHDIR + "/" + c.getId())
						.delete();
				DownloadedDB.getInstance(context).createOrUpdateChunk(c, file.getId());

			} else {
				DownloadedDB.getInstance(context).createOrUpdateChunk(c, file.getId());
				c.setBegin(c.getBegin() + c.getDownloadedSize());
				c.setDownloadedSize(0);
			}
		}

	}

	private void resume(FileModel file, Context context) {
		if (isConnected(context)) {
			int id = MainDownloadManager.files.indexOf(file);
			Intent downloadService = new Intent(context, DownloadService.class);
			Bundle bundle = new Bundle();
			bundle.putInt("fileIndex", id);
			downloadService.putExtras(bundle);
			context.startService(downloadService);
		} else {
			Toast.makeText(context, "Device does'nt connect to network",
					Toast.LENGTH_LONG).show();
		}
	}

	public static void pauseAllFile(Context context) {
		for (FileModel file : MainDownloadManager.files) {
			if (file.getState() == EnumStateFile.DOWNLOADING) {
				pause(file, context);
			}
		}
	}

	private static boolean isConnected(Context context) {
		ConnectivityManager cm;
		NetworkInfo ni;
		cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		}
		return true;
	}

}
