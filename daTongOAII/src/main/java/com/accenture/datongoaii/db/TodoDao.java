package com.accenture.datongoaii.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;

import com.accenture.datongoaii.model.Todo;
import com.accenture.datongoaii.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TodoDao {
    private Context context;

    public static final String TODO_TABLE = "todo_table";

    public static final String TODO_TABLE_ID = "_id";
    public static final String TODO_TABLE_TID = "tid";
    public static final String TODO_TABLE_TITLE = "title";
    public static final String TODO_TABLE_CREATE = "create";
    public static final String TODO_TABLE_FROM = "fr";
    public static final String TODO_TABLE_DEADLINE = "deadline";
    public static final String TODO_TABLE_IMAGE_URL = "img";
    public static final String TODO_TABLE_IMAGE = "image";

    private static final String TAG = "TodoCacheDb";

    public TodoDao(Context context) {
        this.context = context;
    }

    public void add(Todo n) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getWritableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("insert into ");
            sb.append(TODO_TABLE);
            sb.append("(");
            sb.append(TODO_TABLE_ID);
            sb.append(",");
            sb.append(TODO_TABLE_TID);
            sb.append(",");
            sb.append(TODO_TABLE_TITLE);
            sb.append(",");
            sb.append(TODO_TABLE_CREATE);
            sb.append(",");
            sb.append(TODO_TABLE_FROM);
            sb.append(",");
            sb.append(TODO_TABLE_DEADLINE);
            sb.append(",");
            sb.append(TODO_TABLE_IMAGE_URL);
            sb.append(",");
            sb.append(TODO_TABLE_IMAGE);
            sb.append(") values(?,?,?,?,?,?,?,?)");
            Logger.i("TodoCacheDB", "tid = " + n.tid);
            db.execSQL(sb.toString(), new Object[]{null, n.tid, n.title,
                    n.create, n.deadline, n.from, n.img, null});
            db.close();
        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB add is error:" + e.getMessage());
        }
    }

    public void updateText(Todo n) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getWritableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("update ");
            sb.append(TODO_TABLE);
            sb.append(" set ");
            sb.append(TODO_TABLE_TITLE);
            sb.append("=?, ");
            sb.append(TODO_TABLE_CREATE);
            sb.append("=?, ");
            sb.append(TODO_TABLE_DEADLINE);
            sb.append("=?, ");
            sb.append(TODO_TABLE_FROM);
            sb.append("=?, ");
            sb.append(TODO_TABLE_IMAGE_URL);
            sb.append("=? where ");
            sb.append(TODO_TABLE_TID);
            sb.append("=?");
            db.execSQL(sb.toString(), new Object[]{n.title, n.create,
                    n.deadline, n.from, n.img, n.tid});
            db.close();
        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB updateText is error:" + e.getMessage());
        }
    }

    public void updateImage(String tid, Bitmap image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(CompressFormat.PNG, 100, baos);

            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getWritableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("update ");
            sb.append(TODO_TABLE);
            sb.append(" set ");
            sb.append(TODO_TABLE_IMAGE);
            sb.append("=? where ");
            sb.append(TODO_TABLE_TID);
            sb.append("=?");
            db.execSQL(sb.toString(), new Object[]{baos.toByteArray(), tid});
            db.close();
        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB updateImage is error:" + e.getMessage());
        }
    }

    public boolean isTodoCacheEmpty() {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getReadableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("select * from ");
            sb.append(TODO_TABLE);
            Cursor cursor = db.rawQuery(sb.toString(), null);
            if (cursor.getCount() > 0) {
                return false;
            }
            db.close();
        } catch (Exception e) {
            Logger.e(TAG,
                    "TodoCacheDB isTodoCacheEmpty is error:" + e.getMessage());
        }
        return true;
    }

    public Todo getTodoById(String aid) {
        Todo n = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getReadableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("select * from ");
            sb.append(TODO_TABLE);
            sb.append(" where ");
            sb.append(TODO_TABLE_TID);
            sb.append("=?");
            c = db.rawQuery(sb.toString(), new String[]{aid});
            if (c != null && c.moveToNext()) {
                n = new Todo();
                n._id = c.getInt(c.getColumnIndex(TODO_TABLE_ID));
                n.tid = c.getString(c.getColumnIndex(TODO_TABLE_TID));
                n.title = c.getString(c.getColumnIndex(TODO_TABLE_TITLE));
                n.from = c.getString(c.getColumnIndex(TODO_TABLE_FROM));
                n.deadline = c.getString(c.getColumnIndex(TODO_TABLE_DEADLINE));
                n.img = c.getString(c.getColumnIndex(TODO_TABLE_IMAGE_URL));
                byte[] img = c.getBlob(c.getColumnIndex(TODO_TABLE_IMAGE));
                ByteArrayInputStream bais = null;
                if (img != null) {
                    bais = new ByteArrayInputStream(img);
                    n.image = Drawable.createFromStream(bais, TODO_TABLE_IMAGE);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB getTodoById is error:" + e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return n;
    }

    public List<Todo> getTodoList() {
        List<Todo> todoList = new ArrayList<Todo>();
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getReadableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("select * from ");
            sb.append(TODO_TABLE);
            Cursor cursor = db.rawQuery(sb.toString(), null);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Todo todo = new Todo();
                    todo._id = cursor.getInt(cursor
                            .getColumnIndex(TODO_TABLE_ID));
                    todo.create = cursor.getString(cursor
                            .getColumnIndex(TODO_TABLE_CREATE));
                    todo.title = cursor.getString(cursor
                            .getColumnIndex(TODO_TABLE_TITLE));
                    todo.from = cursor.getString(cursor
                            .getColumnIndex(TODO_TABLE_FROM));
                    todo.deadline = cursor.getString(cursor
                            .getColumnIndex(TODO_TABLE_DEADLINE));
                    todo.img = cursor.getString(cursor
                            .getColumnIndex(TODO_TABLE_IMAGE_URL));
                    byte[] img = cursor.getBlob(cursor
                            .getColumnIndex(TODO_TABLE_IMAGE));
                    ByteArrayInputStream bais = null;
                    if (img != null) {
                        bais = new ByteArrayInputStream(img);
                        todo.image = Drawable.createFromStream(bais,
                                TODO_TABLE_IMAGE);
                    }
                    todoList.add(todo);
                    cursor.moveToNext();
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            db.close();

        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB getTodoList is error:" + e.getMessage());
        }

        return todoList;
    }

    public Drawable getTodoImage(String aid) {
        Drawable image = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getReadableDatabase();
            StringBuffer sb = new StringBuffer();
            sb.append("select * from ");
            sb.append(TODO_TABLE);
            sb.append(" where ");
            sb.append(TODO_TABLE_TID);
            sb.append("=?");
            Cursor cursor = db.rawQuery(sb.toString(), new String[]{aid});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    byte[] img = cursor.getBlob(cursor
                            .getColumnIndex(TODO_TABLE_IMAGE));
                    ByteArrayInputStream bais = null;
                    if (img != null) {
                        bais = new ByteArrayInputStream(img);
                        image = Drawable.createFromStream(bais,
                                TODO_TABLE_IMAGE);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "TodoCacheDB getTodoImage is error:" + e.getMessage());
        }
        return image;
    }
}
