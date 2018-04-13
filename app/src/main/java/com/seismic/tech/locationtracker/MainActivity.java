package com.seismic.tech.locationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity
{
    Button button;
    Button startButton;
    Button endButton;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gpsTracker;
    TextView location;
    TextView locationString;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //doAuthentication();
        super.onCreate(savedInstanceState);
        setContentView(com.seismic.tech.locationtracker.R.layout.activity_main);
        try
        {
            if(ActivityCompat.checkSelfPermission(this,mPermission)!= MockPackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{mPermission},REQUEST_CODE_PERMISSION);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        button = (Button)findViewById(R.id.button);
        startButton = (Button)findViewById(R.id.startButton);
        endButton = (Button)findViewById(R.id.endButton);
        locationString = (TextView)findViewById(R.id.locationString);
        location = (TextView)findViewById(R.id.locationText);
        gpsTracker = GPSTracker.getInstance(MainActivity.this);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if(gpsTracker.canGetLocation())
                {
                    Location loc = gpsTracker.getLocation();
                    double latitude = loc.getLatitude();
                    double longitude = loc.getLongitude();
                    location.setText(latitude+":"+longitude);
                }
                else
                {
                    gpsTracker.showSettingsAlert();
                }
            }
        });
        Calendar c = Calendar.getInstance();
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver()
                                                                 {
                                                                     @Override
                                                                     public void onReceive(Context context, Intent intent) {
                     double latitude = intent.getDoubleExtra(LocationService.LATITUDE, 0);
                     double longitude = intent.getDoubleExtra(LocationService.LONGITUDE, 0);
                     String text = locationString.getText().toString();

                     Date dt = new Date();
                     int hours = dt.getHours();
                     int minutes = dt.getMinutes();
                     int seconds = dt.getSeconds();
                     String curTime = hours + ":" + minutes + ":" + seconds;
                     if(text.length()>1000)
                     {
                         text = curTime+"," + latitude + "," + longitude + "\n";
                     }
                     else
                     {
                         text = curTime+"," + latitude + "," + longitude + "\n" + text;
                     }
                     locationString.setText(text);
                 }
             }, new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST)
        );
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                MySQLiteHelper.getInstance(MainActivity.this).initiateTrip();
                startService(new Intent(MainActivity.this, LocationService.class));
            }
        });
        endButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                stopService(new Intent(MainActivity.this,LocationService.class));
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("Trip Complete; Distance Travelled: ");
                alertDialog.setMessage("Do you want to see your trip on the map?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }
}