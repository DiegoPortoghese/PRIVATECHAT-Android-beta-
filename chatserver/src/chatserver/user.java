/*
 * 
 * Created by Diego Portoghese
 * 
 */
package chatserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**         
 * TABELLA DEL DATABASE UTENTI
 * 
CREATE TABLE `UTENTI` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `email` varchar(50) NOT NULL,          //EMAIL NOW NOT is USED but IN THE FUTURE I don't KNOW
  `status` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/
/**         
 * TABELLA DEL DATABASE FriendsRqst
 * 
CREATE TABLE `FriendsRqst` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `from` varchar(50) NOT NULL,
  `to` varchar(50) NOT NULL,
  `accept` tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/

/**         
 * TABELLA DEL DATABASE FriendList
 * 

CREATE TABLE `FriendsRqst` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nick` varchar(50) NOT NULL,
  `FLsize` int(3) unsigned NOT NULL,
  `FList` varchar(300) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/

public class user {
    
    public String username;
    private String password;
    private String email;
    private int id = 0;
    
    private String connectionString="jdbc:mysql://"
            +"localhost:3306/" //host
            +"database" //db
            +"?user=userdb" //id
            +"&password=passworddb" //psw
            +"&useSSL=True"; //ssl connection = true
    
    //COSTRUCTOR
    public user(String username_,String Password_){
            username=username_;
            password=Password_;
    }
    
    //-------------------------------------LOGIN-QUERY---------------------------------------
    public int login(){
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            // Query SEARCH USER WITH PASSWORD
            ResultSet chs = ch.executeQuery("select * from utenti");
            
            PreparedStatement prepar = connection.prepareStatement("select * from utenti where username=? and password=? and status=0;");
            prepar.setString(1, username);
            prepar.setString(2, password);
            ResultSet updateST_e = prepar.executeQuery();
            
            
            //PreparedStatement prepared = connection.prepareStatement("UPDATE utenti SET status=1 WHERE id=?");

            int found = 0;
            while (chs.next()) {
                
                int idr=chs.getInt("id");
                String username_=chs.getString("username");
                String password_=chs.getString("password");
                int status=chs.getInt("status");
                //System.out.println("User: "+ username_ + " "+password_+ "  id :"+ idr);
                //s
                if(username_.equals(username) && password_.equals(password) ){
                    //remember ID
                    id=idr;
                    System.out.println("User: "+ username + " Logged "+ "id :"+ id);
                    found =1 ;
                }else{
                    //user not found or password is incorrect
                    found = 0;
                }
            }
            if(updateST_e.next()==true){
                System.out.print("LOGIN");
            }else{
                return 0;
            }
            //check if the query find the user if not (found=0) return 0;
            //if(found==0){return 0;} // se non lo trova (quindi found rimane 0) RETURN 0

            /** SET STATUS TO 1 (is online) **/
            PreparedStatement prepared = connection.prepareStatement("UPDATE utenti SET status=1 WHERE id=?");
            prepared.setInt(1, id);
            //ex query
            prepared.executeUpdate();

            // Gestione degli errori
            }catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                try {
                    if (connection != null)
                        connection.close();
                } catch (SQLException e) {
                    // gestione errore in chiusura
                }
        }
        return 1;
    }
    
    //------------------------------ONLINE_USER_DB_RQST---------------------------------
    public String[] usersonline(){
        //array of user
        ArrayList list = new ArrayList();
        String[] strArray=null;
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            //coonessione al DB
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from utenti where status=1 and username!=\""+username+"\"");
            while (chs.next()) {
                String user= chs.getString("username");
                list.add(user); 
            }
            strArray = new String[ list.size() ];
            
            for( int j = 0; j < strArray.length; j++ )
                strArray[ j ] = list.get( j ).toString();
            
            } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return strArray;
    }
    
    //---------------------------REGISTRATION-QUERY----------------------------
    public int registration(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            //connessione al DB
            connection = DriverManager.getConnection(connectionString);
            
            //Quering if the username already exist
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from utenti");
            while (chs.next()) {
                    if(chs.getString("username").equals(username))
                    {
                        System.out.println("ERRORE:"+chs.getString("username"));
                        return 0;
                    }else{
                        
                        }
            }
            
            PreparedStatement prepared = connection.prepareStatement("insert into utenti (username, password, email, status) values (?,?,?,?)");
            prepared.setString(1, username);
            prepared.setString(2, password);
            prepared.setString(3, "");
            prepared.setInt(4, 0);
            //ex query for add the user
            int updateST_done = prepared.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
    return 1;
    }
    
    //------------------------------SET-OFFLINE----------------------
    public void offline(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
        connection = DriverManager.getConnection(connectionString);
        
        /** SET STATUS TO 0 (is offline) **/
        PreparedStatement updateST = connection.prepareStatement("update`utenti` set `status` = '0'  where `username` = '"+username+"'");
        int updateST_done = updateST.executeUpdate();
        
        }catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
    }
   
    //-------------------------------------FRIEND-REQUEST-QUERY---------------------------------------
    public int friendAddRqst(String Nto_){
        String Nto=Nto_;
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            //connessione al DB
            connection = DriverManager.getConnection(connectionString);
           
            //Quering if the FriendsRqst already exist
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("SELECT * FROM `friendsrqst` WHERE `userf` = \""+username+"\" AND `to` =\""+Nto+"\"");
            Statement chx = connection.createStatement();
            ResultSet chsx = chx.executeQuery("SELECT * FROM `friendsrqst` WHERE `userf` = \""+Nto+"\" AND `to` =\""+username+"\"");
            if (chs.next() || chsx.next()) {
                return 0;
            }
            
            
            String query = "insert into friendsrqst (userf,`to`, `accept`)"+ " values (?, ?, ?)";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString (1, username);
            preparedStmt.setString (2, Nto);
            int x= 0;
            preparedStmt.setInt   (3, x);
            preparedStmt.execute(); 
                  // execute the preparedstatement
              
            //PreparedStatement pfrepared = connection.prepareStatement("insert into utenti (username, password, email, status) values (?,?,?,?)");
            /**PreparedStatement prepared = connection.prepareStatement("insert into friendsrqst (from, to, accept) values (?,?,?)");
            
            prepared.setString(1, username);
            prepared.setString(2, Nto);
            prepared.setInt(3, 0);
            //ex query for add the FriendRqst
            int updateST_done = prepared.executeUpdate();
            **/
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return 1;
    }
    
    //-------------------------------------FRIEND-CHECK-RQUEST-QUERY---------------------------------------
    public String[] CheckNewRqst(){
        //array of user
        ArrayList list = new ArrayList();
        String[] strArray=null;
        //driver
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("SELECT * FROM `friendsrqst` WHERE `to` =\""+username+"\" AND `accept` =0");
            while (chs.next()) {
                String user= chs.getString("userf");
                list.add(user);
            }
            strArray = new String[ list.size() ];
            
            for( int j = 0; j < strArray.length; j++ )
                strArray[ j ] = list.get( j ).toString();
            

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return strArray;
    }

/**         
 * TABELLA DEL DATABASE FriendList
 * 
CREATE TABLE `FriendsList` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nick` varchar(50) NOT NULL,
  `FLsize` int(3) unsigned NOT NULL,
  `FList` varchar(300) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/
//-------------------------------------FRIEND-LIST-QUERY---------------------------------------
    public String[] FriendsList(){
        //String Nto=Nto_;
        ArrayList list = new ArrayList();
        String[] strArray=null;
        //driver
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection connection = null;
        try {
            //connessione al DB
            connection = DriverManager.getConnection(connectionString);
           
            //Quering if the username already exist
                Statement ch = connection.createStatement();
                ResultSet chs = ch.executeQuery("select * from friendslist");
                while (chs.next()) {
                        if(chs.getString("nick").equals(username)){
                            //receive FLIST
                            String FList=chs.getString("FList");
                            //split FLIST for character , inside one array
                            String[] array = FList.split(",");
          
                            System.out.println(array[0]);
                            
                            for(int i=0;i<array.length;i++){
                                Statement chy = connection.createStatement();
                                ResultSet chsy = chy.executeQuery("select * from utenti where status=1 and username=\""+array[i]+"\"");
                                while (chsy.next()) {
                                    String user= chsy.getString("username");
                                    list.add("{\"user\":\""+user+"\",\"status\":\"1\"}");
                                }
                            }
                            
                            for(int i=0;i<array.length;i++){
                                Statement chy = connection.createStatement();
                                ResultSet chsy = chy.executeQuery("select * from utenti where status=0 and username=\""+array[i]+"\"");
                                while (chsy.next()) {
                                    String user= chsy.getString("username");
                                    list.add("{\"user\":\""+user+"\",\"status\":\"0\"}");
                                }
                            }
                            
                            
                            
                            strArray = new String[ list.size() ];
                            
                            for( int j = 0; j < strArray.length; j++ ){
                                strArray[ j ] = list.get( j ).toString();
                                System.out.println(strArray[j]);
                            }
                            return strArray;
                            //System.out.println(FList);
                            //FLIST = "utente1,utente2,utente3,utente4"
                            
                            //array = append(array, "4");
                        }else{}
                }
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return strArray;
    }
    
    
/**         
 * TABELLA DEL DATABASE FriendList
 * 
CREATE TABLE `FriendsList` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nick` varchar(50) NOT NULL,
  `FLsize` int(3) unsigned NOT NULL,
  `FList` varchar(300) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

**/
//----------------------------QUERY- RESPONSE OF ONE REQUEST OF FRIEDSHIP---------------------------
    public int FrRqResponse(String user_, int rs_){
        String user=user_;
        int rs=rs_;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            
            //connessione al DB
            connection = DriverManager.getConnection(connectionString);
            if(rs!=1)
            {
                PreparedStatement updateST = connection.prepareStatement("update`friendsrqst` set `accept` = '2'  where `to` = '"+username+"'");
                int updateST_done = updateST.executeUpdate();
                return 0;
            }else{
            
                PreparedStatement updateST = connection.prepareStatement("update`friendsrqst` set `accept` = '1'  where `to` = '"+username+"'");
                int updateST_done = updateST.executeUpdate();
            
                
                //if
                int update=0;
                int update2=0;
                //Quering if the username already exist
                Statement ch = connection.createStatement();
                ResultSet chs = ch.executeQuery("select * from friendslist");
                while (chs.next()) {
                        if(chs.getString("nick").equals(username))
                        {
                            //increment List SIZE
                            int FLsize=chs.getInt("FLsize");
                            FLsize= FLsize+1;
                            //receive FLIST
                            String FList=chs.getString("FList");
                            //split FLIST for character , inside one array
                            String[] array = FList.split(",");
                            //add again the character , inside array element
                            for (int i=0;i<array.length;i++){
                                array[i]=array[i]+",";
                            }
                            //array to list, and add the new user(FRIEND) in the list 
                            ArrayList list = new ArrayList();
                            Collections.addAll(list, array);
                            list.add(user);
                            //with another array, get all element of the list inside this array
                            String[] strArray = new String[ list.size() ];
                            for( int j = 0; j < strArray.length; j++ )
                                strArray[ j ] = list.get( j ).toString();
                            
                            //clear FList String
                            FList="";
                            //put inside FList all element of array
                            for(int i=0;i<strArray.length;i++){
                                FList=FList+strArray[i];
                            }
                            System.out.println(FList);
                            //FLIST = "utente1,utente2,utente3,utente4"
                            
                            //array = append(array, "4");
                            PreparedStatement updateFl = connection.prepareStatement("update`friendslist` set `FLsize` = '"+FLsize+"',`FList`='"+FList+"'  where `nick` = '"+username+"'");
                            int updateFL_done = updateFl.executeUpdate();
                            
                            update= 1;
                        }else{}
                        if(chs.getString("nick").equals(user))
                        {
                            //increment List SIZE
                            int FLsize=chs.getInt("FLsize");
                            FLsize= FLsize+1;
                            //receive FLIST
                            String FList=chs.getString("FList");
                            //split FLIST for character , inside one array
                            String[] array = FList.split(",");
                            //add again the character , inside array element
                            for (int i=0;i<array.length;i++){
                                array[i]=array[i]+",";
                            }
                            //array to list, and add the new user(FRIEND) in the list 
                            ArrayList list = new ArrayList();
                            Collections.addAll(list, array);
                            list.add(username);
                            //with another array, get all element of the list inside this array
                            String[] strArray = new String[ list.size() ];
                            for( int j = 0; j < strArray.length; j++ )
                                strArray[ j ] = list.get( j ).toString();
                            
                            //clear FList String
                            FList="";
                            //put inside FList all element of array
                            for(int i=0;i<strArray.length;i++){
                                FList=FList+strArray[i];
                            }
                            System.out.println(FList);
                            //FLIST = "utente1,utente2,utente3,utente4"
                            
                            //array = append(array, "4");
                            PreparedStatement updateFl = connection.prepareStatement("update`friendslist` set `FLsize` = '"+FLsize+"',`FList`='"+FList+"'  where `nick` = '"+user+"'");
                            int updateFL_done = updateFl.executeUpdate();
                            
                            update2= 1;
                        }else{}
                }
                if(update!=1){
                //inizializza nel DB la lista di amici di tale utente
                PreparedStatement prepared = connection.prepareStatement("insert into friendslist (`nick`, `FLsize`, `FList`) values (?,?,?)");
                prepared.setString(1, username);
                prepared.setInt(2, 1);
                prepared.setString(3, user);
                //ex query for add the user
                int updateFL_done = prepared.executeUpdate();
                }
                if(update2!=1)
                {
                    //inizializza nel DB la lista di amici di tale utente
                    PreparedStatement prepared2 = connection.prepareStatement("insert into friendslist (`nick`, `FLsize`, `FList`) values (?,?,?)");
                    prepared2.setString(1, user);
                    prepared2.setInt(2, 1);
                    prepared2.setString(3, username);
                    //ex query for add the user
                    int updateFL2_done = prepared2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
    return 1;
    }
    /**
    CREATE TABLE `messagelist` (
  `id` int(10) UNSIGNED NOT NULL,
  `nfrom` varchar(50) NOT NULL,
  `nto` varchar(50) NOT NULL,
  `data` varchar(50) NOT NULL,
  `timeS` varchar(50) NOT NULL,
  `timeRc` varchar(50) NOT NULL,
  `timeRe` varchar(50) NOT NULL,
  `device` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT **/
        /**
CREATE TABLE `messagelist` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `nfrom` varchar(50) NOT NULL,
  `nto` varchar(50) NOT NULL,
  `data` varchar(50) NOT NULL,
  `read` int(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB; **/
    public int savemessage(String data_,String ToN_){
        String data=data_;
        String ToN=ToN_;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            System.out.println("INSERIMENTO MESSAGGIO NEL DB");
            connection = DriverManager.getConnection(connectionString);
            
            PreparedStatement prepared = connection.prepareStatement("insert into `messagelist` (`nfrom`, `nto`, `data`, `read`) values (?,?,?,?)");
            prepared.setString(1, username);
            prepared.setString(2, ToN);
            prepared.setString(3, data);
            prepared.setInt(4, 0);
            //ex query for add the user
            int updateST_done = prepared.executeUpdate();
            
            //Quering if the username already exist
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from `messagelist`");
            int counter =0;
            while (chs.next()) {
                counter=counter+1;
            }
            return counter;
            
            
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return 0;
    }
    
        /**
    CREATE TABLE `messagelist` (
  `id` int(10) UNSIGNED NOT NULL,
  `from` varchar(50) NOT NULL,
  `to` varchar(50) NOT NULL,
  `data` varchar(50) NOT NULL,
  `timeS` varchar(50) NOT NULL,
  `timeRc` varchar(50) NOT NULL,
  `timeRe` varchar(50) NOT NULL,
  `device` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT **/
    
    public String[] messagelist(int last_){
        //int last= last;
        String[] msgl=null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Connection connection = null;
        try {
            ArrayList list = new ArrayList();
            
            System.out.println("aaaaaa");
            connection = DriverManager.getConnection(connectionString);
            
            Statement ch = connection.createStatement();
            ResultSet chs = ch.executeQuery("select * from `messagelist`");
            while (chs.next()) {
                if(chs.getInt("id")>last_){
                    if(chs.getString("nfrom").equals(username)){
                        list.add("{\"type\":\"newmessage\",\"id\":\""+chs.getInt("id")+"\",\"from\":\""+username+"\",\"to\":\""+chs.getString("nto")+"\",\"data\":\""+chs.getString("data")+"\"}");
                    }else if(chs.getString("nto").equals(username)){
                        list.add("{\"type\":\"newmessage\",\"id\":\""+chs.getInt("id")+"\",\"from\":\""+chs.getString("nfrom")+"\",\"to\":\""+username+"\",\"data\":\""+chs.getString("data")+"\"}");
                    }
                }
            }
            
            
            msgl = new String[ list.size() ];
            
            for( int j = 0; j < msgl.length; j++ )
                msgl[ j ] = list.get( j ).toString();
            
        
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                // gestione errore in chiusura
            }
        }
        return msgl;
    }

}