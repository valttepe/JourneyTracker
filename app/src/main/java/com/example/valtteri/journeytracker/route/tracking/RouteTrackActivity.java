package com.example.valtteri.journeytracker.route.tracking;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.valtteri.journeytracker.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class RouteTrackActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    FragmentTransaction ft;
    FragmentManager fm;
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

}
