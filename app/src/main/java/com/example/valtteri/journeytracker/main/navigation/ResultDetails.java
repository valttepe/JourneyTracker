package com.example.valtteri.journeytracker.main.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.route.tracking.AddTargetFragment;
import com.example.valtteri.journeytracker.route.tracking.OnFragmentInteractionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

/**
 * Created by Otto on 2.10.2017.
 */

public class ResultDetails extends Fragment implements OnMapReadyCallback {

    private OnFragmentInteractionListener mListener;
    GoogleMap googleMap;
    TextView dateTv;
    TextView distanceTv;
    TextView timerTv;
    String distance;
    String date;
    String timer;




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


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_result_details, container, false);

        dateTv = v.findViewById(R.id.date);
        distanceTv = v.findViewById(R.id.kiloMeters);
        timerTv = v.findViewById(R.id.stopWatch);

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


        return v;
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
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

    }
}
