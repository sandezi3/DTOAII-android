package com.accenture.datongoaii.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.accenture.datongoaii.model.Contact;
import com.accenture.datongoaii.util.CharacterParser;
import com.accenture.datongoaii.util.Logger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ContactDao {
    private Context context;

    public static final String CONTACT_TABLE = "contact_table";
    public static final String CONTACT_TABLE_ID = "id";
    public static final String CONTACT_TABLE_NAME = "name";
    public static final String CONTACT_TABLE_HEAD = "head";
    public static final String CONTACT_TABLE_IMID = "imid";
    public static final String CONTACT_TABLE_CELL = "cell";
    public static final String CONTACT_TABLE_FRIEND_STATUS = "friend_status";

    public static final String FRIEND_TABLE = "friend_table";
    public static final String FRIEND_TABLE_USER_ID = "user_id";

    private static final String TAG = "ContactDao";

    public ContactDao(Context context) {
        this.context = context;
    }

    /**
     * 保存好友
     *
     * @param list Contact list
     */
    synchronized public void saveFriends(List<Contact> list) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                db.delete(FRIEND_TABLE, null, null);
                for (Contact c : list) {
                    ContentValues values = new ContentValues();
                    values.put(FRIEND_TABLE_USER_ID, c.id);
                    db.replace(FRIEND_TABLE, null, values);
                    if (isExisted(c)) {
                        update(c, true);
                    } else {
                        save(c);
                    }
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "saveFriends is error:" + e.getMessage());
        }
    }

    /**
     * 获取好友
     *
     * @return 好友列表
     */
    synchronized public List<Contact> getFriends() {
        Cursor c = null;
        List<Contact> list = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                c = db.rawQuery("select * from " + FRIEND_TABLE, new String[]{});
                if (c != null) {
                    list = new ArrayList<Contact>();
                }
                while (c != null && c.moveToNext()) {
                    int userId = c.getInt(c.getColumnIndex(FRIEND_TABLE_USER_ID));
                    Contact contact = getById(userId);
                    list.add(contact);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "getFriends is error:" + e.getMessage());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     * 根据userId获取Contact
     *
     * @param userId userId
     * @return Contact
     */
    synchronized public Contact getById(int userId) {
        Contact contact = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String sql = "select * from " + CONTACT_TABLE + " where " + CONTACT_TABLE_ID + "=?";
                c = db.rawQuery(sql, new String[]{String.valueOf(userId)});
                if (c.moveToFirst()) {
                    contact = generateByCursor(c);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "DeptCacheDB getDeptById is error:" + e.getMessage());
            contact = null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return contact;
    }

    /**
     * 根据imId获取Contact
     *
     * @param imId imId
     * @return Contact
     */
    synchronized public Contact getByImId(String imId) {
        Contact contact = null;
        Cursor c = null;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String sql = "select * from " + CONTACT_TABLE + " where " + CONTACT_TABLE_IMID + "=?";
                c = db.rawQuery(sql, new String[]{imId});
                if (c.moveToFirst()) {
                    contact = generateByCursor(c);
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, "DeptCacheDB getDeptById is error:" + e.getMessage());
            contact = null;
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return contact;
    }

    /**
     * 根据imId获取多个Contact
     *
     * @param ids List of imId
     * @return List of Contact
     */
    public List<Contact> getListByImIds(List<String> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
        List<Contact> list = new ArrayList<Contact>();
        for (String id : ids) {
            list.add(getByImId(id));
        }
        return list;
    }

    /**
     * 根据userId获取多个Contact
     *
     * @param ids List of userId
     * @return List of Contact
     */
    public List<Contact> getListByUserIds(List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
        List<Contact> list = new ArrayList<Contact>();
        for (int id : ids) {
            list.add(getById(id));
        }
        return list;
    }

    /**
     * 保存Contact
     *
     * @param c Contact
     */
    synchronized public void save(Contact c) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                String sql = "insert into " + CONTACT_TABLE + "(" + CONTACT_TABLE_ID + ","
                        + CONTACT_TABLE_NAME + "," + CONTACT_TABLE_HEAD + ","
                        + CONTACT_TABLE_IMID + "," + CONTACT_TABLE_CELL + ","
                        + CONTACT_TABLE_FRIEND_STATUS + ") values(?,?,?,?,?,?)";
                Logger.i(TAG, "save id = " + c.id);
                db.execSQL(sql, new Object[]{c.id, c.name, c.head, c.imId, c.cell, getFriendStatus(c.friendStatus)});
            }
        } catch (Exception e) {
            Logger.e(TAG, "save is error:" + e.getMessage());
        }
    }

    /**
     * 判断Contact是否已缓存
     *
     * @param contact 待查Contact
     * @return boolean
     */
    synchronized public boolean isExisted(Contact contact) {
        Cursor c = null;
        boolean result = false;
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getReadableDatabase();
            if (db.isOpen()) {
                String queryColumn;
                String queryId;
                if (contact.id != null && contact.id > 0) {
                    queryColumn = CONTACT_TABLE_ID;
                    queryId = String.valueOf(contact.id);
                } else if (contact.imId != null && contact.imId.length() > 0) {
                    queryColumn = CONTACT_TABLE_ID;
                    queryId = String.valueOf(contact.id);
                } else {
                    return false;
                }
                c = db.rawQuery("select * from " + CONTACT_TABLE + " where " + queryColumn + "=?", new String[]{queryId});
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
     * 更新Contact
     *
     * @param contact 待更新Contact
     */
    synchronized public void update(Contact contact, boolean updateFriendsStatus) {
        try {
            SQLiteDatabase db = DBHelper.getInstance(context).getWritableDatabase();
            if (db.isOpen()) {
                ContentValues values = new ContentValues();
                if (contact.name != null && contact.name.length() > 0) {
                    values.put(ContactDao.CONTACT_TABLE_NAME, contact.name);
                }
                if (contact.head != null && contact.head.length() > 0) {
                    values.put(ContactDao.CONTACT_TABLE_HEAD, contact.head);
                }
                if (updateFriendsStatus) {
                    values.put(ContactDao.CONTACT_TABLE_FRIEND_STATUS, getFriendStatus(contact.friendStatus));
                }
                if (contact.id > 0) {
                    db.update(ContactDao.CONTACT_TABLE, values, ContactDao.CONTACT_TABLE_ID + " = ?", new String[]{String.valueOf(contact.id)});
                } else if (contact.imId != null && contact.imId.length() > 0) {
                    db.update(ContactDao.CONTACT_TABLE, values, ContactDao.CONTACT_TABLE_IMID + " = ?", new String[]{contact.imId});
                }
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    private Contact generateByCursor(Cursor c) {
        Contact contact = new Contact();
        contact.id = c.getInt(c.getColumnIndex(CONTACT_TABLE_ID));
        contact.name = c.getString(c.getColumnIndex(CONTACT_TABLE_NAME));
        contact.head = c.getString(c.getColumnIndex(CONTACT_TABLE_HEAD));
        contact.imId = c.getString(c.getColumnIndex(CONTACT_TABLE_IMID));
        contact.cell = c.getString(c.getColumnIndex(CONTACT_TABLE_CELL));
        contact.isUser = contact.id > 0;
        contact.friendStatus = getFriendStatus(c.getInt(c.getColumnIndex(CONTACT_TABLE_FRIEND_STATUS)));
        CharacterParser cp = CharacterParser.getInstance();
        contact.mFirstPinYin = cp.getSelling(contact.name).substring(0, 1).toUpperCase();
        return contact;
    }

    /**
     * FriendStatus相关转换
     */
    public static final int STATUS_TO_BE_FRIEND = 0;
    public static final int STATUS_FROM_ME_NOT_ACCEPT = 1;
    public static final int STATUS_TO_ME_NOT_ACCEPT = 2;
    public static final int STATUS_FRIEND = 3;

    private Contact.FriendStatus getFriendStatus(int status) {
        switch (status) {
            case STATUS_TO_BE_FRIEND:
                return Contact.FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND;
            case STATUS_FROM_ME_NOT_ACCEPT:
                return Contact.FriendStatus.FRIENDS_STATUS_FROM_ME_NOT_ACCEPT;
            case STATUS_TO_ME_NOT_ACCEPT:
                return Contact.FriendStatus.FRIENDS_STATUS_TO_ME_NOT_ACCEPT;
            case STATUS_FRIEND:
                return Contact.FriendStatus.FRIENDS_STATUS_FRIEND;
            default:
                return Contact.FriendStatus.FRIENDS_STATUS_TO_BE_FRIEND;
        }
    }

    private int getFriendStatus(Contact.FriendStatus status) {
        switch (status) {
            case FRIENDS_STATUS_TO_BE_FRIEND:
                return STATUS_TO_BE_FRIEND;
            case FRIENDS_STATUS_FROM_ME_NOT_ACCEPT:
                return STATUS_FROM_ME_NOT_ACCEPT;
            case FRIENDS_STATUS_TO_ME_NOT_ACCEPT:
                return STATUS_TO_ME_NOT_ACCEPT;
            case FRIENDS_STATUS_FRIEND:
                return STATUS_FRIEND;
            default:
                return STATUS_TO_BE_FRIEND;
        }
    }
}
