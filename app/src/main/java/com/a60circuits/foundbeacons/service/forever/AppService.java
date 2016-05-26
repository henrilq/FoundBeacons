package com.a60circuits.foundbeacons.service.forever;

/***
 Copyright (c) 2008-2012 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Advanced Android Development_
 http://commonsware.com/AdvAndroid
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class AppService extends JobService implements Observer{

    public static final String TAG = "NOTIFICATION_SERVICE";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback scanCallBack;
    private static List<Beacon> beacons;
    private static Set<String> foundBeacons;
    private BeaconDao dao;

    public AppService() {

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG , "Starting job");

        BeaconCacheManager.getInstance().addObserver(this);
        foundBeacons = new HashSet<>();
        dao = new BeaconDao(getApplicationContext());
        beacons = BeaconCacheManager.getInstance().getData();
        Log.i("beacons ", "CACHE : "+beacons.size());
        if(beacons == null || beacons.isEmpty()){
            beacons = dao.findAll();
            Log.i("beacons ", "DAO : "+beacons.size());
        }
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = manager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(10000).build();
        filters = new ArrayList<ScanFilter>();
        scanCallBack = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                try{
                    Log.i("SCAN ", " Results "+results.size());
                    Log.i("Beacons ", ""+beacons.size());
                    //List<Beacon> tempList = new ArrayList<>(beacons);
                    Set<String> macAddresses = new HashSet<String>();
                    for (ScanResult sc: results) {
                        macAddresses.add(sc.getDevice().getAddress());
                        Log.i("MAC ADDRESS ", sc.getDevice().getAddress() +"  "+sc.getRssi());
                    }

                    for (Beacon beacon: beacons) {
                        String mac = beacon.getMacAddress();
                        if(mac != null && ! macAddresses.contains(mac)){
                            Log.i("Send ", " notification" );
                            addNotification(beacon.getMacAddress());
                        }
                    }
                }catch(Exception e){
                    Log.e("","",e);
                }finally {
                    scanner.stopScan(this);
                }
            }
        };

        scanner.startScan(filters, settings, scanCallBack);
        return false;
    }

    private void addNotification(String message){
        Log.i("NOTIFICATION ", "not found MAC : "+message);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.add2x)
                        .setContentTitle("My notification")
                        .setContentText("not found MAC : "+message);
        notificationManager.notify(1988,mBuilder.build());
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("WORKING" , "I'm working !!");
        return Service.START_STICKY;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(data != null){
            beacons = (List<Beacon>)data;
        }
    }
}
