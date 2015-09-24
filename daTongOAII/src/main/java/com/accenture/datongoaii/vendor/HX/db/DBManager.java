package com.accenture.datongoaii.vendor.HX.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.accenture.datongoaii.vendor.HX.Constant;
import com.accenture.datongoaii.vendor.HX.domain.User;
import com.easemob.util.HanziToPinyin;

public class DBManager {
    static private DBManager dbMgr = new DBManager();
    private DbOpenHelper dbHelper;
    
    void onInit(Context context){
        dbHelper = DbOpenHelper.getInstance(context);
    }
    
    public static synchronized DBManager getInstance(){
        return dbMgr;
    }
    
    /**
     * 保存好友list
     * 
     * @param contactList
     */
    synchronized public void saveContactList(List<User> contactList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            db.delete(UserDao.TABLE_NAME, null, null);
            for (User user : contactList) {
                ContentValues values = new ContentValues();
                values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
                if(user.getNick() != null)
                    values.put(UserDao.COLUMN_NAME_NICK, user.getNick());
                if(user.getAvatar() != null)
                    values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
                db.replace(UserDao.TABLE_NAME, null, values);
            }
        }
    }

    /**
     * 获取好友list
     * 
     * @return
     */
    synchronized public Map<String, User> getContactList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Map<String, User> users = new HashMap<String, User>();
        if (db.isOpen()) {
            Cursor cursor = db.rawQuery("select * from " + UserDao.TABLE_NAME /* + " desc" */, null);
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_ID));
                String nick = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_NICK));
                String avatar = cursor.getString(cursor.getColumnIndex(UserDao.COLUMN_NAME_AVATAR));
                User user = new User();
                user.setUsername(username);
                user.setNick(nick);
                user.setAvatar(avatar);
                String headerName = null;
                if (!TextUtils.isEmpty(user.getNick())) {
                    headerName = user.getNick();
                } else {
                    headerName = user.getUsername();
                }
                
                if (username.equals(Constant.NEW_FRIENDS_USERNAME) || username.equals(Constant.GROUP_USERNAME)
                        || username.equals(Constant.CHAT_ROOM)|| username.equals(Constant.CHAT_ROBOT)) {
                    user.setHeader("");
                } else if (Character.isDigit(headerName.charAt(0))) {
                    user.setHeader("#");
                } else {
                    user.setHeader(HanziToPinyin.getInstance().get(headerName.substring(0, 1))
                            .get(0).target.substring(0, 1).toUpperCase());
                    char header = user.getHeader().toLowerCase().charAt(0);
                    if (header < 'a' || header > 'z') {
                        user.setHeader("#");
                    }
                }
                users.put(username, user);
            }
            cursor.close();
        }
        return users;
    }
    
    /**
     * 删除一个联系人
     * @param username
     */
    synchronized public void deleteContact(String username){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if(db.isOpen()){
            db.delete(UserDao.TABLE_NAME, UserDao.COLUMN_NAME_ID + " = ?", new String[]{username});
        }
    }
    
    /**
     * 保存一个联系人
     * @param user
     */
    synchronized public void saveContact(User user){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDao.COLUMN_NAME_ID, user.getUsername());
        if(user.getNick() != null)
            values.put(UserDao.COLUMN_NAME_NICK, user.getNick());
        if(user.getAvatar() != null)
            values.put(UserDao.COLUMN_NAME_AVATAR, user.getAvatar());
        if(db.isOpen()){
            db.replace(UserDao.TABLE_NAME, null, values);
        }
    }
    
    public void setDisabledGroups(List<String> groups){
        setList(UserDao.COLUMN_NAME_DISABLED_GROUPS, groups);
    }
    
    public List<String>  getDisabledGroups(){       
        return getList(UserDao.COLUMN_NAME_DISABLED_GROUPS);
    }
    
    public void setDisabledIds(List<String> ids){
        setList(UserDao.COLUMN_NAME_DISABLED_IDS, ids);
    }
    
    public List<String> getDisabledIds(){
        return getList(UserDao.COLUMN_NAME_DISABLED_IDS);
    }
    
    synchronized private void setList(String column, List<String> strList){
        StringBuilder strBuilder = new StringBuilder();
        
        for(String hxid:strList){
            strBuilder.append(hxid).append("$");
        }
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.isOpen()) {
            ContentValues values = new ContentValues();
            values.put(column, strBuilder.toString());

            db.update(UserDao.PREF_TABLE_NAME, values, null, null);
        }
    }
    
    synchronized private List<String> getList(String column){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + column + " from " + UserDao.PREF_TABLE_NAME,null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        String strVal = cursor.getString(0);
        if (strVal == null || strVal.equals("")) {
            return null;
        }
        
        cursor.close();
        
        String[] array = strVal.split("$");
        
        if(array != null && array.length > 0){
            List<String> list = new ArrayList<String>();
            for(String str:array){
                list.add(str);
            }
            
            return list;
        }
        
        return null;
    }
    
}