/*
 * 
 * Created by Diego Portoghese
 * 
 */
package chatserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;
import org.json.*;

public class Chatserver {
    //array of clients (for broadcast the message)
    static List<connect> connects = Collections.<connect>synchronizedList(new ArrayList<connect>());

    public static class connect extends Thread{
	// dichiarazione delle variabili socket e degli Stream
	Socket client;
        OutputStream out;
        InputStream in ;
        //class websocket for read and send messages to client in Websocket
        websocket ClientWB;
        BufferedReader inBF;
        PrintStream outBF;
        String device;
        user utente = null;
        
        //CONNECT
	public connect(Socket client) throws IOException, NoSuchAlgorithmException{
            //inizialize Buffer in e Stream Out
            inBF = new BufferedReader(new InputStreamReader(client.getInputStream()));
            outBF = new PrintStream(client.getOutputStream(), true);
            
            System.out.println("new client IN");
            

            //set socket
            this.client = client;
            //set Stream of the socket
            this.in =  client.getInputStream();
            this.out = client.getOutputStream();
            //MYCLASS WEBSOCKET
            
            this.ClientWB = new websocket(in,out);

            // invoca automaticamente il metodo run() //start
            this.start();
            System.out.println("thread start");
	}
        final void streamout(String stringa) throws IOException{
            SendMsg(stringa);
            
        }
        final void checkFRqst() throws IOException, JSONException{
                        // LIST OF FRIEND REQUEST
            String FQStringJSON="{\"type\":\"friendRqst-l\",\"from\":[ ";
            String[] frqstlist;
            frqstlist=utente.CheckNewRqst();
            int k;
            for(k=0;k<frqstlist.length;k++){
                System.out.println(frqstlist[k]);
                FQStringJSON+="\""+frqstlist[k]+"\"";
                if((k+1)==frqstlist.length){
                }else{
                    FQStringJSON+=", ";
                }
            }
            FQStringJSON+=" ]}";
            //SEND TO CLIENT List of User
            JSONObject obj2 = new JSONObject(FQStringJSON);
            SendMsg(obj2.toString());

            System.out.println(FQStringJSON);
        }
        
        final void updateflist() throws JSONException, IOException{
        
            String[] users;
            users= utente.FriendsList();
            //**"cars":[ "Ford", "BMW", "Fiat" ] **/
            String UsersStringJSON="{\"type\":\"friends-l\",\"users\":[ ";
            //FOR USER ONLINE PUT IN THE JSON STRING THE USER
            int i;
            for(i=0;i<users.length;i++){
                System.out.println(users[i]);
                UsersStringJSON+=""+users[i]+"";
                if((i+1)==users.length){    

                }else{
                    UsersStringJSON+=", ";
                }
            }
            UsersStringJSON+=" ]}";
            System.out.println(UsersStringJSON);
            //SEND TO CLIENT List of FRIENDS
            JSONObject obj = new JSONObject(UsersStringJSON);
            SendMsg(obj.toString());
        }
        
        final void SendMsg(String message) throws IOException{
            if(device.equals("mA")){
                outBF.println(message);
            }else{

                //System.out.println("FOR WEB");

                ClientWB.SendMessage(message);

            }
        }

        //CLIENT RUN
	public void run()
	{
            try{
                //Init
                //multiplattform IF
                //HANDSHAKING (the wbsocket need this for life)
                System.out.println("try handshake");
               // String datas=inBF.readLine();
                if(ClientWB.HandShake()!=0){
                    System.out.println("websocket device");
                    device="websocket"; // for javascript
                }else{
                    device="mA";
                    System.out.println(device);
                    //for android    
                }
                //inizializzazione 
                int userstatus=0;
                System.out.println("While");
                //Reading message from client
                while (true){
                    String message; //message Buffer
                    
                    if(device.equals("mA")){
                        message=inBF.readLine();
                        System.out.println(message);
                    }else{
                        message=ClientWB.GetMessage();
                        System.out.println(message);
                    }
                    System.out.println(message);
                    
                    //                           READING DATA
                    //STRING TO JSON OBJECT
                    JSONObject messJson;
                    String type;
                    try{
                        messJson = new JSONObject(message);
                        type=messJson.getString("type");              //TYPE FROM JSON DATA
                        System.out.println("Request Type: "+type);
                    }catch (Exception e){break;}
                   
                    // what type of request?
                    if (type != null){
                       
                    /**------------------------------LOGIN-----------------------------------**/
                    if(type.equals("login-s")){
                        //NICK AND PSW
                        String nick = messJson.getString("nick");
                        String pass = messJson.getString("password");
                        System.out.println(nick);
                        //setup user
                        utente = new user(nick,pass);
                        if (utente.login() != 0){
                            System.out.println("User: \""+ nick + "\" Connected ");
                            userstatus = 1;
                            JSONObject obj = new JSONObject("{\"type\":\"login-r\",\"r\":1}");
                            SendMsg(obj.toString());
                            String msgB = "{\"type\":\"adduser\",\"user\":\""+utente.username+"\"}";
                            //ALLERT ALL CLIENTS
                            for (connect conn : connects) {
                                try{
                                    if(conn == this){}else{conn.streamout(msgB);}
                                }catch(Exception e){ }
                            }
                        }else{
                            JSONObject obj = new JSONObject("{\"type\":\"login-r\",\"r\":0}");
                            
                            SendMsg(obj.toString());
                            client.close();
                            break;
                        }
                    }
                    
                    /**---------------------------------- REGISTRATION------------------------------**/
                    if(type.equals("regg-s")){
                        // Extract Username and PSW from Json String
                        String nick = messJson.getString("nick");
                        String pass = messJson.getString("password");
                        //make new user obj
                        utente = new user(nick,pass);
                        //Check if registration is possible
                        int result = utente.registration();
                        if(result!= 0){
                            //user created
                            System.out.println("User: \""+ nick + "\" Registered");
                            JSONObject obj = new JSONObject("{\"type\":\"regg-r\",\"r\":"+result+"}");
                            SendMsg(obj.toString());
                        }else{
                            //error in registration
                            JSONObject obj = new JSONObject("{\"type\":\"regg-r\",\"r\":"+result+"}");
                            SendMsg(obj.toString());
                        }
                        //break while for closing connection 
                        break;
                    }

                                                        //** USER ONLINE LIST **/
                    if(type.equals("usersonline")){
                        //array of users online
                        String[] users;
                        users= utente.usersonline();
                        String UsersStringJSON="{\"type\":\"users-l\",\"users\":[ ";
                        //FOR USER ONLINE PUT IN THE JSON STRING THE USER
                        int i;
                        for(i=0;i<users.length;i++){
                            System.out.println(users[i]);
                            UsersStringJSON+="\""+users[i]+"\"";
                            if((i+1)==users.length){    
                                
                            }else{
                                UsersStringJSON+=", ";
                            }
                        }
                        UsersStringJSON+=" ]}";
                        //SEND TO CLIENT List of User
                        JSONObject obj = new JSONObject(UsersStringJSON);
                        SendMsg(obj.toString());
                        
                        System.out.println(UsersStringJSON);
                        
                    }
                    
                    //** FRIENDS ONLINE LIST **/
                    if(type.equals("friendsonline")){
                        //array of users online
                        String[] users;
                        users= utente.FriendsList();
                        //**"cars":[ "Ford", "BMW", "Fiat" ] **/
                        String UsersStringJSON="{\"type\":\"friends-l\",\"users\":[ ";
                        //FOR USER ONLINE PUT IN THE JSON STRING THE USER
                        int i;
                        for(i=0;i<users.length;i++){
                            System.out.println(users[i]);
                            UsersStringJSON+=""+users[i]+"";
                            if((i+1)==users.length){    
                                
                            }else{
                                UsersStringJSON+=", ";
                            }
                        }
                        UsersStringJSON+=" ]}";
                        System.out.println(UsersStringJSON);
                        //SEND TO CLIENT List of FRIENDS
                        JSONObject obj = new JSONObject(UsersStringJSON);
                        SendMsg(obj.toString());
                        
                        System.out.println(UsersStringJSON);
                        
                    }
                    
                    //check new friend rqst
                    if(type.equals("checkFrqst")){
                        // LIST OF FRIEND REQUEST
                        String FQStringJSON="{\"type\":\"friendRqst-l\",\"from\":[ ";
                        String[] frqstlist;
                        frqstlist=utente.CheckNewRqst();
                        
                        for(int k=0;k<frqstlist.length;k++){
                            System.out.println(frqstlist[k]);
                            FQStringJSON+="\""+frqstlist[k]+"\"";
                            if((k+1)==frqstlist.length){    
                                
                            }else{
                                FQStringJSON+=", ";
                            }
                        }
                        FQStringJSON+=" ]}";
                        //SEND TO CLIENT List of User
                        JSONObject obj2 = new JSONObject(FQStringJSON);
                        SendMsg(obj2.toString());
                        
                        System.out.println(FQStringJSON);
                    }
                    
                    // request for add one friend 
                    /**
                     type:friendRqst-in
                     userf:Fnick
                     to:Tnick 
                     **/
                    if(type.equals("friendRqst-in")){
                        String ToN=messJson.getString("to");
                        int risultato;
                        risultato=utente.friendAddRqst(ToN);
                        for (connect conn : connects) {
                                try{
                                    if(conn == this){}else{conn.checkFRqst();}
                                }catch(Exception e){ }
                            }
                    }
                    // response of client for one friend request received
                    if(type.equals("friendRqst-rs")){
                        //if is 1 ACCEPT if is 2 REFUSES if is 3 I DNT KNOW                        
                        utente.FrRqResponse(messJson.getString("user"),messJson.getInt("rs"));
                        for (connect conn : connects) {
                            try{
                                conn.updateflist();
                            }catch(Exception e){ }
                        }
                        
                    }
                    
                    
                    //OFFLINE STATUS
                    if(type.equals("offline")){
                        //write in the DB the user is offline
                        System.out.println("OFFLINE RECEIVED FROM "+utente.username);                        
                        utente.offline();
                        userstatus =0;
                        //ALLERT ALL CLIENTS
                        String msgB = "{\"type\":\"rmuser\",\"user\":\""+utente.username+"\"}";
                        //broadcast one user OFFLINE to all 
                        for (connect conn : connects) {
                            try{
                                conn.streamout(msgB);
                                }catch(Exception e){ System.out.println(e);}
                        }
                        client.close();
                        break;
                    }
                    
                    //Messaggio
                    if(type.equals("message")){
                        String castmsg = messJson.getString("data");
                        castmsg = castmsg.replace("\\","\\\\");
                        castmsg = castmsg.replace("\"","\\\"");
                        
                        String msgB="{\"type\":\"message\",\"from\":\""+utente.username+"\",\"data\":\""+castmsg+"\"}";
                        System.out.println("msg "+msgB);
                        //broadcast the message to all
                        for (connect connect : connects) {
                            try{
                            connect.streamout(msgB);
                            //conn.streamout();
                            }catch(Exception e){}
                        }
                        //System.out.println(lastmsg);
                    }
                    
                                        //Messaggio
                    if(type.equals("messagef")){
                        String castmsg = messJson.getString("data");
                        String ToN = messJson.getString("to");
                        castmsg = castmsg.replace("\\","\\\\");
                        castmsg = castmsg.replace("\"","\\\"");
                        int id=utente.savemessage(castmsg,ToN);
                        
                        String msgB="{\"type\":\"newmessage\",\"id\":\""+id+"\",\"from\":\""+utente.username+"\",\"to\":\""+ToN+"\",\"data\":\""+castmsg+"\"}";
                        
                        //broadcast the message to all
                        for (connect connect : connects) {
                            try{
                                if(connect.utente.username.equals(ToN)){
                                    System.out.println("msg "+msgB);
                                    connect.streamout(msgB);
                                }
                                    if(connect.utente.username.equals(utente.username)){
                                        System.out.println("2msg "+msgB);
                                    connect.streamout(msgB);}
                            //conn.streamout();
                            }catch(Exception e){}
                        }
                    }
                    
                    //message list request
                    if(type.equals("message-lr")){
                        
                        int id = Integer.parseInt(messJson.getString("id"));
                        String FQStringJSON="{\"type\":\"newmessage-array\",\"messages\":[ ";
                        String[] messagelistbuff;
                        messagelistbuff=utente.messagelist(id);
                        //tlist=utente.CheckNewRqst();
                        int k;
                        for(k=0;k<messagelistbuff.length;k++){
                            System.out.println("arraymessage+: "+messagelistbuff[k]);
                            JSONObject splitJS = new JSONObject(messagelistbuff[k]);
                            //System.out.println(frqstlist[k]);
                            String msgB="{\"type\":\"newmessage-array\",\"id\":\""+splitJS.getString("id")+"\",\"from\":\""+splitJS.getString("from")+"\",\"to\":\""+splitJS.getString("to")+"\",\"data\":\""+splitJS.getString("data")+"\"}";
                            String castmsg = msgB.replace("\\","\\\\");
                            castmsg = castmsg.replace("\"","\\\"");
                            
                            FQStringJSON+="\""+castmsg+"\"";
                            if((k+1)==messagelistbuff.length){    
                                
                            }else{
                                FQStringJSON+=", ";
                            }
                        }
                        FQStringJSON+=" ]}";
                        
                        System.out.println(FQStringJSON);
                        
                        //SEND TO CLIENT List of User
                        JSONObject obj2 = new JSONObject(FQStringJSON);
                        SendMsg(obj2.toString());
                        
                    }
                    
                    
                   }else {break;}
                }
                    
                //System.out.println("message: "+message);
                
                //if User Crash or not press DISCONNECT on client put offline this user
                //if client is one user online
                System.out.println("userstatus: "+userstatus);
                if(userstatus !=0){
                    utente.offline();
                    System.out.println("user: "+utente.username+" go offline!");
                    userstatus =0;
                        //ALLERT ALL CLIENTS
                        String msgB = "{\"type\":\"rmuser\",\"user\":\""+utente.username+"\"}";
                        for (connect conn : connects) {
                            try{
                                conn.streamout(msgB);
                                }catch(Exception e){ System.out.println(e);}
                                //conn.streamout();
                        }
                }
                
                System.out.println("one Clict OUT!");
                // chiusura del socket
                in.close();
                out.close();
                client.close();
		}
            catch(Exception e){
                    e.printStackTrace();
            }
        }
    }
    
    
    //--------------------------------------------MAIN----------------------------------------------------
    public static void main(String[] args) {
        try{
            ServerSocket server = new ServerSocket(5552);
            // ciclo infinito, in attesa di connessioni
            while(true)
            {
                // chiamata bloccante, in attesa di una nuova connessione
                Socket client = server.accept();
                // la nuova richiesta viene gestita da un thread indipendente, si ripete il ciclo
                connect nuovaConnessione = new connect(client);
                // add for broadcast functions
                connects.add(nuovaConnessione);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
