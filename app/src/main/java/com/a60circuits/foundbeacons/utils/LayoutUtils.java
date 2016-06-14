package com.a60circuits.foundbeacons.utils;

import android.util.TypedValue;
import android.view.View;

/**
 * Created by zoz on 14/06/2016.
 */
public class LayoutUtils {

    public static void overLapView(View leftView, View rightView, boolean rightViewOnTop){
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, leftView.getResources().getDisplayMetrics());
        rightView.setX(rightView.getX()-px);
        if(rightViewOnTop){
            leftView.setZ(0);
            rightView.setZ(1);
        }else{
            leftView.setZ(1);
            rightView.setZ(0);
        }
    }

}
