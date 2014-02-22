package com.example.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.example.control.DownloadService;
import com.example.database.DownloadedDB;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.DownloadedFileModel;
import com.example.model.EnumStateFile;
import com.example.model.FileModel;
import com.example.viewpageradapter.MyFragmentPagerAdapter;

@SuppressLint("NewApi")
public class MainDownloadManager extends FragmentActivity implements Runnable {

	public static ArrayList<FileModel> files = new ArrayList<FileModel>();
	public static ArrayList<FileModel> queues = new ArrayList<FileModel>();
	public static List<DownloadedFileModel> downloaded = new ArrayList<DownloadedFileModel>();
	public static final int MAX_FILE_ASYNC = 5;
	public static FragmentActivity myActivity;
	public static Menu mMenu = null;
	public static boolean isShow;

	private ActionBar mActionBar;
	private ViewPager mPager;
	private Uri uri;
	private long fLength;
	private int tabCurrent;
	private boolean norange;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_download_manager);
		createFolder();
		initialize();
		start();
		myActivity = this;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (MainDownloadManager.mMenu == null) {
			MainDownloadManager.mMenu = menu;
			isShow = false;
		}
		getMenuInflater().inflate(R.menu.main, menu);
		mMenu.findItem(R.id.more).setVisible(isShow);
		return true;
	}

	private void start() {
		uri = getIntent().getData();
		if (uri != null) {
			if (canbeDownloaded(uri)) {
				Thread th = new Thread(this);
				th.start();
			}
		}
	}

	/**
	 * 
	 * @param uri
	 *            link download
	 * @return true--> can download, false = exists
	 */

	private boolean canbeDownloaded(Uri uri) {

		String url = uri.toString();
		String name = url.substring(url.lastIndexOf("/") + 1);
		for (FileModel f : MainDownloadManager.files) {
			if (f.getfUrl().equals(uri.toString())) {
				Toast.makeText(getApplicationContext(),
						"This file is downloading", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		for (FileModel f : MainDownloadManager.queues) {
			if (f.getfUrl().equals(uri.toString())) {
				Toast.makeText(getApplicationContext(),
						"This file is in queue", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		File file = ConstantsVars.DLMDIR;
		for (File fDir : file.listFiles()) {
			if (!fDir.getName().equals(ConstantsVars.CACHDIR)) {
				for (File f : fDir.listFiles()) {
					if (name.equals(f.getName())) {
						Toast.makeText(getApplicationContext(),
								"This file is downloaded", Toast.LENGTH_LONG)
								.show();
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * start to download file
	 */
	private void startDownload() {
		FileModel file = new FileModel(uri.toString(), fLength);
		if (norange) {
			file.setState(EnumStateFile.NORANGE);
		}
		String filePath = setPathDownloadedFile(file.getfUrl());
		file.path = filePath;
		try {
			file.mRAF = new RandomAccessFile(filePath, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (MainDownloadManager.files.size() < MAX_FILE_ASYNC
				&& MainDownloadManager.files.size() >= 0) {
			MainDownloadManager.files.add(file);
			DownloadingFragment.mAdapterD.notifyDataSetChanged();
			int id = MainDownloadManager.files.indexOf(file);
			Intent downloadService = new Intent(this, DownloadService.class);
			Bundle bundle = new Bundle();
			bundle.putInt("fileIndex", id);
			downloadService.putExtras(bundle);
			startService(downloadService);
		} else {
			MainDownloadManager.queues.add(file);
			DownloadingFragment.mAdapterQ.notifyDataSetChanged();
		}

	}

	/**
	 * create folder for downloadmanager
	 */
	private void createFolder() {
		ConstantsVars.DLMDIR = new File(Environment
				.getExternalStorageDirectory().getPath()
				+ ConstantsVars.DIRNAME);
		if (!ConstantsVars.DLMDIR.exists()) {
			ConstantsVars.DLMDIR.mkdirs();

			// images
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.IMDIR)
					.mkdirs();
			// video
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.VIDDIR)
					.mkdirs();
			// music
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.MUDIR)
					.mkdirs();
			// apk
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.OTHERDIR)
					.mkdirs();
			// compressed
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.COMDIR)
					.mkdirs();
			// document
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.DOCDIR)
					.mkdirs();
			// cache
			new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.CACHDIR)
					.mkdirs();

		}
	}

	@SuppressLint("NewApi")
	private void initialize() {
		mActionBar = getActionBar();

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mPager = (ViewPager) findViewById(R.id.view_pager);
		FragmentManager fm = getSupportFragmentManager();
		ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				mActionBar.setSelectedNavigationItem(position);
			}

		};
		mPager.setOnPageChangeListener(pageChangeListener);
		MyFragmentPagerAdapter mfga = new MyFragmentPagerAdapter(fm);

		mPager.setAdapter(mfga);
		mActionBar.setDisplayShowTitleEnabled(true);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				tabCurrent = tab.getPosition();
				mPager.setCurrentItem(tabCurrent);
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}
		};

		Tab tab = mActionBar.newTab().setText("Queue")
				.setTabListener(tabListener);

		mActionBar.addTab(tab);

		tab = mActionBar.newTab().setText("History")
				.setTabListener(tabListener);
		mActionBar.addTab(tab);

		getDownloadedFileFromDB();
		tab = mActionBar.newTab().setText("Files").setTabListener(tabListener);
		mActionBar.addTab(tab);

	}

	/**
	 * get all downloaded file from bd to fetch to history
	 */

	private void getDownloadedFileFromDB() {
		ArrayList<DownloadedFileModel> dl = new ArrayList<DownloadedFileModel>();
		dl = DownloadedDB.getInstance(this).queryDownloadedFile();
		MainDownloadManager.downloaded.clear();
		MainDownloadManager.downloaded.addAll(dl);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			URL url = new URL(uri.toString());
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.connect();
			final String contentLengthStr = connection
					.getHeaderField("content-length");

			final String rangeLength = connection
					.getHeaderField("Accept-Ranges");

			if (rangeLength != null) {
				norange = true;
			} else {
				norange = false;
			}

			if (contentLengthStr != null)
				fLength = Long.parseLong(contentLengthStr);
			else
				fLength = -1;
			connection.disconnect();
			handler.sendEmptyMessage(0);

		} catch (MalformedURLException e) {

		} catch (IOException e) {

		}

	}

	/**
	 * 
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (fLength <= 0) {
				String fname = uri.toString().substring(
						uri.toString().lastIndexOf("/") + 1);
				Toast.makeText(getApplicationContext(),
						fname + " can't download", Toast.LENGTH_LONG).show();
			} else {
				startDownload();
			}
		}
	};

	/**
	 * 
	 * @param fileName
	 * @return path file will be saved
	 */

	private String setPathDownloadedFile(String fileName) {
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1)
				.toUpperCase();
		String type;

		String name = fileName.substring(fileName.lastIndexOf("/") + 1);
		if (ConstantsVars.COMPRESSED.contains(extension)) {
			type = ConstantsVars.COMDIR;
		} else if (ConstantsVars.IMAGE_TYPE.contains(extension)) {
			type = ConstantsVars.IMDIR;
		} else if (ConstantsVars.DOC_TYPE.contains(extension)) {
			type = ConstantsVars.DOCDIR;
		} else if (ConstantsVars.MUSIC_TYPE.contains(extension)) {
			type = ConstantsVars.MUDIR;
		} else if (ConstantsVars.VIDEO_TYPE.contains(extension)) {
			type = ConstantsVars.VIDDIR;
		} else {
			type = ConstantsVars.OTHERDIR;
		}
		return ConstantsVars.DLMDIR.getPath() + type + "/" + name;
	}

	@Override
	public void onBackPressed() {
		if (tabCurrent == 2) {
			if (!FileBrowserFragment.onBackPressed()) {
				finish();
			}
		} else {
			finish();
		}
	}
}
