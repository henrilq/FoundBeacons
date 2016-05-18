package com.a60circuits.foundbeacons;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Observer;

/**
 * Created by zoz on 17/05/2016.
 */
public class ItemFragment extends Fragment{

    private String text;
    private Button textButton;
    private EditText textEdit;
    private Observer observer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.item_fragment,container,false);

        textEdit = (EditText)view.findViewById(R.id.editText);
        if(text != null){
            textEdit.setText(text);
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
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    textEdit.requestFocus();
                    textEdit.setSelection(textEdit.getText().length());
                }else{
                    //imm.hideSoftInputFromWindow(ItemFragment.this.getActivity().getWindowToken(),0);
                }
                if(observer != null){
                    observer.update(null, ItemFragment.this);
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

    public void setObserver(Observer observer){
        this.observer = observer;
    }

    public void setText(String text) {
        this.text = text;
    }
}