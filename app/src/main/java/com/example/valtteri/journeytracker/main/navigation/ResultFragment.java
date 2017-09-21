package com.example.valtteri.journeytracker.main.navigation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.valtteri.journeytracker.R;

/**
 * Created by Valtteri on 19.9.2017.
 */

public class ResultFragment extends Fragment {

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        return v;
    }



}
