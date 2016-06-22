package com.a60circuits.foundbeacons.utils;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by zoz on 22/06/2016.
 */
public class SharedPreferencesUtils {
    private static final String PREF_FILE = "prefFile";

    public static boolean putBoolean(Activity activity, String key, boolean value){
        SharedPreferences.Editor editor = getEditor(activity);
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean getBoolean(Activity activity, String key, boolean defValue){
        final SharedPreferences settings = getSharedPreferences(activity);
        return settings.getBoolean(key,defValue);
    }

    public static boolean putString(Activity activity, String key, String value){
        SharedPreferences.Editor editor = getEditor(activity);
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(Activity activity, String key, String defValue){
        final SharedPreferences settings = getSharedPreferences(activity);
        return settings.getString(key,defValue);
    }

    private static SharedPreferences.Editor getEditor(Activity activity){
        final SharedPreferences settings = getSharedPreferences(activity);
        return settings.edit();
    }

    private static SharedPreferences getSharedPreferences(Activity activity){
        return activity.getSharedPreferences(PREF_FILE, 0);
    }
}
