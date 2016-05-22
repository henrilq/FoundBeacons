package com.a60circuits.foundbeacons.service;

import android.app.AlarmManager;
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
import android.util.Log;
import android.widget.Toast;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoz on 21/05/2016.
 */
public class NotificationService extends PermanentScheduledService{
    public static final String TAG = "NOTIFICATION_SERVICE";
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private final Handler handler = new Handler();

    private BluetoothLeScanner scanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanCallback scanCallBack;

    public NotificationService(){
        super(20000);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = manager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).setReportDelay(10000).build();
        filters = new ArrayList<ScanFilter>();
        scanCallBack = new ScanCallback() {
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                for (ScanResult sc: results) {
                    Log.i("MAC ADDRESS ", sc.getDevice().getAddress() +"  "+sc.getRssi());
                }
                scanner.stopScan(this);
            }
        };
    }

    @Override
    public void doAction() {
        scanner.startScan(filters, settings, scanCallBack);

    }

}
