package com.a60circuits.foundbeacons.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.connection.BeaconCharacteristics;
import com.jaalee.sdk.connection.BeaconConnection;
import com.jaalee.sdk.connection.ConnectionCallback;
import com.jaalee.sdk.connection.JaaleeDefine;
import com.jaalee.sdk.connection.WriteCallback;

/**
 * Created by zoz on 27/05/2016.
 */
public class BeaconConnectionService extends Service {

    public static final String TAG = "BeaconConnectionService";

    public static final String BEACON_ARGUMENT = "Beacon";

    private BeaconConnection connection;

    private Beacon beacon;

    public BeaconConnectionService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            beacon = intent.getParcelableExtra(BEACON_ARGUMENT);
            Log.i(TAG, " START CONNECTION TO " + beacon.getMacAddress());
            connection = new BeaconConnection(getApplicationContext(), beacon, createConnectionCallback());
            connection.connectBeaconWithPassword("666666");
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
                        stopSelf();
                    }
                });
            }

            @Override
            public void onAuthenticationError() {
                Log.i(TAG, " CONNECTION FAILED");
                stopSelf();
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, " DISCONNECTED");
                stopSelf();
            }
        };
    }

    private void saveBeacon(){
        Log.i(TAG," SAVING BEACON");
        BeaconCacheManager.getInstance().save(beacon);
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
