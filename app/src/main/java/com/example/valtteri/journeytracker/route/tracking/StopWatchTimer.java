package com.example.valtteri.journeytracker.route.tracking;

/**
 * Created by Otto on 29.9.2017.
 */
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.os.Handler;

import com.example.valtteri.journeytracker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StopWatchTimer extends AppCompatActivity{
    TextView stopWatch ;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;

    Handler handler;

    int Seconds, Minutes, Hours ;

    String[] ListElements = new String[] {  };

    List<String> ListElementsArrayList ;

    ArrayAdapter<String> adapter ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_orienteering);

        stopWatch = (TextView)findViewById(R.id.stopWatch);


        handler = new Handler() ;

        ListElementsArrayList = new ArrayList<String>(Arrays.asList(ListElements));

        adapter = new ArrayAdapter<String>(StopWatchTimer.this,
                android.R.layout.simple_list_item_1,
                ListElementsArrayList
        );





             ListElementsArrayList.add(stopWatch.getText().toString());

                adapter.notifyDataSetChanged();


    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Hours = Minutes / 60;
            Minutes = Minutes % 60;

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;



            stopWatch.setText("" + Hours + ":"
                    + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));

            handler.postDelayed(this, 0);
        }

    };
}
