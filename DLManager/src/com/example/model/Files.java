package com.example.model;

import java.io.File;

public class Files {
	private File file;
	private boolean checked;
	
	
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
	 * @param checked the checked to set
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
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

}
