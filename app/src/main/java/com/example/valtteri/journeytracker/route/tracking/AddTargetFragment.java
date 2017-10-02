package com.example.valtteri.journeytracker.route.tracking;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.valtteri.journeytracker.R;
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
