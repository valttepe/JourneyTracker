package com.example.valtteri.journeytracker.main.navigation;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.content.provider.SqlContentProvider;
import com.example.valtteri.journeytracker.route.tracking.OnFragmentInteractionListener;


public class ResultFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Content provider variables
    SqlCursorAdapter cursorAdapter;
    ListView lv;
    Bundle args;
    public String d, dis, time;
    public Cursor curi;

    // Interface variable
    private OnFragmentInteractionListener mListener;

    public ResultFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Creates custom cursor adapter for list view without having cursor yet
        cursorAdapter = new SqlCursorAdapter(getActivity(), null, 1);

        //Makes sure that Loader does its job correctly when accessing fragment second time.
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        // Starting creating the loader
        getActivity().getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Adding fragment layout to the activity
        View v;
        v = inflater.inflate(R.layout.fragment_result, container, false);
        // connecting to list view
        lv = v.findViewById(android.R.id.list);

        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        // Adding arguments for the next fragment
        if(mListener != null) {
            // Changing fetched route cursor to clicked position
            curi.moveToPosition(position);
            // Creating the bundle and adding all values what has been fetched for
            // showing them in next fragment and _id for the fetching coordinates
            args = new Bundle();
            args.putString("distance", curi.getString(curi.getColumnIndex("distance")));
            args.putString("date", curi.getString(curi.getColumnIndex("date")));
            args.putString("timer", curi.getString(curi.getColumnIndex("timer")));
            args.putString("id", curi.getString(curi.getColumnIndex("_id")));
            // Using interface to communicate with other fragment
            mListener.changeFragment(args);
        }
    }

    // Changed this function for setting up interface functionality
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
    // stopping interface listener
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // after calling initloader it creates new cursor loader that fetches from database with
        // Uri that is defined in sqlcontentprovider
        return new CursorLoader(getActivity(), SqlContentProvider.get_ALL, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // After fetching is done it changes cursoradapters null cursor to data cursor
        cursorAdapter.swapCursor(data);
        // setting adapter to the list view so that it could use cursor
        lv.setAdapter(cursorAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // if reseted then it changes cursor back to null
        cursorAdapter.swapCursor(null);
    }

    // Own custom cursor setup for list view
    private class SqlCursorAdapter extends CursorAdapter {
        // Variables for text views and inflating the cursor
        private LayoutInflater cursorInflater;
        private TextView date, timer, distance;

        // Required function for custom cursor
        SqlCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

        }
        // Required function for custom cursor
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // R.layout.list_row is your xml layout for each row
            return cursorInflater.inflate(R.layout.routelist_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Getting cursor to variable so that it can be used in item click listener
            curi = cursor;
            // Adding cursors values to the text views
            date = view.findViewById(R.id.date);
            distance = view.findViewById(R.id.distance);
            timer = view.findViewById(R.id.timer);
            d = cursor.getString(cursor.getColumnIndex("date"));
            dis = cursor.getString(cursor.getColumnIndex("distance"));
            time = cursor.getString(cursor.getColumnIndex("timer"));
            if(d != null || dis != null) {
                date.setText(d);
                distance.setText(dis);
                timer.setText(time);
            }
        }
    }



}
