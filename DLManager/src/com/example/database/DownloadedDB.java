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
		file.setfPath(c.getString(c.getColumnIndex(ConstantsVars.FPATH)));//path
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
			return db.update(ConstantsVars.DB_TABLE_DOWNLOADED, v, "where "
					+ ConstantsVars.ID + " =?",
					new String[] { Integer.toString(file.getfId()) });
		} else {
			return db.insert(ConstantsVars.DB_TABLE_DOWNLOADED, null, v);
		}
	}

	public long deleteFile(DownloadedFileModel file) {
		return db.delete(ConstantsVars.DB_TABLE_DOWNLOADED, "where "
				+ ConstantsVars.ID + " =?",
				new String[] { Integer.toString(file.getfId()) });
	}

	private boolean isExistFile(DownloadedFileModel item) {// check if file
															// existed
															// or not
		Cursor c = db.rawQuery(
				"select " + ConstantsVars.ID + " from "
						+ ConstantsVars.DB_TABLE_DOWNLOADED + " where "
						+ ConstantsVars.FNAME + " = ? and "
						+ ConstantsVars.TIMESTAMP + "=?",
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
			super(context, ConstantsVars.DB_DOWNLOADED, null,
					ConstantsVars.DB_DOWNLOADED_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ConstantsVars.DBDED_CREATE_STATEMENT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL("DROP TABLE " + ConstantsVars.DB_TABLE_DOWNLOADED);

			this.onCreate(db);

		}

	}

}
