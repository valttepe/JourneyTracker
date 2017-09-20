package com.example.valtteri.journeytracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.valtteri.journeytracker.route.tracking.RouteTrackActivity;

/**
 * Created by Valtteri on 19.9.2017.
 */

public class RouteFragment extends Fragment {

    Button startbtn;

    public RouteFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_route, container, false);
        startbtn = (Button)v.findViewById(R.id.start_button);
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changetoTracking = new Intent(getActivity(), RouteTrackActivity.class);
                startActivity(changetoTracking);
            }
        });
        return v;
    }
}
