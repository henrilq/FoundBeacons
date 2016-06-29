package com.a60circuits.foundbeacons.factory;

import android.content.Context;

import com.jaalee.sdk.BeaconManager;

/**
 * Created by zoz on 29/06/2016.
 */
public class BeaconManagerFactory {

    private BeaconManager beaconManager;

    public static BeaconManagerFactory getInstance(){
        return BeaconManagerFactoryHolder.instance;
    }

    public static BeaconManager getBeaconManager(Context context){
        if(getInstance().beaconManager == null){
            getInstance().beaconManager = new BeaconManager(context);
        }
        return getInstance().beaconManager;
    }

    private static class BeaconManagerFactoryHolder{
        private static BeaconManagerFactory instance = new BeaconManagerFactory();
    }
}
