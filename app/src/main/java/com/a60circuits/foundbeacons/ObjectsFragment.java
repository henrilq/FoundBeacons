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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.BeaconManager;
import com.jaalee.sdk.RangingListener;
import com.jaalee.sdk.Region;
import com.jaalee.sdk.ServiceReadyCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Created by zoz on 17/05/2016.
 */
public class ObjectsFragment extends Fragment {

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private RecyclerView beaconsView;
    private Button addButton;
    private List<Beacon> beacons;
    private LinearLayoutManager layoutManager;
    private BeaconManager beaconManager;
    private BeaconAdapter adapter;
    private Handler handler;
    private BeaconDao dao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        beaconManager = new BeaconManager(getContext());
        dao = new BeaconDao(getContext());
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.objects_fragment,container,false);
        beacons = new ArrayList<>();
        adapter = new BeaconAdapter(beacons);
        beaconsView = (RecyclerView) view.findViewById(R.id.beaconsView);
        beaconsView.setHasFixedSize(true);

        addButton = (Button) view.findViewById(R.id.addButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Beacon beacon = new Beacon();
                beacon.setName("New Beacon");
                beacons.add(beacon);
                adapter.notifyDataSetChanged();
            }
        });
        layoutManager = new LinearLayoutManager(this.getContext());
        beaconsView.setLayoutManager(layoutManager);
        beaconsView.setAdapter(adapter);
        beaconManager.setRangingListener(new RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List beacons) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        addBeacons(beacons);
                    }
                });
            }
        });
        List<Beacon> savedBeacons = dao.findAll();
        if(savedBeacons != null && ! savedBeacons.isEmpty()){
            beacons.clear();
            beacons.addAll(savedBeacons);
            adapter.notifyDataSetChanged();
        }
        connectToService();
        return view;
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
                }
            }
        });
    }


    private void addBeacons(List<Beacon> newBeacons){
        boolean changed = false;
        for (Beacon beacon: newBeacons){
            if (! beacons.contains(beacon)){
                beacons.add(beacon);
                changed = true;
            }
        }
        if(changed){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
    }
}

