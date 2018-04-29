package com.seismic.tech.locationtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
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
                final Trip trip = trips.get(keys.get(groupPosition)).get(childPosition);
                final Context context = ViewTripsActivity.this;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(keys.get(groupPosition));
                alertDialog.setMessage("Distance Travelled: " + trip.distanceTravelled + " meter\nTime Spent: " + trip.timeSpent + " minute\n"
                        + "Do you want to see this trip on the map?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra("distance", trip.distanceTravelled);
                        ArrayList<String> array = new ArrayList<>(Arrays.asList(trip.positionString.split("\\+")));
                        intent.putStringArrayListExtra("positions", array);
                        intent.putExtra("time", trip.timeSpent);
                        intent.putExtra("activity","ViewTripsActivity");
                        startActivityForResult(intent,1);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
                return false;
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1)
        {
            if(resultCode == Activity.RESULT_OK)
            {

            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {

            }
        }
    }
}
