package uk.co.aquanetix.network;

import java.util.Locale;

import org.json.JSONObject;

import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.model.CageAllocationDB;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A service that is responsible to send HTTP requests to server.
 * Mostly used for the POST requests (recording of data).
 * If there is no Internet connection, it can cache the requests and send them later.
 * By default, it tries to add GPS location to each request.
 * 
 * Use the {@code addToQueue} method to schedule requests.
 */

public class RequestQueue extends IntentService {
    
    private static final int INTERVAL = 5 * 60 * 1000; // 5 mins
    private static RequestQueueSQL sql;
    
    public RequestQueue() {
        super(RequestQueue.class.getSimpleName());
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        //The intent contains no information. It does not contain the request to be sent.
        //It is like a signal reading "I want the pending requests to be send now".
        //The actual requests are stored in the DB.
        //In case there is no Internet, this method will be called repeatedly for
        //the same request.
        
        if (sql==null) {
            //This could occur after abnormal termination of app
            //or if user clicks "Synchronise now", before entering any data.
            sql = RequestQueueSQL.get(getApplicationContext());
        }
        
        //Check for Internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        @SuppressWarnings("deprecation")
        boolean hasInternet = cm.getBackgroundDataSetting() && ni!=null && ni.isConnected();
        if (!hasInternet) {
            //No Internet. Do nothing until next call by AlarmManager
            return;
        }
        //Try to send all requests in queue
        long nextId;
        while ( (nextId=sql.nextReady())>0 ) {
            try {
                RequestDescription req = sql.readOne(nextId);
                String result = req.sendSync();
                if (result!=null && new JSONObject(result).getBoolean("ok")) {
                    sql.remove(nextId); // Done
                } else {
                    sql.postpone(nextId); // Failed. Put outside of the queue for a while and try with other requests
                }
            } catch (Exception e) {
                //Leave the request at the head of queue and wait for next cycle.
                //TODO: this could cause a stuck queue if the item at the head is "faulty"
                AquaLog.error("Failed to send data to server ", e);
                return;
            }
        }
        //The radio is on, so take the chance to update the cage allocation list
        //without using extra battery. Also note that this is happening on an
        //service thread
        boolean shouldUpdate = new AquanetixSharedPreferences(this).shouldUpdate();
        if (shouldUpdate) {
            CageAllocationDB.get(this).forceDownload();
        }
        //If nothing to send, terminate Service
        if (sql.size()==0) {
            RequestQueue.setServiceAlarm(this, false);
        }
    }
    
    public static void addToQueue(Context ctx, final RequestDescription req) {
        //Better context
        ctx = ctx.getApplicationContext();
        //New sql object (if first use)
        if (sql==null) {
            sql = RequestQueueSQL.get(ctx);
        }
        //Add to request queue synchronously.
        final long id = sql.add(req, true);
        //Request an synchronous GPS location
        new GpsService(ctx, new OnResult<Location>() {
            @Override
            public void getResult(Location loc) {
                if (loc==null) {
                    sql.gpsDone(id, req);
                } else {
                    req.addParam("event[user_position_start_lat]", String.format(Locale.US, "%.6f", loc.getLatitude()));
                    req.addParam("event[user_position_start_lng]", String.format(Locale.US, "%.6f", loc.getLongitude()));
                    req.addParam("event[user_position_end_lat]", String.format(Locale.US, "%.6f", loc.getLatitude()));
                    req.addParam("event[user_position_end_lng]", String.format(Locale.US, "%.6f", loc.getLongitude()));
                    sql.gpsDone(id, req);
                }
            }
        }).start();
        
        //Ensure the service is running
        setServiceAlarm(ctx, true);
    }
    
    private static void setServiceAlarm(Context ctx, boolean isOn) {
        Intent i = new Intent(ctx, RequestQueue.class);
        
        //Avoid starting an already started Service.
        if (isOn) {
            PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
            if (pi!=null) {
                return;
            }
        }
        
        //Create PendingIntent
        PendingIntent pi = PendingIntent.getService(ctx, 0, i, 0);
        
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        
        if (isOn) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15*1000, INTERVAL, pi);
        } else {
            am.cancel(pi);
            pi.cancel();
        }
    }
    
}
