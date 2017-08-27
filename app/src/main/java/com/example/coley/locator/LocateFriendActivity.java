package com.example.coley.locator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Coley Pritchett
 *
 * This activity is used to find a person that the user can select from their contact list.
 */
public class LocateFriendActivity extends Activity implements SensorEventListener, LocationListener
{
    private TextView mDistance;
    private Firebase myFirebaseRef;
    private Button mSelectFriendButton;
    private AuthData authData;
    // initialized coordinates to 0 in case either of the users haven't used invenio yet and the database is empty
    private String key, myLat = "0", myLong = "0", friendLat = "0", friendLong = "0";
    public float myLatNum, myLongNum, friendLatNum, friendLongNum;
    private ImageView mImage;
    private SensorManager mSensorManager;
    private Sensor mOrientation;

    private float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_friend);

        myFirebaseRef = new Firebase("https://invenio.firebaseio.com/");

        authData = myFirebaseRef.getAuth();

        mDistance = (TextView)findViewById(R.id.distance);
        mImage = (ImageView)findViewById(R.id.imageViewCompass);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);


        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 0, this);

        if (!displayGpsStatus()) {
            alertbox("Warning: GPS Satus", "Your GPS is OFF");
        }

        mSelectFriendButton = (Button)findViewById(R.id.select_friend_button);
        mSelectFriendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, 1);
            }
        });
    }

    // Method for selecting number from contact list and establishing connection in database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        final String number = c.getString(0);
                        myFirebaseRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            // Called when location is updated
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot child : dataSnapshot.getChildren())
                                {
                                    for (DataSnapshot grandchild : child.getChildren())
                                    {
                                        if (grandchild.getValue().toString().contains(number))
                                        {
                                            // get uid of contact
                                            key = getKey(child.toString());
                                            // set listeners
                                            setFriendLong();
                                            setFriendLat();
                                            setMyLat();
                                            setMyLong();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }



    public void setFriendLat() {
        myFirebaseRef.child("users").child(key).child("lat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendLat = dataSnapshot.getValue().toString();
                System.out.println("FRIEND LAT: " + friendLat);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setFriendLong() {
        myFirebaseRef.child("users").child(key).child("long").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendLong = dataSnapshot.getValue().toString();
                System.out.println("FRIEND Long: " + friendLong);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setMyLat() {
        myFirebaseRef.child("users").child(authData.getUid()).child("lat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myLat = dataSnapshot.getValue().toString();
                System.out.println("My LAT: " + myLat);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void setMyLong() {
        myFirebaseRef.child("users").child(authData.getUid()).child("long").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myLong = dataSnapshot.getValue().toString();
                System.out.println("My Long: " + myLong);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }



    // Gets the UID from a string that consists of all user node data
    public String getKey(String user) {
        String uid = "";

        for (int i = 0; i < user.length(); i++)
        {
            if (user.charAt(i) == 'k' && user.charAt(i + 1) == 'e' && user.charAt(i + 2) == 'y')
            {
                uid = user.substring(i + 6, i + 42);
            }
        }

        return uid;
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {}
    public void onProviderEnabled(String provider) {}
    public void onProviderDisabled(String provider) {}

    public void onLocationChanged(Location location) {
        Map<String, Object> map = new HashMap<>();
        // Called when a new location is found by the network location provider.
        String lat = "" + location.getLatitude();
        String lon = "" + location.getLongitude();
        map.put("lat", lat);
        map.put("long", lon);
        myFirebaseRef.child("users").child(authData.getUid()).updateChildren(map);
    }

    // Method for creating the AlertBox in case GPS is turned off
    protected void alertbox(String title, String mymessage)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disabled")
                .setCancelable(false)
                .setTitle("Gps Status")
                .setPositiveButton("Turn Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                Intent myIntent = new Intent(
                                        Settings.ACTION_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    // Checks whether the GPS is enabled or disabled
    private Boolean displayGpsStatus()
    {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus)
            return true;
        else
            return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        myLatNum = Float.parseFloat(myLat);
        myLongNum = Float.parseFloat(myLong);
        friendLatNum = Float.parseFloat(friendLat);
        friendLongNum = Float.parseFloat(friendLong);

        // get the angle around the z-axis rotated
        //float degree = Math.round(event.values[0]);
        float degree = bearing(myLatNum, myLongNum, friendLatNum, friendLongNum);

        // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        mImage.startAnimation(ra);
        currentDegree = -degree;

        mDistance.setText("" + distFrom(myLatNum, myLongNum, friendLatNum, friendLongNum));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    // Gets the angle between the two sets of coordinates for the compass to point at
    public float bearing(float lat1, float lon1, float lat2, float lon2){
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2-longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1)
                * Math.cos(latitude2) * Math.cos(longDiff);

        return ((float)Math.toDegrees(Math.atan2(y, x)) + 360) % 360 + 180;
    }

    // Gets the distance between the two users
    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
