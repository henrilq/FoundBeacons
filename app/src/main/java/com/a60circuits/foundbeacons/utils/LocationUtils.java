package com.a60circuits.foundbeacons.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.webkit.PermissionRequest;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by zoz on 25/05/2016.
 */
public class LocationUtils {

    public static Location getLocation(Context context){
        Location location = null;
        if(PermissionUtils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            location = locationManager.getLastKnownLocation(provider);
        }
        return location;
    }
}
