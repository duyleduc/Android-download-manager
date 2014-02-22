package com.example.viewpageradapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.activities.DownloadedFragment;
import com.example.activities.DownloadingFragment;
import com.example.activities.FileBrowserFragment;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

	final int PAGE_COUNT = 3;

	/** Constructor of the class */
	public MyFragmentPagerAdapter(FragmentManager fm) {
		super(fm);

	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int arg0) {
		Bundle data = new Bundle();
		
		switch (arg0) {
		case 0:
			DownloadingFragment dl = new DownloadingFragment();
			dl.setArguments(data);
			return dl;

		case 1:
			DownloadedFragment dled = new DownloadedFragment();
			dled.setArguments(data);
			return dled;
		case 2:
			FileBrowserFragment fbf = new FileBrowserFragment();
			fbf.setArguments(data);
			return fbf;

		}
		
		return null;
	}

	/** Returns the number of pages */
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

}
