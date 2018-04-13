package com.seismic.tech.locationtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<String> positions;
    double distance;
    int timespent;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        positions = getIntent().getStringArrayListExtra("positions");
        distance = getIntent().getDoubleExtra("distance",0.0);
        timespent = getIntent().getIntExtra("time",0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(String.format("Distance = %.2f  and Time = %d minute",distance,timespent));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String[] parts;
        double latitude,longitude;
        String timestamp;
        long ct;
        /*LatLng sydney = new LatLng(37,122);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        //Random rand = new Random();
        PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.color(Color.argb(255, 85, 166, 27));
        LatLng sydney = null;

        for(int i=0;i<positions.size();i++)
        {
            parts = positions.get(i).split("~");
            ct = Long.parseLong(parts[0]);
            timestamp = SimpleDateFormat.getDateTimeInstance().format(new Date(ct));
            latitude = Double.parseDouble(parts[1]);
            longitude = Double.parseDouble(parts[2]);
            //LatLng sydney = new LatLng((latitude + rand.nextInt(101))/100.0, (longitude + rand.nextInt(101))/100.0);
            sydney = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions().position(sydney).title(timestamp);
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.school32));
            mMap.addMarker(marker);
            rectOptions.add(sydney);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,15.2f));
        mMap.addPolyline(rectOptions);
    }
}
