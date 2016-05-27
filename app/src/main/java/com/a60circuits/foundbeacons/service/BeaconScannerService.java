package com.a60circuits.foundbeacons.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a60circuits.foundbeacons.DetectionFragment;
import com.a60circuits.foundbeacons.MainActivity;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;
import com.jaalee.sdk.connection.BeaconConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoz on 26/05/2016.
 */
public class BeaconScannerService extends Service {

    public static final String TAG = "BeaconScannerService";

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    public static final String DETECTION_MODE = "detectionMode";

    public static final String CONNECTION_MODE = "connectionMode";

    public static final String BEACON_ARGUMENT = "beacon";

    private BeaconManager beaconManager;

    private Intent broadcastIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " START BEACON SCANNER SERVICE ");
        boolean isDetectionMode = intent.getBooleanExtra(DETECTION_MODE, false);
        boolean isConnectionMode = intent.getBooleanExtra(CONNECTION_MODE, false);
        List<Beacon> beacons = new ArrayList<>();
        beaconManager = new BeaconManager(getApplicationContext());
        if(isConnectionMode){
            beaconManager.setRangingListener(createConnectionRangingListener());
        }else if(isDetectionMode){
            broadcastIntent = new Intent();
            broadcastIntent.setAction(DetectionFragment.DETECTION_RESULT);
            Beacon beacon = intent.getParcelableExtra(BEACON_ARGUMENT);
            if(beacon != null){
                beaconManager.setRangingListener(createDetectionRangingListener(beacon));
            }
        }
        connectToService();
        return Service.START_NOT_STICKY;
    }

    private RangingListener createConnectionRangingListener(){
        return new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                for (final Beacon beacon: beacons){
                    int rssi = Math.abs(beacon.getRssi());
                    Log.i(TAG, " BEACON DETECTED "+beacon.getMacAddress()+"  "+rssi);
                    if(rssi < 60){
                        stopRanging();
                        Intent i = new Intent(getApplicationContext(),BeaconConnectionService.class);
                        i.putExtra(BeaconConnectionService.BEACON_ARGUMENT, beacon);
                        getApplicationContext().startService(i);
                    }
                }
            }
        };
    }

    private RangingListener createDetectionRangingListener(final Beacon beacon){
        return new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                String macAddress = beacon.getMacAddress();
                if(macAddress != null){
                    for (Beacon b: beacons){
                        if(macAddress.equals(b.getMacAddress())){
                            broadcastIntent.putExtra(TAG, b.getRssi());
                            sendBroadcast(broadcastIntent);
                        }
                    }
                }
            }
        };
    }


    private void connectToService() {
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e(TAG,"",e);
                }
            }
        });
    }

    private void stopRanging(){
        try {
            beaconManager.stopRanging(ALL_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.e(TAG,"",e);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " DESTROY");
        if(beaconManager != null){
            stopRanging();
        }
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
