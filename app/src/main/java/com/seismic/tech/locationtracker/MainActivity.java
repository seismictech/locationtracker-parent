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
        doAuthentication();
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
                alertDialog.setTitle("Trip Complete; Distance Travelled: "+MySQLiteHelper.getInstance(MainActivity.this).getDistanceTravelled());
                alertDialog.setMessage("Do you want to see your trip on the map?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        //ArrayList<String> array = new ArrayList<>(Arrays.asList(locationString.getText().toString().split("\n")));
                        ArrayList<String> array = MySQLiteHelper.getInstance(MainActivity.this).getTrip();
                        for(String s:array)
                        {
                            System.out.println(s);
                        }
                        intent.putStringArrayListExtra("positions", array);
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
    void doAuthentication()
    {
        if(MySQLiteHelper.getInstance(this).isRegistered()==false)
        {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.registration);
            dialog.setTitle("Registration");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
                {
                    // TODO Auto-generated method stub
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        System.exit(0);
                    }
                    return false;
                }
            });

            final EditText employeeId = (EditText)dialog.findViewById(R.id.EmployeeId);
            final EditText password = (EditText)dialog.findViewById(R.id.password);
            final EditText confirmPassword = (EditText)dialog.findViewById(R.id.confirm_password);

            Button okButton = (Button)dialog.findViewById(R.id.ok_button);
            okButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(isRegistrationValid(employeeId.getText().toString(),password.getText().toString(),confirmPassword.getText().toString()))
                    {
                        MySQLiteHelper.getInstance(getApplicationContext()).
                                insertUserInfo(employeeId.getText().toString(),password.getText().toString());
                        dialog.dismiss();
                    }
                }
                private boolean isRegistrationValid(String empId, String password, String confirmPassword)
                {
                    if(empId.length()>=8)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Employee Id must be less than 8 characters");
                        alert.setCancelable(true);
                        alert.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });
                        alert.show();
                        return false;
                    }
                    if(isPasswordValid(password,confirmPassword))
                        return true;
                    else
                        return false;
                }
                private boolean isPasswordValid(String password,String confirmPassword)
                {
                    if(!password.equals(confirmPassword))
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password does not match");
                        alert.setCancelable(true);
                        alert.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });
                        alert.show();
                        return false;
                    }
                    if(password.length()!=4)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password must be of 4 characters");
                        alert.setCancelable(true);
                        alert.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });
                        alert.show();
                        return false;
                    }
                    boolean charFound,digitFound;
                    charFound = digitFound = false;
                    for(int i=0;i<password.length();i++)
                    {
                        if(password.charAt(i)>='0'&&password.charAt(i)<='9')
                        {
                            digitFound = true;
                        }
                        else if((password.charAt(i)>='a'&&password.charAt(i)<='z')||(password.charAt(i)>='A'&&password.charAt(i)<='Z'))
                        {
                            charFound = true;
                        }
                    }
                    if(digitFound&&charFound)
                        return true;
                    else
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Pin must be alphanumeric");
                        alert.setCancelable(true);
                        alert.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        });
                        alert.show();
                        return false;
                    }
                }
            });
            dialog.show();
        }
        else
        {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.login);
            dialog.setTitle("Login");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
                {
                    // TODO Auto-generated method stub
                    if(keyCode == KeyEvent.KEYCODE_BACK)
                    {
                        System.exit(0);
                    }
                    return false;
                }
            });

            final EditText pin = (EditText)dialog.findViewById(R.id.password);
            Button okButton = (Button)dialog.findViewById(R.id.ok_button);
            okButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(checkPin(pin.getText().toString()))
                    {
                        dialog.dismiss();
                    }
                    else
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password didn't match");
                        alert.setCancelable(true);
                        alert.setNegativeButton("Ok",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // TODO Auto-generated method stub
                                dialog.cancel();
                            }
                        });
                        alert.show();
                    }
                }
                private boolean checkPin(String password)
                {
                    return MySQLiteHelper.getInstance(MainActivity.this).checkPin(password);
                }
            });
            dialog.show();
        }
    }
}