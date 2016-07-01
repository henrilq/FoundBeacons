package com.a60circuits.foundbeacons.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.a60circuits.foundbeacons.SettingsFragment;

import java.util.List;

/**
 * Created by zoz on 25/05/2016.
 */
public class LocationUtils {

    private static final String TAG = "LocationUtils";

    private static final long MAX_TIME = 15000;

    private LocationManager locationManager;

    private final LocationListener gpsLocationListener =new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG,"remove GPS listener");
            locationManager.removeUpdates(gpsLocationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.i(TAG,"GPS available again");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG,"GPS out of service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG,"GPS temporarily unavailable");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG,"GPS provider enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG,"GPS provider disabled");
        }
    };

    private final LocationListener networkLocationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG,"remove Network listener");
            locationManager.removeUpdates(networkLocationListener);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.i(TAG,"Network location available again");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG,"Network location out of service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG,"Network location temporarily unavailable");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG,"Network provider enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG,"Network provider disabled");
        }
    };


    private LocationUtils(){

    }

    public static Location getLastKnownLocation(Activity activity){
        Location bestLocation = null;
        try{
            boolean gpsEnabled = SharedPreferencesUtils.getBoolean(activity, SettingsFragment.GPS_ENABLED, false);
            if(gpsEnabled){
                Long currentTime = System.currentTimeMillis();
                PermissionUtils.requestPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
                if(PermissionUtils.checkPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)){
                    LocationManager locationManager = getInstance().locationManager;
                    if(locationManager == null){
                        getInstance().locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                        locationManager = getInstance().locationManager;
                    }
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
                if(bestLocation != null){
                    long diff = Math.abs(currentTime - bestLocation.getTime());
                    if(diff > MAX_TIME){
                        requestLocationUpdates(activity);
                    }
                }
            }
        }catch (Exception e){
            Log.e(TAG,"",e);
        }
        return bestLocation;
    }

    public static void requestLocationUpdates(Activity activity){
        try{
            if(getInstance().locationManager == null){
                getInstance().locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            }
            removeListeners();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getInstance().locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0,getInstance().networkLocationListener);
                    getInstance().locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, getInstance().gpsLocationListener);
                }
            });
        }catch (Exception e){
            Log.e(TAG,"",e);
        }
    }

    public static void removeListeners(){
        if(getInstance().locationManager != null){
            getInstance().locationManager.removeUpdates(getInstance().networkLocationListener);
            getInstance().locationManager.removeUpdates(getInstance().gpsLocationListener);
        }
    }

    public static LocationUtils getInstance(){
        return LocationHolder.instance;
    }

    private static class LocationHolder{
        private static LocationUtils instance = new LocationUtils();
    }
}
