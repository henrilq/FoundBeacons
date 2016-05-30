package com.a60circuits.foundbeacons.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a60circuits.foundbeacons.MainActivity;
import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.connection.BeaconCharacteristics;
import com.jaalee.sdk.connection.BeaconConnection;
import com.jaalee.sdk.connection.ConnectionCallback;
import com.jaalee.sdk.connection.JaaleeDefine;
import com.jaalee.sdk.connection.WriteCallback;

import java.util.Date;

/**
 * Created by zoz on 27/05/2016.
 */
public class BeaconConnectionService extends Service {

    public static final String TAG = "BeaconConnectionService";

    public static final String BEACON_ARGUMENT = "Beacon";

    private BeaconConnection connection;

    private Beacon beacon;
    private Intent broadcastIntent;

    public BeaconConnectionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.SCAN_RESULT);
            beacon = intent.getParcelableExtra(BEACON_ARGUMENT);
            Beacon found = BeaconCacheManager.getInstance().findInCacheByMacAddress(beacon);
            if(found != null){
                sendBroadcastMessage(getResources().getString(R.string.scanned_beacon_already_saved)+" : "+found.getName());
            }else{
                Log.i(TAG, " START CONNECTION TO " + beacon.getMacAddress());
                connection = new BeaconConnection(getApplicationContext(), beacon, createConnectionCallback());
                connection.connectBeaconWithPassword("666666");
            }
        }
        return Service.START_NOT_STICKY;
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
                        sendBroadcastMessage(getResources().getString(R.string.scanned_beacon_activation_error));
                        stopSelf();
                    }
                });
            }

            @Override
            public void onAuthenticationError() {
                Log.i(TAG, " CONNECTION FAILED");
                sendBroadcastMessage(getResources().getString(R.string.scanned_beacon_connection_error));
                stopSelf();
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, " DISCONNECTED");
                sendBroadcastMessage(getResources().getString(R.string.scanned_beacon_connection_error));
                stopSelf();
            }
        };
    }

    private void saveBeacon(){
        Log.i(TAG," SAVING BEACON");
        beacon.setDate(new Date());
        Beacon found = BeaconCacheManager.getInstance().findInCacheByMacAddress(beacon);
        String message = null;
        if(found != null){
            message = getResources().getString(R.string.scanned_beacon_already_saved)+" : "+found.getName() ;
        }else{
            boolean success = BeaconCacheManager.getInstance().save(beacon);
            if(success){
                message = getResources().getString(R.string.scanned_beacon_saved);
            }else{
                message = getResources().getString(R.string.scanned_beacon_saving_technical_error);
            }
        }
        sendBroadcastMessage(message);
        stopSelf();
    }

    private void sendBroadcastMessage(String message){
        broadcastIntent.putExtra(TAG, message);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, " DESTROY");
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