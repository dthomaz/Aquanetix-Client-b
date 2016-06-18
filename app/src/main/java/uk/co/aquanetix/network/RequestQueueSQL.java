package uk.co.aquanetix.network;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Do NOT use this class directly. Use {@link RequestQueue} instead.
 * This is a utility class to provide SQL persistence to RequestQueue.
 */
public class RequestQueueSQL extends SQLiteOpenHelper {

    private static final String TAG = RequestQueueSQL.class.getSimpleName();
    private static final String DB_NAME = "requestQueue.sqlite";
    private static final String TABLE_NAME = "requestQueue";
    private static final int VERSION = 1;
    
    private static RequestQueueSQL instance;
    
    public static RequestQueueSQL get(Context ctx) {
        if (instance==null) {
            instance = new RequestQueueSQL(ctx.getApplicationContext());
        }
        return instance;
    }
    
    private RequestQueueSQL(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String s = 
            "CREATE TABLE " + TABLE_NAME + " (" +
    		"id integer primary key autoincrement, " +
    		"tm integer," +
    		"needsGPS integer," +
    		"json text)";
        db.execSQL(s);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Nothing yet
    }

    /** Add request to queue */
    public long add(RequestDescription req, boolean needsGps) {
        ContentValues cv = new ContentValues();
        cv.put("tm", System.currentTimeMillis());
        cv.put("needsGPS", needsGps ? 1 : 0);
        cv.put("json", req.toJson());
        SQLiteDatabase db = getWritableDatabase();
        long newId = db.insert(TABLE_NAME, null, cv);
        return newId;
    }
    
    /**
     * Returns the id of the next request that is ready to be sent to server (has GPS
     * location or it is at least 2 mins old)
     */
    public long nextReady() {
        SQLiteDatabase db = getReadableDatabase();
        long after = System.currentTimeMillis() - 2*60*1000; //2 mins
        String where = "needsGPS=0 OR tm<"+after;
        Cursor c = db.query(TABLE_NAME, null, where, null, null, null, "id ASC", "1");
        long id = c.moveToFirst() ? c.getLong(c.getColumnIndex("id")) : -1;
        return id;
    }
    
    /** Marks a request as not-needing GPS location */
    public void gpsDone(long id, RequestDescription req) {
        ContentValues cv = new ContentValues();
        cv.put("needsGPS", 0);
        cv.put("json", req.toJson());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, cv, "id=" + id, null);
    }
    
    /** Marks a request as not-ready to be sent to server yet */
    public void postpone(long id) {
        ContentValues cv = new ContentValues();
        cv.put("needsGPS", 1);
        cv.put("tm", System.currentTimeMillis());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, cv, "id=" + id, null);
    }

    /** Get a specific request */
    public RequestDescription readOne(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, "id=" + id, null, null, null, null);
        String jsonStr = c.moveToNext() ? c.getString(c.getColumnIndex("json")) : null;
        return jsonStr==null ? null : RequestDescription.fromJson(jsonStr);
    }
    
    /** Write all pending requests at the Android log */
    public void logAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, "id ASC");
        while (c.moveToNext()) {
            StringBuffer sb = new StringBuffer();
            sb.append(c.getInt(c.getColumnIndex("id")));
            sb.append(" ");
            sb.append(c.getInt(c.getColumnIndex("needsGPS")));
            sb.append(" ");
            long age = (System.currentTimeMillis() - c.getLong(c.getColumnIndex("tm"))) / 1000;
            sb.append(age);
            sb.append(" ");
            sb.append(c.getString(c.getColumnIndex("json")));
            Log.e(TAG, sb.toString());
        }
    }
    
    public int size() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        c.moveToFirst();
        int count = c.getInt(0);
        return count;
    }
    
    /** Remove request */
    public void remove(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "id=" + id, null);
    }
    
    @Override
    public void finalize() throws Throwable {
        getWritableDatabase().close();
        super.finalize();
    }
}
