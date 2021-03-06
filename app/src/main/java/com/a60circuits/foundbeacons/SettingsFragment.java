package com.a60circuits.foundbeacons;

import android.Manifest;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.utils.LayoutUtils;
import com.a60circuits.foundbeacons.utils.LocationUtils;
import com.a60circuits.foundbeacons.utils.PermissionUtils;
import com.a60circuits.foundbeacons.utils.ResourcesUtils;
import com.a60circuits.foundbeacons.utils.SharedPreferencesUtils;

/**
 * Created by zoz on 17/05/2016.
 */
public class SettingsFragment extends Fragment {

    public static final String GPS_ENABLED = "gpsEnabled";
    public static final String NOTIFICATION_ENABLED = "notificationEnabled";

    private Switch gpsSwitch;
    private Switch notificationSwitch;
    private ImageButton legalButton;
    private ImageButton faqButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        gpsSwitch = (Switch) view.findViewById(R.id.gps_switch);
        notificationSwitch = (Switch) view.findViewById(R.id.notification_switch);
        legalButton = (ImageButton) view.findViewById(R.id.legal_button);
        faqButton = (ImageButton) view.findViewById(R.id.faq_button);
        LayoutUtils.overLapView(legalButton, faqButton, true);
        legalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                legalButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorSelectionBlue));
                faqButton.setColorFilter(null);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putBoolean(SettingsTextFragment.LEGAL_MENTION, true);
                Fragment fragment = new SettingsTextFragment();
                fragment.setArguments(bundle);
                transaction.replace(R.id.central, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        faqButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                faqButton.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorSelectionBlue));
                legalButton.setColorFilter(null);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Bundle bundle = new Bundle();
                bundle.putBoolean(SettingsTextFragment.FAQ, true);
                Fragment fragment = new SettingsTextFragment();
                fragment.setArguments(bundle);
                transaction.replace(R.id.central, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        TextView paramText = (TextView) view.findViewById(R.id.parameter_text);
        final TextView gpsText = (TextView) view.findViewById(R.id.gps_text);
        TextView notificationText = (TextView) view.findViewById(R.id.notification_text);
        TextView detailText = (TextView) view.findViewById(R.id.detail_text);
        TextView detail1 = (TextView) view.findViewById(R.id.detail_1);
        TextView detail2 = (TextView) view.findViewById(R.id.detail_2);

        Typeface face = ResourcesUtils.getTypeFace(getContext(), R.string.font_brandon_med);
        paramText.setTypeface(face);
        gpsText.setTypeface(face);
        notificationText.setTypeface(face);
        detailText.setTypeface(face);
        detail1.setTypeface(face);
        detail2.setTypeface(face);

        notificationSwitch.setChecked(SharedPreferencesUtils.getBoolean(getActivity(), NOTIFICATION_ENABLED, false));
        gpsSwitch.setChecked(SharedPreferencesUtils.getBoolean(getActivity(), GPS_ENABLED, false));

        if(!PermissionUtils.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
            gpsSwitch.setChecked(false);
            SharedPreferencesUtils.putBoolean(getActivity(), NOTIFICATION_ENABLED, false);
        }

        notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtils.putBoolean(getActivity(), NOTIFICATION_ENABLED, notificationSwitch.isChecked());
                if(notificationSwitch.isChecked()){
                    NotificationServiceManager.getInstance().startNotificationService();
                }else{
                    NotificationServiceManager.getInstance().killNoficationService();
                }
            }
        });

        gpsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesUtils.putBoolean(getActivity(), GPS_ENABLED, gpsSwitch.isChecked());
                if(gpsSwitch.isChecked()){
                    LocationUtils.requestLocationUpdates(getActivity());
                }else{
                    LocationUtils.removeListeners();
                }
            }
        });
        return view;
    }
}
