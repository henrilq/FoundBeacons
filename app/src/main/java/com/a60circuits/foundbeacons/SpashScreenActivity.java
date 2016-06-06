package com.a60circuits.foundbeacons;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by zoz on 13/05/2016.
 */
public class SpashScreenActivity extends Activity{

    private static int SPLASH_TIME_OUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREF_FILE, 0);
        final boolean hideAppTourActivity = settings.getBoolean(AppTourActivity.HIDE_APP_TOUR, false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = null;
                if(hideAppTourActivity){
                    intent = new Intent(SpashScreenActivity.this, MainActivity.class);
                }else{
                    intent = new Intent(SpashScreenActivity.this, AppTourActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
