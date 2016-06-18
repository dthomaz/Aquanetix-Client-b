package uk.co.aquanetix.network;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.model.FeedDB;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;

/**
 * Handles all download synchronisation with server:
 * - Cage allocation list
 * - User list
 * - Temperature & Oxygen
 */
public class Sync {
    
    public static final int OK = 0;
    public static final int REGISTER_DEVICE = 1;
    public static final int NO_INTERNET = 2;
    public static final int USER_LIST_ERROR = 3;
    public static final int CAGE_LIST_ERROR = 4;
    public static final int FEED_LIST_ERROR = 5;
    
    private final Context ctx;
    private final AquanetixSharedPreferences prefs;
    
    public Sync(Context ctx) {
        this.ctx = ctx;
        this.prefs = new AquanetixSharedPreferences(ctx);
    }
    
    public int update() {
        return update(false);
    }
    
    public int update(boolean forceCompleteUpdate) {
        //Check for Internet connection
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean hasInternet = ni!=null && ni.isConnected();
        
        boolean needsUpdate = forceCompleteUpdate || prefs.needsUpdate();
        boolean shouldUpdate = prefs.shouldUpdate();
        
        //Check if we need to register the device
        if (prefs.getDeviceSecret().length()==0) {
            return hasInternet ? REGISTER_DEVICE : NO_INTERNET;
        }
        
        //When no Internet, we don't update. However if we need to update, show an error
        if (!hasInternet) {
            return needsUpdate ? NO_INTERNET : OK;
        }
        
        //We are OK, no update for now
        if (!needsUpdate && !shouldUpdate) {
            return OK;
        }
        
        // TODO: Check if we the already-registered device is still valid

        //Update user list
        if (!UserDB.get(ctx).forceDownload()) {
            return USER_LIST_ERROR;
        }
        //Update feed list
        if (!FeedDB.get(ctx).forceDownload()) {
            return FEED_LIST_ERROR;
        }

        //Update cage allocation list
        CageAllocationDB c = CageAllocationDB.get(ctx);
        if (forceCompleteUpdate) {
            c.clearAllData();
        } else {
            c.removeOldData();
        }
        if (!c.forceDownload()) {
            return CAGE_LIST_ERROR;
        }
        
        //Update environmental measurements
        updateTemperature();
        updateOxygen();
        updateDeviceInfo();
        
        // Sync logs
        AquaLog.sync(ctx);
        
        //Update worked OK
        prefs.setLastServerUpdate(System.currentTimeMillis());
        return OK;
    }
    
    //Updates data from server. Works in a fire-and-forget manner.
    //This means it does not return anything even if the task fails
    public void updateAsync() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                update();
                return null;
            }
        }.execute();
    }
    
    private void updateTemperature() {
        SimpleDateFormat sdfIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK);
        // Make request
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "GET", R.string.url_temperature);
        String jsonStr = req.sendSync();
        //Parse response
        if (jsonStr==null) {
            return; // Probably network error
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.getBoolean("ok")) {
                JSONObject jsonData = json.getJSONObject("data");
                float value = (float) jsonData.getDouble("temperature");
                long tm = sdfIn.parse(jsonData.getString("date")).getTime();
                if (tm>prefs.getLastTemperatureTm()) {
                    prefs.setLastTemperature(value);
                    prefs.setLastTemperatureTm(tm);
                }
            }
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Failed to parse server JSON", e);
        } catch (ParseException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Failed to parse date from server JSON", e);
        }
    }
    
    private void updateOxygen() {
        SimpleDateFormat sdfIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK);
        // Make request
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "GET", R.string.url_oxygen);
        String jsonStr = req.sendSync();
        //Parse response
        if (jsonStr==null) {
            return; // Probably network error
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.getBoolean("ok")) {
                JSONObject jsonData = json.getJSONObject("data");
                float value = (float) jsonData.getDouble("oxygen");
                long tm = sdfIn.parse(jsonData.getString("date")).getTime();
                if (tm>prefs.getLastTemperatureTm()) {
                    prefs.setLastOxygen(value);
                    prefs.setLastOxygenTm(tm);
                }
            }
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Failed to parse server JSON", e);
        } catch (ParseException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Failed to parse date from server JSON", e);
        }
    }
    
    private void updateDeviceInfo() {
        // Build info string
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionCode).append(",");
        } catch (NameNotFoundException e1) {
            sb.append("?").append(",");
        }
        sb.append(Build.VERSION.SDK_INT).append(",");
        sb.append(Build.MANUFACTURER).append(",");
        sb.append(Build.MODEL);
        // Send it to server
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "PUT", R.string.url_deviceInfo);
        req.addParam("info", sb.toString());
        String jsonStr = req.sendSync();
        // Parse response
        if (jsonStr==null) {
            return; // Probably network error
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json.getBoolean("ok")) {
                int loglevel = json.getInt("loglevel");
                new AquanetixSharedPreferences(ctx).setLogLevel(loglevel);
            }
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Failed to parse server JSON", e);
        }
    }
    
}
