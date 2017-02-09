/*
 *
 * Created by Diego Portoghese
 *
 */
package com.example.utente.chatclient;


import android.app.Activity;

public class UberManager
{
    private static UberManager instance = new UberManager();

    private Activity mainActivity = null;

    public UberManager()
    {

    }

    public static UberManager getInstance()
    {
        return instance;
    }

    public void setMainActivity( Activity mainActivity )
    {
        this.mainActivity = mainActivity;
    }

    public Activity getMainActivity()
    {
        return mainActivity;
    }

    public void cleanup()
    {
        mainActivity = null;
    }
}