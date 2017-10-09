package com.example.valtteri.journeytracker.main.navigation;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.content.provider.SqlContentProvider;
import com.example.valtteri.journeytracker.route.tracking.OnFragmentInteractionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class ResultDetails extends Fragment implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    //ResultDetails variables.
    protected OnFragmentInteractionListener mListener;
    int normalMap = MAP_TYPE_NORMAL;
    int terrainMap = MAP_TYPE_TERRAIN;
    int satelliteMap = MAP_TYPE_SATELLITE;
    int hybridMap = MAP_TYPE_HYBRID;
    GoogleMap googleMap;
    boolean ownLocLoadsRun = false;
    boolean markerLocLoadsRun = false;
    TextView dateTv;
    TextView distanceTv;
    TextView timerTv;
    String distance;
    String date;
    String timer;
    ArrayList<LatLng> ownLocations;
    ArrayList<LatLng> markerLocations;

    // ContentProvider variables
    String[] selectionArgs = new String[1];

    public ResultDetails() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get arguments from SQL.
        if(getArguments() != null) {

            distance = getArguments().getString("distance");
            date = getArguments().getString("date");
            timer = getArguments().getString("timer");

            selectionArgs[0] = getArguments().getString("id");
        }
        //Makes sure that Loader does its job correctly when accessing fragment second time.
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        // Create cursor loader for the own coordinates
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_result_details, container, false);

        dateTv = v.findViewById(R.id.date);
        distanceTv = v.findViewById(R.id.kiloMeters);
        timerTv = v.findViewById(R.id.stopWatch);

        //Initializes array lists for location points of user and markers.
        ownLocations = new ArrayList<>();
        markerLocations = new ArrayList<>();

        //Set date of the exercise to view.
        dateTv.setText(date);

        //Show meters as meters or as kilometers depending on the amount of meters.
        if (Double.valueOf(distance) >= 1000) {
            double dist = Double.valueOf(distance)/1000;

            dist = Math.round(dist * 10.0) / 10.0;
            distanceTv.setText(String.valueOf(dist) + " km");
        }
        else{
            double dist = Double.valueOf(distance);
            dist = Math.round(dist * 10.0) / 10.0;
            distanceTv.setText(String.valueOf(dist) + " m");
        }
        //Set stopwatch value to view.
        timerTv.setText(timer);

        //get the spinner from the xml.
        Spinner dropdown = v.findViewById(R.id.spinner3);
        //create a list of items for the spinner.
        String[] items = new String[]{"Hybrid", "Roadmap", "Terrain", "Satellite"};
        /*
        create an adapter to describe how the items are displayed, adapters are used in several places in android.
        There are multiple variations of this, but this is the basic variant.
        */
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //Set map types to change by clicking items.
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                switch (selectedItem) {
                    case "Roadmap":
                        googleMap.setMapType(normalMap);

                        break;
                    case "Satellite":
                        googleMap.setMapType(satelliteMap);

                        break;
                    case "Terrain":
                        googleMap.setMapType(terrainMap);

                        break;
                    case "Hybrid":
                        googleMap.setMapType(hybridMap);

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                googleMap.setMapType(hybridMap);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
    public void onMapReady(GoogleMap map) {
        //Initializes Google Map variable and sets maptype to Hybrid.
        googleMap = map;
        googleMap.setMapType(hybridMap);
    }

    //Method for drawing a red line between the location points of user.
    private void drawLine(ArrayList<LatLng> locations) {
        googleMap.addPolyline(new PolylineOptions().geodesic(true)
                    .color(Color.RED)
                    .addAll(locations)
            );
    }

    //Method for adding the markers to map.
    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position));

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if( id == 0){
            return new CursorLoader(getActivity(), SqlContentProvider.getOwn_Coords, null, null, selectionArgs, null );
        }
        else if ( id == 1){
            return new CursorLoader(getActivity(), SqlContentProvider.getMarker_Coordinates, null, null, selectionArgs, null );
        }
        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            if( loader.getId() == 0) {
                if(data.getCount() != 0){
                    data.moveToFirst();

                    for(int i = 0; i < data.getCount(); i++){

                        data.moveToPosition(i);

                        LatLng loc = new LatLng
                                (Double.valueOf(data.getString(data.getColumnIndex("latitude"))),
                                        Double.valueOf(data.getString(data.getColumnIndex("longitude"))));
                        ownLocations.add(loc);
                    }
                    ownLocLoadsRun = true;
                }

                getActivity().getSupportLoaderManager().restartLoader(1, null, this);
                // Create cursor loader for the marker coordinates
                getActivity().getSupportLoaderManager().initLoader(1, null, this);

            }
            else if( loader.getId() == 1 ) {

                    if (data.getCount() != 0) {

                        data.moveToFirst();


                        for (int i = 0; i < data.getCount(); i++) {

                            data.moveToPosition(i);
                            LatLng loc = new LatLng
                                    (Double.valueOf(data.getString(data.getColumnIndex("latitude"))),
                                            Double.valueOf(data.getString(data.getColumnIndex("longitude"))));
                            markerLocations.add(loc);
                        }
                        for (LatLng markerLocs : markerLocations) {
                            addTarget(markerLocs);
                        }
                }
                markerLocLoadsRun = true;
            }

            if((ownLocLoadsRun && markerLocLoadsRun)) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ownLocations.get(0), 16));

                drawLine(ownLocations);
            }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
