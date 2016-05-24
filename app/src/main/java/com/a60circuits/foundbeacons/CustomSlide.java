package com.a60circuits.foundbeacons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author Vlonjat Gashi (vlonjatg)
 */
public class CustomSlide extends Fragment {

    public static final String KEY = "image";

    private int id;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_custom_slide, container, false);
        ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
        int key = getArguments().getInt(KEY);
        if(key > 0){
            imageView.setBackground(ContextCompat.getDrawable(getActivity(), key));
        }

        return rootView;
    }
}
