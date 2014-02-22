package com.example.control;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.activities.DownloadedFragment;
import com.example.activities.DownloadingFragment;
import com.example.activities.MainDownloadManager;
import com.example.database.DownloadedDB;
import com.example.model.ChunkFileModel;
import com.example.model.ConstantsVars;
import com.example.model.DownloadedFileModel;
import com.example.model.EnumStateFile;
import com.example.model.FileModel;

public class DownloadService extends Service {
	private DownloadFileChunk dfcThread;
	private NetworkStateChange nwStateChange;
	private List<DownloadFileChunk> threads;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		threads = new ArrayList<DownloadFileChunk>();
		registerNewtworkBroadcastReceiver();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			int index = intent.getExtras().getInt("fileIndex");
			FileModel file = MainDownloadManager.files.get(index);
			file.dlService = this;

			long chunkSize = calculateChunksize(file);
			long startPosition = 0;
			long id = 0;

			while (startPosition < file.getfSize()) { // divise file into many
														// chunks
				String fPath = file.path;
				String fName = fPath.substring(fPath.lastIndexOf("/") + 1);
				final ChunkFileModel aChunk = new ChunkFileModel(file, fName
						+ Long.toString(id), startPosition,
						EnumStateFile.READY, chunkSize);
				dfcThread = new DownloadFileChunk(aChunk) {
					@Override
					public void run() {
						download(aChunk, getApplicationContext());
					}
				};
				threads.add(dfcThread);
				dfcThread.start();

				file.getParts().add(aChunk);
				startPosition += chunkSize;
				if (startPosition + chunkSize > file.getfSize()) {
					chunkSize = file.getfSize() - startPosition;
				}
				id++;
			}
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(nwStateChange);
	}

	public void stopThread() {
		for (DownloadFileChunk dl : this.threads) {
			if (dl != null) {
				dl.interrupt();
				dl = null;
			}
		}

	}

	private void registerNewtworkBroadcastReceiver() {
		nwStateChange = new NetworkStateChange(dfcThread, this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
		intentFilter.addAction("android.net.wifi.STATE_CHANGE");
		registerReceiver(nwStateChange, intentFilter);
	}

	public class DownloadFileChunk extends Thread {

		private ChunkFileModel file;
		private int count = 0;
		private byte data[] = new byte[1024 * 256];

		public DownloadFileChunk(ChunkFileModel file) {
			this.file = file;
		}

		public void download(final ChunkFileModel file, final Context context) {
			URL url;
			FileOutputStream fos = null;
			BufferedInputStream ins = null;
			count = 0;
			final String partDir = ConstantsVars.DLMDIR.getPath()
					+ ConstantsVars.CACHDIR + "/" + file.getId(); // temp file
																	// of this
																	// part

			HttpURLConnection connection = null;
			try {
				url = new URL(this.file.getFile().getfUrl());
				long end = file.getBegin() + file.getSizeChunk() - 1;
				connection = (HttpURLConnection) url.openConnection();

				String range = "bytes=" + (file.getBegin()) + "-" + (end);
				connection.setRequestProperty("Range", range);

				connection.connect();

				ins = new BufferedInputStream(connection.getInputStream());
				fos = new FileOutputStream(partDir);

				while ((count = ins.read(data)) != -1) {

					fos.write(data, 0, count);

					long value = file.getDownloadedSize();
					long val = file.getFile().getDownloadedLenght();

					val += count;
					value += count;

					file.getFile().setDownloadedLenght(val); // entire file
					file.setDownloadedSize(value);// chunk file

					Message msg = new Message();
					msg.obj = file;
					ProgressingBar.sendMessage(msg); // send message for update
														// progressing bar
														// downloading
				}
				fos.flush();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				this.interrupt();

			} finally {
				if (ins != null) {
					try {
						ins.close();// close this first
						fos.close();
						if (connection != null)
							connection.disconnect();
						SaveFile svThread = new SaveFile() {
							@Override
							public void run() {
								saveFileByRAF(file, partDir);
							}
						};
						svThread.start();

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private class SaveFile extends Thread {

		public void saveFileByRAF(ChunkFileModel file, String partDir) {
			RandomAccessFile raf = file.getFile().mRAF;
			FileInputStream fis = null;
			File f = null;
			int count;
			byte[] data = new byte[1024 * 256];
			try {
				f = new File(partDir);
				fis = new FileInputStream(f);
				raf.seek(file.getBegin());
				count = 0;

				while ((count = fis.read(data)) != -1) {
					raf.write(data, 0, count);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					try {
						fis.close();
						f.delete();
						if (file.getDownloadedSize() == file.getSizeChunk()) // if
																				// this
																				// chunk
																				// is
																				// downloaded
							file.setState(EnumStateFile.DOWNLOADED);
						if (checkDownloadFinish(file)) { // file
							// finish
							// download
							file.getFile().setState(EnumStateFile.DOWNLOADED);
							file.getFile().mRAF.close();
							Message msg = new Message();
							msg.obj = file;
							DownloadFinishHandler.sendMessage(msg);

						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * 
		 * @param chunk
		 *            chunk file downloaded
		 * @return whole file is downloaded or not
		 */
		private boolean checkDownloadFinish(ChunkFileModel chunk) {
			boolean check = true;
			for (ChunkFileModel c : chunk.getFile().getParts()) {
				check = check && (c.getState() == EnumStateFile.DOWNLOADED);
			}
			return check;
		}
	}

	/**
	 * calculate number of chunks file in function of file's size
	 * 
	 * @param file
	 * @return chunk's size
	 */
	private long calculateChunksize(FileModel file) {
		long length = file.getfSize();
		if (file.getState() == EnumStateFile.NORANGE){
			return length;
		}
		
		if (length <= ConstantsVars.PART) {
			return length;
		} else if (length <= ConstantsVars.PARTS_4) {
			return length / 4 + 1;
		} else {
			return length / 8 + 1;
		}
	}

	/**
	 * Handler when a file downloaded
	 */
	private Handler DownloadFinishHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ChunkFileModel chunk = (ChunkFileModel) msg.obj;
			String path = chunk.getFile().getfUrl();
			DownloadedFileModel dl = new DownloadedFileModel(
					path.substring(path.lastIndexOf("/") + 1), chunk.getFile()
							.getfUrl(), chunk.getFile().getfSize(), 2,chunk.getFile().path,
					System.currentTimeMillis());

			long id = DownloadedDB.getInstance(getApplicationContext())
					.updateOrCreate(dl); // save to db
			
			dl.setfId((int) id);

			DownloadedFragment.dataDownloaded.get(
					DownloadedFragment.title.get(0)).add(0, dl);
			DownloadedFragment.mAdapter.notifyDataSetChanged();

			MainDownloadManager.files.remove(chunk.getFile());
			DownloadingFragment.mAdapterD.notifyDataSetChanged();

			if (MainDownloadManager.queues.size() > 0) { // new
															// job
				FileModel file = MainDownloadManager.queues.remove(0);
				MainDownloadManager.files.add(file);
				DownloadingFragment.mAdapterD.notifyDataSetChanged();
				DownloadingFragment.mAdapterQ.notifyDataSetChanged();
				Intent self = new Intent(getApplicationContext(),
						this.getClass());
				int index = MainDownloadManager.files.indexOf(file);
				Bundle bundle = new Bundle();
				bundle.putInt("fileIndex", index);
				self.putExtras(bundle);
				startService(self);
			}
		}
	};

	/**
	 * update ui progressing bar
	 */

	private Handler ProgressingBar = new Handler() { // handler for setting the
														// progressing bar
														// downloading file
		@Override
		public void handleMessage(Message msg) {
			ChunkFileModel chunk = (ChunkFileModel) msg.obj;
			FileModel file = chunk.getFile();
			double per = (double) chunk.getFile().getDownloadedLenght()
					/ (double) chunk.getFile().getfSize();
			per = Math.round(per * 100 * 1.0) / 1.0; // round to 1 decimal
			file.setPercentDownloaded(Double.toString(per) + "%");
			DownloadingFragment.mAdapterD.notifyDataSetChanged();

		}
	};

	public List<DownloadFileChunk> getThreads() {
		return this.threads;
	}
}
