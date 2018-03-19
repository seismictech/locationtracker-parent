package com.rizve.tina.locationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

/**
 * Created by lislam on 3/14/18.
 */

public class GPSTracker extends Service implements LocationListener
{
    private final Context mContext;
    boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    boolean canGetLocation = false;
    Location location;
    double latitude,longitude;
    private static final long MIN_DISTANCE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000*5;
    private static GPSTracker gpsTracker = null;
    public static GPSTracker getInstance(Context context)
    {
        if(gpsTracker==null)
        {
            gpsTracker = new GPSTracker(context);
        }
        return gpsTracker;
    }
    protected LocationManager locationManager;
    private GPSTracker(Context context)
    {
        this.mContext = context;
        getLocation();
    }
    public Location getLocation()
    {
        try
        {
            locationManager = (LocationManager)mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(!isGPSEnabled && !isNetworkEnabled)
            {

            }
            else
            {
                this.canGetLocation = true;
                if(isNetworkEnabled)
                {
                    if ((ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                            (ActivityCompat.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        return null;
                    }
                    else
                    {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_FOR_UPDATES,this);
                        if(locationManager!=null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if(location!=null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }

                if(isGPSEnabled)
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_FOR_UPDATES, this);
                        if (locationManager != null)
                        {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null)
                            {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS()
    {
        if(locationManager!=null)
        {
            if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                return;
            }
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    public double getLatitude()
    {
        if(location!=null)
        {
            latitude = location.getLatitude();
        }
        return latitude;
    }
    public double getLongitude()
    {
        if(location!=null)
        {
            longitude = location.getLongitude();
        }
        return longitude;
    }
    public boolean canGetLocation()
    {
        return this.canGetLocation;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void showSettingsAlert()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS Settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go " +
                "to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int which){
                dialog.cancel();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location)
    {
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {
    }

    @Override
    public void onProviderEnabled(String s)
    {
    }

    @Override
    public void onProviderDisabled(String s)
    {
    }
}
