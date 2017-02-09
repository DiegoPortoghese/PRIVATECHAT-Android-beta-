/*
 *
 * Created by Diego Portoghese
 *
 */
package com.example.utente.chatclient;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class message {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    List<String> datalist = new ArrayList<String>();
    int key_id;
    String id="0";
    DatabaseHelper mydb;
    String usernametable;

    message(String username_){

        mydb = new DatabaseHelper(UberManager.getInstance().getMainActivity(),username_);

        ArrayList array_list = mydb.getAllMessage();
        for(int i=0;i<array_list.size();i++) {
            try{
                JSONObject nuovoJSobject = new JSONObject((String) array_list.get(i));
                id=nuovoJSobject.getString("id");
                datalist.add((String) array_list.get(i));
            }catch(Exception e){}
        }
    }

    public void addMessage(String data_) throws JSONException {

        System.out.println("ADDMESSAGE FUNC " + data_);
        String data = data_;
        try{
            JSONObject nvJS = new JSONObject(data);
            String id=nvJS.getString("id");
            String from=nvJS.getString("from");
            String to=nvJS.getString("to");
            String redata=nvJS.getString("data");
            mydb.insertMessage(id,from,to,redata,"0");
            System.out.println("Json inside array ok");
        }catch(Exception e){}

    }

    public String[] getmessage(String user){
        String[] OUTARRAY;
        String[] nullx=new String[0];
        if(user.equals(""))
        {}else {
            ArrayList array_list = mydb.getAllMessage();
            ListIterator listIterator = array_list.listIterator();
            List<String> dalistout = new ArrayList<String>();
            String userTRm = user;
            //Find the user in list and remove IT
            while (listIterator.hasNext()) {

                String a = (String) listIterator.next();
                JSONObject JSx = null;
                try {
                    JSx = new JSONObject(a);
                    if (JSx.getString("from").equals(user)) {
                        dalistout.add(JSx.toString());
                    }
                    if (JSx.getString("to").equals(user)) {
                        dalistout.add(JSx.toString());
                    }
                } catch (Exception e) {
                }
            }

            OUTARRAY = new String[dalistout.size()];

            for (int j = 0; j < OUTARRAY.length; j++) {
                OUTARRAY[j] = dalistout.get(j).toString();
            }
            return OUTARRAY;
        }
        return nullx;
    }

    public void setRead(String FriendName){
        mydb.updateMessage(FriendName);
    }

    public boolean ifIsRead(String FriendName){
        return true;
    }
}
