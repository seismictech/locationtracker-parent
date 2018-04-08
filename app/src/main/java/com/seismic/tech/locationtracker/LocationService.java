package com.seismic.tech.locationtracker;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LocationService extends Service {
    private DatabaseReference database;

    private GPSTracker gpsTracker;
    public static final long NOTIFY_INTERVAL = 1 * 60 * 1000; // 60s = 1 Minute
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    public static final String ACTION_LOCATION_BROADCAST = LocationService.class.getName() + "LocationService";
    public static final String LATITUDE = "extra_latitude";
    public static final String LONGITUDE = "extra_longitude";

    private static final String TAG = "LocationService";
    private String FIREBASE_CLOUD_FUNCTION_URL =
            "https://us-central1-locationtracker-69235.cloudfunctions.net/storeLatLong";
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    @Override
    public void onCreate()
    {
        if(mTimer != null)
        {
            mTimer.cancel();
        }
        else
        {
            mTimer = new Timer();
        }
        gpsTracker = GPSTracker.getInstance(getApplicationContext());
        database = FirebaseDatabase.getInstance().getReference();
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }
    @Override
    public void onDestroy()
    {
        mTimer.cancel();
    }
    class TimeDisplayTimerTask extends TimerTask
    {
        @Override
        public void run()
        {
            mHandler.post(new Runnable()
            {
                Location location;
                @Override
                public void run()
                {
                    location = gpsTracker.getLocation();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    MySQLiteHelper.getInstance(getApplicationContext()).addLocations(latitude,longitude);
                    addLocation(latitude,longitude,MySQLiteHelper.getInstance(getApplicationContext()).getUserId());
                    Toast.makeText(getApplicationContext(), latitude+"::"+longitude,Toast.LENGTH_SHORT).show();
                    //sendLatLongToServer(latitude,longitude);
                    Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
                    intent.putExtra(LATITUDE, location.getLatitude());
                    intent.putExtra(LONGITUDE, location.getLongitude());
                    LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);
                }
            });
        }
        private Map<String,Object> locToMap(double latitude, double longitude,String name)
        {
            HashMap<String, Object> result = new HashMap<>();
            result.put("lat", latitude);
            result.put("long", longitude);
            result.put("name", name);
            return result;
        }
        private void addLocation(double latitude, double longitude, String name)
        {
            String key = database.child("locations").push().getKey();
            Map<String, Object> postValues = locToMap(latitude,longitude,name);
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/locations/" + key, postValues);
            database.updateChildren(childUpdates);
        }
        private void sendLatLongToServer(double latitude,double longitude)
        {
            OkHttpClient httpClient = new OkHttpClient();
            HttpUrl.Builder httpBuilder = HttpUrl.parse(FIREBASE_CLOUD_FUNCTION_URL).newBuilder();
            httpBuilder.addQueryParameter("latitude",String.valueOf(latitude));
            httpBuilder.addQueryParameter("longitude",String.valueOf(longitude));
            httpBuilder.addQueryParameter("name","seismic");
            Request request = new Request.Builder().url(httpBuilder.build()).build();
            //String responseString = "";
            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    Log.e(TAG, "error in getting response from firebase cloud function");
                    //Toast.makeText(getApplicationContext(),"Cound't get response from cloud function",
                    //        Toast.LENGTH_SHORT).show();
                    //responseString = "Cound't get response from cloud function";
                }
                @Override
                public void onResponse(Call call, Response response)
                {
                    ResponseBody responseBody = response.body();
                    String resp = "";
                    if (!response.isSuccessful())
                    {
                        Log.e(TAG, "fail response from firebase cloud function");
                        //Toast.makeText(getApplicationContext(), "Cound't get response from cloud function",
                        //        Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        try
                        {
                            resp = responseBody.string();
                            System.out.println(resp);
                            //Toast.makeText(getApplicationContext(), "response: "+resp,Toast.LENGTH_SHORT).show();
                        }
                        catch (IOException e) {
                            resp = "Problem in getting response info";
                            Log.e(TAG, "Problem in reading response " + e);
                        }
                    }
                }
            });
        }
    }
}