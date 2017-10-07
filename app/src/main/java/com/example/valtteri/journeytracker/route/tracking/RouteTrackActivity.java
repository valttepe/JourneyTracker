package com.example.valtteri.journeytracker.route.tracking;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.valtteri.journeytracker.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RouteTrackActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    FragmentTransaction ft;
    FragmentManager fm;
    private Boolean exit = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_track);


        if (findViewById(R.id.route_content) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            AddTargetFragment addTargetFragment = new AddTargetFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.route_content, addTargetFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void changeFragment(Bundle bundle) {
        OrienteeringFragment newFrag = new OrienteeringFragment();
        newFrag.setArguments(bundle);
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.route_content, newFrag);
        ft.addToBackStack(null);
        ft.commit();
    }
    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

}
