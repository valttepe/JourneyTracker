package com.example.valtteri.journeytracker.route.tracking;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.example.valtteri.journeytracker.R;

public class RouteTrackActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    FragmentTransaction ft;
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

    // Interface method which is called from the AddTargetFragment
    @Override
    public void changeFragment(Bundle bundle) {
        // Creating new fragment and setting bundle that contains markers LatLng array list.
        OrienteeringFragment newFrag = new OrienteeringFragment();
        newFrag.setArguments(bundle);
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.route_content, newFrag);
        ft.addToBackStack(null);
        ft.commit();
    }
    @Override
    public void onBackPressed() {
        // if androids own back button is pressed then UI alerts with dialog that either does nothing or goes to back main page.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure that you want to exit?")
                .setMessage("Current tracking data will be lost")
                .setCancelable(true)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish(); // finish activity
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();


    }

}
