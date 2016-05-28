package com.a60circuits.foundbeacons;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.a60circuits.foundbeacons.cache.BeaconCacheManager;
import com.a60circuits.foundbeacons.service.BeaconConnectionService;
import com.a60circuits.foundbeacons.service.BeaconScannerService;
import com.jaalee.sdk.Beacon;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zoz on 26/05/2016.
 */
public class DetectionFragment extends Fragment {

    public static final String BEACON_ARGUMENT = "Beacon";
    public static final String DETECTION_RESULT = "detectionResult";

    private static final double MAX_DISTANCE = 10;

    private ProgressBar progressBar;
    private TextView beaconName;
    private TextView textView;
    private int animationDuration = 300;

    private int currentPosition;
    private boolean detectionStarted;

    private Beacon beacon;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    private Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detection_fragment,container,false);
        if(getArguments() == null){
            beacon = BeaconCacheManager.getInstance().findInCacheLastDetectedBeacon();
        }else{
            beacon = getArguments().getParcelable(BEACON_ARGUMENT);
        }
        if(beacon != null){
            beaconName = (TextView) view.findViewById(R.id.beaconName);
            beaconName.setText(beacon.getName());
        }
        initBroadcastReceiver();
        progressBar = (ProgressBar) view.findViewById(R.id.circleProgress);
        textView = (TextView) view.findViewById(R.id.nbText);

        final ImageButton detectionButton = (ImageButton) view.findViewById(R.id.detectionButton);
        final ImageButton lastPositionButton = (ImageButton) view.findViewById(R.id.lastPositionButton);

        lastPositionButton.setColorFilter(null);
        detectionButton.setColorFilter(ContextCompat.getColor(getContext(),R.color.colorSelectionBlue));

        lastPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = DetectionFragment.this.getFragmentManager().beginTransaction();
                transaction.replace(R.id.central, new GMapFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 1000);
        animation.setDuration (300); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(beacon == null){
                    Toast.makeText(getActivity().getBaseContext(),getResources().getString(R.string.no_beacon_saved), Toast.LENGTH_LONG).show();
                }else if(!detectionStarted){
                    startDetectionService();
                }
            }
        });

        return view;
    }

    private void startDetectionService(){
        Intent i = new Intent(getActivity(),BeaconScannerService.class);
        i.putExtra(BeaconScannerService.DETECTION_MODE, true);
        i.putExtra(BeaconScannerService.BEACON_ARGUMENT, beacon);
        getActivity().startService(i);
        detectionStarted = true;
    }

    private void initBroadcastReceiver(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int rssi = intent.getIntExtra(BeaconScannerService.TAG, 0);
                updateValue(computeDistance(rssi));
            }
        };

        filter = new IntentFilter();
        filter.addAction(DetectionFragment.DETECTION_RESULT);
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    private double computeDistance(int rssi){
        int absRssi = Math.abs(rssi);
        int measuredPower = 59;
        if (absRssi == 0.0D) {
            return -1.0D;
        }

        double ratio = absRssi * 1.0D / measuredPower;
        if (ratio < 1.0D) {
            return Math.pow(ratio, 8.0D);
        }

        return 0.69976D * Math.pow(ratio, 7.7095D) + 0.111D;
    }

    private void updateValue(double distance){
        double res = distance / MAX_DISTANCE * progressBar.getMax();
        res = Math.min(res, progressBar.getMax());
        Long resInt = Math.abs(Math.round(res) - progressBar.getMax());
        changeToPosition(resInt.intValue());
        sleep(animationDuration);
        updateTextView(roundToString(distance,2), "METRES");
    }

    private void changeToPosition(final int newPosition){
        handler.post(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "secondaryProgress", currentPosition, newPosition);
                animation.setDuration (animationDuration); //in milliseconds
                animation.setInterpolator (new DecelerateInterpolator());
                animation.start ();
                currentPosition = newPosition;
            }
        });
    }

    private void updateTextView(String distance, String unit){
        String textValue = distance + "\n"+unit;
        SpannableString ss1=  new SpannableString(textValue);
        ss1.setSpan(new RelativeSizeSpan(2f), 0,distance.length(), 0); // set size
        ss1.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getContext(),R.color.circleGrey)), 0, textValue.length(), 0);// set color
        ss1.setSpan(new RelativeSizeSpan(0.7f), distance.length(),textValue.length(), 0);
        textView.setText(ss1);
    }

    private String roundToString(double val, int nbDecimals){
        double denominator = Math.pow(10, nbDecimals);
        double roundedNumber = Math.round(val * denominator) / denominator;
        String result = ""+roundedNumber;
        result = result.replace(".",",");
        if(result.length() < 4){
            result += "0";
        }
        return result;
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e("","",e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(broadcastReceiver);
        getActivity().stopService(new Intent(getActivity(),BeaconScannerService.class));
    }

}
