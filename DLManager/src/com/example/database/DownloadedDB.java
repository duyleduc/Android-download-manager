package com.example.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.model.ChunkFileModel;
import com.example.model.ConstantsVars;
import com.example.model.DownloadedFileModel;
import com.example.model.EnumStateFile;
import com.example.model.FileModel;

public class DownloadedDB {

	private static DownloadedDB dbh = null;
	private SQLiteDatabase db;
	private Context mContext;

	public static DownloadedDB getInstance(Context context) {
		if (dbh == null) {
			synchronized (DownloadedDB.class) {
				dbh = new DownloadedDB(context);
			}
		}
		return dbh;
	}

	private DownloadedDB(Context context) {
		mContext = context;
		DownloadHelper openHelper = new DownloadHelper(mContext);
		db = openHelper.getWritableDatabase();
	}

	public ArrayList<DownloadedFileModel> queryDownloadedFile() {
		ArrayList<DownloadedFileModel> files = new ArrayList<DownloadedFileModel>();
		String query = "select * from " + ConstantsVars.DB_TABLE_DOWNLOADED
				+ " where " + ConstantsVars.FSTATE + "!= 0" + " ORDER BY "
				+ ConstantsVars.TIMESTAMP + " DESC;";

		Cursor c = db.rawQuery(query, null);
		if (c != null && c.moveToFirst()) {
			do {
				files.add(cursorToFile(c));
			} while (c.moveToNext());
		}
		c.close();
		return files;
	}

	private DownloadedFileModel cursorToFile(Cursor c) {
		DownloadedFileModel file = new DownloadedFileModel();
		file.setfId(c.getInt(c.getColumnIndex(ConstantsVars.ID))); // id
		file.setfName(c.getString(c.getColumnIndex(ConstantsVars.FNAME))); // fname
		file.setfLink(c.getString(c.getColumnIndex(ConstantsVars.FLINK))); // flink
		file.setfSize(c.getLong(c.getColumnIndex(ConstantsVars.FSIZE))); // fsize
		file.setfState(c.getInt(c.getColumnIndex(ConstantsVars.FSTATE)));// fstate
		file.setfPath(c.getString(c.getColumnIndex(ConstantsVars.FPATH)));// path
		file.setfTimestamp(c.getLong(c.getColumnIndex(ConstantsVars.TIMESTAMP)));
		return file;
	}

	public long updateOrCreate(DownloadedFileModel file) {
		ContentValues v = new ContentValues();
		v.put(ConstantsVars.FNAME, file.getfName());
		v.put(ConstantsVars.FLINK, file.getfLink());
		v.put(ConstantsVars.FSIZE, file.getfSize());
		v.put(ConstantsVars.FSTATE, file.getfState());
		v.put(ConstantsVars.TIMESTAMP, file.getfTimestamp());
		v.put(ConstantsVars.FPATH, file.getPath());
		if (isExistFile(file)) {// if file existed then update
			db.update(ConstantsVars.DB_TABLE_DOWNLOADED, v, ConstantsVars.ID
					+ " = ?", new String[] { Long.toString(file.getfId()) });
			return file.getfId();
		} else {
			return db.insert(ConstantsVars.DB_TABLE_DOWNLOADED, null, v);
		}
	}

	public long createOrUpdateChunk(ChunkFileModel chunk, long id_file) {

		ContentValues v = new ContentValues();

		v.put(ConstantsVars.DL_END, chunk.getEnd());
		v.put(ConstantsVars.DL_LENGTH, chunk.getDownloadedSize());
		v.put(ConstantsVars.DL_BG, chunk.getBegin());
		v.put(ConstantsVars.FSTATE, chunk.getState().toString());

		if (!isChunkExist(chunk, id_file)) {
			v.put(ConstantsVars.ID_CHUNK, chunk.getId());
			v.put(ConstantsVars.ID_FILE, id_file);
			return db.insert(ConstantsVars.DB_TABLE_PFILE, null, v);
		} else {
			return db.update(ConstantsVars.DB_TABLE_PFILE, v,
					ConstantsVars.ID_FILE + " = " + id_file + " and "
							+ ConstantsVars.ID_CHUNK + "='" + chunk.getId()
							+ "'", null);
		}
	}

	private boolean isChunkExist(ChunkFileModel chunk, long id_file) {

		String query = "select " + ConstantsVars.FSTATE + " from "
				+ ConstantsVars.DB_TABLE_PFILE + " where "
				+ ConstantsVars.ID_FILE + "=" + id_file + " and "
				+ ConstantsVars.ID_CHUNK + "='" + chunk.getId() + "'";
		Cursor c = db.rawQuery(query, null);
		try {
			if (c.getCount() > 0) {
				return true;
			}
		} finally {
			c.close();
		}

		return false;
	}

	public List<FileModel> queryPausedFiles() {
		List<FileModel> files = new ArrayList<FileModel>();
		String query = "select * from " + ConstantsVars.DB_TABLE_DOWNLOADED
				+ " where " + ConstantsVars.FSTATE + " = '1';";
		Cursor c = db.rawQuery(query, null);

		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			do {
				long id = c.getLong(0);
				long size = c.getLong(2);

				String link = c.getString(3);
				long timestamp = c.getLong(5);
				String path = c.getString(6);
				FileModel f = new FileModel(link, size, timestamp);
				f.setId(id);
				f.path = path;

				String query_part = "select * from "
						+ ConstantsVars.DB_TABLE_PFILE + " where "
						+ ConstantsVars.ID_FILE + " = " + id;
				Cursor cp = db.rawQuery(query_part, null);

				long dlw = 0;
				if (cp != null && cp.getCount() > 0) {
					cp.moveToFirst();
					do {
						String id_chunk = cp.getString(2);
						long dl_end = cp.getLong(3);
						long dl_begin = cp.getLong(5);
						long dl_length = cp.getLong(cp
								.getColumnIndex(ConstantsVars.DL_LENGTH));
						String state = cp.getString(cp
								.getColumnIndex(ConstantsVars.FSTATE));
						dlw += dl_length;

						ChunkFileModel cfm = new ChunkFileModel(f, id_chunk,
								dl_begin + dl_length,
								EnumStateFile.DOWNLOADING, dl_end);
						cfm.setDownloadedSize(dl_length);
						cfm.setState(EnumStateFile.valueOf(state));
						f.getParts().add(cfm);
						f.setState(EnumStateFile.PAUSED);
					} while (cp.moveToNext());
				}
				cp.close();
				files.add(f);
				double per;
				f.setDownloadedLenght(dlw);
				per = (double) dlw / (double) f.getfSize();
				per = Math.round(per * 100 * 1.0) / 1.0; // round to 1
				f.setPercentDownloaded(Double.toString(per) + "%");
			} while (c.moveToNext());
		}
		c.close();
		return files;
	}

	public void query_deleteChunkFile(long file_id) {
		String query = "delete from " + ConstantsVars.DB_TABLE_PFILE
				+ " where " + ConstantsVars.ID_FILE + " = " + file_id;
		db.execSQL(query);
	}

	public long deleteFile(DownloadedFileModel file) {
		return db.delete(ConstantsVars.DB_TABLE_DOWNLOADED, "where "
				+ ConstantsVars.ID + " =?",
				new String[] { Long.toString(file.getfId()) });
	}

	private boolean isExistFile(DownloadedFileModel item) {// check if file
															// existed
															// or not
		Cursor c = db.rawQuery(
				"select " + ConstantsVars.ID + " from "
						+ ConstantsVars.DB_TABLE_DOWNLOADED + " where "
						+ ConstantsVars.FNAME + " = ? and "
						+ ConstantsVars.TIMESTAMP + "= ?",
				new String[] { item.getfName(),
						Long.toString(item.getfTimestamp()) });
		try {
			if (c.getCount() > 0) {
				return true;
			}

		} finally {
			c.close();
		}

		return false;

	}

	private static class DownloadHelper extends SQLiteOpenHelper {

		public DownloadHelper(Context context) {
			super(context, "mydb", null, ConstantsVars.DB_DOWNLOADED_VERSION);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ConstantsVars.DBDED_CREATE_STATEMENT);
			db.execSQL(ConstantsVars.PSF_CREATE_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL("DROP TABLE " + ConstantsVars.DB_TABLE_DOWNLOADED);
			db.execSQL("DROP TABLE " + ConstantsVars.DB_TABLE_PFILE);
			this.onCreate(db);

		}

	}

}
