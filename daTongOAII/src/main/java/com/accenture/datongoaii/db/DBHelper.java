package com.accenture.datongoaii.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "DatongOAII.db";
	private static DBHelper instance;
	private static Context context;

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public static synchronized DBHelper getInstance(Context appContext) {
		if (context == null)
			context = appContext;
		if (instance != null) {
			return instance;
		} else {
			instance = new DBHelper(appContext, DB_NAME, null, 1);
			return instance;
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("create table if not exists ");
		// sb.append(TodoDao.TODO_TABLE);
		// sb.append("(");
		// sb.append(TodoDao.TODO_TABLE_ID);
		// sb.append(" integer PRIMARY KEY AUTOINCREMENT");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_TID);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_TITLE);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_CREATE);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_FROM);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_DEADLINE);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_IMAGE_URL);
		// sb.append(" text");
		// sb.append(",");
		// sb.append(TodoDao.TODO_TABLE_IMAGE);
		// sb.append(" BINARY");
		// sb.append(")");
		// db.execSQL(sb.toString());

		String sql = "create table if not exists " + DeptDao.DEPT_TABLE + "("
				+ DeptDao.DEPT_TABLE_ID + " int," + DeptDao.DEPT_TABLE_VERSION
				+ " text," + DeptDao.DEPT_TABLE_NAME + " text,"
				+ DeptDao.DEPT_TABLE_IMG + " text," + DeptDao.DEPT_TABLE_PID
				+ " int)";
		db.execSQL(sql);

		sql = "create table if not exists " + ContactDao.CONTACT_TABLE + "("
				+ ContactDao.CONTACT_TABLE_ID + " int,"
				+ ContactDao.CONTACT_TABLE_NAME + " text,"
				+ ContactDao.CONTACT_TABLE_HEAD + " text,"
				+ ContactDao.CONTACT_TABLE_PDID + " int)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion) {
			StringBuffer sb = new StringBuffer();
			sb.append("DROP TABLE IF EXISTS ");
			sb.append(TodoDao.TODO_TABLE);
			db.execSQL(sb.toString());
			String sql = "DROP TABLE IF EXISTS " + DeptDao.DEPT_TABLE;
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS" + ContactDao.CONTACT_TABLE;
			db.execSQL(sql);
		} else {
			return;
		}
		onCreate(db);
	}

}
