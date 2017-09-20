package com.example.valtteri.journeytracker.route.tracking;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.valtteri.journeytracker.R;

public class RouteTrackActivity extends AppCompatActivity {
    Button ready;
    Fragment fragment;
    FragmentTransaction ft;
    FragmentManager fm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_track);
        ready = (Button)findViewById(R.id.ready_button);
        ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragment = new OrienteeringFragment();
                fm = getSupportFragmentManager();
                ft = fm.beginTransaction();
                //ft.replace(R.id.)
            }
        });
    }
}
