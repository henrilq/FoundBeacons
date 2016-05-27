package com.a60circuits.foundbeacons.dao;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.jaalee.sdk.Beacon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zoz on 20/05/2016.
 */
public class BeaconJSonConverter {

    private Gson gson;
    private static final Type COLLECTION_TYPE = new TypeToken<List<Beacon>>(){}.getType();

    public BeaconJSonConverter(){
        gson = new GsonBuilder().create();
    }

    /*public String format(Beacon beacon){
        return gson.toJson(beacon);
    }

    public Beacon parse(String str){
        return gson.fromJson(str, Beacon.class);
    }

    public String formatFromList(List<Beacon> beacons){
        return gson.toJson(beacons, COLLECTION_TYPE);
    }

    public List<Beacon> parseFromList(String beaconValues){
        return gson.fromJson(beaconValues, COLLECTION_TYPE);
    }*/

    public List<Beacon> parseFromFile(File file){
        List<Beacon> beacons = null;
        try {
            JsonReader reader = new JsonReader(new FileReader(file));
            beacons = gson.fromJson(reader, COLLECTION_TYPE);
        } catch (FileNotFoundException e) {
            Log.e("","",e);
        }
        return beacons;
    }

    public boolean formatToFile(List<Beacon> beacons, File file){
        FileWriter writer = null;
        boolean success = true;
        try {
            writer = new FileWriter(file);
            String str = gson.toJson(beacons, COLLECTION_TYPE);
            writer.write(str);
        } catch (IOException e) {
            Log.e("","",e);
            success = false;
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                Log.e("","",e);
            }
        }
        return success;
    }



}
