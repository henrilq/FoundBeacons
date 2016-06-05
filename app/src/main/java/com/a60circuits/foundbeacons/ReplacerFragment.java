package com.a60circuits.foundbeacons;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.ImageButton;

/**
 * Created by zoz on 27/05/2016.
 */
public class ReplacerFragment extends Fragment{

    protected void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.central, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
