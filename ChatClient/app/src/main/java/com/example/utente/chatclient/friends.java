
/*
 *
 * Created by Diego Portoghese
 *
 */
package com.example.utente.chatclient;


public class friends {
    private String name;
    private String newmsg;
    message msg;
    boolean ifnew=false;
    String status;

    public friends(String name_,String status,message msg_) {

        this.name=name_;
        this.status=status;
        this.msg=msg_;
        this.ifnew=msg.ifIsRead(name);

    }

    public String getName() {
        return name;
    }

    public String getNewmsg() {
        //String[] msx=msg.getmessage(this.name);
        if(ifnew){
            return "";
        }
        return "new";
    }

}
