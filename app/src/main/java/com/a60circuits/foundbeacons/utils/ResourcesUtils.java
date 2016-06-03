package com.a60circuits.foundbeacons.utils;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by zoz on 04/06/2016.
 */
public class ResourcesUtils {

    public static Typeface getTypeFace(Context context, int id){
        return Typeface.createFromAsset(context.getAssets(),context.getResources().getString(id));
    }
}
