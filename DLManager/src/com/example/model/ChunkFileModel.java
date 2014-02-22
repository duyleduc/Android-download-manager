package com.example.model;

import java.io.Serializable;

public class ChunkFileModel implements Serializable {
	private FileModel file;
	private String path;
	private long begin; // point begining download
	private EnumStateFile state;
	private long size;
	private long downloadedSize;
	

	public ChunkFileModel(FileModel file, String id, long startPos,
			EnumStateFile state, long blockSize) {
		super();
		this.file = file;
		this.path = id;
		this.begin = startPos;
		this.state = state;

		this.size = blockSize;

	}

	/**
	 * @return the file
	 */
	public FileModel getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(FileModel file) {
		this.file = file;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return path;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.path = id;
	}

	/**
	 * @return the begin
	 */
	public long getBegin() {
		return begin;
	}

	/**
	 * @param begin
	 *            the begin to set
	 */
	public void setBegin(int begin) {
		this.begin = begin;
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
	 * @return the length
	 */
	public long getSizeChunk() {
		return size;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		this.size = length;
	}

	/**
	 * @return the downloadedSize
	 */
	public long getDownloadedSize() {
		return downloadedSize;
	}

	/**
	 * @param downloadedSize
	 *            the downloadedSize to set
	 */
	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

}
