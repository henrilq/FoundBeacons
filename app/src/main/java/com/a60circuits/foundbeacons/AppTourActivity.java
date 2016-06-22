package com.a60circuits.foundbeacons;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;

import com.a60circuits.foundbeacons.service.NotificationServiceManager;
import com.a60circuits.foundbeacons.utils.SharedPreferencesUtils;
import com.vlonjatg.android.apptourlibrary.AppTour;

/**
 * Created by zoz on 24/05/2016.
 */
public class AppTourActivity extends CustomAppTour{

    public static final String HIDE_APP_TOUR = "hideAppTour";

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        int dotColor = ContextCompat.getColor(getApplicationContext(),R.color.colorBorderBlue);
        int inactiveDotColor = ContextCompat.getColor(getApplicationContext(),R.color.colorInactiveDot);
        int textColor = ContextCompat.getColor(getApplicationContext(),R.color.unselectedButtonColor);
        int customSlideColor = ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary);
        ImageButton nextSlideImageButton = (ImageButton) findViewById(com.vlonjatg.android.apptourlibrary.R.id.nextSlideImageButton);
        nextSlideImageButton.setColorFilter(dotColor);

        //Custom slide
        addSlide(createSlide(R.drawable.tuto_1), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_3), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_4), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_5), customSlideColor);
        addSlide(createSlide(R.drawable.tuto_6), customSlideColor);

        //Customize tour
        setDoneText(getResources().getString(R.string.tuto_start));
        hideSkip();
        hideNext();
        setActiveDotColor(dotColor);
        setInactiveDocsColor(inactiveDotColor);
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
        SharedPreferencesUtils.putBoolean(this, HIDE_APP_TOUR, true);
        SharedPreferencesUtils.putBoolean(this, SettingsFragment.GPS_ENABLED, true);
    }

    private CustomSlide createSlide(int id){
        CustomSlide customSlide = new CustomSlide();
        Bundle args = new Bundle();
        args.putInt(CustomSlide.KEY, id);
        customSlide.setArguments(args);
        return customSlide;
    }

    private void startMainActivity(){
        Intent intent = new Intent(AppTourActivity.this, MainActivity.class);
        Bundle args = new Bundle();
        args.putBoolean(MainActivity.START_SCAN, true);
        intent.putExtras(args);
        startActivity(intent);
    }
}
