package com.a60circuits.foundbeacons;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import com.a60circuits.foundbeacons.utils.SharedPreferencesUtils;

/**
 * Created by zoz on 13/05/2016.
 */
public class SpashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.spash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final boolean hideAppTourActivity = SharedPreferencesUtils.getBoolean(this,AppTourActivity.HIDE_APP_TOUR, false);
        SharedPreferencesUtils.putBoolean(this,AppTourActivity.HIDE_APP_TOUR, true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = null;
                if(hideAppTourActivity){
                    intent = new Intent(SpashScreenActivity.this, MainActivity.class);
                    Bundle args = new Bundle();
                    //args.putBoolean(MainActivity.START_SCAN, true);
                    intent.putExtras(args);
                }else{
                    intent = new Intent(SpashScreenActivity.this, AppTourActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
