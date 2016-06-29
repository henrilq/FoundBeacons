package com.a60circuits.foundbeacons.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a60circuits.foundbeacons.DetectionFragment;
import com.a60circuits.foundbeacons.MainActivity;
import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.cache.CacheVariable;
import com.a60circuits.foundbeacons.factory.BeaconManagerFactory;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;
import com.jaalee.sdk.connection.BeaconCharacteristics;
import com.jaalee.sdk.connection.BeaconConnection;
import com.jaalee.sdk.connection.ConnectionCallback;
import com.jaalee.sdk.connection.JaaleeDefine;
import com.jaalee.sdk.connection.WriteCallback;

import java.util.Date;
import java.util.List;

/**
 * Created by zoz on 26/05/2016.
 */
public class BeaconScannerService extends Service {

    public static final String TAG = "BeaconScannerService";

    public static final long TIME_OUT = 30000;

    public static final long MAX_RSSI = 60;

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    public static final String DETECTION_MODE = "detectionMode";

    public static final String CONNECTION_MODE = "connectionMode";

    public static final String BEACON_ARGUMENT = "beacon";

    private BeaconManager beaconManager;

    private BeaconConnection connection;

    private Intent broadcastIntent;

    private Handler handler = new Handler();

    private Beacon beacon;

    private long startTime;
    private int connectionNb;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, " START BEACON SCANNER SERVICE ");
        this.startTime = System.currentTimeMillis();
        boolean isDetectionMode = intent.getBooleanExtra(DETECTION_MODE, false);
        boolean isConnectionMode = intent.getBooleanExtra(CONNECTION_MODE, false);
        beaconManager = BeaconManagerFactory.getBeaconManager(getApplicationContext());
        if(isConnectionMode){
            beaconManager.setRangingListener(createConnectionRangingListener());
            broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.SCAN_RESULT);
            sendInfoBroadcastMessage(getResources().getString(R.string.scanning_beacon));
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
                long time = System.currentTimeMillis() - startTime;
                Log.i(TAG, " TIME "+time);
                if(time > TIME_OUT){
                    stopRanging();
                    sendStopBroadcastMessage(getResources().getString(R.string.scanning_time_out));
                }else{
                    Beacon nearestBeacon = null;
                    for (final Beacon beacon: beacons){
                        if(nearestBeacon == null){
                            nearestBeacon = beacon;
                        }
                        int rssi = Math.abs(beacon.getRssi());
                        Log.i(TAG, " BEACON DETECTED "+beacon.getMacAddress()+"  "+beacon.getName()+"   "+rssi);
                        Beacon found = BeaconCacheManager.getInstance().findInCacheByMacAddress(beacon);
                        if(found == null && rssi < Math.abs(nearestBeacon.getRssi())){
                            nearestBeacon = beacon;
                        }
                    }
                    if(nearestBeacon != null){
                        int rssi = Math.abs(nearestBeacon.getRssi());
                        if(rssi < MAX_RSSI){
                            stopRanging();
                            BeaconScannerService.this.beacon = nearestBeacon;
                            sendInfoBroadcastMessage(getResources().getString(R.string.connecting_beacon));
                            connect();
                        }
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
                        Log.i(TAG, " BEACON DETECTED "+b.getMacAddress()+"  "+b.getName()+"   "+b.getRssi());
                        if(macAddress.equals(b.getMacAddress())){
                            broadcastIntent.putExtra(TAG, b.getRssi());
                            sendBroadcast(broadcastIntent);
                        }
                    }
                }
            }
        };
    }

    private ConnectionCallback createConnectionCallback() {
        return new ConnectionCallback() {
            @Override
            public void onAuthenticated(BeaconCharacteristics paramBeaconCharacteristics) {
                Log.i(TAG, " CONNECTION SUCCESS  " + connection.isConnected());
                connection.writeBeaconState(JaaleeDefine.JAALEE_BEACON_STATE_ENABLE, new WriteCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, " STATE CHANGED WITH SUCCES");
                        saveBeacon();
                    }

                    @Override
                    public void onError() {
                        Log.i(TAG, " STATE CHANGE FAILURE");
                        reconnect(getResources().getString(R.string.scanned_beacon_activation_error));
                    }
                });
            }

            @Override
            public void onAuthenticationError() {
                Log.i(TAG, " CONNECTION FAILED");
                reconnect(getResources().getString(R.string.scanned_beacon_connection_error));
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, " DISCONNECTED");
                reconnect(getResources().getString(R.string.scanned_beacon_connection_error));
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

    private void connect(){
        if(connection != null){
            connection.disconnect();
        }
        connectionNb++;
        Log.i(TAG, " START CONNECTION TO " + beacon.getMacAddress());
        handler.post(new Runnable() {
            @Override
            public void run() {
                connection = new BeaconConnection(getApplicationContext(), beacon, createConnectionCallback());
                connection.connectBeaconWithPassword("666666");
            }
        });

    }

    private void reconnect(String errorMessage){
        boolean stopped = stopOutOfTime();
        if(! stopped){
            if(connectionNb > 3){
                sendStopBroadcastMessage(errorMessage);
                stopSelf();
            }else{
                Log.i(TAG, " NEXT TRY " + connectionNb);
                connect();
            }
        }
    }

    private boolean stopOutOfTime(){
        long time = System.currentTimeMillis() - startTime;
        Log.i(TAG, " TIME "+time);
        boolean stopped = false;
        if(time > TIME_OUT){
            stopped = true;
            sendStopBroadcastMessage(getResources().getString(R.string.scanning_time_out));
            stopSelf();
        }
        return stopped;
    }

    private void sendStopBroadcastMessage(String message){
        broadcastIntent.putExtra(MainActivity.SERVICE_INFO, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_SUCCESS, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_STOP, message);
        sendBroadcast(broadcastIntent);
    }

    private void sendSuccessBroadcastMessage(String message){
        broadcastIntent.putExtra(MainActivity.SERVICE_INFO, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_STOP, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_SUCCESS, message);
        sendBroadcast(broadcastIntent);
    }

    private void sendInfoBroadcastMessage(String message){
        broadcastIntent.putExtra(MainActivity.SERVICE_STOP, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_SUCCESS, "");
        broadcastIntent.putExtra(MainActivity.SERVICE_INFO, message);
        sendBroadcast(broadcastIntent);
    }

    private void saveBeacon(){
        Log.i(TAG," SAVING BEACON");
        beacon.setDate(new Date());
        Beacon found = BeaconCacheManager.getInstance().findInCacheByMacAddress(beacon);
        String message = null;
        if(found == null){
            beacon.setName("");
            CacheVariable.put(MainActivity.SCANNING, false);
            boolean success = BeaconCacheManager.getInstance().save(beacon);
            if(success){
                message = getResources().getString(R.string.scanned_beacon_saved);
                sendSuccessBroadcastMessage(message);
            }else{
                message = getResources().getString(R.string.scanned_beacon_saving_technical_error);
                sendStopBroadcastMessage(message);
            }
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " DESTROY");
        if(beaconManager != null){
            stopRanging();
        }
        beaconManager.disconnect();
        if(connection != null){
            connection.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
