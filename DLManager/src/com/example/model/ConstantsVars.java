package com.example.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConstantsVars { // constant values using in this project
	// directories
	public static File DLMDIR;
	public final static String DIRNAME = "/DDM";
	public final static String IMDIR = "/images";
	public final static String VIDDIR = "/video";
	public final static String MUDIR = "/music";
	public final static String OTHERDIR = "/other";
	public final static String COMDIR = "/compressed";
	public final static String DOCDIR = "/document";
	public final static String CACHDIR = "/cache";
	public final static int PART = 1024 * 1024 / 2;
	public final static int PARTS_4 = 1024 * 1024 * 2;
	public final static int PARTS_8 = 1024 * 1024 * 20;
	public static HashMap<String, String> listApplication = new HashMap<String, String>() {
		{
			/**
			 * text
			 */
			put("txt", "text/*");
			put("cvs", "text/*");
			put("txt", "xml/*");
			put("htm", "text/*");
			put("html", "text/*");
			put("php", "text/*");
			put("xml", "text/*");
			/**
			 * image
			 */
			put("png", "image/*");
			put("gif", "image/*");
			put("jpg", "image/*");
			put("jpeg", "image/*");
			put("bmp", "image/*");
			/**
			 * audio
			 */
			put("mp3", "audio/*");
			put("wav", "audio/*");
			put("ogg", "audio/*");
			put("mid", "audio/*");
			put("midi", "audio/*");
			put("amr", "audio/*");
			/**
			 * video
			 */
			put("mpeg", "video/*");
			put("3gp", "video/*");
			/**
			 * package
			 */
			put("jar", "application/*");
			put("zip", "application/*");
			put("rar", "application/*");
			put("gz", "application/*");
			put("apk", "application/*");
			put("pdf", "application/*");

		}

	};

	// file type

	public final static List<String> IMAGE_TYPE = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("JPEG");
			add("PNG");
			add("JPG");
			add("GIF");
		}
	};

	public final static List<String> COMPRESSED = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("ZIP");
			add("RAR");
			add("7Z");
		}
	};

	public final static List<String> VIDEO_TYPE = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("MP4");
			add("3GP");
			add("TS");
			add("AAC");
			add("MKV");
		}
	};

	public final static List<String> MUSIC_TYPE = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("MP3");
			add("FLAC");
			add("MID");
			add("WAV");
		}
	};

	public final static List<String> DOC_TYPE = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("PDF");
			add("DOC");
			add("DOCX");
			add("XLS");
			add("XLXS");
		}
	};

	// db
	// downloaded file
	public static final String DB_DOWNLOADED = "db_dled.db";
	public static final int DB_DOWNLOADED_VERSION = 1;
	public static final String DB_TABLE_DOWNLOADED = "downloadedfile";
	public static final String ID = "id";
	public static final String FNAME = "fname";
	public static final String FLINK = "flink";
	public static final String TIMESTAMP = "timestamp";
	public static final String FSIZE = "fsize";
	public static final String FSTATE = "fstate";
	public static final String FPATH = "fpath";

	public static final String DBDED_CREATE_STATEMENT = "create table if not exists "
			+ DB_TABLE_DOWNLOADED
			+ "("
			+ ID
			+ " integer primary key autoincrement,"
			+ FNAME
			+ " text not null,"
			+ FSIZE
			+ " integer not null,"
			+ FLINK
			+ " text not null,"
			+ FSTATE
			+ " integer not null,"
			+ TIMESTAMP
			+ " integer not null,"
			+ FPATH
			+ ",text);";

	;

	// db paused file

	public static final String DB_PAUSED_FILE = "db_pausedf.db";
	public static final int DB_PAUSEDF_VERSION = 1;
	public static final String DB_TABLE_PFILE = "pausedfile";
	public static final String ID_FILE = "id_file";
	public static final String ID_CHUNK = "id_chunk";
	public static final String CHUNK_SIZE = "chunk_size";
	public static final String DL_LENGTH = "dl_length";
	public static final String DL_BG = "dl_bg";

	public static final String PSF_CREATE_STATEMENT = "create table if not exists "
			+ DB_TABLE_PFILE
			+ "("
			+ ID
			+ " integer primary key autoincrement,"
			+ ID_FILE
			+ " integer not null,"
			+ ID_CHUNK
			+ " text not null,"
			+ CHUNK_SIZE
			+ " integer not null,"
			+ DL_LENGTH
			+ " integer not null," + DL_BG + " integer not null);";

}
