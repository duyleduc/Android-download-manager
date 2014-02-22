package com.example.model;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import com.example.control.DownloadService;

public class FileModel {

	private EnumStateFile state;
	private String fUrl;
	private long fSize;
	private List<ChunkFileModel> parts;
	private long downloadedLenght;
	private String percentDownloaded;
	
	public RandomAccessFile mRAF;
	public String path;
	public DownloadService dlService;

	public FileModel(String fUrl, long fLength) {
		super();
		state = EnumStateFile.DOWNLOADING;
		this.fUrl = fUrl;
		this.fSize = fLength;
		this.parts = new ArrayList<ChunkFileModel>();
		this.downloadedLenght = 0;
		this.percentDownloaded = "0%";
	}

	public FileModel() {
		// TODO Auto-generated constructor stub
		this.parts = new ArrayList<ChunkFileModel>();
	}

	/**
	 * @return the state
	 */
	public EnumStateFile getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(EnumStateFile state) {
		this.state = state;
	}

	/**
	 * @return the fUrl
	 */
	public String getfUrl() {
		return fUrl;
	}

	/**
	 * @param fUrl
	 *            the fUrl to set
	 */
	public void setfUrl(String fUrl) {
		this.fUrl = fUrl;
	}

	/**
	 * @return the fSize
	 */
	public long getfSize() {
		return fSize;
	}

	/**
	 * @param fSize
	 *            the fSize to set
	 */
	public void setfSize(long fSize) {
		this.fSize = fSize;
	}

	/**
	 * @return the parts
	 */
	public List<ChunkFileModel> getParts() {
		return parts;
	}

	/**
	 * @param parts
	 *            the parts to set
	 */
	public void setParts(List<ChunkFileModel> parts) {
		this.parts = parts;
	}

	public long getDownloadedLenght() {
		return downloadedLenght;
	}

	public void setDownloadedLenght(long downloadedLenght) {
		this.downloadedLenght = downloadedLenght;
	}

	/**
	 * @return the percentDownloaded
	 */
	public String getPercentDownloaded() {
		return percentDownloaded;
	}

	/**
	 * @param percentDownloaded
	 *            the percentDownloaded to set
	 */
	public void setPercentDownloaded(String percentDownloaded) {
		this.percentDownloaded = percentDownloaded;
	}

}
