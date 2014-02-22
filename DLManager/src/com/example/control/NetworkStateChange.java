package com.example.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.control.DownloadService.DownloadFileChunk;

public class NetworkStateChange extends BroadcastReceiver {

	private Thread thread;
	private DownloadService dlS;

	public NetworkStateChange(Thread thread, DownloadService dls) {
		this.thread = thread;
		this.dlS = dlS;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		testNetworkState(context);
	}

	private void testNetworkState(Context context) {
		ConnectivityManager cm;
		NetworkInfo ni;
		cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			if (thread != null) {
				this.thread.interrupt();
				this.thread = null;
			}
			if (dlS != null) {
				for (DownloadFileChunk dls : this.dlS.getThreads()) {
					if (dls != null) {
						dls.interrupt();
						dls = null;
					}
				}
			}

		}

	}

}
