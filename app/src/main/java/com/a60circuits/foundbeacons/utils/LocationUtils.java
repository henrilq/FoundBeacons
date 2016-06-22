package com.a60circuits.foundbeacons.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.a60circuits.foundbeacons.SettingsFragment;

import java.util.List;

/**
 * Created by zoz on 25/05/2016.
 */
public class LocationUtils {

    public static Location getLastKnownLocation(Activity activity){
        Location bestLocation = null;
        boolean gpsEnabled = SharedPreferencesUtils.getBoolean(activity, SettingsFragment.GPS_ENABLED, false);
        if(gpsEnabled){
            PermissionUtils.requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
            if(PermissionUtils.checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
                LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                List<String> providers = locationManager.getProviders(true);
                for (String provider : providers) {
                    Location l = locationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                        bestLocation = l;
                    }
                }
            }
        }
        return bestLocation;
    }
}
