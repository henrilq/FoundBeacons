package com.a60circuits.foundbeacons.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by zoz on 25/05/2016.
 */
public class PermissionUtils {

    public static boolean checkPermission(Context context, String permission){
        int res = context.checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }
}
