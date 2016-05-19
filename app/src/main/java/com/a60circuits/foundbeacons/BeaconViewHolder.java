package com.a60circuits.foundbeacons;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * Created by zoz on 19/05/2016.
 */
public class BeaconViewHolder extends RecyclerView.ViewHolder{
    public EditText editText;
    public ImageButton editButton;

    public BeaconViewHolder(View view) {
        super(view);
        editText = (EditText) view.findViewById(R.id.editText);
        editButton = (ImageButton) view.findViewById(R.id.editButton);
        editText.setEnabled(false);
    }
}
