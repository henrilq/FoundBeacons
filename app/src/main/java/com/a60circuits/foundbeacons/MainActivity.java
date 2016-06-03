package com.a60circuits.foundbeacons;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.cache.CacheVariable;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.service.BeaconConnectionService;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.a60circuits.foundbeacons.service.NotificationServiceManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String PREF_FILE = "prefFile";
    public static final String BUTTON_POSITION = "buttonPosition";
    public static final String SCAN_RESULT = "com.a60circuits.foundbeacons.result";

    private Map<ImageButton, Class<? extends Fragment>> map;
    private ImageButton selectedButton;
    private ImageButton scanButton;
    private int buttonPosition;
    private boolean scanning;

    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver;

    private ImageButton settingsButton;
    private ImageButton mapButton;
    private ImageButton objectsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);

        settingsButton = (ImageButton) findViewById(R.id.b1);
        mapButton = (ImageButton)findViewById(R.id.b2);
        objectsButton = (ImageButton)findViewById(R.id.b3);
        scanButton = (ImageButton) tb.findViewById(R.id.scanButton);

        initMapButton();
        initPermissions();
        initBeaconCache();
        initBroadcastReceiver();

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanning = !scanning;
                if(scanning){
                    scanButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
                    Intent i = new Intent(MainActivity.this,BeaconScannerService.class);
                    i.putExtra(BeaconScannerService.CONNECTION_MODE, true);
                    MainActivity.this.startService(i);
                }else{
                    scanButton.setColorFilter(null);
                    stopService(new Intent(MainActivity.this,BeaconScannerService.class));
                    stopService(new Intent(MainActivity.this,BeaconConnectionService.class));
                }
            }
        });

        NotificationServiceManager.getInstance().setActivity(this);
        Object lastPosition = CacheVariable.get(BUTTON_POSITION);
        if(lastPosition == null){
            replaceFragment(settingsButton);

        }else{
            selectMenuButton(Integer.valueOf(""+lastPosition));
        }
        settingsButton.setOnClickListener(createButtonListener(settingsButton));
        mapButton.setOnClickListener(createButtonListener(mapButton));
        objectsButton.setOnClickListener(createButtonListener(objectsButton));
        //starts app on beacon list fragment
        replaceFragment(objectsButton);
    }

    private void replaceFragment(ImageButton button){
        try {
            Fragment fragment = map.get(button).newInstance();
            replaceFragment(button, fragment);
        } catch (Exception e) {
            Log.e(TAG,"",e);
        }
    }

    private void replaceFragment(ImageButton button, Fragment fragment){
        if(selectedButton == null || ! button.equals(selectedButton)){
            selectMenuButton(button);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.central, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public ImageButton selectMenuButton(ImageButton button){
        selectedButton = button;
        int index = 0;
        for(ImageButton imageButton: map.keySet()){
            if(button.equals(imageButton)){
                buttonPosition = index;
            }
            imageButton.setColorFilter(null);
            index++;
        }
        selectedButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
        return selectedButton;
    }

    public ImageButton selectMenuButton(int position){
        if(position >= 0){
            buttonPosition = position;
            int index = 0;
            for (ImageButton imageButton: map.keySet()){
                if(position == index){
                    selectedButton = imageButton;
                }else{
                    imageButton.setColorFilter(null);
                }
                index++;
            }
            selectedButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
        }
        return selectedButton;
    }

    private void initBeaconCache(){
        BeaconCacheManager cache = BeaconCacheManager.getInstance();
        BeaconDao dao = new BeaconDao(getApplicationContext());
        cache.setDao(dao);
        cache.setActivity(this);
        cache.loadData();
    }

    private void initMapButton(){
        map = new LinkedHashMap<>();
        map.put(settingsButton, SettingsFragment.class);
        map.put(mapButton, GMapFragment.class);
        map.put(objectsButton, ObjectsFragment.class);
    }

    private void initPermissions(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (! ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
            }
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getBaseContext(),getResources().getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void initBroadcastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanning = false;
                scanButton.setColorFilter(null);
                String message = intent.getStringExtra(BeaconConnectionService.TAG);
                if(message != null){
                    Toast.makeText(MainActivity.this.getBaseContext(),message, Toast.LENGTH_LONG).show();
                }
            }
        };
        filter = new IntentFilter();
        filter.addAction(SCAN_RESULT);
    }

    private View.OnClickListener createButtonListener(final ImageButton button){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(button);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(this,BeaconScannerService.class));
        stopService(new Intent(this,BeaconConnectionService.class));
        BeaconCacheManager.getInstance().deleteObservers();
        CacheVariable.put(BUTTON_POSITION, buttonPosition);
    }

}
