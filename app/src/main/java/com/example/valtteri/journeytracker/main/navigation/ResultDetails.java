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
import android.util.Log;
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

/**
 * Created by Otto on 2.10.2017.
 */

public class ResultDetails extends Fragment implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private OnFragmentInteractionListener mListener;
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

    private Cursor locationCursor;
    private Cursor markerCursor;




    public ResultDetails() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            Log.i("Result arguments", getArguments().getString("distance"));
            Log.i("Result arguments", getArguments().getString("date"));
            Log.i("Result arguments", getArguments().getString("timer"));
            Log.i("Result arguments", getArguments().getString("id"));
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

        ownLocations = new ArrayList<>();
        markerLocations = new ArrayList<>();

        dateTv.setText(date);

        if (Double.valueOf(distance) >= 1000) {
            double dist = Double.valueOf(distance)/1000;
            distanceTv.setText(String.valueOf(dist) + " km");
        }
        else{
            double dist = Double.valueOf(distance);
            distanceTv.setText(dist + " m");
        }
        timerTv.setText(timer);

        //get the spinner from the xml.
        Spinner dropdown = (Spinner) v.findViewById(R.id.spinner3);
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
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

    }

    private void drawLine(ArrayList<LatLng> locations) {
        googleMap.addPolyline(new PolylineOptions().geodesic(true)
                    .color(Color.RED)
                    .addAll(locations)
            );
    }

    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Remove marker")
                .draggable(true));

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
            if( loader.getId() == 0 && data.getCount() != 0) {
                locationCursor = data;
                locationCursor.moveToFirst();

                double test = locationCursor.getDouble(locationCursor.getColumnIndex("latitude"));

                Log.i("LocationCursor", "" + test);

                for(int i = 0; i < locationCursor.getCount(); i++){

                    locationCursor.moveToPosition(i);
                    Log.d(locationCursor.getString(locationCursor.getColumnIndex("latitude")), locationCursor.getString(locationCursor.getColumnIndex("longitude")));

                    LatLng loc = new LatLng
                            (Double.valueOf(locationCursor.getString(locationCursor.getColumnIndex("latitude"))),
                                    Double.valueOf(locationCursor.getString(locationCursor.getColumnIndex("longitude"))));
                    ownLocations.add(loc);
                    Log.d(locationCursor.getString(locationCursor.getColumnIndex("latitude")), locationCursor.getString(locationCursor.getColumnIndex("longitude")));
                }


                getActivity().getSupportLoaderManager().restartLoader(1, null, this);
                // Create cursor loader for the marker coordinates
                getActivity().getSupportLoaderManager().initLoader(1, null, this);


                ownLocLoadsRun = true;
            }
            else if( loader.getId() == 1) {
                if (data.getCount() != 0){
                    markerCursor = data;
                    markerCursor.moveToFirst();

                    Log.i("MarkerCursor", "" + markerCursor.getString(markerCursor.getColumnIndex("_id")));

                    for(int i = 0; i < markerCursor.getCount(); i++){

                        markerCursor.moveToPosition(i);
                        LatLng loc = new LatLng
                                (Double.valueOf(markerCursor.getString(markerCursor.getColumnIndex("latitude"))),
                                        Double.valueOf(markerCursor.getString(markerCursor.getColumnIndex("longitude"))));
                        markerLocations.add(loc);
                        Log.d("TULLEET MARKERIT", markerCursor.getString(markerCursor.getColumnIndex("latitude")));
                    }
                    for(LatLng markerLocs : markerLocations) {
                        addTarget(markerLocs);

                    }
                }
                markerLocLoadsRun = true;
            }

            if((ownLocLoadsRun == true && markerLocLoadsRun == true)) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ownLocations.get(0), 16));

                drawLine(ownLocations);
            }
            /*else if(ownLocLoadsRun == true && markerLocLoadsRun == false ) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ownLocations.get(0), 16));

                drawLine(ownLocations);
            }*/



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
