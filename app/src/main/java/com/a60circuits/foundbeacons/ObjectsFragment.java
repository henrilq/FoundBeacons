package com.a60circuits.foundbeacons;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.service.BeaconConnectionService;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * Created by zoz on 17/05/2016.
 */
public class ObjectsFragment extends ReplacerFragment implements Observer{

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);

    private RecyclerView beaconsView;
    private Button addButton;
    private List<Beacon> beacons;
    private Set<String> beaconsAddress;
    private LinearLayoutManager layoutManager;
    private BeaconAdapter adapter;
    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        beaconsAddress = new HashSet<>();
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.objects_fragment,container,false);
        BeaconCacheManager.getInstance().addObserver(this);
        beacons = new ArrayList<>();
        adapter = new BeaconAdapter(beacons);
        beaconsView = (RecyclerView) view.findViewById(R.id.beaconsView);
        beaconsView.setHasFixedSize(true);

        beaconsView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                Beacon selectedBeacon = beacons.get(position);
                Log.i("TEST ", "CLICKED "+selectedBeacon.getName());
                Bundle bundle = new Bundle();
                bundle.putParcelable(DetectionFragment.BEACON_ARGUMENT, selectedBeacon);
                replaceByFragment(new DetectionFragment(), bundle);
                selectMenuButton(1);
            }
        }));

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
        Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BeaconCacheManager.getInstance().deleteAll();
                beacons.clear();
                beaconsAddress.clear();
                adapter.notifyDataSetChanged();
            }
        });

        layoutManager = new LinearLayoutManager(this.getContext());
        beaconsView.setLayoutManager(layoutManager);
        beaconsView.setAdapter(adapter);

        List<Beacon> savedBeacons = BeaconCacheManager.getInstance().getData();
        if(savedBeacons != null && ! savedBeacons.isEmpty()){
            setBeacons(savedBeacons);
        }
        return view;
    }

    private void setBeacons(List<Beacon> newBeacons){
        beacons.clear();
        beaconsAddress.clear();
        addBeacons(newBeacons);
    }


    private void addBeacons(final List<Beacon> newBeacons){
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean changed = false;
                for (Beacon beacon: newBeacons){
                    String mac = beacon.getMacAddress();
                    if(! beaconsAddress.contains(mac)){
                        beacons.add(beacon);
                        beaconsAddress.add(mac);
                        changed = true;
                    }
                }
                if(changed){
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getContext().stopService(new Intent(getContext(),BeaconScannerService.class));
        getContext().stopService(new Intent(getContext(),BeaconConnectionService.class));
        BeaconCacheManager.getInstance().deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        setBeacons((List<Beacon>)data);
    }
}

