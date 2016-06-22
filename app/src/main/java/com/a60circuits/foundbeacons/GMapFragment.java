package com.a60circuits.foundbeacons;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.utils.LayoutUtils;
import com.a60circuits.foundbeacons.utils.LocationUtils;
import com.a60circuits.foundbeacons.utils.PermissionUtils;
import com.a60circuits.foundbeacons.utils.ResourcesUtils;
import com.a60circuits.foundbeacons.utils.SharedPreferencesUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jaalee.sdk.Beacon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GMapFragment extends ReplacerFragment implements GoogleMap.OnMarkerClickListener{

    public static final String BEACON_ARGUMENT = "beacon";
    private MapView mMapView;
    private GoogleMap googleMap;
    private Beacon beacon;
    private Map<Marker, Beacon> markerToBeacon = new HashMap<>();
    private Marker selectedMarker;
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    private final static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");
    private ImageButton detectionButton;
    private ImageButton lastPositionButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment, container,
                false);
        List<Beacon> beacons = BeaconCacheManager.getInstance().getData();
        if(beacons != null && ! beacons.isEmpty()){
            if(getArguments() != null){
                beacon = (Beacon)getArguments().get(BEACON_ARGUMENT);
            }else{
                beacon = BeaconCacheManager.getInstance().findInCacheLastDetectedBeacon();
            }
        }
        boolean gpsEnabled = SharedPreferencesUtils.getBoolean(getActivity(), SettingsFragment.GPS_ENABLED, false);
        if(!gpsEnabled){
            if(getArguments() != null){
                if (getArguments().get(BEACON_ARGUMENT) != null){
                    Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.gps_disabled), Toast.LENGTH_SHORT).show();
                }
            }
            goToDetectionScreen();
        }else if(beacon == null){
            Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.no_beacon_saved), Toast.LENGTH_SHORT).show();
            goToDetectionScreen();
        }else if(beacon.getLatitude() == 0.0 && beacon.getLongitude() == 0.0){
            goToDetectionScreen();
        }else{
            mMapView = (MapView) v.findViewById(R.id.mapView);
            detectionButton = (ImageButton) v.findViewById(R.id.detectionButton);
            lastPositionButton = (ImageButton) v.findViewById(R.id.lastPositionButton);
            LayoutUtils.overLapView(detectionButton,lastPositionButton,true);
            detectionButton.setColorFilter(null);
            lastPositionButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));

            detectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToDetectionScreen();
                }
            });

            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();// needed to get the map to display immediately
            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
                googleMap = mMapView.getMap();
                if (PermissionUtils.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                    //googleMap.setMyLocationEnabled(true);
                }
                googleMap.setOnMarkerClickListener(this);
                googleMap.setPadding(0, 0, 0, 200);//Move up itinerary button
                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        View view = getLayoutInflater(new Bundle()).inflate(R.layout.map_beacon_info, null);
                        TextView textView = (TextView) view.findViewById(R.id.text);
                        textView.setText(marker.getTitle()+"\n"+marker.getSnippet());
                        Typeface face = ResourcesUtils.getTypeFace(getActivity(),R.string.font_brandon_bld);
                        textView.setTypeface(face);
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

                if(beacon != null){
                    googleMap.addMarker(createMarker(beacon));
                    myPosition = new LatLng(beacon.getLatitude(), beacon.getLongitude());
                }else{
                    Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.no_beacon_saved), Toast.LENGTH_SHORT).show();
                }
                if(myPosition != null){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                }
            } catch (Exception e) {
                Log.e("","",e);
            }
        }
        return v;
    }

    private void goToDetectionScreen(){
        DetectionFragment detectionFragment = new DetectionFragment();
        if(beacon != null){
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetectionFragment.BEACON_ARGUMENT, beacon);
            detectionFragment.setArguments(bundle);
        }
        GMapFragment.super.replaceFragment(detectionFragment);
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