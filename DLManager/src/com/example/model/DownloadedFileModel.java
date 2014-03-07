package com.example.model;

public class DownloadedFileModel {
	private long fId;
	private String fName;
	private String fLink;
	private long fTimestamp;
	private long fSize;
	private int fState;
	private String fPath;

	public DownloadedFileModel(String fName, String fLink, long fSize,
			int fState, String path, long timestamp) {
		super();
		this.fName = fName;
		this.fLink = fLink;
		this.fSize = fSize;
		this.fTimestamp = timestamp;
		this.fState = fState;
		this.fPath = path;
	}

	public DownloadedFileModel() {
	}

	/**
	 * @return the fId
	 */
	public long getfId() {
		return fId;
	}

	/**
	 * @param l
	 *            the fId to set
	 */
	public void setfId(long l) {
		this.fId =  l;
	}

	/**
	 * @return the fName
	 */
	public String getfName() {
		return fName;
	}

	/**
	 * @param fName
	 *            the fName to set
	 */
	public void setfName(String fName) {
		this.fName = fName;
	}

	/**
	 * @return the fLink
	 */
	public String getfLink() {
		return fLink;
	}

	/**
	 * @param fLink
	 *            the fLink to set
	 */
	public void setfLink(String fLink) {
		this.fLink = fLink;
	}

	/**
	 * @return the fTimestamp
	 */
	public long getfTimestamp() {
		return fTimestamp;
	}

	/**
	 * @param fTimestamp
	 *            the fTimestamp to set
	 */
	public void setfTimestamp(long fTimestamp) {
		this.fTimestamp = fTimestamp;
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
	 * @return the fState
	 */
	public int getfState() {
		return fState;
	}

	/**
	 * @param fState
	 *            the fState to set
	 */
	public void setfState(int fState) {
		this.fState = fState;
	}

	public String getPath() {

		return this.fPath;
	}

	public void setfPath(String string) {
		this.fPath = string;

	}

}
