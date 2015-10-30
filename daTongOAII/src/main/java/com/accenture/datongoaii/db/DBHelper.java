package com.accenture.datongoaii.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.accenture.datongoaii.util.Logger;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DB_NAME = "DatongOAII.db";
    public static final int DATABASE_VERSION = 1;
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
            instance = new DBHelper(appContext, DB_NAME, null, DATABASE_VERSION);
            return instance;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + DeptDao.DEPT_TABLE + "("
                + DeptDao.DEPT_TABLE_ID + " int," + DeptDao.DEPT_TABLE_VERSION
                + " text," + DeptDao.DEPT_TABLE_NAME + " text,"
                + DeptDao.DEPT_TABLE_IMG + " text," + DeptDao.DEPT_TABLE_PID
                + " int)");

        db.execSQL("create table if not exists " + ContactDao.CONTACT_TABLE + "("
                + ContactDao.CONTACT_TABLE_ID + " int,"
                + ContactDao.CONTACT_TABLE_NAME + " text,"
                + ContactDao.CONTACT_TABLE_HEAD + " text,"
                + ContactDao.CONTACT_TABLE_IMID + " text,"
                + ContactDao.CONTACT_TABLE_CELL + " text,"
                + ContactDao.CONTACT_TABLE_FRIEND_STATUS + " int)");

        db.execSQL("create table if not exists " + ContactDao.FRIEND_TABLE + "("
                + ContactDao.FRIEND_TABLE_USER_ID + " int)");

        db.execSQL("create table if not exists " + GroupDao.GROUP_TABLE + "("
                + GroupDao.GROUP_TABLE_ID + " int,"
                + GroupDao.GROUP_TABLE_NAME + " text,"
                + GroupDao.GROUP_TABLE_IMG + " text,"
                + GroupDao.GROUP_TABLE_IMID + " text,"
                + GroupDao.GROUP_TABLE_USER_NUM + " int,"
                + GroupDao.GROUP_TABLE_OWNER_ID + " int,"
                + GroupDao.GROUP_TABLE_USER_IDS + " text)");

        db.execSQL("create table if not exists " + GroupDao.MY_GROUP_TABLE + "("
                + GroupDao.MY_GROUP_TABLE_GROUP_ID + " int)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            dropTables(db);
        } else {
            return;
        }
        onCreate(db);
    }

    public void clearDataBase() {
        SQLiteDatabase db = getWritableDatabase();
        if (db.isOpen()) {
            dropTables(db);
            onCreate(db);
        }
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TodoDao.TODO_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DeptDao.DEPT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ContactDao.CONTACT_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ContactDao.FRIEND_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GroupDao.GROUP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GroupDao.MY_GROUP_TABLE);
        Logger.i(TAG, "clearDataBase success");
    }
}
