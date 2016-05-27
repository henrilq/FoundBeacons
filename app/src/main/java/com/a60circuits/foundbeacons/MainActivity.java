package com.a60circuits.foundbeacons;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.service.BeaconConnectionService;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.service.PermanentScheduledService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PermanentScheduledService notificationService;

    private ImageButton[] menuButtons;

    private ImageButton selectedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        ab.setDisplayShowTitleEnabled(false);

        initBeaconCache();
        NotificationServiceManager.getInstance().setActivity(this);

        final ImageButton settingsButton = (ImageButton)findViewById(R.id.b1);
        final ImageButton mapButton = (ImageButton)findViewById(R.id.b2);
        final ImageButton objectsButton = (ImageButton)findViewById(R.id.b3);

        menuButtons = new        ImageButton[]{settingsButton, mapButton, objectsButton};

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
            selectImageButton(button);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.central, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void selectImageButton(ImageButton button){
        selectedButton = button;
        for(ImageButton b: menuButtons){
            b.setColorFilter(null);
        }
        selectedButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
    }

    private void initBeaconCache(){
        BeaconCacheManager cache = BeaconCacheManager.getInstance();
        BeaconDao dao = new BeaconDao(getApplicationContext());
        cache.setDao(dao);
        cache.setContext(getApplicationContext());
        cache.loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,BeaconScannerService.class));
        stopService(new Intent(this,BeaconConnectionService.class));
        BeaconCacheManager.getInstance().deleteObservers();
    }

}
