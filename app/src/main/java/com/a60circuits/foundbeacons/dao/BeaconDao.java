package com.a60circuits.foundbeacons.dao;

import android.content.Context;

import com.jaalee.sdk.Beacon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zoz on 20/05/2016.
 */
public class BeaconDao {

    private BeaconJSonConverter converter = new BeaconJSonConverter();
    private File file;

    public BeaconDao(Context context){
        file = new File(context.getFilesDir(), "beacons");
    }

    public boolean save(List<Beacon> beacons){
        return converter.formatToFile(beacons, file);
    }

    public List<Beacon> findAll(){
        List<Beacon> list = null;
        if(file.exists()){
            list = converter.parseFromFile(file);
        }
        return list;
    }

    public boolean deleteAll(){
        return file.delete();
    }
}
