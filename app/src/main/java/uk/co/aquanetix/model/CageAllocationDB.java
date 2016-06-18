package uk.co.aquanetix.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import android.content.Context;
import android.util.SparseArray;

/**
 * Provides access to CageAllocations. It stores in memory today's
 * allocations. It delegates persistence to SQLite.
 */
public class CageAllocationDB {

    private static CageAllocationDB instance;
    
    public static CageAllocationDB get(Context ctx) {
        if (instance==null) {
            instance = new CageAllocationDB(ctx.getApplicationContext());
        }
        return instance;
    }
    
    private final Context ctx;
    private final CageAllocSQL sql;
    
    private CageAllocationDB(Context ctx) {
        this.ctx = ctx;
        this.sql = CageAllocSQL.get(ctx);
    }

    /** Get active (today's) cage allocations */
    public Collection<CageAllocation> getCageAllocations() {
        return sql.readTodays();
    }
    
    /** Get active (today's) cage allocations, as a Map */
    public SparseArray<CageAllocation> getCageAllocationsMap() {
        SparseArray<CageAllocation> result = new SparseArray<CageAllocation>();
        Collection<CageAllocation> all = getCageAllocations();
        for (CageAllocation ca:all) {
            result.put(ca.getCageAllocationId(), ca);
        }
        return result;
    }
    
    /** Get specific cage allocation, only if it is from today. */
    public CageAllocation getCageAllocation(int cageAllocationId) {
        return sql.readOne(cageAllocationId);
    }
    
    /** Persist in DB the new object. */
    public void update(CageAllocation ca) {
        sql.update(ca);
    }
    
    /**
     * Forces a cage-list update and return true if successful.
     */
    public boolean forceDownload() {
        //Make request to get JSON
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "GET", R.string.url_cageAlloc);
        long lastUpdate = new AquanetixSharedPreferences(ctx).getLastServerUpdate();
        if (lastUpdate>0) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            req.addHeader("If-Modified-Since", sdf.format(new Date(lastUpdate)));
        }
        String jsonStr = req.sendSync();
        //Parse JSON
        if (jsonStr==null) {
            return false; // Probably network error
        }
        List<CageAllocation> cages = parseServerJson(jsonStr);
        //If successful, persist result (for off-line usage)
        if (cages!=null) {
            //We do the fancy merging of data.
            //Cage allocations are altered both locally by the user on this
            //phone and remotely by other users on other phones. So we must
            //merge the two changes manually.
            
            for (CageAllocation caServer:cages) {
                CageAllocation caLocal = sql.readOne(caServer.getCageAllocationId());
                if (caLocal==null) {
                    //New cage allocation. Add it
                    sql.create(caServer);
                } else {
                    //We have new data about old cage allocation
                    caLocal.setFeedDone(caLocal.isFeedDone() || caServer.isFeedDone());
                    caLocal.setMortalitiesDone(caLocal.isMortalitiesDone() || caServer.isMortalitiesDone());
                    caLocal.setNetDone(caLocal.isNetDone() || caServer.isNetDone());
                    caLocal.setHealthDone(caLocal.isHealthDone() || caServer.isHealthDone());
                    caLocal.setTemperatureDone(caLocal.isTemperatureDone() || caServer.isTemperatureDone());
                    caLocal.setOxygenDone(caLocal.isOxygenDone() || caServer.isOxygenDone());
                    
                    caLocal.setFeedingId(caServer.getFeedingId());
                    caLocal.setFeedQuantityApproved(caServer.getFeedQuantityApproved());
                    caLocal.setFeedType(caServer.getFeedType());
                    caLocal.setFeedId(caServer.getFeedId());
                    caLocal.setFeedQuantityUsed(caServer.getFeedQuantityUsed());
                    caLocal.setMeanWeightGr(caServer.getMeanWeightGr());
                    caLocal.setUnits(caServer.getUnits());
                    
                    sql.update(caLocal);
                }
            }
        }
        return cages!=null;
    }
    
    /**
     * Deletes old cage allocations.
     */
    public void removeOldData() {
        sql.removeOld(7);
    }
    
    /**
     * Deletes all cage allocations.
     */
    public void clearAllData() {
        sql.clearAll();
    }
    
    /**
     * Parses a JSON string, as returned by the server. On error, returns null.
     */
    private List<CageAllocation> parseServerJson(String jsonStr) {
        SimpleDateFormat sdfIn = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        
        List<CageAllocation> result = new ArrayList<CageAllocation>();
        try {
            if (jsonStr==null || jsonStr.length()==0) {
                return null;
            }
            JSONObject root = new JSONObject(jsonStr);
            //If error
            if (!root.getBoolean("ok")) {
                return null;
            }
            
            JSONArray dataJson = root.getJSONArray("data");
            for (int i=0; i<dataJson.length(); i++) {
                JSONObject caJson = dataJson.getJSONObject(i);
                CageAllocation ca = new CageAllocation();
                
                ca.setCageAllocationId(caJson.getInt("id"));
                Date dt = sdfIn.parse(caJson.getString("date"));
                ca.setTm(dt.getTime());
                ca.setUserId(caJson.getInt("user_id"));
                
                ca.setFeedDone(caJson.getBoolean("did_feed"));
                ca.setMortalitiesDone(caJson.getBoolean("did_species_loss_check"));
                ca.setNetDone(caJson.getBoolean("did_net_assesment"));
                ca.setHealthDone(caJson.getBoolean("did_species_batch_check"));
                ca.setTemperatureDone(caJson.getBoolean("did_temperature_measurement"));
                ca.setOxygenDone(caJson.getBoolean("did_oxygen_measurement"));
                ca.setUnits(caJson.getString("units"));
                
                JSONObject cageJson = caJson.getJSONObject("cage");
                ca.setCageId(cageJson.getInt("id"));
                ca.setCageName(cageJson.getString("name"));
                ca.setIndividuals(cageJson.getInt("current_number_of_individuals"));
                ca.setMeanWeightGr((float)cageJson.getDouble("current_mean_weight_gr"));
                ca.setCageType(cageJson.getString("type"));
                
                JSONArray feedingsJson = caJson.getJSONArray("feedings");
                if (feedingsJson.length()>0) {
                    JSONObject feedJson = feedingsJson.getJSONObject(0);
                    ca.setFeedingId(feedJson.getInt("id"));
                    ca.setFeedType(feedJson.getString("feed_type"));
                    ca.setFeedId(feedJson.getInt("feed_id"));
                    ca.setFeedQuantityApproved((float)feedJson.getDouble("quantity_approved"));
                    ca.setFeedQuantityUsed((float)feedJson.getDouble("quantity_fed"));
                }
                result.add(ca);
            }
            return result;
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Recovered, data loss. Failed to parse server JSON", e);
            return null;
        } catch (ParseException e) {
            //Should never happen. We expect correct data from server.
            AquaLog.error("Recovered, data loss. Failed to parse date from server JSON", e);
            return null;
        }
    }
    
}
