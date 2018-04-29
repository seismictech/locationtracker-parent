package com.seismic.tech.locationtracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener
{
    ImageView startImage;
    TextView startText;
    ImageView endImage;
    TextView getStartText;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gpsTracker;
    boolean isTripGoingOn;
    private Handler mHandler;
    private Runnable mUpdateTimeTask;
    private long counter = 0;
    private FirebaseDatabase database;
    private String userName;
    public static int interval;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        doAuthentication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        interval = Integer.valueOf("1");
        userName = MySQLiteHelper.getInstance(getApplicationContext()).getUserId();
        isTripGoingOn = false;
        startImage = (ImageView) findViewById(R.id.startId);
        endImage = (ImageView) findViewById(R.id.endId);
        startText = (TextView) findViewById(R.id.startText);
        getStartText = (TextView) findViewById(R.id.sttext);
        gpsTracker = GPSTracker.getInstance(HomeActivity.this);
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission) != MockPackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE_PERMISSION);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mHandler = new Handler();
        mUpdateTimeTask = new Runnable() {
            public void run() {
                long hours = counter / 3600;
                long seconds = counter % 3600;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                if (counter % 2 == 0) {
                    startText.setTextColor(Color.BLACK);
                } else {
                    startText.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
                startText.setText(String.format("Trip: (%02d:%02d:%02d)", hours, minutes, seconds));
                counter++;
                mHandler.postDelayed(mUpdateTimeTask, 1000);
            }
        };
    }
    void doAuthentication()
    {
        if(MySQLiteHelper.getInstance(this).isRegistered()==false)
        {
            database = FirebaseDatabase.getInstance();
            database.setPersistenceEnabled(true);
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.registration);
            dialog.setTitle("Registration");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
                {
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Employee Id must be less than 8 characters");
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
                    if(isPasswordValid(password,confirmPassword))
                        return true;
                    else
                        return false;
                }
                private boolean isPasswordValid(String password,String confirmPassword)
                {
                    if(!password.equals(confirmPassword))
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password does not match");
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
                    if(password.length()!=4)
                    {
                        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password must be of 4 characters");
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
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
            database = FirebaseDatabase.getInstance();
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.login);
            dialog.setTitle("Login");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener()
            {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
                {
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
                        alert.setTitle("Warning");
                        alert.setMessage("Password didn't match");
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
                    }
                }
                private boolean checkPin(String password)
                {
                    return MySQLiteHelper.getInstance(HomeActivity.this).checkPin(password);
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }


    @Override
    public void onRestart()
    {
        super.onRestart();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.startId:
                if(isTripGoingOn==false) {
                    isTripGoingOn = true;
                    counter = 0L;
                    startImage.setImageResource(R.drawable.pause);
                    getStartText.setText("Started");
                    mHandler.post(mUpdateTimeTask);
                    startText.setTypeface(null, Typeface.BOLD);
                    MySQLiteHelper.getInstance(HomeActivity.this).initiateTrip();
                    startService(new Intent(HomeActivity.this, LocationService.class));
                }
                else
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                    alertDialog.setTitle("Can not start a new trip");
                    alertDialog.setMessage("You are already in a trip now. Do you want to see your current trip on the map?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
                break;
            case R.id.endId:
                if(isTripGoingOn==true)
                {
                    isTripGoingOn = false;
                    startImage.setImageResource(R.drawable.start2);
                    startText.setText("Start Trip");
                    getStartText.setText("Start");
                    isTripGoingOn = false;
                    startText.setTypeface(null, Typeface.NORMAL);
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    stopService(new Intent(HomeActivity.this, LocationService.class));
                    database.getReference().child("/livelocations/"+userName).removeValue();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                    final Trip currentTrip = MySQLiteHelper.getInstance(HomeActivity.this).getAndSaveTrip();
                    alertDialog.setTitle("Trip Completed");
                    alertDialog.setMessage("Distance Travelled: " + currentTrip.distanceTravelled + ". Do you want to see your trip on the map?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(HomeActivity.this, MapsActivity.class);
                            intent.putExtra("distance", currentTrip.distanceTravelled);
                            intent.putStringArrayListExtra("positions", currentTrip.positions);
                            intent.putExtra("time", currentTrip.timeSpent);
                            intent.putExtra("activity", "HomeActivity");

                            startActivity(intent);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
                else
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
                    alertDialog.setTitle("Invalid");
                    alertDialog.setMessage("You are currently not on any trip");
                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
                }
                break;
            case R.id.viewId:
                Intent intent = new Intent(HomeActivity.this,ViewTripsActivity.class);
                startActivity(intent);
                break;
            case R.id.settingId:
                Toast.makeText(HomeActivity.this, "Settings",Toast.LENGTH_SHORT).show();
                break;
            case R.id.contactId:
                Toast.makeText(HomeActivity.this, "Contact Us",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}