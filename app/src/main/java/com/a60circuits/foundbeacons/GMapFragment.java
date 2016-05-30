package com.a60circuits.foundbeacons;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.utils.LocationUtils;
import com.a60circuits.foundbeacons.utils.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaalee.sdk.Beacon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener{

    public static final String BEACON_ARGUMENT = "beacon";
    private MapView mMapView;
    private GoogleMap googleMap;
    private Beacon beacon;
    private Map<Marker, Beacon> markerToBeacon = new HashMap<>();
    private Marker selectedMarker;
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    private final static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        final ImageButton detectionButton = (ImageButton) v.findViewById(R.id.detectionButton);
        final ImageButton lastPositionButton = (ImageButton) v.findViewById(R.id.lastPositionButton);
        detectionButton.setColorFilter(null);
        lastPositionButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));



        detectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = GMapFragment.this.getFragmentManager().beginTransaction();
                DetectionFragment detectionFragment = new DetectionFragment();
                if(beacon != null){
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(DetectionFragment.BEACON_ARGUMENT, beacon);
                    detectionFragment.setArguments(bundle);
                }
                transaction.replace(R.id.central, detectionFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e("","",e);
        }

        googleMap = mMapView.getMap();
        if (PermissionUtils.checkPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)){
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setOnMarkerClickListener(this);

        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (! ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
                ActivityCompat.requestPermissions(this.getActivity(), new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
            }
        }
        LatLng myPosition = null;
        Location location = LocationUtils.getLastKnownLocation(getActivity());
        if(location != null){
            myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        }
        List<Beacon> beacons = BeaconCacheManager.getInstance().getData();
        if(beacons != null && ! beacons.isEmpty()){
            if(getArguments() != null){
                beacon = (Beacon)getArguments().get(BEACON_ARGUMENT);
            }
            if(beacons != null){
                for (Beacon b: beacons){
                    Marker marker = googleMap.addMarker(createMarker(b));
                    markerToBeacon.put(marker, b);
                }
            }
            if(beacon != null){
                myPosition = new LatLng(beacon.getLatitude(), beacon.getLongitude());
            }else{
                Beacon lastDetectedBeacon = BeaconCacheManager.getInstance().findInCacheLastDetectedBeacon();
                myPosition = new LatLng(lastDetectedBeacon.getLatitude(), lastDetectedBeacon.getLongitude());
            }
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        return v;
    }

    private MarkerOptions createMarker(Beacon beacon){
        Date date = beacon.getDate();
        String dateformated = "LE "+DATE_FORMAT.format(date)+" A "+HOUR_FORMAT.format(date);
        LatLng latLng = new LatLng(beacon.getLatitude(), beacon.getLongitude());
        MarkerOptions marker = new MarkerOptions().title(beacon.getName()).snippet(dateformated).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latLng);
        return marker;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mMapView != null){
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMapView != null){
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMapView != null){
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mMapView != null){
            mMapView.onLowMemory();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Beacon beacon = markerToBeacon.get(marker);
        if(beacon != null){
            this.beacon = beacon;
        }
        return false;
    }
}