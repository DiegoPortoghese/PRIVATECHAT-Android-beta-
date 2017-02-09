/*
 *
 * Created by Diego Portoghese
 *
 */
package com.example.utente.chatclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "messagedb.db";
    private static final String MESSAGE_TABLE_NAME = "message";
    private static final String MESSAGE_COLUMN_ID = "id";
    private static final String MESSAGE_COLUMN_FROM = "nfrom";
    private static final String MESSAGE_COLUMN_TO = "nto";
    private static final String MESSAGE_COLUMN_DATA = "data";
    private static final String MESSAGE_COLUMN_READ = "read";
    //private HashMap hp;

    public DatabaseHelper(Context context,String username_) {
        super(context, username_+".db" , null, 1);
        this.DATABASE_NAME = username_+".db";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table if not exists "+MESSAGE_TABLE_NAME+" " +
                        "(id text,nfrom text,nto text,data text, read text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS "+MESSAGE_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertMessage (String id, String from, String to, String data,String read) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("nfrom", from);
        contentValues.put("nto", to);
        contentValues.put("data", data);
        contentValues.put("read", read);
        db.insert(MESSAGE_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+MESSAGE_TABLE_NAME+" where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, MESSAGE_TABLE_NAME);
        return numRows;
    }

    public boolean updateMessage (String user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("read", "1");
        db.update(MESSAGE_TABLE_NAME, contentValues, "nfrom = ? ", new String[] { user } );
        db.update(MESSAGE_TABLE_NAME, contentValues, "nto = ? ", new String[] { user } );
        return true;
    }

    public Integer deleteMessage (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(MESSAGE_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllMessage() {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor res =  db.rawQuery( "select * from "+MESSAGE_TABLE_NAME, null );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            String id = res.getString(res.getColumnIndex("id"));
            String from = res.getString(res.getColumnIndex("nfrom"));
            String to = res.getString(res.getColumnIndex("nto"));
            String data = res.getString(res.getColumnIndex("data"));
            String read = res.getString(res.getColumnIndex("read"));

            System.out.println("READ FROM LOCAL DB {\"id\":\""+id+"\",\"from\":\""+from+"\",\"to\":\""+to+"\",\"data\":\""+data+"\",\"read\":\""+read+"\"}");

            array_list.add("{\"id\":\""+id+"\",\"from\":\""+from+"\",\"to\":\""+to+"\",\"data\":\""+data+"\",\"read\":\""+read+"\"}");
            res.moveToNext();
        }
        return array_list;
    }
}