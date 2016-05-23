package com.a60circuits.foundbeacons.cache;

import android.content.Context;

import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * Created by zoz on 23/05/2016.
 */
public class BeaconCacheManager extends Observable{

    private List<Beacon> data;

    private BeaconDao dao;

    private volatile long version;

    public BeaconCacheManager(){
        version = 0;
    }

    public void loadData(){
        List<Beacon> loadedData = dao.findAll();
        if(loadedData == null){
            data = Collections.synchronizedList(new ArrayList<Beacon>());
        }else{
            data = Collections.synchronizedList(loadedData);
        }
        notifyChanges();
    }

    public void addBeacon(Beacon beacon){
        data.add(beacon);
        notifyChanges();
    }

    public synchronized void saveData(){
        dao.save(data);
    }

    public synchronized void deleteData(){
        dao.deleteAll();
    }

    public void notifyChanges(){
        version++;
        notifyObservers(data);
    }

    public void setDao(BeaconDao dao) {
        this.dao = dao;
    }

    public long getVersion() {
        return version;
    }

    public List<Beacon> getData() {
        return data;
    }

    public static BeaconCacheManager getInstance() {
        return CacheManagerHolder.cacheManager;
    }

    private static class CacheManagerHolder{
        private static BeaconCacheManager cacheManager = new BeaconCacheManager();
    }
}
