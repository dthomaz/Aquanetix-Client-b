package uk.co.aquanetix.network;

import uk.co.aquanetix.android.AquaLog;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class AquanetixSharedPreferences {

    //File name
    private static final String APP_SHARED_PREFS = AquanetixSharedPreferences.class.getSimpleName();
    //Authentication
    private static final String UUID = "uuid";
    private static final String DEVICE_SECRET = "deviceSecret";
    private static final String USER_ID = "userId";
    private static final String FEEDING_ID = "feedingId";
    private static final String FEED_ID = "feedId";
    private static final String USERNAME = "username";
    private static final String ROLE = "role";
    private static final String FEEDNAME = "feedname";
    private static final String COMPANY_ID = "companyId";
    // Logging
    private static final String LOG_LEVEL = "loglevel";
    private static final String LAST_LOG_SYNC = "tmLastLogSync";
    //User-list
    private static final String USERS_JSON = "usersJson";
    //Feed-list
    private static final String FEEDS_JSON = "feedsJson";
    //Environmental measurements
    private static final String TEMPERATURE_VALUE = "lastTemperature";
    private static final String TEMPERATURE_LAST_TM = "tmLastTemperature";
    private static final String OXYGEN_VALUE = "lastOxygen";
    private static final String OXYGEN_LAST_TM = "tmLastOxygen";
    //Last timestamp we downloaded data from server
    private static final String LAST_SERVER_UPADTE = "tmLastTemperature";
    
    private SharedPreferences sharedPrefs;
    
    public AquanetixSharedPreferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
    }
    
    public String getUUID() {
        String uuid = sharedPrefs.getString(UUID, null);
        if (uuid==null) {
            uuid = java.util.UUID.randomUUID().toString();
            save(UUID, uuid);
        }
        return uuid;
    }
    
    public String getDeviceSecret() { return sharedPrefs.getString(DEVICE_SECRET, ""); }
    public int getUserId() { return sharedPrefs.getInt(USER_ID, -1); }
    public String getUsername() { return sharedPrefs.getString(USERNAME, ""); }
    public String getRole() { return sharedPrefs.getString(ROLE, ""); }
    public String getFeedname() { return sharedPrefs.getString(FEEDNAME, ""); }
    public int getCompanyId() { return sharedPrefs.getInt(COMPANY_ID, -1); }
    public int getLogLevel() { return sharedPrefs.getInt(LOG_LEVEL, 0); }
    public String getUsersJson(){ return sharedPrefs.getString(USERS_JSON, ""); }
    public String getFeedsJson(){ return sharedPrefs.getString(FEEDS_JSON, ""); }
    public float getLastTemperature(){ return sharedPrefs.getFloat(TEMPERATURE_VALUE, 20f); }
    public long getLastTemperatureTm(){ return sharedPrefs.getLong(TEMPERATURE_LAST_TM, 0); }
    public float getLastOxygen(){ return sharedPrefs.getFloat(OXYGEN_VALUE, 12f); }
    public long getLastOxygenTm(){ return sharedPrefs.getLong(TEMPERATURE_LAST_TM, 0); }
    public long getLastServerUpdate(){ return sharedPrefs.getLong(LAST_SERVER_UPADTE, 0); }
    
    public void setDeviceSecret(String deviceSecret){ save(DEVICE_SECRET, deviceSecret); }
    public void setUserId(int userId){ save(USER_ID, userId); }
    public void setUsername(String username){ save(USERNAME, username); }
    public void setRole(String role){ save(ROLE, role); }
    public void setCompanyId(int companyId){ save(COMPANY_ID, companyId); }
    public void setLastLogSync(long tm){ save(LAST_LOG_SYNC, tm); }
    public void setUsersJson(String json){ save(USERS_JSON, json); }
    public void setFeedsJson(String json){ save(FEEDS_JSON, json); }
    public void setLastTemperature(float val){ save(TEMPERATURE_VALUE, val); }
    public void setLastTemperatureTm(long tm){ save(TEMPERATURE_LAST_TM, tm); }
    public void setLastOxygen(float val){ save(OXYGEN_VALUE, val); }
    public void setLastOxygenTm(long tm){ save(OXYGEN_LAST_TM, tm); }
    public void setLastServerUpdate(long tm){ save(LAST_SERVER_UPADTE, tm); }
    
    // This is an different to other setters, because the value is cached at AquaLog for performance reasons.
    public void setLogLevel(int logLevel) {
        save(LOG_LEVEL, logLevel);
        AquaLog.setLogLevel(logLevel);
    }
    
    /** Returns true if we have not done a server update in the last 7 days */
    public boolean needsUpdate() {
        long lastUpdate = sharedPrefs.getLong(LAST_SERVER_UPADTE, 0);
        return System.currentTimeMillis() - lastUpdate > 7*24*3600*1000;
    }
    
    /** Returns true if we have not done a server update in the last 5 minutes */
    public boolean shouldUpdate() {
        long lastUpdate = sharedPrefs.getLong(LAST_SERVER_UPADTE, 0);
        return System.currentTimeMillis() - lastUpdate > 5*60*1000;
    }
    
    /** Returns true if we have not done a log sync in the hour */
    public boolean shouldSyncLogs() {
        long lastSync = sharedPrefs.getLong(LAST_LOG_SYNC, 0);
        return System.currentTimeMillis() - lastSync > 3600*1000;
    }
    
    private void save(String key, float value) {
        Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putFloat(key, value);
        prefsEditor.commit();
    }
    
    private void save(String key, long value) {
        Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putLong(key, value);
        prefsEditor.commit();
    }
    
    private void save(String key, int value) {
        Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }
    
    private void save(String key, String value) {
        Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }
    
}
