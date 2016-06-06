package com.a60circuits.foundbeacons;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.jaalee.sdk.Beacon;

/**
 * Created by zoz on 17/05/2016.
 */
public class ItemFragment extends Fragment{

    private Beacon beacon;
    private Button textButton;
    private EditText textEdit;
    private boolean editionMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.item_fragment,container,false);

        textEdit = (EditText)view.findViewById(R.id.editText);
        if(beacon != null){
            textEdit.setText(beacon.getName());
        }
        textEdit.setEnabled(false);
        textEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ItemFragment.this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        Button textButton = (Button)view.findViewById(R.id.editButton);
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = !textEdit.isEnabled();
                textEdit.setEnabled(enabled);
                InputMethodManager imm = (InputMethodManager) ItemFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(enabled){
                    textEdit.setText(beacon.getName());
                    editionMode = true;
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    textEdit.requestFocus();
                    textEdit.setSelection(textEdit.getText().length());
                }else{
                    //imm.hideSoftInputFromWindow(ItemFragment.this.getActivity().getWindowToken(),0);
                    beacon.setName(textEdit.getText().toString());
                    editionMode = false;
                }
            }
        });

        return view;
    }

    public EditText getTextEdit() {
        return textEdit;
    }

    public Button getTextButton() {
        return textButton;
    }

    public boolean isEnabled() {
        return textEdit.isEnabled();
    }

    public void setEnabled(boolean enabled) {
        textEdit.setEnabled(enabled);
    }

    public void setBeacon(Beacon beacon) {
        if(beacon != null){
            if(this.beacon == null){
                this.beacon = beacon;
            }else if(beacon.getMacAddress().equals(this.beacon.getMacAddress())){
                this.beacon.setRssi(beacon.getRssi());
            }
        }
    }

    public void update(){
        if(!editionMode){
            textEdit.setText(beacon.getName() +" : "+beacon.getRssi());
        }
    }

    public Beacon getBeacon() {
        return beacon;
    }

}