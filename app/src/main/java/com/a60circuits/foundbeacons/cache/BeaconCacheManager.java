package com.a60circuits.foundbeacons.cache;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.utils.LocationUtils;
import com.google.android.gms.maps.model.LatLng;
import com.jaalee.sdk.Beacon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
 * Created by zoz on 23/05/2016.
 */
public class BeaconCacheManager extends Observable{

    private List<Beacon> data;

    private BeaconDao dao;

    private Activity activity;

    private boolean empty;

    private volatile long version;

    public BeaconCacheManager(){
        version = 0;
        data = new ArrayList<>();
        empty = true;
    }

    public void loadData(){
        List<Beacon> loadedData = dao.findAll();
        if(loadedData != null){
            data = Collections.synchronizedList(loadedData);
            empty = false;
        }
        notifyChanges();
    }

    public Beacon findLastDetectedBeacon(){
        Beacon beacon = null;
        if(data != null && ! data.isEmpty()){
            beacon = data.get(0);
            Date lastDetected = beacon.getDate();
            for (Beacon b: data){
                if(b.getDate().after(lastDetected)){
                    beacon = b;
                    lastDetected = b.getDate();
                }
            }
        }
        return beacon;
    }

    public void addBeacon(Beacon beacon){
        data.add(beacon);
        notifyChanges();
    }

    public void save(Beacon beacon){
        Location location = LocationUtils.getLastKnownLocation(activity);
        if(location != null){
            beacon.setLatitude(location.getLatitude());
            beacon.setLongitude(location.getLongitude());
        }
        beacon.setDate(new Date());
        data.add(beacon);
        dao.save(data);
        notifyChanges();
    }

    public synchronized void saveAll(){
        dao.save(data);
    }

    public synchronized void deleteAll(){
        data.clear();
        dao.deleteAll();
        empty = true;
    }

    public void notifyChanges(){
        version++;
        setChanged();
        notifyObservers(data);
    }

    public void setDao(BeaconDao dao) {
        this.dao = dao;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public long getVersion() {
        return version;
    }

    public List<Beacon> getData() {
        return data;
    }

    public boolean isEmpty() {
        return empty;
    }

    public static BeaconCacheManager getInstance() {
        return CacheManagerHolder.cacheManager;
    }

    private static class CacheManagerHolder{
        private static BeaconCacheManager cacheManager = new BeaconCacheManager();
    }
}
