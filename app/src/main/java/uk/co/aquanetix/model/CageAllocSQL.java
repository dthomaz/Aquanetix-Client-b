package uk.co.aquanetix.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.android.AquaLog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Do NOT use this class directly. Use CageAllocationDB instead.
 * This is a utility class to provide SQL persistence to CageAllocationDB.
 */
class CageAllocSQL extends SQLiteOpenHelper {

    private static final String DB_NAME = "caleAllocation.sqlite";
    private static final String TABLE_NAME = "cageAllocation";
    private static final int VERSION = 1;

    private static CageAllocSQL instance;
    
    public static CageAllocSQL get(Context ctx) {
        if (instance==null) {
            instance = new CageAllocSQL(ctx.getApplicationContext());
        }
        return instance;
    }
    
    private CageAllocSQL(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String s = 
            "CREATE TABLE " + TABLE_NAME + " (" +
    		"cageAllocationId integer primary key, " +
    		"tm integer," +
    		"cageId integer, " +
            "cageName string," +
    		"json text)";
        db.execSQL(s);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing yet
    }
    
    /** Create one cage allocation by id */
    public CageAllocation readOne(int cageAllocationId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "cageAllocationId=" + cageAllocationId, null, null, null, null);
        CageAllocation ca = c.moveToFirst() ? fromJson(c.getString(c.getColumnIndex("json"))) : null; 
        c.close();
        return ca;
    }
    
    /** Return a list of today's cage allocations */
    public List<CageAllocation> readTodays() {
        //Today (in phones local timezone)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        String today = sdf.format(new Date());
        //Read from DB today's cage allocations
        List<CageAllocation> result = new ArrayList<CageAllocation>();
        SQLiteDatabase db = getReadableDatabase();
        String where = "date(tm, 'unixepoch')='" + today + "'";
        Cursor c = db.query(TABLE_NAME, null, where, null, null, null, "cageName ASC");
        while (c.moveToNext()) {
            CageAllocation ca = fromJson(c.getString(c.getColumnIndex("json")));
            if (ca!=null) {
                result.add(ca);
            }
        }
        c.close();
        return result;
    }

    /** Create new cage allocation */
    public void create(CageAllocation ca) {
        ContentValues cv = new ContentValues();
        cv.put("cageAllocationId", ca.getCageAllocationId());
        cv.put("tm", ca.getTm()/1000); //SQLite has no date/time type
        cv.put("cageId", ca.getCageId());
        cv.put("cageName", ca.getCageName());
        cv.put("json", toJson(ca));
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, cv);
    }
    
    /** Update existing cage allocation */
    public void update(CageAllocation ca) {
        ContentValues cv = new ContentValues();
        cv.put("cageAllocationId", ca.getCageAllocationId());
        cv.put("tm", ca.getTm()/1000); //SQLite has no date/time type
        cv.put("cageId", ca.getCageId());
        cv.put("cageName", ca.getCageName());
        cv.put("json", toJson(ca));
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, cv, "cageAllocationId=" + ca.getCageAllocationId(), null);
    }
    
    /** Remove a cage allocation */
    public void remove(int cageAllocationId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "cageAllocationId=" + cageAllocationId, null);
    }
    
    /** Remove cage allocations that are older then {@code daysOlderThan} days */
    public void removeOld(int daysOlderThan) {
        //How many seconds ago is the delete threshold
        int limit = (int) (System.currentTimeMillis()/1000 - daysOlderThan * 24 * 3600);
        //Run query
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "strftime('%s', tm)<strftime('%s', " + limit + ")", null);
    }
    
    /** Remove all cage allocations */
    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
    
    private String toJson(CageAllocation ca) {
        try {
            JSONObject json = new JSONObject();
            
            json.put("cageAllocationId", ca.getCageAllocationId());
            json.put("tm", ca.getTm());
            json.put("userId", ca.getUserId());
            
            json.put("feedDone", ca.isFeedDone());
            json.put("mortalitiesDone", ca.isMortalitiesDone());
            json.put("netDone", ca.isNetDone());
            json.put("healthDone", ca.isHealthDone());
            json.put("temperatureDone", ca.isTemperatureDone());
            json.put("oxygenDone", ca.isOxygenDone());
            
            json.put("cageId", ca.getCageId());
            json.put("cageName", ca.getCageName());
            json.put("individuals", ca.getIndividuals());
            json.put("cageType", ca.getCageType());
            json.put("meanWeightGrams", ca.getMeanWeightGr());
            
            json.put("feedingId", ca.getFeedingId());
            json.put("feedType", ca.getFeedType());
            json.put("feedId", ca.getFeedId());
            json.put("feedQuantityApproved", ca.getFeedQuantityApproved());
            json.put("feedQuantityUsed", ca.getFeedQuantityUsed());
            
            return json.toString();
        } catch (JSONException e) {
            //Should never happen. Silently drop this CageAllocation
            AquaLog.error("Should never happen. Failed to encode CageAllocation to JSON", e);
            return "";
        }
    }
    
    private CageAllocation fromJson(String jsonStr) {
        try {
            if (jsonStr==null || jsonStr.length()==0) {
                return null;
            }
            JSONObject obj = new JSONObject(jsonStr);
            CageAllocation ca = new CageAllocation();
            
            ca.setCageAllocationId(obj.getInt("cageAllocationId"));
            ca.setTm(obj.getLong("tm"));
            ca.setUserId(obj.getInt("userId"));
            
            ca.setFeedDone(obj.getBoolean("feedDone"));
            ca.setMortalitiesDone(obj.getBoolean("mortalitiesDone"));
            ca.setNetDone(obj.getBoolean("netDone"));
            ca.setHealthDone(obj.getBoolean("healthDone"));
            if (obj.has("temperatureDone")) ca.setTemperatureDone(obj.getBoolean("temperatureDone"));
            if (obj.has("oxygenDone")) ca.setOxygenDone(obj.getBoolean("oxygenDone"));
            
            ca.setCageId(obj.getInt("cageId"));
            ca.setCageName(obj.getString("cageName"));
            ca.setIndividuals(obj.getInt("individuals"));
            ca.setMeanWeightGr((float)obj.getDouble("meanWeightGrams"));
            if (obj.has("cageType")) {
                ca.setCageType(obj.getString("cageType"));
            }
            ca.setFeedingId(obj.getInt("feedingId"));
            ca.setFeedType(obj.getString("feedType"));
            ca.setFeedId(obj.getInt("feedId"));
            ca.setFeedQuantityApproved((float)obj.getDouble("feedQuantityApproved"));
            ca.setFeedQuantityUsed((float)obj.getDouble("feedQuantityUsed"));

            return ca;
        } catch (JSONException e) {
            //Should never happen. We expect correct data because they are generated in this class.
            AquaLog.error("Should never happen. Failed to parse JSON from SQLite", e);
            return null;
        }
    }

    @Override
    public void finalize() throws Throwable {
        getWritableDatabase().close();
        super.finalize();
    }
}
