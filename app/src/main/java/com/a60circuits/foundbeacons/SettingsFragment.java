package com.a60circuits.foundbeacons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.a60circuits.foundbeacons.service.NotificationService;
import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.service.PermanentScheduledService;

/**
 * Created by zoz on 17/05/2016.
 */
public class SettingsFragment extends Fragment{

    private Switch gpsSwitch;
    private Switch notificationSwitch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment,container,false);
        gpsSwitch = (Switch) view.findViewById(R.id.gps_switch);
        notificationSwitch = (Switch) view.findViewById(R.id.notification_switch);

        gpsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsSwitch.setChecked(true);
            }
        });

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(notificationSwitch.isChecked()){
                    NotificationServiceManager.getInstance().startNotificationService();
                    NotificationServiceManager.getInstance().doBindService();
                }else{
                    NotificationServiceManager.getInstance().killNoficationService();
                }
            }
        });

        return view;
    }
}
