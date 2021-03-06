package com.a60circuits.foundbeacons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.cache.CacheVariable;
import com.a60circuits.foundbeacons.dao.BeaconDao;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.utils.LocationUtils;
import com.a60circuits.foundbeacons.utils.PermissionUtils;
import com.google.android.gms.maps.MapView;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String BUTTON_POSITION = "buttonPosition";
    public static final String SCAN_RESULT = "com.a60circuits.foundbeacons.result";
    public static final String SERVICE_STOP = "Service_Stop";
    public static final String SERVICE_INFO = "Service_Info";
    public static final String SERVICE_SUCCESS = "Service_Success";
    public static final String SCANNING = "Scanning";
    public static final String START_FIRST_SCAN = "Start_First_Scan";
    public static final String FIRST_RENAMING = "First_Renaming";

    private Map<ImageButton, List<Class<? extends Fragment>>> map;
    private ImageButton selectedButton;
    private ImageButton scanButton;
    private int buttonPosition;

    private Handler handler;
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver;

    private ImageButton settingsButton;
    private ImageButton mapButton;
    private ImageButton objectsButton;
    private View logo;
    private View backButton;
    private ImageView backButtonArrow;
    private TextView backButtonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocationUtils.requestLocationUpdates(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        backButton = toolbar.findViewById(R.id.back_button);
        backButtonArrow = (ImageView) toolbar.findViewById(R.id.back_button_arrow);
        backButtonText = (TextView) toolbar.findViewById(R.id.back_button_text);
        setSupportActionBar(toolbar);

        handler = new Handler();

        settingsButton = (ImageButton) findViewById(R.id.b1);
        objectsButton = (ImageButton)findViewById(R.id.b2);
        mapButton = (ImageButton)findViewById(R.id.b3);
        scanButton = (ImageButton) toolbar.findViewById(R.id.scanButton);
        logo = (View) toolbar.findViewById(R.id.toolbar_logo);

        initToolbar(toolbar);
        initMapButton();
        initBeaconCache();
        initBroadcastReceiver();
        initListeners();
        initMap();

        NotificationServiceManager.getInstance().setActivity(this);
        Object lastPosition = CacheVariable.get(BUTTON_POSITION);
        if(lastPosition == null){
            replaceFragment(settingsButton);
        }else{
            selectMenuButton(Integer.valueOf(""+lastPosition));
        }

        //starts app on beacon list fragment
        replaceFragment(objectsButton);

        if(CacheVariable.getBoolean(START_FIRST_SCAN)){
            startScan();
        }
    }

    private void initToolbar(final Toolbar toolbar){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true); // show or hide the default home button
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        actionBar.setDisplayShowTitleEnabled(false);
        backButtonArrow.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorFoundLogo));
        backButtonText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.colorFoundLogo));
        backButtonText.setTypeface(Typeface.createFromAsset(getAssets(),getResources().getString(R.string.font_brandon_med)));
    }

    public void initListeners(){
        settingsButton.setOnClickListener(createButtonListener(settingsButton));
        mapButton.setOnClickListener(createButtonListener(mapButton));
        objectsButton.setOnClickListener(createButtonListener(objectsButton));
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                replaceByLastFragment();
            }
        });
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(objectsButton);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void initMap(){
        //ugly but fix first loading map slowness
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                }catch (Exception ignored){
                    //Log.w(TAG, ignored);
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        getSupportFragmentManager().popBackStack();
        return super.onOptionsItemSelected(item);
    }

    public void replaceByLastFragment(){
        Fragment fragment = null;
        try{
            for(Fragment f: getSupportFragmentManager().getFragments()){
                if(f != null){
                    if(f.isVisible()){
                        fragment = f;
                        break;
                    }
                }
            }
            if(fragment != null){
                showArrow(fragment.getClass().isAssignableFrom(SettingsTextFragment.class));
            }

            if(fragment != null){
                for(Map.Entry<ImageButton, List<Class<? extends Fragment>>> entry: map.entrySet()){
                    for(Class<?> clazz: entry.getValue()){
                        if(clazz.equals(fragment.getClass())){
                            selectMenuButton(entry.getKey());
                            break;
                        }
                    }
                }
            }
        }catch(Exception e){
            Log.e(TAG, "",e);
        }
    }

    public void replaceFragment(ImageButton button){
        replaceFragment(button, null);
    }

    private void replaceFragment(ImageButton button, Bundle bundle){
        replaceFragment(button, bundle, false);
    }

    private void replaceFragment(ImageButton button, Bundle bundle, boolean forceReplacement){
        try {
            Fragment fragment = map.get(button).get(0).newInstance();
            replaceFragment(button, fragment, bundle, forceReplacement);
        } catch (Exception e) {
            Log.e(TAG,"",e);
        }
    }

    private void replaceFragment(ImageButton button, Fragment fragment, Bundle bundle, boolean forceReplacement){
        if(selectedButton == null || ! button.equals(selectedButton) || forceReplacement){
            showArrow(false);
            if(bundle != null){
                fragment.setArguments(bundle);
            }
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
        addInMap(settingsButton, SettingsFragment.class, SettingsTextFragment.class);
        addInMap(objectsButton, ObjectsFragment.class);
        addInMap(mapButton, GMapFragment.class, DetectionFragment.class);
    }

    private void addInMap(ImageButton button, Class<? extends Fragment> ... fragments){
        map.put(button, Arrays.asList(fragments));
    }

    private void initBroadcastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean stop = false;
                boolean success = false;
                String message = intent.getStringExtra(SERVICE_INFO);
                if(message == null || message.isEmpty()){
                    message = intent.getStringExtra(SERVICE_STOP);
                    if(message == null || message.isEmpty()){
                        message = intent.getStringExtra(SERVICE_SUCCESS);
                        if(message != null && ! message.isEmpty()){
                            success = true;
                            CacheVariable.put(FIRST_RENAMING, true);
                        }
                    }else{
                        stop = true;
                    }
                    if(stop || success){
                        stopScan();
                        //reload objects fragment
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(ObjectsFragment.SCANNING, false);
                        replaceFragment(objectsButton, bundle, true);
                    }

                }
                if(message != null && ! message.isEmpty()){
                    Toast.makeText(MainActivity.this.getBaseContext(),message, Toast.LENGTH_SHORT).show();
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

    public void startScan(){
        if(! CacheVariable.getBoolean(SCANNING)){
            if(PermissionUtils.requestBluetooth(this) || CacheVariable.getBoolean(START_FIRST_SCAN)){
                CacheVariable.put(SCANNING, true);
                replaceFragment(objectsButton, null, true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scanButton.setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorSelectionBlue));
                        Intent i = new Intent(MainActivity.this,BeaconScannerService.class);
                        i.putExtra(BeaconScannerService.CONNECTION_MODE, true);
                        MainActivity.this.startService(i);
                    }
                });
            }
        }
    }

    public void stopScan(){
        scanButton.setColorFilter(null);
        CacheVariable.put(SCANNING, false);
        stopService(new Intent(this,BeaconScannerService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        stopScanOnPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BeaconCacheManager.getInstance().deleteObservers();
        CacheVariable.put(BUTTON_POSITION, buttonPosition);
    }

    private void stopScanOnPause(){
        try{
            unregisterReceiver(broadcastReceiver);
            stopScan();
        }catch(Exception e){
            Log.e(TAG,"",e);
        }
    }

    public ImageButton getMapButton() {
        return mapButton;
    }

    public ImageButton getSettingsButton() {
        return settingsButton;
    }

    public ImageButton getObjectsButton() {
        return objectsButton;
    }

    public void showArrow(boolean show) {
        if(show){
            backButton.setVisibility(View.VISIBLE);
        }else{
            backButton.setVisibility(View.INVISIBLE);
        }
    }
}
