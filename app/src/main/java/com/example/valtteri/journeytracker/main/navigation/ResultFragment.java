package com.example.valtteri.journeytracker.main.navigation;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.valtteri.journeytracker.R;
import com.example.valtteri.journeytracker.content.provider.SqlContentProvider;
import com.example.valtteri.journeytracker.route.tracking.OnFragmentInteractionListener;

/**
 * Created by Valtteri on 19.9.2017.
 */

public class ResultFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Content provider variables
    SqlCursorAdapter cursorAdapter;
    ListView lv;
    Bundle args;
    public String d, dis, time;
    public Cursor curi;

    private OnFragmentInteractionListener mListener;

    public ResultFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cursorAdapter = new SqlCursorAdapter(getActivity(), null, 1);

        //Makes sure that Loader does its job correctly when accessing fragment second time.
        getActivity().getSupportLoaderManager().restartLoader(0, null, this);

        getActivity().getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_result, container, false);
        lv = (ListView)v.findViewById(android.R.id.list);

        return v;
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        if(mListener != null) {
            curi.moveToPosition(position);
            Log.d("CLICK", curi.getString(curi.getColumnIndex("date")));

            args = new Bundle();
            args.putString("distance", curi.getString(curi.getColumnIndex("distance")));
            args.putString("date", curi.getString(curi.getColumnIndex("date")));
            args.putString("timer", curi.getString(curi.getColumnIndex("timer")));
            args.putString("id", curi.getString(curi.getColumnIndex("_id")));
            mListener.changeFragment(args);
        }
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(), SqlContentProvider.get_ALL, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
        lv.setAdapter(cursorAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }


    public class SqlCursorAdapter extends CursorAdapter {
        private LayoutInflater cursorInflater;
        private TextView date, timer, distance;


        public SqlCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            cursorInflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // R.layout.list_row is your xml layout for each row
            return cursorInflater.inflate(R.layout.routelist_row, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            curi = cursor;
            date = (TextView)view.findViewById(R.id.date);
            distance = (TextView)view.findViewById(R.id.distance);
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
