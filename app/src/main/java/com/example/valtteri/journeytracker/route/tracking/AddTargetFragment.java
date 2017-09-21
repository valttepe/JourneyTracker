package com.example.valtteri.journeytracker.route.tracking;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.valtteri.journeytracker.R;


public class AddTargetFragment extends Fragment {

    Button readybtn;
    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";

    private String mParam1 = "testing";
    private String mParam2 = "Testingtesting";

    Bundle args;

    private OnFragmentInteractionListener mListener;
    public AddTargetFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add_target, container, false);
        readybtn = v.findViewById(R.id.ready_button);
        readybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: get target longitude and altitude here and send them forward to the Orienteering fragment
                if(mListener != null){
                    args = new Bundle();
                    args.putString(ARG_PARAM1, mParam1);
                    args.putString(ARG_PARAM2, mParam2);

                    mListener.changeFragment(args);
                }



            }
        });

        return v;
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


}
