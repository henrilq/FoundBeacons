package com.a60circuits.foundbeacons;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.a60circuits.foundbeacons.service.NotificationServiceManager;

/**
 * Created by zoz on 17/05/2016.
 */
public class SettingsFragment extends Fragment{

    public static final String NOTIFICATION_ENABLED = "notificationEnabled";

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

        final SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.PREF_FILE, 0);
        boolean notificationEnabled = settings.getBoolean(NOTIFICATION_ENABLED, false);
        notificationSwitch.setChecked(notificationEnabled);

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                if(notificationSwitch.isChecked()){
                    NotificationServiceManager.getInstance().startNotificationService();
                    editor.putBoolean(NOTIFICATION_ENABLED, true);
                }else{
                    NotificationServiceManager.getInstance().killNoficationService();
                    editor.putBoolean(NOTIFICATION_ENABLED, false);
                }
                editor.commit();
            }
        });

        return view;
    }
}
