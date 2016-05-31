package com.a60circuits.foundbeacons.service;


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
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class NotificationService extends JobService implements Observer{

    public static final long PERIOD = 60000;
    public static final String TAG = "NOTIFICATION_SERVICE";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback scanCallBack;
    private static List<Beacon> beacons;
    private static Set<String> foundBeacons;
    private BeaconDao dao;
    private BeaconManager beaconManager;

    public NotificationService() {

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG , "Starting job");
        Toast.makeText(getApplicationContext(),"Starting job", Toast.LENGTH_SHORT).show();
        BeaconCacheManager.getInstance().addObserver(this);
        foundBeacons = new HashSet<>();
        dao = new BeaconDao(getApplicationContext());
        beacons = BeaconCacheManager.getInstance().getData();
        Log.i("beacons ", "CACHE : "+beacons.size());
        if(beacons == null || beacons.isEmpty()){
            beacons = dao.findAll();
            Log.i("beacons ", "DAO : "+beacons.size());
        }

        beaconManagerDetection();

        return false;
    }

    private void beaconManagerDetection(){
        beaconManager = new BeaconManager(getApplicationContext());
        final long start = System.currentTimeMillis();
        final Set<String> detectedMacAdresses = new HashSet<>();
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region paramRegion, List<Beacon> paramList) {
                long length = System.currentTimeMillis() - start;
                if(length < 30000){
                    for (Beacon b: paramList){
                        detectedMacAdresses.add(b.getMacAddress());
                    }
                }else{
                    for (Beacon b: beacons){
                        Log.i("CHECK ", " BEACON "+b.getName()+"  "+b.getMacAddress());
                        if(detectedMacAdresses.contains(b.getMacAddress())){
                            Log.i("FOUND ", " BEACON "+b.getName()+"  "+b.getMacAddress());
                        }else{
                            Log.i("NOT FOUND ", " BEACON "+b.getName()+"  "+b.getMacAddress());
                            sendNotification(b);
                        }
                    }
                    stopRanging();
                }
            }
        });
        connectToService();
    }

    private void basicDetection(){
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
                    Set<String> macAddresses = new HashSet<String>();
                    for (ScanResult sc: results) {
                        macAddresses.add(sc.getDevice().getAddress());
                        Log.i("MAC ADDRESS ", sc.getDevice().getAddress() +"  "+sc.getRssi());
                    }

                    for (Beacon beacon: beacons) {
                        String mac = beacon.getMacAddress();
                        if(mac != null && ! macAddresses.contains(mac)){
                            Log.i("Send ", " notification" );
                            sendNotification(beacon);
                        }
                    }
                }catch(Exception e){
                    Log.e("","",e);
                }finally {
                    scanner.stopScan(this);
                }
            }
        };

        connectToService();
    }

    private void sendNotification(Beacon beacon){
        Log.i("NOTIFICATION ", "not found MAC : "+beacon.getMacAddress());
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("My notification")
                        .setContentText("not found MAC : "+beacon.getMacAddress());
        Long longId = Long.parseLong(beacon.getMacAddress().replace(":", ""), 16);
        int id = Math.abs(longId.intValue());
        notificationManager.notify(id,mBuilder.build());
        notificationManager.notify();
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
    public boolean onStopJob(JobParameters params) {
        if(scanner != null){
            scanner.stopScan(scanCallBack);
        }
        if(beaconManager != null){
            stopRanging();
            beaconManager.disconnect();
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG , "On start command");
        return Service.START_STICKY;
    }

    @Override
    public void update(Observable observable, Object data) {
        if(data != null){
            beacons = (List<Beacon>)data;
        }
    }
}
