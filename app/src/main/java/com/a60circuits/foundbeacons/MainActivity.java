package com.a60circuits.foundbeacons;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompatBase;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.service.NotificationService;
import com.a60circuits.foundbeacons.service.PermanentScheduledService;
import com.jaalee.sdk.BeaconManager;

public class MainActivity extends AppCompatActivity {

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
                    killNoficationService();
                }
            });
        }
        startService(new Intent(getApplicationContext(), NotificationService.class));
        doBindService();
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
        cache.loadData();
    }

    public void killNoficationService() {
        if (isServiceBound())
            notificationService.stopActionEvent();
        doUnbindService();
        stopService(new Intent(getApplicationContext(), NotificationService.class));
    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationService = ((PermanentScheduledService.LocalBinder)service).getService();
            //chkUseAlarm.setChecked(NotificationService.getUseAlarm());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationService = null;
        }
    };
    private void doBindService() {
        bindService(new Intent(getApplicationContext(), NotificationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }
    private void doUnbindService() {
        if (notificationService != null) {
            unbindService(serviceConnection);
            notificationService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        BeaconCacheManager.getInstance().deleteObservers();
    }

    private boolean isServiceBound() {
        return notificationService != null;
    }
}
