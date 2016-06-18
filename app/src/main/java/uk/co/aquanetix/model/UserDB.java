package uk.co.aquanetix.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import android.content.Context;

/**
 * A singleton that provides access to the list of registered users.
 * The list is stored in SharedPreferences in JSON format and needs
 * to be updated at least every 24 hours.
 */
public class UserDB {
    
    private static UserDB instance;
    
    public static UserDB get(Context ctx) {
        if (instance==null) {
            instance = new UserDB(ctx.getApplicationContext());
        }
        return instance;
    }
    
    private final Context ctx;
    private final AquanetixSharedPreferences prefs;
    private Map<Integer, User> users = new LinkedHashMap<Integer, User>();
    
    private UserDB(Context ctx) {
        this.ctx = ctx;
        this.prefs = new AquanetixSharedPreferences(ctx);
        //Read the previous user list from disk (it could fail, so the list will be empty)
        List<User> result = parseJson(prefs.getUsersJson());
        if (result!=null) {
            setUsers(result);
        }
    }
    
    public Collection<User> getUsers() {
        return users.values();
    }
    
    private void setUsers(List<User> users) {
        this.users = new LinkedHashMap<Integer, User>();
        for (User u:users) {
            this.users.put(u.getUserId(), u);
        }
    }

    public String getUsername(int userId) {
        User u = users.get(userId);
        return u==null ? "-" : u.getUsername();
    }

    public String getUnitSystem(int userId) {
        User u = users.get(userId);
        return u==null ? "-" : u.getUnitSystem();
    }

    public String getUnitSystem() {
        return users.entrySet().iterator().next().getValue().getUnitSystem();
    }

    public String getRole(int userId) {
        User u = users.get(userId);
        return u==null ? "-" : u.getRole();
    }

    public boolean isBagsFlag(int userId) {
        User u = users.get(userId);
        return u==null ? false : u.isBagsFlag();
    }

    /**
     * Forces a user-list update and return true if successful.
     */
    public boolean forceDownload() {
        //Make request to get JSON
        RequestDescription req = RequestDescription.makeAuthenticated(ctx, "GET", R.string.url_users);
        String jsonStr = req.sendSync();
        //Parse JSON
        List<User> result = parseJson(jsonStr);
        if (result!=null) {
            //If successful, persist result (for off-line usage)
            setUsers(result);
            prefs.setUsersJson(jsonStr);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Parses a JSON string, as returned by the server.
     * On error, returns null.
     */
    private static List<User> parseJson(String jsonStr) {
        try {
            if (jsonStr==null || jsonStr.length()==0) {
                return null;
            }
            JSONObject json = new JSONObject(jsonStr);
            //If error
            if (!json.getBoolean("ok")) {
                return null;
            }
            //Make list of users
            List<User> result = new ArrayList<User>();
            JSONArray jsonAr = json.getJSONArray("data");
            for (int i=0; i<jsonAr.length(); i++) {
                JSONObject obj = jsonAr.getJSONObject(i);
                String pincode = obj.has("pincode") ? obj.getString("pincode") : "0000";
                boolean hide = obj.has("hideFromClient") && obj.getBoolean("hideFromClient");
                boolean bags = obj.has("bagsFlag") && obj.getBoolean("bagsFlag");
                String user_role = obj.has("role") ? obj.getString("role") : "feeder";
                String user_unit_system = obj.has("unit_system") ? obj.getString("unit_system") : "metric";
                User u = new User(obj.getInt("id"), obj.getString("full_name"), pincode, hide, bags, user_role, user_unit_system);
                result.add(u);
            }
            return result;
        } catch (JSONException e) {
            //Should never happen. We expect correct data from server.
            throw new RuntimeException(e);
        }
    }

}
