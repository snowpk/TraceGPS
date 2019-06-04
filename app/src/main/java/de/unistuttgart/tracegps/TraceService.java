package de.unistuttgart.tracegps;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TraceService extends Service {
    private static final String TAG = TraceService.class.getCanonicalName();
    public static final int REQUEST_ACCESS_FINE_LOCATION = 1;

    LocationManager lmanager;
    Location current;
    Location origin;
    //TODO: MAYBE JUST LOG INSTANTKY WITHOUT SAVING TO LIST
    ArrayList<Location> route;
    private double distance;
    String fileName;
    File sdDir;
    File myFile;
    private final IBinder mBinder = new LocalBinder();


    /**
     * Callback for Location or Provider Updates
     */
    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (origin == null) {
                origin = location;
                current = location;
                route.add(location);
            } else {
                distance = distance + location.distanceTo(current);
                current = location;
                route.add(location);
            }

            try {
                writeLocation(location);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "onLocationChanged()");



        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    /**
     *  Write all locations of the traced route to a file
     */

    public void writeToFile(boolean preamble) throws IOException {

        if(preamble){
            FileOutputStream outStreamWriter = new FileOutputStream(myFile,false);
            String result = formatGpx(true);
            outStreamWriter.write(result.getBytes());

            outStreamWriter.close();

        } else {
            FileOutputStream outStreamWriter = new FileOutputStream(myFile, true);
            String result = formatGpx(false);
            outStreamWriter.write(result.getBytes());

            outStreamWriter.close();

        }


        Log.d(TAG, "Printing something");




    }

     public void writeLocation(Location location) throws IOException {

         FileOutputStream outStreamWriter = new FileOutputStream(myFile,true);
         StringBuilder sb = new StringBuilder();
         sb.append("         <trkpt lat=\""+location.getLatitude()+"\" lon=\""+location.getLongitude()+"\"> </trkpt>\n");
         outStreamWriter.write(sb.toString().getBytes());
         outStreamWriter.close();
     }



    /**
     * Formats the locations of a provides List into a String suitable for
     * a GPX File
     * @param preamble
     * @return
     */
    public String formatGpx(boolean preamble){

        if (preamble) {

            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
            sb.append("<gpx version=\"1.1\" creator=\"Team14\">\n");
            sb.append(" <trk>\n");
            sb.append("     <trkseg>\n");

            return sb.toString();

        } else {

        /*for (int i = 0 ; i < locations.size(); i++){

            sb.append("         <trkpt lat=\""+locations.get(i).getLatitude()+"\" lon=\""+locations.get(i).getLongitude()+"\"> </trkpt>\n");
        }*/

            StringBuilder sb = new StringBuilder();
            sb.append("     </trkseg>\n");
            sb.append(" </trk>\n");
            sb.append("</gpx>");

            return sb.toString();
        }
    }


    @Override
    public IBinder onBind(Intent intent) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No Permission");
        }
        lmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
        distance = 0;
        sdDir = Environment.getExternalStorageDirectory();
        myFile = new File(sdDir+"/Download", "trace.gpx");
        try {
            writeToFile(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, myFile.getName());
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            writeToFile(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        distance = 0;
        lmanager.removeUpdates(listener);
        Log.d(TAG, Integer.toString(route.size()));
        route.clear();
        current = null;
        origin = null;
        //TODO: WRITE LOG
        Log.d(TAG, Integer.toString(route.size()));
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate(){
        lmanager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        route = new ArrayList<>();
    }

    // Methods for Activity to query Information about current route

    public double getLatitude(){
        if(current!=null) {
            return current.getLatitude();
        } else {
            return -1;
        }
    }

    public double getLongitude(){
        if(current!=null) {
            return current.getLongitude();
        } else {
            return -1;
        }
    }

    public double getAverageSpeed(){
        if(current!=null) {
            return current.getSpeed();
        } else {
            return -1;
        }
    }

    public double getDistance(){
        return distance;
    }

    class LocalBinder extends Binder {
        TraceService getService() {
            return TraceService.this;
        }
    }

}
