package com.seismic.tech.locationtracker;

/**
 * Created by lislam on 3/19/18.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MySQLiteHelper extends SQLiteOpenHelper
{
    private static MySQLiteHelper instance;
    private static Context context;
    private static final String DATABASE_NAME = "locationtracker.db";
    private static final int DATABASE_VERSION = 7;

    private static final String createUserTable = "CREATE TABLE UserInfo ( _id TEXT PRIMARY KEY, password TEXT )";
    private static final String createTripTable = "CREATE TABLE Trip ( timestamp INTEGER, latitude REAL, longitude REAL )";
    private static final String createPastTripsTable = "CREATE TABLE PastTrips ( _id INT PRIMARY KEY, trip TEXT, distance REAL, time INTEGER, tripDateTime INTEGER )";

    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(createUserTable);
        database.execSQL(createTripTable);
        database.execSQL(createPastTripsTable);
    }

    public void insertUserInfo(String userId,String password)
    {
        try
        {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("_id",userId);
            values.put("password",password);
            db.insert("UserInfo",null,values);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static MySQLiteHelper getInstance(Context contxt)
    {
        if (instance == null)
        {
            context = contxt;
            instance = new MySQLiteHelper(contxt);
        }
        return instance;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(MySQLiteHelper.class.getName(),"Upgrading database from version " + oldVersion +
                " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS UserInfo");
        db.execSQL("DROP TABLE IF EXISTS Trip");
        db.execSQL("DROP TABLE IF EXISTS PastTrips");
        System.out.println("Upgrading the database. Tables are deleted");
        onCreate(db);
    }

    public boolean isRegistered()
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select count(*) from UserInfo", null);
            cursor.moveToFirst();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Integer.parseInt(cursor.getString(0))==1;
    }
    public String getUserId()
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        String userId = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select _id from UserInfo", null);
            if(cursor.moveToFirst())
            {
                System.out.println("User Id = " + cursor.getString(0));
                userId = cursor.getString(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {

        }
        return userId;
    }
    public void initiateTrip()
    {
        SQLiteDatabase db = null;
        try
        {
            db = getWritableDatabase();
            db.delete("Trip", null, null);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    private double getDistance(location loc1,location loc2)
    {
        double lon1,lon2,lat1,lat2;
        lat1 = loc1.latitude;
        lon1 = loc1.longitude;
        lat2 = loc2.latitude;
        lon2 = loc2.longitude;
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg)
    {
        return (deg * Math.PI / 180.0);
    }
    private double rad2deg(double rad)
    {
        return (rad * 180.0 / Math.PI);
    }

    public HashMap<String,ArrayList<Trip>> getTrips()
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        HashMap<String,ArrayList<Trip>> hashmap = new HashMap<>();
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from PastTrips",null);
            if(cursor.moveToFirst())
            {
                while (cursor.moveToNext())
                {
                    Trip trip = new Trip();
                    trip.position = cursor.getString(cursor.getColumnIndex("trip"));
                    trip.distance = cursor.getDouble(cursor.getColumnIndex("distance"));
                    trip.time = cursor.getInt(cursor.getColumnIndex("time"));
                    trip.startTime = cursor.getLong(cursor.getColumnIndex("tripDateTime"));
                    String dateTime = SimpleDateFormat.getDateTimeInstance().format(new Date(trip.startTime)).substring(0,12);
                    System.out.println(dateTime);
                    if(hashmap.containsKey(dateTime)==true)
                    {
                        hashmap.get(dateTime).add(trip);
                    }
                    else
                    {
                        ArrayList<Trip> trips = new ArrayList<>();
                        trips.add(trip);
                        hashmap.put(dateTime,trips);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cursor.close();
        }
        return hashmap;
    }

    private class location
    {
        String timestamp;
        double latitude;
        double longitude;
        location(String timestamp,double latitude,double longitude)
        {
            this.timestamp = timestamp;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
    public Trip getAndSaveTrip()
    {
        Trip currentTrip = new Trip();
        SQLiteDatabase db = null;
        double distance = 0.0;
        Cursor cursor = null;
        int counter;
        String currentDateTimeString = null;
        ArrayList<String> positions = new ArrayList<>();
        ArrayList<location> locations = new ArrayList<>();
        try
        {
            db = getWritableDatabase();
            cursor = db.rawQuery("select * from Trip",null);
            if(cursor.moveToFirst())
            {
                StringBuffer  strBuff = new StringBuffer();
                counter = 0;
                while (cursor.moveToNext())
                {
                    positions.add(cursor.getString(0)+"~"+cursor.getString(1)+"~"+cursor.getString(2));
                    if(counter==0)
                    {
                        currentDateTimeString = cursor.getString(0);
                    }
                    counter++;
                    strBuff.append(positions.get(positions.size()-1)+"+");
                    locations.add(new location(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2))));
                }
                for(int i=1;i<locations.size();i++)
                {
                    distance += getDistance(locations.get(i),locations.get(i-1));
                }
                currentTrip.positions = positions;
                currentTrip.distance = distance;
                currentTrip.time = (locations.size()-1)*HomeActivity.interval;
                ContentValues values = new ContentValues();
                strBuff.deleteCharAt(strBuff.length()-1);
                values.put("trip",strBuff.toString());
                values.put("distance", currentTrip.distance);
                values.put("time",currentTrip.time);
                values.put("tripDateTime",Long.parseLong(currentDateTimeString));
                db.insert("PastTrips",null,values);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cursor.close();
        }
        return currentTrip;
    }

    public void addLocations(double latitude,double longitude)
    {
        try
        {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("timestamp", System.currentTimeMillis());
            values.put("latitude", latitude);
            values.put("longitude", longitude);
            db.insert("Trip", null, values);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {

        }
    }
    public boolean checkPin(String password)
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select count(*) from UserInfo where password='"+password+"'", null);
            cursor.moveToFirst();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Integer.parseInt(cursor.getString(0))==1;
    }
}
