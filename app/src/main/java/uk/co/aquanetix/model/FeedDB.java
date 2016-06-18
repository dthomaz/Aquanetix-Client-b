package uk.co.aquanetix.model;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.co.aquanetix.R;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import android.content.Context;

/**
 * A singleton that provides access to the list of registered users.
 * The list is stored in SharedPreferences in JSON format and needs
 * to be updated at least every 24 hours.
 */
public class FeedDB {

    private static FeedDB instance;

    public static FeedDB get(Context ctx) {
        if (instance==null) {
            instance = new FeedDB(ctx.getApplicationContext());
        }
        return instance;
    }

    private final Context ctx;
    private final AquanetixSharedPreferences prefs;
    private Map<Integer, Feed> feeds = new LinkedHashMap<Integer, Feed>();

    private FeedDB(Context ctx) {
        this.ctx = ctx;
        this.prefs = new AquanetixSharedPreferences(ctx);
        //Read the previous feed list from disk (it could fail, so the list will be empty)
        List<Feed> result = parseJson(prefs.getFeedsJson());
        if (result!=null) {
            setFeeds(result);
        }
    }



    public Collection<Feed> getFeeds() {
        return feeds.values();
    }

    private void setFeeds(List<Feed> feeds) {
        this.feeds = new LinkedHashMap<Integer, Feed>();
        for (Feed f:feeds) {
            this.feeds.put(f.getFeedId(), f);
        }
    }

    public double getFeedMinWeight(int feedId) {
        Feed f = feeds.get(feedId);
        return f==null ? 0.0 : f.getMinWeight();
    }

    public double getFeedMaxWeight(int feedId) {
        Feed f = feeds.get(feedId);
        return f==null ? 0.0 : f.getMaxWeight();
    }

    public String getFeedname(int feedId) {
        Feed f = feeds.get(feedId);
        return f==null ? "-" : f.getFeedname();
    }

    public double getFeedWeightPerUnit(int feedId) {
        Feed f = feeds.get(feedId);
        return f==null ? 1.0 : f.getWeightPerUnit();
    }

    /**
     * Forces a feed-list update and return true if successful.
     */
    public boolean forceDownload() {
        //Make request to get JSON
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "GET", R.string.url_feeds);
        String jsonStr = req.sendSync();
        //Parse JSON
        List<Feed> result = parseJson(jsonStr);
        if (result!=null) {
            //If successful, persist result (for off-line usage)
            setFeeds(result);
            prefs.setFeedsJson(jsonStr);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Parses a JSON string, as returned by the server.
     * On error, returns null.
     */
    private static List<Feed> parseJson(String jsonStr) {
        try {
            if (jsonStr==null || jsonStr.length()==0) {
                return null;
            }
            JSONObject json = new JSONObject(jsonStr);
            //If error
            if (!json.getBoolean("ok")) {
                return null;
            }
            //Make list of feeds
            List<Feed> result = new ArrayList<Feed>();
            JSONArray jsonAr = json.getJSONArray("data");
            for (int i=0; i<jsonAr.length(); i++) {
                JSONObject obj = jsonAr.getJSONObject(i);
                Integer id = obj.has("feedId") ? obj.getInt("feedId") : 0;
                String name = obj.has("feedname") ? obj.getString("feedname") : "no feed";
                Float min_w = obj.has("min_weight") ? (float) obj.getDouble("min_weight") : (float) 0.0;
                Float max_w = obj.has("max_weight") ? (float) obj.getDouble("max_weight") : (float) 10000.0;
                Float wpu = obj.has("weight_per_unit") ? (float) obj.getDouble("weight_per_unit") : (float) 1.0;
                Feed f = new Feed(id, name, min_w, max_w, wpu);
                result.add(f);
            }
            return result;
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            throw new RuntimeException(e);
        }
    }

}
