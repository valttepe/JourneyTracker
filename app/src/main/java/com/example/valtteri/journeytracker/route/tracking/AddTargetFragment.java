package com.example.valtteri.journeytracker.route.tracking;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.main.navigation.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class AddTargetFragment extends Fragment implements OnMapReadyCallback {

    Button readybtn;
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String TARGETS = "targets";

    private String mParam1 = "testing";
    private String mParam2 = "Testingtesting";
    private MapView mapView;
    private GoogleMap googleMap;
    Bundle args;

    ArrayList<LatLng> markerPositions;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    private OnFragmentInteractionListener mListener;

    public AddTargetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_target, container, false);

        markerPositions = new ArrayList<>();
        readybtn = v.findViewById(R.id.ready_button);
        readybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: get target longitude and altitude here and send them forward to the Orienteering fragment
                if(mListener != null){
                    args = new Bundle();
                    args.putString(ARG_PARAM1, mParam1);
                    args.putString(ARG_PARAM2, mParam2);
                    args.putParcelableArrayList(TARGETS, markerPositions);

                    for(LatLng locs : markerPositions) {
                        Log.d("Locations ", locs.toString());
                    }
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
                    googleMap.setMapType(googleMap.MAP_TYPE_NORMAL);
                }
                else if(selectedItem.equals("Satellite")) {
                    googleMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

                }
                else if(selectedItem.equals("Terrain")) {
                    googleMap.setMapType(googleMap.MAP_TYPE_TERRAIN);

                }
                else if(selectedItem.equals("Hybrid")) {
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

    private void addTarget(LatLng position) {
        googleMap.addMarker(new MarkerOptions()
                .position(position)
                .title("Remove marker")
                .draggable(true));

        markerPositions.add(position);
    }

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

        googleMap.setMapType(googleMap.MAP_TYPE_HYBRID);

        LatLng start = new LatLng(60.32453, 25.0516);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng position) {
                addTarget(position);
                Log.d("Added a marker: ", position.toString());
            }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker m){
                removeTarget(m);
            }
        });


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

}
