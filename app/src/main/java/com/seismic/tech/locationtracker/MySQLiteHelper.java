package com.seismic.tech.locationtracker;

/**
 * Created by lislam on 3/19/18.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MySQLiteHelper extends SQLiteOpenHelper
{
    private static MySQLiteHelper instance;
    private static Context context;
    private static final String DATABASE_NAME = "locationtracker.db";
    private static final int DATABASE_VERSION = 3;

    private static final String createUserTable = "CREATE TABLE UserInfo ( _id TEXT PRIMARY KEY, password TEXT )";
    private static final String createTripTable = "CREATE TABLE Trip ( timestamp TEXT, latitude REAL, longitude REAL )";

    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(createUserTable);
        database.execSQL(createTripTable);
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
    public double getDistanceTravelled()
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<location> locations = new ArrayList<>();
        double distance = 0;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from Trip",null);
            while (cursor.moveToNext())
            {
                locations.add(new location(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2))));
            }
            for(int i=1;i<locations.size();i++)
            {
                distance += getDistance(locations.get(i),locations.get(i-1));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return distance;
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
    public ArrayList<String> getTrip()
    {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try
        {
            db = getReadableDatabase();
            cursor = db.rawQuery("select * from Trip",null);
            if(cursor.moveToFirst())
            {
                System.out.println(cursor.getCount());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ArrayList<String> locations = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                locations.add(cursor.getString(0)+"~"+cursor.getString(1)+"~"+cursor.getString(2));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cursor.close();
        }
        return locations;
    }

    public void addLocations(double latitude,double longitude)
    {
        try
        {
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            System.out.println(currentDateTimeString);
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("timestamp", currentDateTimeString);
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
