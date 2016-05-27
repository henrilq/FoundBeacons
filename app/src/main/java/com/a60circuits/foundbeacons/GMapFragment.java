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
import java.util.List;

public class GMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener{

    private MapView mMapView;
    private GoogleMap googleMap;
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
                transaction.replace(R.id.central, new DetectionFragment());
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

        // latitude and longitude
        double latitude = 17.385044;
        double longitude = 78.486671;
        LatLng latLng = new LatLng(latitude, longitude);
        LatLng myPosition = null;
        Log.i("TEST", " "+ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION));
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
        // create marker
        MarkerOptions marker = new MarkerOptions().title("Hello Maps").snippet("Twitter HQ").icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).position(latLng);

        List<Beacon> beacons = BeaconCacheManager.getInstance().getData();
        if(beacons != null){
            for (Beacon b: beacons){
                googleMap.addMarker(createMarker(b));
            }
        }
        Location location = LocationUtils.getLastKnownLocation(getActivity());
        if(location != null){
            myPosition = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
        }
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


        return false;
    }
}