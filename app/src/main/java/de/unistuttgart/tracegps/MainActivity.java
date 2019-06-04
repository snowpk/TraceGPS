package de.unistuttgart.tracegps;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    public static final int REQUEST_WRITE_STORAGE = 112;

    private static final String TAG = MainActivity.class.getCanonicalName();
    TraceService traceService;
    private boolean isBound;

    TextView latitude;
    TextView longitude;
    TextView speed;
    TextView dist;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cannot use GPS without Location permission", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else if ( requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cannot use Write to External Storage without permission", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void initializeViews(){

        setContentView(R.layout.activity_main);
        //TODO: LAYOUT
        Button updateButton = (Button) findViewById(R.id.buttonUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBound) {
                    updateDisplay();
                }
            }
        });
        final Button startStopButton = (Button) findViewById(R.id.buttonStartStop);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isBound) {
                    latitude.setText("-");
                    longitude.setText("-");
                    dist.setText("-");
                    speed.setText("-");
                    startAndBindService();
                    Log.d(TAG, "Press Start Trace()");
                    startStopButton.setText("Stop Trace");
                } else {
                    stopAndUnbindService();
                    Log.d(TAG, "Press Stop Trace()");
                    startStopButton.setText("Start Trace");
                }
            }
        });



        latitude = findViewById(R.id.latitudeText);
        longitude = findViewById(R.id.longitudeText);
        dist = findViewById(R.id.distanceText);
        speed = findViewById(R.id.speedText);


    }

    private void updateDisplay(){
        latitude.setText("Latitude: "+ String.valueOf(traceService.getLatitude()));
        longitude.setText("Longitude: "+String.valueOf(traceService.getLongitude()));
        double distdbl = traceService.getDistance();
        int distance = (int) (distdbl * 100);
        distdbl = distance / 100;
        dist.setText("Distance: " + String.valueOf(distdbl) + " m");
        speed.setText("Avg. Speed: "+String.valueOf(traceService.getAverageSpeed() * 3.6) + " km/h Current: " +String.valueOf(traceService.getSpeed() * 3.6) + " km/h" );
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeViews();
        isBound = false;
        traceService = null;
        Log.d(TAG, "onCreate()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission denied");
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    REQUEST_ACCESS_FINE_LOCATION);

        }

        boolean hasPermission = (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){

            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                   REQUEST_WRITE_STORAGE);

        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopAndUnbindService();
        Log.d(TAG, "onDestroy()");

    }

    private void startAndBindService() {
        if (!isBound) {
            Toast.makeText(this,"Please Wait",Toast.LENGTH_LONG).show();
            Intent serviceIntent = new Intent(this, TraceService.class);
            startService(serviceIntent);
            bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        }
    }

    private void stopAndUnbindService(){
        if(isBound) {
            Intent serviceIntent = new Intent(this, TraceService.class);
            unbindService(this);
            stopService(serviceIntent);
            traceService = null;
            isBound = false;
        }

    }


   @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        traceService = ((TraceService.LocalBinder) service).getService();
        isBound = true;
        Log.d(TAG, "onServiceConnected()");


    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        traceService = null;
        isBound = false;
        Log.d(TAG, "onServiceDisconnected()");

    }
}
