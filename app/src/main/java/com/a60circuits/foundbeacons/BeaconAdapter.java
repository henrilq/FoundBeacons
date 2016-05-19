package com.a60circuits.foundbeacons;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.jaalee.sdk.Beacon;

import java.util.List;
import java.util.Set;

/**
 * Created by zoz on 19/05/2016.
 */
public class BeaconAdapter extends RecyclerView.Adapter<BeaconViewHolder>{

    private Context context;
    private List<Beacon> beacons;
    private InputMethodManager imm;
    public BeaconAdapter(List<Beacon> beacons){
        this.beacons = beacons;
    }

    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = (View)LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_item, parent, false);
        return new BeaconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BeaconViewHolder holder, int position) {
        final Beacon beacon = beacons.get(position);
        holder.editText.setText(beacon.getName());
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !holder.editText.isEnabled();
                holder.editText.setEnabled(enabled);
                holder.editText.setFocusable(enabled);
                holder.editText.setFocusableInTouchMode(enabled);
                if(enabled){
                    holder.editButton.setColorFilter(Color.argb(255, 0, 123, 247)); // Blue Tint
                    holder.editText.requestFocus();
                    holder.editText.setSelection(holder.editText.getText().length());
                }else{
                    beacon.setName(holder.editText.getText().toString());
                    holder.editButton.setColorFilter(null);
                }
            }
        });
        holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                }else{
                    beacon.setName(holder.editText.getText().toString());
                    holder.editButton.setColorFilter(null);
                    holder.editText.setEnabled(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }
}
