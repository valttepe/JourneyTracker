package com.example.valtteri.journeytracker;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Valtteri on 19.9.2017.
 */

public class RouteFragment extends Fragment implements SensorEventListener {


    private SensorManager mSensorManager;
    boolean activityRunning;
    Button startbtn;
    TextView steps;
    private int stepsInSensor = 1;

    public RouteFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_route, container, false);
        startbtn = v.findViewById(R.id.start_button);
        steps = v.findViewById(R.id.steps_view);
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changetoTracking = new Intent(getActivity(), RouteTrackActivity.class);
                startActivity(changetoTracking);
            }
        });
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);


        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        activityRunning = true;
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        if (countSensor != null) {
            mSensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(getActivity(), "Count sensor not available!", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityRunning = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (activityRunning) {
            Log.d("AABLYAT", sensorEvent.toString());

            steps.setText(String.valueOf(stepsInSensor++));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
