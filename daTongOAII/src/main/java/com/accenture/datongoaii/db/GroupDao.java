package com.accenture.datongoaii.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.model.Group;
import com.accenture.datongoaii.util.Logger;
import com.accenture.datongoaii.util.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class GroupDao {
    private Context context;

    public static final String GROUP_TABLE = "GROUP_TABLE";
    public static final String GROUP_TABLE_ID = "id";
    public static final String GROUP_TABLE_NAME = "name";
    public static final String GROUP_TABLE_IMG = "img";
    public static final String GROUP_TABLE_IMID = "imid";
    public static final String GROUP_TABLE_USER_NUM = "user_num";
    public static final String GROUP_TABLE_OWNER_ID = "owner";
    public static final String GROUP_TABLE_USER_IDS = "user_ids";

    public static final String MY_GROUP_TABLE = "MY_GROUP_TABLE";
    public static final String MY_GROUP_TABLE_GROUP_ID = "user_id";

    private static final String TAG = "GroupDao";

    public GroupDao(Context context) {
        this.context = context;
    }

    synchronized public void saveMyGroups(List<Group> groups) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                db.delete(MY_GROUP_TABLE, null, null);
                for (Group group : groups) {
                    ContentValues values = new ContentValues();
                    values.put(MY_GROUP_TABLE_GROUP_ID, group.id);
                    db.replace(MY_GROUP_TABLE, null, values);
                    if (isExisted(group)) {
                        update(group);
                    } else {
                        save(group);
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "saveFriends is error:" + e.getMessage());
        }
    }

    synchronized public List<Group> getMyGroups() {
        Cursor c = null;
        List<Group> list = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                c = db.rawQuery("select * from " + MY_GROUP_TABLE, new String[]{});
                if (c != null) {
                    list = new ArrayList<Group>();
                }
                while (c != null && c.moveToNext()) {
                    int groupId = c.getInt(c.getColumnIndex(MY_GROUP_TABLE_GROUP_ID));
                    Group group = getById(groupId);
                    list.add(group);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "getMyGroups is error:" + e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    synchronized public void deleteFromMyGroups(Group group) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getWritableDatabase();
            db.delete(MY_GROUP_TABLE, MY_GROUP_TABLE_GROUP_ID + "=?", new String[]{String.valueOf(group.id)});
            db.close();
        } catch (Exception e) {
            Logger.e(TAG, "delete is error:" + e.getMessage());
        }
    }

    /**
     * 根据userId获取Group
     *
     * @param groupId groupId
     * @return Group
     */
    synchronized public Group getById(int groupId) {
        Group group = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String sql = "select * from " + GROUP_TABLE + " where " + GROUP_TABLE_ID + "=?";
                c = db.rawQuery(sql, new String[]{String.valueOf(groupId)});
                if (c.moveToFirst()) {
                    group = generateByCursor(c);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "getById is error:" + e.getMessage());
            group = null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return group;
    }

    /**
     * 根据imId获取Group
     *
     * @param imId imId
     * @return Group
     */
    synchronized public Group getByImId(String imId) {
        Group group = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String sql = "select * from " + GROUP_TABLE + " where " + GROUP_TABLE_IMID + "=?";
                c = db.rawQuery(sql, new String[]{imId});
                if (c.moveToFirst()) {
                    group = generateByCursor(c);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "getByImId is error:" + e.getMessage());
            group = null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return group;
    }

    /**
     * 根据imId获取多个Group
     *
     * @param ids List of imId
     * @return List of Group
     */
    public List<Group> getListByImIds(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
        List<Group> list = new ArrayList<Group>();
        for (String id : ids) {
            Group group = getByImId(id);
            if (group!= null) {
                list.add(group);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    /**
     * 保存Group
     *
     * @param group Group
     */
    synchronized public void save(Group group) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                String sql = "insert into " + GROUP_TABLE + "(" + GROUP_TABLE_ID + ","
                        + GROUP_TABLE_NAME + "," + GROUP_TABLE_IMG + ","
                        + GROUP_TABLE_IMID + "," + GROUP_TABLE_USER_NUM + ","
                        + GROUP_TABLE_OWNER_ID + "," + GROUP_TABLE_USER_IDS
                        + ") values(?,?,?,?,?,?,?)";
                Logger.i(TAG, "save id = " + group.id);
                db.execSQL(sql, new Object[]{group.id, group.name, group.img, group.imId, group.userNum, group.owner == null ? -1 : group.owner.id, convertUserIds2Str(group.contactList)});
                saveOwner(group);
                saveMembers(group);
            }
        } catch (Exception e) {
            Logger.e(TAG, "save is error:" + e.getMessage());
        }
    }

    /**
     * 判断Group是否已缓存
     *
     * @param Group 待查Group
     * @return boolean
     */
    synchronized public boolean isExisted(Group Group) {
        Cursor c = null;
        boolean result = false;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String queryColumn;
                String queryId;
                if (Group.id != null && Group.id > 0) {
                    queryColumn = GROUP_TABLE_ID;
                    queryId = String.valueOf(Group.id);
                } else if (Group.imId != null && Group.imId.length() > 0) {
                    queryColumn = GROUP_TABLE_ID;
                    queryId = String.valueOf(Group.id);
                } else {
                    return false;
                }
                c = db.rawQuery("select * from " + GROUP_TABLE + " where " + queryColumn + "=?", new String[]{queryId});
                if (c.moveToFirst()) {
                    result = true;
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }


    /**
     * 更新Group
     *
     * @param group 待更新Group
     */
    synchronized public void update(Group group) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                ContentValues values = new ContentValues();
                if (group.imId != null && group.imId.length() > 0) {
                    values.put(GroupDao.GROUP_TABLE_IMID, group.imId);
                }
                if (group.name != null && group.name.length() > 0) {
                    values.put(GroupDao.GROUP_TABLE_NAME, group.name);
                }
                if (group.img != null && group.img.length() > 0) {
                    values.put(GroupDao.GROUP_TABLE_IMG, group.img);
                }
                if (group.userNum > 0) {
                    values.put(GroupDao.GROUP_TABLE_USER_NUM, group.userNum);
                }
                if (group.owner != null) {
                    values.put(GroupDao.GROUP_TABLE_OWNER_ID, group.owner.id);
                    saveOwner(group);
                } else {
                    values.put(GroupDao.GROUP_TABLE_OWNER_ID, -1);
                }
                if (group.contactList != null) {
                    values.put(GroupDao.GROUP_TABLE_USER_IDS, convertUserIds2Str(group.contactList));
                    saveMembers(group);
                }
                db.update(GroupDao.GROUP_TABLE, values, GroupDao.GROUP_TABLE_ID + " = ?", new String[]{String.valueOf(group.id)});
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    /**
     * 删除群
     *
     * @param group 待删除群
     */
    synchronized public void delete(Group group) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context)
                    .getWritableDatabase();
            db.delete(GROUP_TABLE, GROUP_TABLE_ID + "=?", new String[]{String.valueOf(group.id)});
            db.close();
        } catch (Exception e) {
            Logger.e(TAG, "delete is error:" + e.getMessage());
        }
    }

    /**
     * 转换群成员->字符串
     *
     * @param list 成员list
     * @return 群成员字符串
     */
    private String convertUserIds2Str(List<Contact> list) {
        String str = "";
        if (list == null || list.size() == 0) {
            return str;
        }
        for (Contact contact : list) {
            str += contact.id + ",";
        }
        return str.substring(0, str.length() - 1);
    }

    /**
     * 转换群成员字符串->群成员list
     *
     * @param str 群成员字符串
     * @return 群成员list
     */
    private List<Contact> getMembersByUserIdsStr(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        List<String> list = Utils.splitStrings(str, ",");
        ArrayList<Integer> idList = new ArrayList<Integer>();
        for (String idStr : list) {
            idList.add(Integer.parseInt(idStr));
        }
        return new ContactDao(context).getListByUserIds(idList);
    }

    private Group generateByCursor(Cursor c) {
        Group group = new Group();
        group.id = c.getInt(c.getColumnIndex(GROUP_TABLE_ID));
        group.name = c.getString(c.getColumnIndex(GROUP_TABLE_NAME));
        group.img = c.getString(c.getColumnIndex(GROUP_TABLE_IMG));
        group.imId = c.getString(c.getColumnIndex(GROUP_TABLE_IMID));
        group.userNum = c.getInt(c.getColumnIndex(GROUP_TABLE_USER_NUM));
        group.contactList = getMembersByUserIdsStr(c.getString(c.getColumnIndex(GROUP_TABLE_USER_IDS)));
        int ownerId = c.getInt(c.getColumnIndex(GROUP_TABLE_OWNER_ID));
        if (ownerId == -1) {
            group.owner = null;
        } else {
            group.owner = new ContactDao(context).getById(ownerId);
        }
        group.mFirstPinYin = "#";
        return group;
    }

    private void saveOwner(Group group) {
        if (group.owner != null) {
            ContactDao dao = new ContactDao(context);
            if (dao.isExisted(group.owner)) {
                dao.update(group.owner, false);
            } else {
                dao.save(group.owner);
            }
        }
    }

    private void saveMembers(Group group) {
        if (group.contactList != null) {
            for (Contact c : group.contactList) {
                ContactDao dao = new ContactDao(context);
                if (dao.isExisted(c)) {
                    dao.update(c, false);
                } else {
                    dao.save(c);
                }
            }
        }
    }
}
