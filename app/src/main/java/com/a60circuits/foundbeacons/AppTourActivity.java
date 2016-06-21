package com.a60circuits.foundbeacons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.vlonjatg.android.apptourlibrary.AppTour;

/**
 * Created by zoz on 24/05/2016.
 */
public class AppTourActivity extends CustomAppTour{

    public static final String HIDE_APP_TOUR = "hideAppTour";

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        int dotColor = ContextCompat.getColor(getApplicationContext(),R.color.colorBorderBlue);
        int textColor = ContextCompat.getColor(getApplicationContext(),R.color.unselectedButtonColor);
        int customSlideColor = ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary);
        ImageButton nextSlideImageButton = (ImageButton) findViewById(com.vlonjatg.android.apptourlibrary.R.id.nextSlideImageButton);
        nextSlideImageButton.setColorFilter(dotColor);

        setDoneText("Démarrer");
        hideSkip();
        hideNext();

        setActiveDotColor(dotColor);
        //Custom slide
        addSlide(createSlide(R.drawable.tuto_1), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_3), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_4), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_5), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_6), customSlideColor);


        //Customize tour
        setSkipButtonTextColor(textColor);
        setNextButtonColorToBlack();
        setDoneButtonTextColor(textColor);

    }

    @Override
    public void onSkipPressed() {
        registerPreferences();
        startMainActivity();
    }

    @Override
    public void onDonePressed() {
        registerPreferences();
        startMainActivity();
    }

    private void registerPreferences(){
        SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(HIDE_APP_TOUR, true);
        editor.commit();
    }

    private CustomSlide createSlide(int id){
        CustomSlide customSlide = new CustomSlide();
        Bundle args = new Bundle();
        args.putInt(CustomSlide.KEY, id);
        customSlide.setArguments(args);
        return customSlide;
    }

    private void startMainActivity(){
        final SharedPreferences settings = getSharedPreferences(MainActivity.PREF_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SettingsFragment.GPS_ENABLED, true);
        editor.commit();

        Intent intent = new Intent(AppTourActivity.this, MainActivity.class);
        Bundle args = new Bundle();
        //args.putBoolean(MainActivity.START_SCAN, true);
        intent.putExtras(args);
        startActivity(intent);
    }
}
