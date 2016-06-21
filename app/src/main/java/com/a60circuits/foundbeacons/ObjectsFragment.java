package com.a60circuits.foundbeacons;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.cache.CacheVariable;
import com.jaalee.sdk.Beacon;
import com.jaalee.sdk.Region;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by zoz on 17/05/2016.
 */
public class ObjectsFragment extends ReplacerFragment implements Observer{

    private static final Region ALL_BEACONS_REGION = new Region("rid", null, null, null);
    public final static String SCANNING = "Scanning";

    private RecyclerView beaconsView;
    private Button addButton;
    private List<Beacon> beacons;
    private Set<String> beaconsAddress;
    private LinearLayoutManager layoutManager;
    private BeaconAdapter adapter;
    private Handler handler;
    private TextView text;
    private GifImageView loader;
    private View line;
    private ImageButton scanButton;
    private MainActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        beaconsAddress = new HashSet<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.objects_fragment,container,false);
        BeaconCacheManager.getInstance().addObserver(this);
        mainActivity = (MainActivity)getActivity();
        beacons = new ArrayList<>();
        adapter = new BeaconAdapter(beacons);
        beaconsView = (RecyclerView) view.findViewById(R.id.beaconsView);
        beaconsView.setHasFixedSize(true);
        loader = (GifImageView)view.findViewById(R.id.loader);

        Typeface face = Typeface.createFromAsset(getActivity().getAssets(),getResources().getString(R.string.font_brandon_med));
        text = (TextView)view.findViewById(R.id.text);
        text.setTypeface(face);

        line = (View)view.findViewById(R.id.firstLine);
        scanButton = (ImageButton)view.findViewById(R.id.scanButton);

        layoutManager = new LinearLayoutManager(this.getContext());
        beaconsView.setLayoutManager(layoutManager);
        beaconsView.setAdapter(adapter);

        List<Beacon> savedBeacons = BeaconCacheManager.getInstance().getData();
        if(savedBeacons != null && ! savedBeacons.isEmpty()){
            setBeacons(savedBeacons);
        }
        initListeners();
        reset();
        return view;
    }

    private void initListeners(){
        beaconsView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                Beacon selectedBeacon = beacons.get(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable(DetectionFragment.BEACON_ARGUMENT, selectedBeacon);
                DetectionFragment fragment = new DetectionFragment();
                fragment.setArguments(bundle);
                ObjectsFragment.super.replaceFragment(fragment);
            }
        }));
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.startScan();
                reset();
            }
        });
    }

    private void setBeacons(List<Beacon> newBeacons){
        beacons.clear();
        beaconsAddress.clear();
        addBeacons(newBeacons);
        reset();
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


    public void reset(){
        final boolean scanMode = CacheVariable.getBoolean(MainActivity.SCANNING);
        boolean emptyCache = BeaconCacheManager.getInstance().getData().isEmpty();
        final int firstStepVisibility = emptyCache & !scanMode ? View.VISIBLE : View.INVISIBLE;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(scanMode){
                    loader.setVisibility(View.VISIBLE);
                }else{
                    loader.setVisibility(View.INVISIBLE);
                }
                text.setVisibility(firstStepVisibility);
                line.setVisibility(firstStepVisibility);
                scanButton.setVisibility(firstStepVisibility);
            }
        });
    }

    @Override
    public void onPause() {
        loader.setVisibility(View.INVISIBLE);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

