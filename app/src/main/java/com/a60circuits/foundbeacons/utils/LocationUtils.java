package com.a60circuits.foundbeacons.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.webkit.PermissionRequest;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by zoz on 25/05/2016.
 */
public class LocationUtils {

    public static Location getLastKnownLocation(Activity activity){
        if(! PermissionUtils.checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
            ActivityCompat.requestPermissions(activity, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
        }
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
