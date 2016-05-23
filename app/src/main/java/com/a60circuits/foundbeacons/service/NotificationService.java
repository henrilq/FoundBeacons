package com.a60circuits.foundbeacons.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.MonitoringListener;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Created by zoz on 21/05/2016.
 */
public class NotificationService extends PermanentScheduledService implements Observer {
    public static final String TAG = "NOTIFICATION_SERVICE";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);
    private static final long LENGHT = 20000;

    private final Handler handler = new Handler();

    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback scanCallBack;
    private static List<Beacon> beacons;
    private static Set<String> foundBeacons;
    private BeaconDao dao;
    private BeaconManager beaconManager;
    private long startTime;


    public NotificationService(){
        super(40000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CREATE ", " NOTIFICATION SERVICE");
        BeaconCacheManager.getInstance().addObserver(this);
        foundBeacons = new HashSet<>();
        dao = new BeaconDao(getApplicationContext());
        if(beacons == null){
            beacons = dao.findAll();
            beaconManager = new BeaconManager(getApplicationContext());
        }
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = manager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(30000).build();
        filters = new ArrayList<ScanFilter>();
        scanCallBack = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                try{
                    //List<Beacon> tempList = new ArrayList<>(beacons);
                    Set<String> macAddresses = new HashSet<String>();
                    for (ScanResult sc: results) {
                        macAddresses.add(sc.getDevice().getAddress());
                        Log.i("MAC ADDRESS ", sc.getDevice().getAddress() +"  "+sc.getRssi());
                    }
                    for (Beacon beacon: beacons) {
                        String mac = beacon.getMacAddress();
                        if(mac != null && ! macAddresses.contains(mac)){
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
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> detected) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long length = System.currentTimeMillis() - startTime;
                        Log.i("LENGTH ", ""+length);
                        if(length > LENGHT){
                            for (Beacon beacon: beacons) {
                                String mac = beacon.getMacAddress();
                                if(mac != null && ! foundBeacons.contains(mac)){
                                    addNotification(beacon.getMacAddress());
                                }
                            }
                            foundBeacons.clear();
                            beaconManager.disconnect();
                        }else{
                            for (Beacon beacon: detected) {
                                Log.i("MAC ADDRESS 2 ", beacon.getMacAddress());
                                foundBeacons.add(beacon.getMacAddress());
                            }
                        }
                    }
                });
            }
        });
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
    public void doAction() {
        if(scanner != null){
            scanner.startScan(filters, settings, scanCallBack);
        }

        //beaconManager.disconnect();
        startTime = System.currentTimeMillis();
        /*beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e("","",e);
                }
            }
        });*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scanner.stopScan(scanCallBack);
    }

    @Override
    public void update(Observable observable, Object data) {
        if(data != null){
            beacons = (List<Beacon>)data;
        }
    }
}
