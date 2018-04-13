package com.seismic.tech.locationtracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewTripsActivity extends AppCompatActivity
{
    ExpandableListView expandableListView;
    ViewTripsAdapter viewTripsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trips);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final HashMap<String,ArrayList<Trip>> trips = MySQLiteHelper.getInstance(this).getTrips();
        final ArrayList<String> keys = new ArrayList<>();
        keys.addAll(trips.keySet());
        expandableListView = (ExpandableListView) findViewById(R.id.exList);
        viewTripsAdapter = new ViewTripsAdapter(this,trips,keys);
        expandableListView.setAdapter(viewTripsAdapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                Trip trip = trips.get(keys.get(groupPosition)).get(childPosition);
                Toast.makeText(ViewTripsActivity.this,trip.distance+" "+trip.time,Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}
