package com.example.valtteri.journeytracker.main.navigation;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.route.tracking.RouteTrackActivity;


public class RouteFragment extends Fragment {


    boolean activityRunning;
    Button startbtn;
    private Intent changetoTracking;

    public RouteFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_route, container, false);
        startbtn = v.findViewById(R.id.start_button);
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                changetoTracking = new Intent(getActivity(), RouteTrackActivity.class);
                startActivity(changetoTracking);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
    }


}
