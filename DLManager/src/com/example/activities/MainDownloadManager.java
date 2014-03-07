package com.example.activities;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.control.DownloadService;
import com.example.database.DownloadedDB;
import com.example.dlmanager.R;
import com.example.model.ConstantsVars;
import com.example.model.EnumStateFile;
import com.example.model.FileModel;
import com.example.viewpageradapter.MyFragmentPagerAdapter;

@SuppressLint("NewApi")
public class MainDownloadManager extends FragmentActivity implements Runnable {

	public static ArrayList<FileModel> files = new ArrayList<FileModel>();
	public static ArrayList<FileModel> queues = new ArrayList<FileModel>();
	public static final int MAX_FILE_ASYNC = 5;
	public static FragmentActivity myActivity;
	public static Menu mMenu = null;
	public static String CACHDIR = "";

	private ActionBar mActionBar;
	private ViewPager mPager;
	private String linkToDownload;
	private long fLength;
	private int tabCurrent;
	private boolean norange;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_download_manager);
		createFolder();
		initialize();
		Uri uri = getIntent().getData();

		if (uri != null) {
			linkToDownload = uri.toString();
			start(linkToDownload);
		}
		myActivity = this;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
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

		}
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void start(String url) {

		if (canbeDownloaded(url)) {
			Thread th = new Thread(this);
			th.start();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			FileBrowserFragment.deleteSelectedFile();
			invalidateOptionsMenu();
			return true;
		case R.id.add_url:
			addNewLink();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	private void addNewLink() {
		final Dialog add_new_link = new Dialog(this);

		add_new_link.setContentView(R.layout.new_url);
		add_new_link.setTitle(R.string.add_new_link_title);
		final Button cancel = (Button) add_new_link
				.findViewById(R.id.bt_cancel_dl);
		final Button download = (Button) add_new_link
				.findViewById(R.id.btn_download_url);
		final EditText new_link = (EditText) add_new_link
				.findViewById(R.id.add_new_url);

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				add_new_link.dismiss();

			}
		});

		download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String link = new_link.getText().toString();
				if (!link.equals("")) {
					linkToDownload = link;
					start(linkToDownload);
				}
				add_new_link.dismiss();

			}
		});
		add_new_link.show();
	}

	/**
	 * 
	 * @param uri
	 *            link download
	 * @return true--> can download, false = exists
	 */

	private boolean canbeDownloaded(String url) {

		String name = url.substring(url.lastIndexOf("/") + 1);
		for (FileModel f : MainDownloadManager.files) {
			if (f.getfUrl().equals(url)) {
				Toast.makeText(getApplicationContext(),
						"This file is downloading", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		for (FileModel f : MainDownloadManager.queues) {
			if (f.getfUrl().equals(url)) {
				Toast.makeText(getApplicationContext(),
						"This file is in queue", Toast.LENGTH_LONG).show();
				return false;
			}
		}

		File file = ConstantsVars.DLMDIR;
		for (File fDir : file.listFiles()) {

			for (File f : fDir.listFiles()) {
				if (name.equals(f.getName())) {
					Toast.makeText(getApplicationContext(),
							"This file is downloaded", Toast.LENGTH_LONG)
							.show();
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * start to download file
	 */
	private void startDownload() {

		FileModel file = new FileModel(linkToDownload, fLength,
				System.currentTimeMillis());
		if (norange) {
			file.setState(EnumStateFile.NORANGE);
		}
		String filePath = setPathDownloadedFile(file.getfUrl());
		file.path = filePath;

		if (canExecute()) {
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

	private boolean canExecute() {
		int nbrDownloading = 0;
		for (FileModel f : MainDownloadManager.files) {
			if (f.getState() != EnumStateFile.PAUSED)
				nbrDownloading++;
		}

		if (nbrDownloading < MAX_FILE_ASYNC)
			return true;
		return false;
	}

	/**
	 * create folder for downloadmanager
	 */
	private void createFolder() {
		ConstantsVars.DLMDIR = new File(Environment
				.getExternalStorageDirectory().getPath()
				+ ConstantsVars.DIRNAME);

		ConstantsVars.DLMDIR.mkdirs();

		// images
		new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.IMDIR).mkdirs();
		// video
		new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.VIDDIR)
				.mkdirs();
		// music
		new File(ConstantsVars.DLMDIR.getPath() + ConstantsVars.MUDIR).mkdirs();
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
		CACHDIR = ConstantsVars.DLMDIR.getPath() + "/.cache";
		new File(CACHDIR).mkdir();

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

			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {

				tabCurrent = tab.getPosition();
				mPager.setCurrentItem(tabCurrent);
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {

			}
		};

		Tab tab = mActionBar.newTab().setText("Queue")
				.setTabListener(tabListener);

		mActionBar.addTab(tab);

		getPausedFileFromDB();

		tab = mActionBar.newTab().setText("Files").setTabListener(tabListener);
		mActionBar.addTab(tab);

	}

	private void getPausedFileFromDB() {

		List<FileModel> f = DownloadedDB.getInstance(getApplicationContext())
				.queryPausedFiles();

		for (FileModel fi : f) {
			boolean canInsert = true;
			for (FileModel fm : MainDownloadManager.files) {
				if (fi.getId() == fm.getId())
					canInsert = false;
			}
			if (canInsert)
				MainDownloadManager.files.add(fi);
		}

	}

	/**
	 * get all downloaded file from bd to fetch to history
	 */

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			URL url = new URL(linkToDownload);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.connect();
			final String contentLengthStr = connection
					.getHeaderField("content-length");

			String rangeLength = "";
			rangeLength = connection.getHeaderField("Accept-Ranges");

			if (rangeLength == null || !rangeLength.equals("bytes")) {
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
				String fname = linkToDownload.substring(linkToDownload
						.lastIndexOf("/") + 1);
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
		if (tabCurrent == 1) {
			FileBrowserFragment.onBackPressed();
		} else {

			finish();
		}
	}
}
