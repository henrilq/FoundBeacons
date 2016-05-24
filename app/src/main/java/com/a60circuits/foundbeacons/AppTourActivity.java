package com.a60circuits.foundbeacons;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.vlonjatg.android.apptourlibrary.AppTour;

/**
 * Created by zoz on 24/05/2016.
 */
public class AppTourActivity extends AppTour{
    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        int customSlideColor = ContextCompat.getColor(getApplicationContext(),R.color.colorBorderBlue);
        Button skipButton = (Button) findViewById(com.vlonjatg.android.apptourlibrary.R.id.skipIntroButton);
        skipButton.setText("Passer");

        Button doneSlideButton = (Button) findViewById(com.vlonjatg.android.apptourlibrary.R.id.doneSlideButton);
        doneSlideButton.setText("Continuer");

        //Custom slide
        addSlide(createSlide(R.drawable.tuto1x2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto2x2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto3x2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto4x2), customSlideColor);
        addSlide(createSlide(R.drawable.tuto5x2), customSlideColor);

        //Customize tour
        setSkipButtonTextColor(Color.WHITE);
        setNextButtonColorToWhite();
        setDoneButtonTextColor(Color.WHITE);

    }

    @Override
    public void onSkipPressed() {
        startMainActivity();
    }

    @Override
    public void onDonePressed() {
        startMainActivity();
    }

    private CustomSlide createSlide(int id){
        CustomSlide customSlide = new CustomSlide();
        Bundle args = new Bundle();
        args.putInt(CustomSlide.KEY, id);
        customSlide.setArguments(args);
        return customSlide;
    }

    private void startMainActivity(){
        Intent i = new Intent(AppTourActivity.this, MainActivity.class);
        startActivity(i);
    }
}
