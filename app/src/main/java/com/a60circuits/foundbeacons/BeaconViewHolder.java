package com.a60circuits.foundbeacons;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.utils.ResourcesUtils;
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
        editText.setTypeface(ResourcesUtils.getTypeFace(editText.getContext(), R.string.font_brandon_med));
        editButton = (ImageButton) view.findViewById(R.id.editButton);
    }

    public void updateBeacon(){
        item.setName(editText.getText().toString());
        editButton.setColorFilter(null);
        editText.setEnabled(false);
        BeaconCacheManager.getInstance().save(item);
    }

    public void setEditMode(boolean editable){
        editText.setEnabled(editable);
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        if(editable){
            int selectionColor = ContextCompat.getColor(editText.getContext(),R.color.colorSelectionBlue);
            editButton.setColorFilter(selectionColor); // Blue Tint
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
        }else{
            updateBeacon();
        }
    }

    public boolean isEditable(){
        return editText.isEnabled();
    }

    public void setEditable(boolean editable){
        editText.setEnabled(editable);
    }

    public void setText(String text){
        this.editText.setText(text);
    }

    public void setItem(Beacon item) {
        this.item = item;
    }

}
