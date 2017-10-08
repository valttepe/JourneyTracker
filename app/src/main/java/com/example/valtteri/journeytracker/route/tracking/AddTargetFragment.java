package com.example.valtteri.journeytracker.route.tracking;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.example.valtteri.journeytracker.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN;

public class AddTargetFragment extends Fragment implements OnMapReadyCallback {

    Button readybtn;
    public static final String TARGETS = "targets";
    private GoogleMap googleMap;
    Bundle args;
    int normalMap = MAP_TYPE_NORMAL;
    int terrainMap = MAP_TYPE_TERRAIN;
    int satelliteMap = MAP_TYPE_SATELLITE;
    int hybridMap = MAP_TYPE_HYBRID;

    ArrayList<LatLng> markerPositions;

    private OnFragmentInteractionListener mListener;

    public AddTargetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_target, container, false);

        //Create an arraylist for markers.
        markerPositions = new ArrayList<>();
        readybtn = v.findViewById(R.id.ready_button);
        //When ready button is clicked, marker locations are sent to Orienteering fragment and fragment is changed.
        readybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mListener != null){
                    args = new Bundle();
                    args.putParcelableArrayList(TARGETS, markerPositions);

                    mListener.changeFragment(args);
                }
            }
        });

        //get the spinner from the xml.
        Spinner dropdown = (Spinner)v.findViewById(R.id.spinner1);
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
                if(selectedItem.equals("Roadmap")) {
                    googleMap.setMapType(normalMap);
                }
                else if(selectedItem.equals("Satellite")) {
                    googleMap.setMapType(satelliteMap);

                }
                else if(selectedItem.equals("Terrain")) {
                    googleMap.setMapType(terrainMap);

                }
                else if(selectedItem.equals("Hybrid")) {
                    googleMap.setMapType(hybridMap);

                }
            }

            //Set map type to Hybrid as default.
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                googleMap.setMapType(hybridMap);
            }
        });
        return v;
    }

    //Method for adding markers to view and to array list.
    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Remove marker")
                .draggable(true));

        markerPositions.add(position);
    }

    //Remove marker from view and from array list.
    private void removeTarget(Marker m) {
        m.remove();

       for(int i = 0; i < markerPositions.size(); i++) {
           LatLng obj = markerPositions.get(i);
           if (obj.equals(m.getPosition())) {
               markerPositions.remove(i);
               break;
           }
       }
    }

    public void onResume() {
        super.onResume();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set up Google Map view.
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

    //Functionality when Google Map view is created.
    @Override
    public void onMapReady(GoogleMap map) {
        //Initialize variable googleMap.
        googleMap = map;
        //Set map type to hybrid.
        googleMap.setMapType(hybridMap);
        //Set the first camera view to Finland.
        LatLng start = new LatLng(60.32453, 25.0516);

        //Add a marker by holding map.
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {
                addTarget(position);
            }
        });
        //Remove a marker by clicking the title "Remove" of a marker.
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker m){
                removeTarget(m);
            }
        });

        // Camera movement.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

}
