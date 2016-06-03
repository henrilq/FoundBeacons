package com.a60circuits.foundbeacons.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.a60circuits.foundbeacons.R;

/**
 * Created by zoz on 04/06/2016.
 */
public class ButtonUtils {

    public static void setSelectStyle(Button button){
        Context ctx = button.getContext();
        button.setBackground(getDrawable(ctx, R.drawable.button_selected));
        button.setTypeface(ResourcesUtils.getTypeFace(ctx, R.string.font_brandon_bld));
        button.setTextColor(getColor(ctx, R.color.colorSelectionBlue));
    }

    public static void setUnSelectStyle(Button button){
        Context ctx = button.getContext();
        button.setBackground(getDrawable(ctx, R.drawable.button_unselected));
        button.setTypeface(ResourcesUtils.getTypeFace(ctx, R.string.font_brandon_bld));
        button.setTextColor(getColor(ctx, R.color.unselectedButtonColor));
    }

    private static Drawable getDrawable(Context context, int id){
        return context.getResources().getDrawable(id, null);
    }

    private static int getColor(Context context, int id){
        return ContextCompat.getColor(context,id);
    }
}
