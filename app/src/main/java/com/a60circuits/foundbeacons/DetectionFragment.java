package com.a60circuits.foundbeacons;

import android.animation.ObjectAnimator;
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

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by zoz on 26/05/2016.
 */
public class DetectionFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView textView;
    private int animationDuration = 300;

    private int currentPosition;
    private double value;
    private boolean stop = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detection_fragment,container,false);
        progressBar = (ProgressBar) view.findViewById(R.id.circleProgress);
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
        animation.setDuration (200); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();

        textView = (TextView) view.findViewById(R.id.nbText);
        initThread();
        return view;
    }

    private void initThread(){
        final Handler handler = new Handler();
        Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            random();
                        }
                    });
                    sleep(1000);
                    if(stop){break;}
                }
            }
        });
        th.start();
    }

    private void random(){
        double min = 0;
        double max = 5;
        double val = ThreadLocalRandom.current().nextDouble(min,max);
        updateValue(val);
    }

    private void updateValue(double distance){
        double distanceMax = 5;
        double res = distance / distanceMax * progressBar.getMax();
        res = Math.min(res, progressBar.getMax());
        Long resInt = Math.abs(Math.round(res) - progressBar.getMax());
        changeToPosition(resInt.intValue());
        sleep(animationDuration);
        updateTextView(roundToString(distance,2), "METRES");
    }

    private void changeToPosition(int newPosition){
        ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "secondaryProgress", currentPosition, newPosition);
        animation.setDuration (animationDuration); //in milliseconds
        animation.setInterpolator (new DecelerateInterpolator());
        animation.start ();
        currentPosition = newPosition;
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
    public void onDestroyView() {
        super.onDestroyView();
        stop = true;
    }
}
