package com.example.valtteri.journeytracker.main.navigation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.route.tracking.OnFragmentInteractionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, OnFragmentInteractionListener{

    //MainActivity variables.
    private Fragment fragment;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private Boolean exit = false;
    private Boolean backToList = false;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    int MY_PERMISSION_ACCESS_COURSE_LOCATION=0;
    int MY_PERMISSION_WRITE_EXTERNAL_STORAGE=0;

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

    @TargetApi(23)
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

            // Create a new Fragment to be placed in the activity layout.
            RouteFragment routeFragment = new RouteFragment();

            // Add the fragment to the 'fragment_container' FrameLayout.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, routeFragment).commit();

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            //Set listener to bottom navigation view.
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            //Initialize a location manager variable.
            final LocationManager lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //If location is off in device, a toast message will recommend user to turn it on.
            if (!lManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(MainActivity.this, R.string.gps_location_toast,
                        Toast.LENGTH_LONG).show();
            }

            //If device's Api level is less than 23, lower if statement is run to handle permission checks for GPS and external storage. If it's
            //23 or higher, else statement is run to handle the permission checks for GPS and external storage.
            if(Build.VERSION.SDK_INT <= 22) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COURSE_LOCATION);
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSION_WRITE_EXTERNAL_STORAGE);
                }else{
                }

            }else {
                List<String> permissionsNeeded = new ArrayList<>();

                final List<String> permissionsList = new ArrayList<>();

                if (!addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    permissionsNeeded.add("Write External Storage");

                if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                    permissionsNeeded.add("GPS");

                if (permissionsList.size() > 0) {
                    if (permissionsNeeded.size() > 0) {

                        String message = "You need to grant access to " + permissionsNeeded.get(0);
                        for (int i = 1; i < permissionsNeeded.size(); i++)
                            message = message + ", " + permissionsNeeded.get(i);
                        showMessageOKCancel(message,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                                    }
                                });
                        return;
                    }
                    requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                            REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                }
            }
        }
    }

    //Method for adding permissions for GPS and external storage.
    @TargetApi(23)
    private boolean addPermission(List<String> permissionList, String permission) {

        if (ActivityCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= 23) {
            permissionList.add(permission);

            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    //Shows alert message with OK and Cancel options.
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    //Handles permission results. Checks if permissions are applied or denied.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {

        switch(requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();

                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Some Permission is Denied",
                            Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void changeFragment(int itemid) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        // Change to home page fragment
        if(itemid == R.id.navigation_home) {
            fragment = new RouteFragment();
            ft.replace(R.id.content, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        // Change to metawear (Compass) fragment
        else if(itemid == R.id.navigation_map) {
            fragment = new MetaWearFragment();
            ft.replace(R.id.content, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
        // Change to Route list fragment
        else if(itemid == R.id.navigation_saved_routes) {
            fragment = new ResultFragment();
            ft.replace(R.id.content, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void changeFragment(Bundle bundle) {
        // Change to details list
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        // Setting boolean true for onBackPressed function
        backToList = true;
        
        fragment = new ResultDetails();
        fragment.setArguments(bundle);
        ft.replace(R.id.content, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        // if pressed twice it closes activity
        if (exit) {
            finish(); // finish activity
        } else {
            // If user is using back button from details it returns to list view
            // Couldn't get it work with popStack in here
            if(backToList){
                backToList = false;
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                fragment = new ResultFragment();
                ft.replace(R.id.content, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
            else {
                // Shows toast if accidentally pressed back button and keeps exit boolean true
                // for three seconds if user wants to press again to exit
                Toast.makeText(this, R.string.exit_toast,
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
}
