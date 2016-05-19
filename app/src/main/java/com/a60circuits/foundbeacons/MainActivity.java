package com.a60circuits.foundbeacons;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageButton b1 = (ImageButton)findViewById(R.id.b1);
        final ImageButton b2 = (ImageButton)findViewById(R.id.b2);
        final ImageButton b3 = (ImageButton)findViewById(R.id.b3);
        if(b1 != null && b2 != null && b3 != null){
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.central, new SettingsFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    b1.setColorFilter(Color.argb(255, 0, 123, 247)); // Blue Tint
                    b2.setColorFilter(null);
                    b3.setColorFilter(null);
                }
            });

            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.central, new GMapFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    b2.setColorFilter(Color.argb(255, 0, 123, 247)); // Blue Tint
                    b1.setColorFilter(null);
                    b3.setColorFilter(null);
                }
            });

            b3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.central, new ObjectsFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                    b3.setColorFilter(Color.argb(255, 0, 123, 247)); // Blue Tint
                    b1.setColorFilter(null);
                    b2.setColorFilter(null);
                }
            });
        }


    }
}
