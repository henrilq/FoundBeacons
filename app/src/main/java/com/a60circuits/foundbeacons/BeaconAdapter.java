package com.a60circuits.foundbeacons;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.cache.CacheVariable;
import com.jaalee.sdk.Beacon;

import java.util.List;

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
        holder.setItem(beacon);
        holder.setText(beacon.getName());
        initListeners(holder, beacon);
        if(beacon.getName() != null && beacon.getName().isEmpty()){
            holder.editButton.performClick();
        }else{
            holder.setEditable(false);
        }
    }

    private void initListeners(final BeaconViewHolder holder, final Beacon beacon){
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !holder.editText.isEnabled();
                holder.editText.setEnabled(enabled);
                holder.editText.setFocusable(enabled);
                holder.editText.setFocusableInTouchMode(enabled);
                if(enabled){
                    int selectionColor = ContextCompat.getColor(context,R.color.colorSelectionBlue);
                    holder.editButton.setColorFilter(selectionColor); // Blue Tint
                    holder.editText.requestFocus();
                    holder.editText.setSelection(holder.editText.getText().length());
                }else{
                    holder.updateBeacon();
                    redirectToMap(beacon, holder.editText.getContext());
                }
            }
        });
        holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                }else{
                    holder.editButton.setColorFilter(null);
                    holder.editText.setEnabled(false);
                }
            }
        });

        holder.editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean res = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    holder.updateBeacon();
                    notifyDataSetChanged();
                    redirectToMap(beacon, holder.editText.getContext());
                    res = true;
                }
                return res;
            }
        });
    }



    @Override
    public int getItemCount() {
        return beacons.size();
    }

    private void redirectToMap(Beacon beacon, Context context){
        if(beacon != null && ! beacon.getName().isEmpty() && CacheVariable.getBoolean(MainActivity.FIRST_RENAMING)){
            CacheVariable.put(MainActivity.FIRST_RENAMING, false);
            ContextWrapper ctx = (ContextWrapper)context;
            MainActivity mainActivity = (MainActivity) ctx.getBaseContext();
            mainActivity.replaceFragment(mainActivity.getMapButton());
        }
    }

}
