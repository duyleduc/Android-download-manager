package com.example.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.model.ConstantsVars;

public class PausedFileDB {

	private Context mContext;
	private SQLiteDatabase db;
	private static PausedFileDB dbh = null;

	public static PausedFileDB getInstance(Context context) {
		if (dbh == null) {
			synchronized (PausedFileDB.class) {
				dbh = new PausedFileDB(context);
			}
		}
		return dbh;
	}
	
	

	private PausedFileDB(Context context) {
		mContext = context;
		DBHelper openHelper = new DBHelper(mContext);
		db = openHelper.getWritableDatabase();
	}
	
	
	
	

	private class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, ConstantsVars.DB_PAUSED_FILE, null,
					ConstantsVars.DB_PAUSEDF_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(ConstantsVars.PSF_CREATE_STATEMENT);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table " + ConstantsVars.DB_TABLE_PFILE + ";");
			this.onCreate(db);
		}

	}
}
