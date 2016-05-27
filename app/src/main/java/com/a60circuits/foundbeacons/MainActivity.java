package com.a60circuits.foundbeacons;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.service.BeaconConnectionService;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.service.PermanentScheduledService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String SCAN_RESULT = "com.a60circuits.foundbeacons.result";

    private ImageButton[] menuButtons;
    private ImageButton selectedButton;
    private ImageButton scanButton;
    private boolean scanning;

    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        scanButton = (ImageButton) tb.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanning = !scanning;
                if(scanning){
                    scanButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
                    Intent i = new Intent(MainActivity.this,BeaconScannerService.class);
                    MainActivity.this.startService(i);
                }else{
                    scanButton.setColorFilter(null);
                    stopService(new Intent(MainActivity.this,BeaconScannerService.class));
                }
            }
        });

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);

        initPermissions();
        initBeaconCache();
        initBroadcastReceiver();

        NotificationServiceManager.getInstance().setActivity(this);

        final ImageButton settingsButton = (ImageButton)findViewById(R.id.b1);
        final ImageButton mapButton = (ImageButton)findViewById(R.id.b2);
        final ImageButton objectsButton = (ImageButton)findViewById(R.id.b3);

        menuButtons = new ImageButton[]{settingsButton, mapButton, objectsButton};

        if(settingsButton != null && mapButton != null && objectsButton != null){
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(settingsButton, new SettingsFragment());
                }
            });

            mapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(mapButton, new GMapFragment());
                }
            });

            objectsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(objectsButton, new ObjectsFragment());
                }
            });
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

    public void selectMenuButton(ImageButton button){
        selectedButton = button;
        for(ImageButton b: menuButtons){
            b.setColorFilter(null);
        }
        selectedButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
    }

    public void selectMenuButton(int position){
        for (int i = 0; i < menuButtons.length; i++) {
            if(position == i){
                selectedButton = menuButtons[i];
            }else{
                menuButtons[i].setColorFilter(null);
            }
        }
        selectedButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
    }

    private void initBeaconCache(){
        BeaconCacheManager cache = BeaconCacheManager.getInstance();
        BeaconDao dao = new BeaconDao(getApplicationContext());
        cache.setDao(dao);
        cache.setActivity(this);
        cache.loadData();
    }

    private void initPermissions(){
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },1);
        ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },1);
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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,BeaconScannerService.class));
        stopService(new Intent(this,BeaconConnectionService.class));
        BeaconCacheManager.getInstance().deleteObservers();
    }

}
