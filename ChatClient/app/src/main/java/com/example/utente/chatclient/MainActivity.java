/*
 *
 * Created by Diego Portoghese
 *
 */

package com.example.utente.chatclient;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class MainActivity extends AppCompatActivity {

    private static UberManager instance = new UberManager();
    //PORT SERVER
    private static final int SERVERPORT = 5552; //port server 
    //IP SERVER
    private static final String SERVER_IP = "ipofserver"; //Server IP
    ClientThread clThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        UberManager.getInstance().setMainActivity( this );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        clThread.offline();
        //Put your http calls code here . It always called when your activity close
    }

    //ON CLICK LOGIN
    public void sendMessageLogin(View view) throws IOException {
        Socket socket;
        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
        socket = new Socket(serverAddr, SERVERPORT);
        clThread=new ClientThread(socket);
    }

    //ON CLICK REGISTATION
    public void sendMessageRegg(View view) {
        new Thread(new ReggThread()).start();
    }

    public void gotoR(View view) {
        setContentView(R.layout.chatroom);
    }

    //ON CLICK back from chatroom
    public void back_fromchat(View view){
        setContentView(R.layout.layout2);
        clThread.userlistF();
    }
    public void back_toflist(View view){
        setContentView(R.layout.layfriendslist);
        clThread.friendList();

    }
    //ON CLICK send Message
    public void SendMsgF(View view) {
        EditText textboxB = (EditText) findViewById(R.id.editText2);

        String testo = String.valueOf(textboxB.getText());
        testo = testo.replace("\\","\\\\");
        testo= testo.replace("\"","\\\"");
        clThread.SendMsgF(testo);
    }

    public void SendMsgR(View view) {
        EditText textboxB = (EditText) findViewById(R.id.editText);
        String testo = String.valueOf(textboxB.getText());

        testo = testo.replace("\\","\\\\");
        testo= testo.replace("\"","\\\"");
        clThread.SendMsgR(testo);
    }

    public void friendListClick(View view){
        setContentView(R.layout.layfriendslist);
        clThread.rfriendlist();
    }

    public void touslist_fromflist(View view){
        setContentView(R.layout.layout2);
        clThread.userlistF();
    }

    public void AddFriend(View view) {
    }



    /*******************************************LOGIN **/
    class ClientThread extends Thread {
        message messageb;
        //TextView response;
        public PrintWriter out;
        String response = "";
        public Socket socket;

        String resultx;//FOR LOGIN OR REGGISTRATION ERROR
        // ARRAY LIST
        List<String> userslist = new ArrayList<String>();
        List<String> frqlist = new ArrayList<String>();
        List<friends> friedslist = new ArrayList<friends>();
        List<String> msglist = new ArrayList<String>();

        String username;
        String password;

        public ClientThread(Socket socket_) throws IOException {
            this.socket = socket_;
            out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            this.start();
        }

        /********************************************************
         * CLIENT THREAD RUN MTHD
         ******************************************/
        @Override
        public void run() {
            try {
                //---------------------------collect-Username-And-Password---------------------------
                EditText userBox = (EditText) findViewById(R.id.usernameText);
                EditText pswBox = (EditText) findViewById(R.id.passwordText);
                //TextView responseBox = (TextView) findViewById(R.id.textView);
                username = String.valueOf(userBox.getText());
                password = String.valueOf(pswBox.getText());
                //------------------------------Sending-Whot-type-of-device-are-you---------------------
                out.println("mA");
                //JSON Login send
                out.println("{\"type\":\"login-s\",\"nick\":\"" + username + "\",\"password\":\"" + password + "\"}");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                        1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
                //-----------------WHILE CONNECTION IS OPEN------------------(SESSION)----
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    //READING-Stream
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response = byteArrayOutputStream.toString("UTF-8");
                    String type = "";
                    JSONObject JSmessage = null;
                    //MACKING JSON
                    try {
                        JSmessage = new JSONObject(response);
                        System.out.println("R: "+response);
                        //TAKE TYPE
                        type = JSmessage.getString("type");
                        byteArrayOutputStream.reset();
                    } catch (Exception e) {
                    }

                    //LOGIN RESPONSE FROM SERVER
                    if (type.equals("login-r")) {
                        //if is 1 is logged if is 0 not is logged
                        if (JSmessage.getInt("r") != 0) {
                            resultx = "LOGGED";
                            //launch login UI function
                            loginFunc();
                            //Request LIST OF USER in LOGIN time
                            messageb = new message(username);
                            out.println("{\"type\":\"usersonline\"}");
                            out.println("{\"type\":\"checkFrqst\"}");
                            out.println("{\"type\":\"message-lr\",\"id\":\""+messageb.id+"\"}");

                        } else {
                            resultx = "ERROR LOGIN";
                            loginFunc();
                            socket.close();
                        }
                    }
                    //RECEIVE USER online LIST From Server
                    if (type.equals("users-l")) {
                        JSONArray cast = JSmessage.getJSONArray("users");
                        for (int i = 0; i < cast.length(); i++) {
                            //ADD all users online in list of users online
                            userslist.add(cast.getString(i));
                        }
                        userlistF();
                    }

                    //RECEIVE ADD new user USER online
                    if (type.equals("adduser")) {
                        //ADD NEW USER IN LIST
                        userslist.add(JSmessage.getString("user"));
                        //Launch Func for update list
                        addUserF();
                        for (int i = 0;i<friedslist.size();i++) {
                            if (friedslist.get(i).getName().equals(JSmessage.getString("user"))) {
                                friedslist.get(i).status="1";
                            }
                        }
                        friendList();
                    }

                    //REMOVE USER,When one user go offline , REMOVE USER
                    if (type.equals("rmuser")) {
                        ListIterator usersIterator = userslist.listIterator();
                        String userTRm = JSmessage.getString("user");
                        //Find the user in list and remove IT
                        while (usersIterator.hasNext()) {
                            String a = (String) usersIterator.next();

                            if (a.equals(userTRm)) {
                                usersIterator.remove();
                            }

                        }
                        //Launch Func for update list
                        rmUserF();
                        //ListIterator friendsIterator = friedslist.listIterator();
                        for (int i = 0;i<friedslist.size();i++){
                            if (friedslist.get(i).getName().equals(userTRm)){
                                friedslist.get(i).status="0";
                            }
                        }
                        //update friendlist
                        friendList();
                    }

                    //RECEIVE USER online LIST From Server
                    if (type.equals("friends-l")) {
                        friedslist.clear();
                        JSONArray cast = JSmessage.getJSONArray("users");
                        for (int i = 0; i < cast.length(); i++) {
                            //ADD all users online in list of users online
                            JSONObject splitJS=new JSONObject(cast.getString(i));
                            friedslist.add(new friends(splitJS.getString("user"),splitJS.getString("status"),messageb));
                        }
                        friendList();
                    }

                    if (type.equals("friendRqst-l")) {
                        frqlist.clear();
                        JSONArray cast = JSmessage.getJSONArray("from");
                        if(cast.length()!=0){
                            for (int i = 0; i < cast.length(); i++) {
                                //ADD all FriendRequest to Friendrequest list
                                frqlist.add(cast.getString(i));
                            }
                            newFrqst();
                        }
                    }

                    //MESSAGE RECEIVED
                    if (type.equals("message")) {
                        //ListIterator listIterator = msglist.listIterator();
                        String msg = "";
                        msg += JSmessage.getString("from") + ": ";
                        msg += JSmessage.getString("data");
                        msglist.add(msg);
                        //newmessage();
                        //rmUserF();
                    }
                    //MESSAGE RECEIVED array
                    if (type.equals("newmessage-array")) {
                        JSONArray cast = JSmessage.getJSONArray("messages");
                        for (int i = 0; i < cast.length(); i++) {
                            String buff=cast.getString(i);
                            buff = buff.replace("\\","");
                            //ADD all users online in list of users online
                            messageb.addMessage(buff);
                        }
                        System.out.println("JS TO STRING: "+JSmessage.toString());
                        updatemsglistf();
                        friendList();
                        //messagelistf(JSmessage.getString());
                    }
                    //MESSAGE RECEIVED
                    if (type.equals("newmessage")) {
                        System.out.println("JS TO STRING: "+JSmessage.toString());
                        messageb.addMessage(JSmessage.toString());
                        friendList();
                        updatemsglistf();
                        //messagelistf(JSmessage.getString());

                    }

                    response = "";
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


                                        /********************************************************
                                         ***************** METHODS OF CLIENT ********************
                                         ********************************************************/

        //-----------------------------------------------------LOGIN-------------------------------------------------
        public void loginFunc() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TextView responseBox = (TextView) findViewById(R.id.textView);
                        responseBox.setText(resultx);
                        String a = "";
                        a = resultx;
                        if (a.equals("LOGGED")) {
                            setContentView(R.layout.layout2);

                        }
                    } catch (Exception e) {
                    }

                }
            });
        }

        //---------------------------------------------------USERLIST------------------------------------------------
        public void userlistF() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ListView mylist = (ListView) findViewById(R.id.listView1);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                        mylist.setAdapter(adapter);

                        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {

                                for (int i = 0; i < parent.getChildCount(); i++) {
                                    if (i == position) {
                                        parent.getChildAt(i).setBackgroundColor(Color.GREEN);
                                    } else {
                                        parent.getChildAt(i).setBackgroundColor(Color.WHITE);
                                    }
                                }
                                Button addBtn = (Button) findViewById(R.id.addBtn);
                                addBtn.setVisibility(View.VISIBLE);

                                final String ToName = (String) parent.getItemAtPosition(position);

                                addBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String FRqstJS = "{\"type\":\"friendRqst-in\",\"to\":\"" + ToName + "\"}";

                                        out.println(FRqstJS);

                                        new AlertDialog.Builder(UberManager.getInstance().getMainActivity())
                                                .setTitle("Friend request")
                                                .setMessage("you sended the request to: " + ToName)
                                                .setCancelable(false)
                                                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Whatever...
                                                    }
                                                }).show();
                                    }
                                });
                                //Dialog dialog;

                                System.out.println(ToName);
                            }

                        });


                    } catch (Exception e) {
                    }
                }
            });
        }

        //-------------------------------------------------ADD-NEW-USERS---------------------------------------------
        public void addUserF() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ListView mylist = (ListView) findViewById(R.id.listView1);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                        mylist.setAdapter(adapter);


                    } catch (Exception e) {
                    }
                }
            });
        }

        //---------------------------------------REMOVE USER(When one user go offline)-------------------------------
        public void rmUserF() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        final ListView mylist = (ListView) findViewById(R.id.listView1);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, userslist);
                        mylist.setAdapter(adapter);

                    } catch (Exception e) {
                    }
                }
            });
        }

        //----------------------------------------------NEW FRIEND REQUEST-------------------------------------------
        public void newFrqst() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ListView myfqlist = (ListView) findViewById(R.id.frlistview);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, frqlist);
                        myfqlist.setAdapter(adapter);
                        //inizialize BUTTON for show the new Friend Rqst received
                        final ImageButton newFBUTTON = (ImageButton) findViewById(R.id.newFrB);
                        newFBUTTON.setVisibility(View.VISIBLE);
                        System.out.print("deb");

                        //ON CLICK BUTTON FOR VIEW LIST
                        newFBUTTON.setOnClickListener(new View.OnClickListener() {
                            boolean openL=false;
                            @Override
                            public void onClick(View v) {
                                final LinearLayout lineFQ = (LinearLayout) findViewById(R.id.linearforFQ);
                                //SET VISIBLE OR NOT LINEAR LAYOUT
                                if(openL){
                                    lineFQ.setVisibility(View.GONE);
                                    openL=false;
                                }else{
                                    lineFQ.setVisibility(View.VISIBLE);
                                    openL=true;
                                }

                                //ON CLICK ITEM
                                myfqlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> parent, View view,
                                                            int position, long id) {

                                        for (int i = 0; i < parent.getChildCount(); i++) {
                                            if (i == position) {
                                                parent.getChildAt(i).setBackgroundColor(Color.CYAN);
                                            } else {
                                                parent.getChildAt(i).setBackgroundColor(Color.WHITE);
                                            }
                                        }

                                        //final String ToName = (String) parent.getItemAtPosition(position);
                                        final String ToName = (String) parent.getItemAtPosition(position);

                                        //DIALOGO PER ACCETTARE LA RICHIESTA
                                                new AlertDialog.Builder(UberManager.getInstance().getMainActivity())
                                                        .setTitle("Friend request")
                                                        .setMessage("do you want accept\nthis request of friendship?")
                                                        .setCancelable(false)
                                                        .setPositiveButton("Accept", new DialogInterface.OnClickListener()
                                                        {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                String BuffDmsg = "{\"type\":\"friendRqst-rs\",\"user\":\"" + ToName + "\",\"rs\":1}";
                                                                out.println(BuffDmsg);

                                                                ListIterator listIterator = frqlist.listIterator();
                                                                String userTRm = ToName;
                                                                //Find the user in list and remove IT
                                                                while (listIterator.hasNext()) {
                                                                    String a = (String) listIterator.next();
                                                                    if (a.equals(ToName)) {
                                                                        listIterator.remove();
                                                                    }
                                                                }
                                                                final ListView myfqlist = (ListView) findViewById(R.id.frlistview);
                                                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, frqlist);
                                                                myfqlist.setAdapter(adapter);
                                                                if(frqlist.isEmpty()){
                                                                    lineFQ.setVisibility(View.GONE);
                                                                    newFBUTTON.setVisibility(View.GONE);
                                                                }

                                                            }
                                                        })
                                                        .setNegativeButton("Refuses", new DialogInterface.OnClickListener(){
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                String BuffDmsg = "{\"type\":\"friendRqst-rs\",\"user\":\"" + ToName + "\",\"rs\":2}";
                                                                out.println(BuffDmsg);

                                                                myfqlist.setAdapter(adapter);
                                                                ListIterator listIterator = frqlist.listIterator();
                                                                String userTRm = ToName;
                                                                //Find the user in list and remove IT
                                                                while (listIterator.hasNext()) {
                                                                    String a = (String) listIterator.next();
                                                                    if (a.equals(ToName)) {
                                                                        listIterator.remove();
                                                                    }
                                                                }
                                                                final ListView myfqlist = (ListView) findViewById(R.id.frlistview);
                                                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, frqlist);
                                                                myfqlist.setAdapter(adapter);
                                                                if(frqlist.isEmpty()){
                                                                    lineFQ.setVisibility(View.GONE);
                                                                    newFBUTTON.setVisibility(View.GONE);
                                                                }
                                                            }
                                                        }).show();

                                        }
                                    });
                                        //Dialog dialog;
                                }

                        });

                    } catch (Exception e) {}
                }
            });
        }

        //-------------------------------------------------FRIEND LIST ----------------------------------------------
        public void friendList(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        final ListView mylist = (ListView) findViewById(R.id.friendslistview);
                        //ListView listView = (ListView)findViewById(R.id.listView);
                        FriendTextViewAdapter FriendTextViewAdapter = new FriendTextViewAdapter(MainActivity.this, R.layout.friend_text_view_layout, friedslist);
                        //final ArrayAdapter<friends> adapter = new ArrayAdapter<friends>(MainActivity.this, R.layout.friend_text_view_layout, friedslist);
                        mylist.setAdapter(FriendTextViewAdapter);

                        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                               // Button addBtn = (Button) findViewById(R.id.addBtn);
                                //addBtn.setVisibility(View.VISIBLE);
                                final friends friend = (friends) parent.getItemAtPosition(position);
                                String ToName= friend.getName();
                                messageb.setRead(ToName);
                                messagelistf(ToName);
                                //Dialog dialog;
                                System.out.println(ToName);
                            }

                        });


                    } catch (Exception e) {
                    }
                }
            });
        }

        //------------------------------------------------MESSAGE LIST-----------------------------------------------
        String userbox="";
        public void messagelistf(String user_){
            final String user = user_;
            userbox=user;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setContentView(R.layout.layfriendmsg);
                        String[] messagelist;
                        messagelist=messageb.getmessage(userbox);

                        System.out.println(messagelist[0]);
                        //setContentView(R.layout.layfriendmsg);
                        List<String> msgflist = new ArrayList<String>();

                        for (String item: messagelist) {
                            try {
                                String messageCast;
                                JSONObject JSmex=new JSONObject(item);
                                messageCast=JSmex.getString("from")+": "+JSmex.getString("data");
                                msgflist.add(messageCast);
                            }catch(Exception e){}
                        }

                        final ListView mylistm = (ListView) findViewById(R.id.messagefriendview);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, msgflist);
                        mylistm.setAdapter(adapter);

                    } catch (Exception e) {
                    }
                }
            });
        }

        //--------------------------------------------UPDATE MSG LIST-FRIEND-----------------------------------------
        public void updatemsglistf(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //setContentView(R.layout.layfriendmsg);
                        friendList();
                        String[] messagelist;
                        messagelist=messageb.getmessage(userbox);
                        //setContentView(R.layout.layfriendmsg);
                        List<String> msgflist = new ArrayList<String>();

                        for (String item: messagelist) {
                            try {
                                String messageCast;
                                JSONObject JSmex=new JSONObject(item);
                                messageCast=JSmex.getString("from")+": "+JSmex.getString("data");
                                msgflist.add(messageCast);
                            }catch(Exception e){}
                        }

                        final ListView mylistm = (ListView) findViewById(R.id.messagefriendview);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, msgflist);
                        mylistm.setAdapter(adapter);
            } catch (Exception e) {
            }
        }
        });
        }

        //------------------------------------------SENDING MESSAGE TO FRIEND----------------------------------------
        public void SendMsgF(String testo){

            String FRqstJS = "{\"type\":\"messagef\",\"to\":\"" + userbox + "\",\"from\":\"" + username + "\",\"data\":\"" + testo + "\"}";
            out.println(FRqstJS);

            messagelistf(userbox);
        }

        //------------------------------------------SENDING MESSAGE TO CHATROOM--------------------------------------
        public void SendMsgR(String testo){

            String FRqstJS = "{\"type\":\"message\",\"from\":\"" + username + "\",\"data\":\"" + testo + "\"}";
            out.println(FRqstJS);
        }

        //-------------------------------------------LIST OF FRIEND REQUEST------------------------------------------
        public void rfriendlist(){
            out.println("{\"type\":\"friendsonline\"}");

        }

        //----------------------------------------------WHEN GO OFFLINE----------------------------------------------
        public void offline()
        {
            out.println("{\"type\":\"offline\"}");
        }


    }

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\



/*************************************************************************************************   REGISTRATION   **/
    class ReggThread implements Runnable {

        PrintWriter out;
        String response="";

        public Socket socket;
        String resultx;
        public void SetResutX ()
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView responseBox = (TextView) findViewById(R.id.textView);
                    responseBox.setText(resultx);

                }
            });
        }


        @Override
        public void run() {
            try {

                EditText userBox = (EditText) findViewById(R.id.usernameText);
                EditText pswBox = (EditText) findViewById(R.id.passwordText);
                //TextView responseBox = (TextView) findViewById(R.id.textView);

                String username= String.valueOf(userBox.getText());
                String password= String.valueOf(pswBox.getText());

                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                this.socket = new Socket(serverAddr, SERVERPORT);
                this.out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                out.println("mA");
                //JSON Login Rqst
                out.println("{\"type\":\"regg-s\",\"nick\":\""+username+"\",\"password\":\""+password+"\"}");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                        1024);
                byte[] buffer = new byte[1024];
                int bytesRead;
                InputStream inputStream = socket.getInputStream();
			/*
             * notice: inputStream.read() will block if no data return
			 */
                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response += byteArrayOutputStream.toString("UTF-8");
                    String type="";
                    JSONObject JSmessage = null;

                    try {
                        JSmessage = new JSONObject(response);
                        type= JSmessage.getString("type");
                    }catch (Exception e){}

                    if(type.equals("regg-r"))
                    {
                        if(JSmessage.getInt("r")!=0)
                        {
                            resultx="Registered Successfully\n Now you can login";
                            SetResutX();
                            socket.close();
                        }else{
                            resultx="REGISTRATION ERROR";
                            SetResutX();
                            socket.close();
                        }
                    }

                    response="";
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}