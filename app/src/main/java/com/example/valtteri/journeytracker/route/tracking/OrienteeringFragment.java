package com.example.valtteri.journeytracker.route.tracking;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.main.navigation.MainActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


public class OrienteeringFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        StepCheck.StepCounterListener {

    private Button stopbtn;
    private OnFragmentInteractionListener mListener;
    GoogleApiClient gac;
    Location loc;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 61124;
    private GoogleMap googleMap;
    boolean mRequestingLocationUpdates;
    boolean isMapZoomOn = true;
    boolean ifCircleIsGot = false;
    LatLng myLocation;
    double lat;
    double lon;
    double latCombined = 0;
    double lonCombined = 0;
    double avgLat;
    double avgLon;
    double prevLat = 0;
    double prevLon = 0;
    float distanceTotal = 0;
    float distanceThis;
    private StepCheck stepCounter;
    boolean stepsTaken = false;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    int Seconds, Minutes, Hours;
    int everyFifthValue = 0;
    TextView stopWatch;
    TextView metersTotal;
    Handler handler;
    String finalTime;
    Circle myLoc;
    public static ArrayList<LatLng> markerPositions = new ArrayList<>();
    public static ArrayList<LatLng> locations;

    public OrienteeringFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Log.i("Add target arguments", getArguments().getString("param1"));
            markerPositions = getArguments().getParcelableArrayList(AddTargetFragment.TARGETS);

            for (LatLng locs : markerPositions) {
                Log.d("RECEIVED LOCATIONS ", locs.toString());


            }

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_orienteering, container, false);


        handler = new Handler();

        //get the spinner from the xml.
        Spinner dropdown = (Spinner) v.findViewById(R.id.spinner1);
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
                    googleMap.setMapType(googleMap.MAP_TYPE_NORMAL);
                } else if (selectedItem.equals("Satellite")) {
                    googleMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

                } else if (selectedItem.equals("Terrain")) {
                    googleMap.setMapType(googleMap.MAP_TYPE_TERRAIN);

                } else if (selectedItem.equals("Hybrid")) {
                    googleMap.setMapType(googleMap.MAP_TYPE_HYBRID);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                googleMap.setMapType(googleMap.MAP_TYPE_HYBRID);
            }
        });

        stepCounter = new StepCheck(getActivity());
        stepCounter.setListener(this);
        locations = new ArrayList<>();
        stopbtn = v.findViewById(R.id.stop_button);
        stopWatch = (TextView) v.findViewById(R.id.stopWatch);
        metersTotal = (TextView) v.findViewById(R.id.metersTotal);


        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO finalTime contains the final time of stop watch


                finalTime = stopWatch.getText().toString();
                ContentValues values = new ContentValues();
                values.put("timer", finalTime);
                values.put("distance", distanceTotal);
                markerPositions.get(0);
                locations.get(0);
                // TODO: Coordinates

                getActivity().getContentResolver().insert(SqlContentProvider.inserROUTE, values);
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


        gac = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


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

                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    setLoc(lat, lon);

                    //Todo myLocation contains real time locations
                    myLocation = new LatLng(getLat(), getLon());

                    //getLocCircle();


                    //Add current location to arraylist.
                    locations.add(myLocation);

                   /* if (myLocation != null) {
                        myLoc.setCenter(myLocation);
                    }*/
                    if (prevLat != 0 && isMapZoomOn == true) {

                        Log.d("MENNÄÄKS", "TÄHÄN???");
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16));
                    } else if (isMapZoomOn == false) {

                    }


                    //Combine latitudes and longitudes.
                    if (prevLat != 0 && prevLon != 0) {
                        latCombined = latCombined + lat;
                        lonCombined = lonCombined + lon;

                        Log.d("LATITUDE YHTEENSÄ", Double.toString(latCombined));
                        Log.d("LONGITUDE YHTEENSÄ", Double.toString(lonCombined));
                    }

                    //Get the average location in every fifth callbacks to get more accurate meter calculation.
                    if (everyFifthValue % 5 == 0) {

                        avgLat = latCombined / 5;
                        avgLon = lonCombined / 5;
                        Log.d("LATITUDE KESKIARVO", Double.toString(avgLat));
                        Log.d("LATITUDE KESKIARVO", Double.toString(avgLon));

                        LatLng locNow = new LatLng(avgLat, avgLon);
                        LatLng locPrev = new LatLng(prevLat, prevLon);

                        if (stepsTaken) {
                            distanceThis = getDistance(locNow, locPrev);
                            //Todo distanceTotal contains the final meters
                            distanceTotal = distanceTotal + distanceThis;
                            metersTotal.setText(Float.toString(distanceTotal) + "m");
                            Log.d("VIELÄ EI KÄVELLÄ", "EIHÄN??");
                        }

                        latCombined = 0;
                        lonCombined = 0;
                        everyFifthValue = 0;
                    }

                    if (avgLat > 0 && avgLon > 0) {
                        Log.d("MENNÄÄKS TÄNNE?", "JOOOO");
                        prevLat = avgLat;
                        prevLon = avgLon;
                    } else {
                        Log.d("MENNÄÄKS TÄNNE HETKEKS?", "JOO");
                        prevLat = lat;
                        prevLon = lon;
                    }
                    everyFifthValue = everyFifthValue + 1;
                }
                // googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
            }
        };


        return v;
    }

    protected void createLocationRequest() {
        //LocationRequest
        Log.d("location request", "Normally created");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void setLoc(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

   /* public void getLocCircle() {
        if (ifCircleIsGot == false) {
            myLoc = googleMap.addCircle(new CircleOptions()
                    .center(myLocation)
                    .fillColor(Color.WHITE)
                    .strokeColor(Color.MAGENTA)
                    .visible(true)
                    .radius(15));
        }
        ifCircleIsGot = true;
    }*/

    public void onStart() {
        gac.connect();
        super.onStart();
        createLocationRequest();
    }

    public void onResume() {
        super.onResume();
        startLocationUpdates();
        if (!stepCounter.register()) {
            Toast.makeText(getActivity(), "Step counter not supported!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStop() {
        super.onStop();
        gac.disconnect();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        stepCounter.unregister();
    }

    public float getDistance(LatLng p1, LatLng p2) {
        double lat1 = (p1.latitude);
        double lng1 = (p1.longitude);
        double lat2 = (p2.latitude);
        double lng2 = (p2.longitude);
        float[] dist = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, dist);
        return dist[0];
    }

    protected void startLocationUpdates() {

        try {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
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
            Log.d("loc update", "normally yes?");
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        loc = LocationServices.FusedLocationApi.getLastLocation(gac);
        Log.d("loc connected", "normally yes?" + loc);
        if (loc != null) {
           /* mLatitude.setText("Lat: " + loc.getLatitude());
            mLongitude.setText("Lon: " + loc.getLongitude());*/

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

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng suomi = new LatLng(60.11021, 24.7385007);
        googleMap = map;
        googleMap.setMapType(googleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(suomi, 5));
        /*for (int i = 0; i < markerPositions.size() -1; i++) {
            LatLng src = markerPositions.get(i);
            LatLng dest = markerPositions.get(i + 1);

                googleMap.addPolyline(
                        new PolylineOptions().add(
                                new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude)
                        ).geodesic(true).color(Color.RED)
                );

        }*/

        for(LatLng locationPoint : markerPositions) {
            addTarget(locationPoint);
            googleMap.addPolyline(new PolylineOptions().addAll(markerPositions).geodesic(true).color(Color.RED));
        }


        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng position) {
                isMapZoomOn = false;
            }
        });

    }

    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Remove marker")
                .draggable(true));
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

            handler.postDelayed(this, 0);
        }

    };
}
