package com.example.valtteri.journeytracker;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Fragment fragment;
    private FragmentManager fm;
    private FragmentTransaction ft;

    //Bottom bar that contains three buttons that are clickable.
    // It is our base navigation in our application
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //Here is code that is executed when clicked man icon
                    changeFragment(R.id.navigation_home);
                    return true;
                case R.id.navigation_map:
                    //Here is code that is executed when clicked map icon
                    changeFragment(R.id.navigation_map);
                    return true;
                case R.id.navigation_saved_routes:
                    //Here is code that is executed when clicked archive icon
                    changeFragment(R.id.navigation_saved_routes);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.content) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            RouteFragment routeFragment = new RouteFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, routeFragment).commit();
        }

       // mTextMessage = (TextView) findViewById(R.id.message);
        // Adding bottom navigation bar and setting onclicklistener to it.
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onBackPressed() {
        // show this alert when pushing androids own backbutton in the main three fragments
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        MainActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    public void changeFragment(int itemid) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();

        if(itemid == R.id.navigation_home) {
            fragment = new RouteFragment();
            ft.replace(R.id.content, fragment);
            ft.commit();
        }
        else if(itemid == R.id.navigation_map) {
            fragment = new MapFragment();
            ft.replace(R.id.content, fragment);
            ft.commit();
        }
        else if(itemid == R.id.navigation_saved_routes) {
            fragment = new ResultFragment();
            ft.replace(R.id.content, fragment);
            ft.commit();
        }
    }



}