package com.accenture.datongoaii.db;

import android.content.Context;

public class DeptDao {
    private Context context;
    private ContactDao cd;

    public static final String DEPT_TABLE = "dept_table";

    public static final String DEPT_TABLE_ID = "id";
    public static final String DEPT_TABLE_VERSION = "version";
    public static final String DEPT_TABLE_NAME = "name";
    public static final String DEPT_TABLE_IMG = "img";
    public static final String DEPT_TABLE_PID = "pid";

    private static final String TAG = "DeptCacheDb";

    public DeptDao(Context context) {
        this.context = context;
        cd = new ContactDao(context);
    }

//    public void add(Dept d) {
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getWritableDatabase();
//            StringBuffer sb = new StringBuffer();
//            sb.append("insert into ");
//            sb.append(DEPT_TABLE);
//            sb.append("(");
//            sb.append(DEPT_TABLE_ID);
//            sb.append(",");
//            sb.append(DEPT_TABLE_VERSION);
//            sb.append(",");
//            sb.append(DEPT_TABLE_NAME);
//            sb.append(",");
//            sb.append(DEPT_TABLE_IMG);
//            sb.append(",");
//            sb.append(DEPT_TABLE_PID);
//            sb.append(") values(?,?,?,?,?)");
//            Logger.i("DeptCacheDB", "id = " + d.id);
//            Object[] oArray = null;
////			if (d.parent != null) {
////				oArray = new Object[] { d.id, d.version, d.name, d.img,
////						d.parent.id };
////			} else {
//            oArray = new Object[]{d.id, d.version, d.name, d.img, ""};
////			}
//            db.execSQL(sb.toString(), oArray);
//            db.close();
//            for (Dept dd : d.subDept) {
//                add(dd);
//            }
//            for (Contact c : d.contactList) {
//                cd.add(c);
//            }
//        } catch (Exception e) {
//            Logger.e(TAG, "DeptCacheDB add is error:" + e.getMessage());
//        }
//    }

//    public Dept getDeptById(Integer id) {
//        Dept d = null;
//        Cursor c = null;
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getReadableDatabase();
//            StringBuffer sb = new StringBuffer();
//            sb.append("select * from ");
//            sb.append(DEPT_TABLE);
//            sb.append(" where ");
//            sb.append(DEPT_TABLE_ID);
//            sb.append("=?");
//            c = db.rawQuery(sb.toString(), new String[]{id + ""});
//            if (c != null && c.moveToNext()) {
//                d = new Dept();
//                d.id = c.getInt(c.getColumnIndex(DEPT_TABLE_ID));
//                d.version = c.getString(c.getColumnIndex(DEPT_TABLE_VERSION));
//                d.name = c.getString(c.getColumnIndex(DEPT_TABLE_NAME));
//                d.img = c.getString(c.getColumnIndex(DEPT_TABLE_IMG));
//                d.subDept = getSubList(d.id);
//                d.mFirstPinYin = "#";
//                d.contactList = Contact.getListFromDataByPdid(context, d.id);
//                for (Contact cc : d.contactList) {
//                    cc.parent = d;
//                }
//            }
//        } catch (Exception e) {
//            Logger.e(TAG, "DeptCacheDB getDeptById is error:" + e.getMessage());
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//        return d;
//    }
//
//    public Dept getRootDept() {
//        Dept d = null;
//        Cursor c = null;
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getReadableDatabase();
//            StringBuffer sb = new StringBuffer();
//            sb.append("select * from ");
//            sb.append(DEPT_TABLE);
//            sb.append(" where ");
//            sb.append(DEPT_TABLE_PID);
//            sb.append("=?");
//            c = db.rawQuery(sb.toString(), new String[]{""});
//            if (c != null && c.moveToNext()) {
//                d = new Dept();
//                d.id = c.getInt(c.getColumnIndex(DEPT_TABLE_ID));
//                d.version = c.getString(c.getColumnIndex(DEPT_TABLE_VERSION));
//                d.name = c.getString(c.getColumnIndex(DEPT_TABLE_NAME));
//                d.img = c.getString(c.getColumnIndex(DEPT_TABLE_IMG));
//                d.subDept = getSubList(d.id);
////				for (Dept dd : d.subDept) {
////					dd.parent = d;
////				}
//                d.mFirstPinYin = "#";
//                d.contactList = Contact.getListFromDataByPdid(context, d.id);
//                for (Contact cc : d.contactList) {
//                    cc.parent = d;
//                }
//            }
//        } catch (Exception e) {
//            Logger.e(TAG, "DeptCacheDB getDeptById is error:" + e.getMessage());
//        } finally {
//            if (c != null) {
//                c.close();
//            }
//        }
//        return d;
//    }
//
//    public List<Dept> getDeptList() {
//        List<Dept> deptList = new ArrayList<Dept>();
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getReadableDatabase();
//            StringBuffer sb = new StringBuffer();
//            sb.append("select * from ");
//            sb.append(DEPT_TABLE);
//            Cursor c = db.rawQuery(sb.toString(), null);
//            if (c != null) {
//                c.moveToFirst();
//                while (!c.isAfterLast()) {
//                    Dept d = new Dept();
//                    d.id = c.getInt(c.getColumnIndex(DEPT_TABLE_ID));
//                    d.version = c.getString(c
//                            .getColumnIndex(DEPT_TABLE_VERSION));
//                    d.name = c.getString(c.getColumnIndex(DEPT_TABLE_NAME));
//                    d.img = c.getString(c.getColumnIndex(DEPT_TABLE_IMG));
//                    d.subDept = getSubList(d.id);
//
//                    deptList.add(d);
//                    c.moveToNext();
//                }
//                if (c != null) {
//                    c.close();
//                }
//            }
//            db.close();
//
//        } catch (Exception e) {
//            Logger.e(TAG, "DeptCacheDB getDeptList is error:" + e.getMessage());
//        }
//
//        return deptList;
//    }
//
//    public List<Dept> getSubList(Integer pId) {
//        List<Dept> deptList = new ArrayList<Dept>();
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getReadableDatabase();
//            StringBuffer sb = new StringBuffer();
//            sb.append("select * from ");
//            sb.append(DEPT_TABLE);
//            sb.append(" where ");
//            sb.append(DEPT_TABLE_PID);
//            sb.append("=?");
//            Cursor c = db.rawQuery(sb.toString(), new String[]{pId + ""});
//            if (c != null) {
//                c.moveToFirst();
//                while (!c.isAfterLast()) {
//                    Dept d = new Dept();
//                    d.id = c.getInt(c.getColumnIndex(DEPT_TABLE_ID));
//                    d.version = c.getString(c
//                            .getColumnIndex(DEPT_TABLE_VERSION));
//                    d.name = c.getString(c.getColumnIndex(DEPT_TABLE_NAME));
//                    d.img = c.getString(c.getColumnIndex(DEPT_TABLE_IMG));
//                    d.subDept = getSubList(d.id);
////					for (Dept dd : d.subDept) {
////						dd.parent = d;
////					}
//                    d.mFirstPinYin = "#";
//                    d.contactList = Contact
//                            .getListFromDataByPdid(context, d.id);
//                    for (Contact cc : d.contactList) {
//                        cc.parent = d;
//                    }
//                    deptList.add(d);
//                    c.moveToNext();
//                }
//                if (c != null) {
//                    c.close();
//                }
//            }
//            db.close();
//
//        } catch (Exception e) {
//            Logger.e(TAG, "DeptCacheDB getSubList is error:" + e.getMessage());
//        }
//
//        return deptList;
//    }
//
////    public void deleteDeptById(Integer id) {
////        List<Dept> list = getSubList(id);
////        for (Dept d : list) {
////            deleteDeptById(d.id);
////            deleteDeptByPidInner(d.id);
////            deleteDeptByIdInner(id);
////        }
////        deleteDeptByIdInner(id);
////        cd.deleteByPdid(id);
////    }
//
//    private void deleteDeptByIdInner(Integer id) {
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getWritableDatabase();
//            db.delete(DEPT_TABLE, DEPT_TABLE_ID + "=?", new String[]{id + ""});
//            db.close();
//        } catch (Exception e) {
//            Logger.e(TAG,
//                    "DeptCacheDB deleteDeptById is error:" + e.getMessage());
//        }
//    }
//
//    private void deleteDeptByPidInner(Integer id) {
//        try {
//            SQLiteDatabase db = DBHelper.getInstance(context)
//                    .getWritableDatabase();
//            db.delete(DEPT_TABLE, DEPT_TABLE_PID + "=?", new String[]{id + ""});
//            db.close();
//        } catch (Exception e) {
//            Logger.e(TAG,
//                    "DeptCacheDB deleteDeptById is error:" + e.getMessage());
//        }
//    }
}
