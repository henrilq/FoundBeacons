package com.a60circuits.foundbeacons;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
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
import android.widget.TextView;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
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
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
            googleMap = mMapView.getMap();
            if (PermissionUtils.checkPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)){
                googleMap.setMyLocationEnabled(true);
            }
            googleMap.setOnMarkerClickListener(this);

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    View view = getLayoutInflater(new Bundle()).inflate(R.layout.map_beacon_info, null);
                    TextView textView = (TextView) view.findViewById(R.id.text);
                    textView.setText(marker.getTitle()+"\n"+marker.getSnippet());
                    return view;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return null;
                }
            });

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
        } catch (Exception e) {
            Log.e("","",e);
        }
        return v;
    }

    private MarkerOptions createMarker(Beacon beacon){
        Date date = beacon.getDate();
        String dateformated = "LE "+DATE_FORMAT.format(date)+" A "+HOUR_FORMAT.format(date);
        LatLng latLng = new LatLng(beacon.getLatitude(), beacon.getLongitude());

        /*LevelListDrawable d=(LevelListDrawable) getResources().getDrawable(R.drawable.pin2x, null);
        d.setLevel(1234);
        BitmapDrawable bd=(BitmapDrawable) d.getCurrent();
        Bitmap b=bd.getBitmap();
        Bitmap bhalfsize=Bitmap.createScaledBitmap(b, b.getWidth()/2,b.getHeight()/2, false);*/
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.pin);
        BitmapDescriptor imageMarker = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        MarkerOptions marker = new MarkerOptions().title(beacon.getName()).snippet(dateformated).icon(descriptor).position(latLng);
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