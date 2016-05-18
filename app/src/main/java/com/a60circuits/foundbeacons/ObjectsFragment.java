package com.a60circuits.foundbeacons;

import android.Manifest;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.ButtonBarLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by zoz on 17/05/2016.
 */
public class ObjectsFragment extends Fragment implements Observer{

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);
    private Handler handler;
    private BeaconManager beaconManager;
    private List<ItemFragment> fragments = new ArrayList<>();
    private Map<String, String> beaconNames = new LinkedHashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.objects_fragment,container,false);

        Button button = (Button) view.findViewById(R.id.addButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ItemFragment item = new ItemFragment();
                fragments.add(item);
                item.setObserver(ObjectsFragment.this);
                fragmentTransaction.add(R.id.objects, item, "TEST");
                fragmentTransaction.commit();
            }
        });
        connectToService();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        beaconManager = new BeaconManager(ObjectsFragment.this.getContext());
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List beacons) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        addBeacons(filterBeacons(beacons));
                    }
                });
            }
        });
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
        //backgroundThread.start();
    }

    private void connectToService() {
        Log.i("Thread", "Connect to service");
        beaconManager.connect(new ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRangingAndDiscoverDevice(ALL_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e("","",e);
                    e.printStackTrace();
                }
            }
        });
    }

    private List<Beacon> filterBeacons(List<Beacon> beacons) {
        List<Beacon> filteredBeacons = new ArrayList<>(beacons.size());
        for (Beacon beacon : beacons)
        {
            Log.i("Beacon", beacon.getName()+"  "+beacon.getMacAddress()+"   "+beacon.getRssi());
            String name = beaconNames.get(beacon.getMacAddress());
            if(name == null){
                beaconNames.put(beacon.getMacAddress(), beacon.getName());
                filteredBeacons.add(beacon);
            }
        }
        return filteredBeacons;
    }

    private void addBeacons(List<Beacon> beacons){
        for (Beacon beacon: beacons){
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ItemFragment item = new ItemFragment();
            item.setText(beacon.getName());
            fragments.add(item);
            item.setObserver(ObjectsFragment.this);
            fragmentTransaction.add(R.id.objects, item, "TEST");
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onDestroy() {
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        ItemFragment item = (ItemFragment)data;
        for (ItemFragment f: fragments){
            if(!f.equals(item)){
                f.setEnabled(false);
            }
        }
        if(!item.isEnabled()){

        }
    }
}

