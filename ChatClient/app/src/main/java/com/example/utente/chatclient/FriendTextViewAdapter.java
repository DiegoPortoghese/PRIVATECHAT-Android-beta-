/*
 *
 * Created by Diego Portoghese
 *
 */
package com.example.utente.chatclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class FriendTextViewAdapter extends ArrayAdapter<friends> {

    private int layoutResource;

    public FriendTextViewAdapter(Context context, int layoutResource, List<friends> friendsList) {
        super(context, layoutResource, friendsList);
        this.layoutResource = layoutResource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(layoutResource, null);
        }

        friends friends = getItem(position);

        if (friends != null) {
            TextView leftTextView = (TextView) view.findViewById(R.id.nameTextView);
            TextView rightTextView = (TextView) view.findViewById(R.id.newmsgTextView);
            ImageView imgView = (ImageView) view.findViewById(R.id.imageOnline);


            if (leftTextView != null) {
                leftTextView.setText(friends.getName());
            }
            if(friends.status.equals("1")){
                imgView.setVisibility(View.VISIBLE);
            }

            if (rightTextView != null) {
                    rightTextView.setText(friends.getNewmsg());
            }

        }

        return view;
    }

}