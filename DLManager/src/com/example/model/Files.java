package com.example.model;

import java.io.File;

import android.graphics.Bitmap;

public class Files {
	private File file;
	private boolean checked;
	private Bitmap avatar;

	public Files(File file, boolean checked) {
		super();
		this.file = file;
		this.checked = checked;
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the avatar
	 */
	public Bitmap getAvatar() {
		return avatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}

}
