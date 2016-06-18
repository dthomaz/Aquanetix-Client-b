package uk.co.aquanetix.network;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * Provides asynchronous GPS location. Not a Service in the  Android sense. 
 */
public class GpsService {

    private static final String TAG = GpsService.class.getSimpleName();
    private static final float ACCURACY_THRESHOLD = 20.0f;
    private static final long TIME_THRESHOLD = 60*1000;

    private final Handler handler;
    private final LocationManager mgr ;
    private final OnResult<Location> callback;
    private Location bestLoc;

    public GpsService(Context context, OnResult<Location> callback) {
        this.handler = new Handler();
        this.mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.callback = callback;
    }

    public void start() {
        if (mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Log.d(TAG, "GPS start");
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, onLocationChange);
            handler.postDelayed(end, TIME_THRESHOLD);
        } else { //No GPS
            Log.d(TAG, "No GPS available. Skip location.");
            callback.getResult(null);
        }
    }

    private void terminate() {
        Log.d(TAG, "GPS end");
        mgr.removeUpdates(onLocationChange);
        handler.removeCallbacks(end);
        callback.getResult(bestLoc);
    }
    
    private Runnable end = new Runnable() {
        @Override
        public void run() {
            terminate();
        }
    };
    
    private LocationListener onLocationChange=new LocationListener() {
        public void onLocationChanged(Location location) {

            if (bestLoc==null || location.getAccuracy()<bestLoc.getAccuracy()) {
                bestLoc = location;
            }
            
            if (location.getAccuracy()<ACCURACY_THRESHOLD) {
                terminate();
            }
        }
        
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
    
}
