package com.a60circuits.foundbeacons.service;


import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.a60circuits.foundbeacons.R;
import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class NotificationService extends JobService{

    public static final String TAG = "NOTIFICATION_SERVICE";
    public static final long PERIOD = 60000;
    public static final long TASK_LENGTH = 30000;
    public static final int NOTIFICATION_ID = 001;
    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private List<Beacon> beacons;
    private BeaconDao dao;
    private BeaconManager beaconManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        try{
            Log.i(TAG , "Starting job");
            dao = new BeaconDao(getApplicationContext());
            beacons = new ArrayList(BeaconCacheManager.getInstance().getData());
            Log.i("beacons ", "CACHE : "+beacons.size());
            if(beacons == null || beacons.isEmpty()){
                beacons = dao.findAll();
                Log.i("beacons ", "DAO : "+beacons.size());
            }
            if(beacons != null && ! beacons.isEmpty()){
                beaconManagerDetection();
            }
        }catch(Exception e){
            Log.e(TAG , "", e);
        }
        return false;
    }

    private void beaconManagerDetection(){
        beaconManager = new BeaconManager(getApplicationContext());
        final long start = System.currentTimeMillis();
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region paramRegion, List<Beacon> paramList) {
                try{
                    long time = System.currentTimeMillis() - start;
                    if(time < TASK_LENGTH && ! beacons.isEmpty()){
                        for (Beacon b: paramList){
                            removeBeaconByMacAddress(b);
                        }
                    }else{
                        stopRanging();
                        Log.i("NOTIFICATION ", "not found : "+beacons.size() +"  time : "+time);
                        if(! beacons.isEmpty()){
                            sendNotification(beacons);
                        }
                    }
                }catch(Exception e){
                    Log.e(TAG , "", e);
                }
            }
        });
        connectToService();
    }

    private void removeBeaconByMacAddress(Beacon beacon){
        Iterator<Beacon> iterator = beacons.iterator();
        while(iterator.hasNext()){
            Beacon b = iterator.next();
            if(b.getMacAddress().equals(beacon.getMacAddress())){
                iterator.remove();
                break;
            }
        }
    }

    private void sendNotification(List<Beacon> beacons){
        String title = "Found : accessoire trop éloigné";
        if(beacons.size() > 1){
            title = "Found : accessoires trop éloignés";
        }

        StringBuilder content = new StringBuilder();
        int index = 0;
        for (Beacon beacon: beacons) {
            Log.i("NOT FOUND ", " BEACON "+beacon.getName()+"  "+beacon.getMacAddress());
            content.append(beacon.getName());
            if(index < beacons.size() - 1){
                content.append(", ");
            }
            index++;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(content.toString());
        notificationManager.notify(NOTIFICATION_ID,mBuilder.build());
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
        if(beaconManager != null){
            try {
                beaconManager.stopRanging(ALL_BEACONS_REGION);

            } catch (RemoteException e) {
                Log.e(TAG,"",e);
            }finally {
                beaconManager.disconnect();
            }
        }
    }


    @Override
    public boolean onStopJob(JobParameters params) {
        if(beaconManager != null){
            stopRanging();
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG , "On start command");
        return Service.START_STICKY;
    }

}
