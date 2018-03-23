package com.seismic.tech.locationtracker;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<String> array;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        array = getIntent().getStringArrayListExtra("positions");
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
        /*LatLng sydney = new LatLng(37,122);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker at Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        Random rand = new Random();
        PolylineOptions rectOptions = new PolylineOptions();
        rectOptions.color(Color.argb(255, 85, 166, 27));

        for(int i=0;i<array.size();i++)
        {
            parts = array.get(i).split("~");
            timestamp = parts[0];
            latitude = Double.parseDouble(parts[1]);
            longitude = Double.parseDouble(parts[2]);
            //LatLng sydney = new LatLng((latitude + rand.nextInt(101))/100.0, (longitude + rand.nextInt(101))/100.0);
            LatLng sydney = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions().position(sydney).title(timestamp);
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.school32));
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            rectOptions.add(sydney);
        }
        mMap.addPolyline(rectOptions);
    }
}
