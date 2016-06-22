package com.a60circuits.foundbeacons.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.a60circuits.foundbeacons.R;

/**
 * Created by zoz on 25/05/2016.
 */
public class PermissionUtils {

    public static final String TAG = "PermissionUtils";

    public static boolean checkPermission(Activity activity, String permission){
        int res = ActivityCompat.checkSelfPermission(activity, permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationAndBluetooth(Activity activity){
        requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(activity.getBaseContext(),activity.getResources().getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public static void requestPermission(Activity activity, String permission, boolean forcePermission){
        if (!checkPermission(activity, permission)) {
            if (forcePermission || ! ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)) {
                ActivityCompat.requestPermissions(activity, new String[] { permission },1);
            }
        }
    }

    public static void requestPermission(Activity activity, String permission){
        requestPermission(activity, permission, false);
    }


}
