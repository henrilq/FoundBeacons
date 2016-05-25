package com.a60circuits.foundbeacons;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;

/**
 * Created by zoz on 19/05/2016.
 */
public class BeaconViewHolder extends RecyclerView.ViewHolder{
    public EditText editText;
    public ImageButton editButton;
    private Beacon item;

    public BeaconViewHolder(View view) {
        super(view);
        editText = (EditText) view.findViewById(R.id.editText);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        editText.setEnabled(false);
    }

    public void setItem(Beacon item) {
        this.item = item;
    }

}
