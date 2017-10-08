package com.example.valtteri.journeytracker.route.tracking;


import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.valtteri.journeytracker.content.provider.SqlContentProvider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.main.navigation.MainActivity;
import java.util.ArrayList;

public class OrienteeringFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        StepCheck.StepCounterListener {

    private Button stopbtn;
    private Button center;
    private OnFragmentInteractionListener mListener;
    GoogleApiClient gac;
    Location loc;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 61124;
    private GoogleMap googleMap;
    boolean isMapZoomOn = true;
    boolean centerButtonVisible = false;
    boolean firstMinuteGone = false;
    LatLng myLocation;
    LatLng locNow;
    LatLng locPrev;
    double lat;
    double lon;
    double prevLat = 0;
    double prevLon = 0;
    ArrayList<Double> medianValuesLat;
    ArrayList<Double> medianValuesLon;
    float distanceTotal = 0;
    float distanceThis;
    private StepCheck stepCounter;
    boolean stepsTaken = false;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    int Seconds, Minutes, Hours;
    TextView stopWatch;
    TextView metersTotal;
    Handler handler;
    String finalTime;
    SensorManager mSensorManager;
    Sensor stepDetector;
    public static ArrayList<LatLng> markerPositions = new ArrayList<>();
    public static ArrayList<LatLng> locations;

    public OrienteeringFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get locations of the markers which were set up in AddTargetFragment.
        if (getArguments() != null) {
            Log.i("Add target arguments", getArguments().getString("param1"));
            markerPositions = getArguments().getParcelableArrayList(AddTargetFragment.TARGETS);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialize Google Map view.
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_orienteering, container, false);

        stepCounter = new StepCheck(getActivity());
        stepCounter.setListener(this);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        locations = new ArrayList<>();
        medianValuesLat = new ArrayList<Double>();
        medianValuesLon = new ArrayList<Double>();
        stopbtn = v.findViewById(R.id.stop_button);
        center = v.findViewById(R.id.centerButton);
        stopWatch = v.findViewById(R.id.stopWatch);
        metersTotal = v.findViewById(R.id.metersTotal);

        handler = new Handler();

        //get the spinner from the xml.
        Spinner dropdown = v.findViewById(R.id.spinner1);
        //create a list of items for the spinner.
        String[] items = new String[]{"Hybrid", "Roadmap", "Terrain", "Satellite"};
        /*
        create an adapter to describe how the items are displayed, adapters are used in several places in android.
        There are multiple variations of this, but this is the basic variant.
        */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //Set map types to change by clicking items.
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if (selectedItem.equals("Roadmap")) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (selectedItem.equals("Satellite")) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                } else if (selectedItem.equals("Terrain")) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                } else if (selectedItem.equals("Hybrid")) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                }
            }

            //Set map type to Hybrid as default.
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });



        //Functionality when the stop button is clicked.
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the value from stopwatch and pass it and meters to SQL.
                finalTime = stopWatch.getText().toString();
                ContentValues values = new ContentValues();
                values.put("timer", finalTime);
                values.put("distance", distanceTotal);

                getActivity().getContentResolver().insert(SqlContentProvider.insertROUTE, values);
                //Stop timer
                handler.removeCallbacks(runnable);

                //Change fragment to Main
                Intent changetoMain = new Intent(getActivity(), MainActivity.class);
                startActivity(changetoMain);
            }
        });


        //Start the stopwatch
        StartTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);

        //Create GoogleApiClient variable.
        gac = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Set up location settings.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        });

        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Activity activity = getActivity();
                if (activity != null && isAdded()) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied, but this can be fixed
                            // by showing the user a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().

                                ResolvableApiException resolvable = (ResolvableApiException) e;
                                resolvable.startResolutionForResult(activity,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way
                            // to fix the settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

        //This method is run everytime the location is updated.
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                Log.d("Location result", locationResult.toString());
                for (Location location : locationResult.getLocations()) {


                    //Set current location to a method as a double.
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    setLoc(lat, lon);



                    //Set current location to a LatLng type variable.
                    myLocation = new LatLng(getLat(), getLon());

                    //Before starting to save locations we wait one minute to detect enough satellites
                    //to make location accurate.
                    if (firstMinuteGone == true){
                    //Add current location to arraylist.
                    locations.add(myLocation);

                    //Keeps map camera centered to the current location. If map is tapped once, you can explore
                    // the map freely. Also a "center button" appears which allows you to center camera back
                    //to the current location.
                    if (prevLat != 0 && isMapZoomOn == true) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                    } else if (isMapZoomOn == false) {
                        center.setVisibility(View.VISIBLE);
                        center.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                                center.setVisibility(View.INVISIBLE);
                                isMapZoomOn = true;
                            }
                        });
                    }
                        //Set current location as LatLng to locNow variable and previous location as LatLng
                        //to locPrev variable.
                        locNow = new LatLng(getLat(), getLon());
                        locPrev = new LatLng(prevLat, prevLon);

                        //If user's device contains Step detector sensor, app starts to calculate meters after
                        //first step is taken. If device doesn't support the sensor, the meter calculation starts
                        //without requiring a step first.
                    if (stepsTaken || stepDetector == null) {
                        distanceThis = getDistance(locNow, locPrev);
                        distanceTotal = distanceTotal + distanceThis;
                        metersTotal.setText(Float.toString(distanceTotal) + "m");
                    }
            }
                        prevLat = lat;
                        prevLon = lon;
                    }

                }
            };
        return v;
    }

    protected void createLocationRequest() {
        //LocationRequest
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    //Sets location to variables.
    public void setLoc(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    //Get current latitude as a double.
    public double getLat() {
        return lat;
    }
    //Get current longitude as double.
    public double getLon() {
        return lon;
    }

    public void onStart() {
        //Connect to GoogleApiClient on start.
        gac.connect();
        super.onStart();
        //Request location.
        createLocationRequest();
    }

    public void onResume() {
        super.onResume();
        startLocationUpdates();
        //Create a toast to inform user if step counter is not supported.
        if (!stepCounter.register()) {
            Toast.makeText(getActivity(), "Step counter not supported!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStop() {
        super.onStop();
        //Disconnect Google Api Client, stop location updates and unregister step counter.
        gac.disconnect();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stepCounter.unregister();
    }

    //Returns distance between two locations.
    public float getDistance(LatLng p1, LatLng p2) {
        double lat1 = (p1.latitude);
        double lng1 = (p1.longitude);
        double lat2 = (p2.latitude);
        double lng2 = (p2.longitude);
        float[] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
        return dist[0];
    }

    //Start location updates.
    protected void startLocationUpdates() {

        try {

            //Check permissions.
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
        } catch (Exception e) {
            Log.d("loc update", "nope?" + e);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Get location.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        loc = LocationServices.FusedLocationApi.getLastLocation(gac);

        //If got a location, set it to variables lat and lon.
        if (loc != null) {
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //Functionality when map is created.
    @Override
    public void onMapReady(GoogleMap map) {
        //The very first view on map before the camera centers to the current location.
        LatLng finland = new LatLng(60.11021, 24.7385007);
        //Initialize googleMap variable.
        googleMap = map;
        //Set map type to Hybrid which is a mix of a satellite and a road map.
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //Check location permissions.
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Sets the blue icon to usage as a current location.
        googleMap.setMyLocationEnabled(true);
        //Moves camera to Finland view before placing it to the current location.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finland, 5));

        //Adds those markers to map which were set in previous view.
        for(LatLng locationPoint : markerPositions) {
            addTarget(locationPoint);
        }

        //If map is tapped once, auto centering of the camera turns off and a center button appears which
        //allows user to center camera back to the current location.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                isMapZoomOn = false;
                centerButtonVisible = true;
                center.setVisibility(View.VISIBLE);

            }
        });
    }

    //Method for adding markers.
    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position));
    }

    //If a step is taken, change value to true and start meter calculation.
    @Override
    public void stepCountChanged(String sensoriChanged) {
        if(sensoriChanged != null) {
            stepsTaken = true;
        }
    }

    //Stopwatch functionality.
    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Hours = Minutes / 60;
            Minutes = Minutes % 60;
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;

            stopWatch.setText("" + Hours + ":"
                    + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));

            if(Minutes == 1){
                firstMinuteGone = true;
            }
            handler.postDelayed(this, 0);
        }

    };
}
