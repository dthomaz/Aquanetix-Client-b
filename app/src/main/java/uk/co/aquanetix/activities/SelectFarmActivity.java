package uk.co.aquanetix.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.network.OnResult;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.Sync;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Selects the farm that the device is attached to.
 */
public class SelectFarmActivity extends AquanetixAbstractActivity {
    
    private ProgressDialog progress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_farm);
        setHeader();
        
        // Prepare a loading dialog
        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.splash_loading));
        
        // Async load and show farms
        final RadioGroup rg = (RadioGroup) findViewById(R.id.select_farms_radio_group);
        RequestDescription req = RequestDescription.makeAuthenticated(this, "GET", R.string.url_farmList);
        req.sendAsync(new OnResult<String>() {
            
            @Override
            public void getResult(String jsonStr) {
                Farms farms = new Farms(jsonStr);
                if (farms.all.size()==0) {
                    Toast.makeText(SelectFarmActivity.this, R.string.selectFarm_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selected = null;
                for (String[] farm : farms.all) {
                    RadioButton r = new RadioButton(SelectFarmActivity.this);
                    r.setTag(farm[0]); // farm_id
                    r.setText(farm[1]);
                    r.setTextColor(Color.WHITE);
                    if (farm[0].equals(farms.activeFarmId)) {
                        selected = r;
                    }
                    rg.addView(r);
                }
                if (selected!=null) { // Currently active farm
                    rg.check(selected.getId());
                }
                rg.setOnCheckedChangeListener(listener);
            }
        });
        
        //Apply fonts
        applyCustomFontToAll();
    }
    
    private RadioGroup.OnCheckedChangeListener listener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            final RadioButton rb=(RadioButton)findViewById(checkedId);
            if (rb!=null) {
                changeFarm.execute((String) rb.getTag());
            }
        }
    };
    
    // Changes the PIN at the server and reloads all users (to refresh local copy of PIN)
    private final AsyncTask<String, Void, Boolean> changeFarm = new AsyncTask<String, Void, Boolean>() {
        
        final private Context ctx = SelectFarmActivity.this;

        @Override
        protected void onPreExecute() {
            progress.show();
//            Toast.makeText(ctx, R.string.selectFarm_wait, Toast.LENGTH_SHORT).show();
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            RequestDescription req = RequestDescription.makeAuthenticated(ctx, "POST", R.string.url_deviceFarm);
            req.addParam("farm_id", params[0]);
            String result = req.sendSync();
            try {
                boolean done = result!=null && new JSONObject(result).getBoolean("ok");
                if (!done) {
                    return false;
                }
                // Force user/cage reload
                return new Sync(ctx).update(true) == Sync.OK;
            } catch (Exception e) {
                AquaLog.warn("Recovered. Failed to attach device to farm.", e);
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            progress.dismiss();
            int msg = success ? R.string.selectFarm_ok : R.string.selectFarm_failed;
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            CageSelectActivity.needsRefresh = true;
            SelectFarmActivity.super.onBackPressed();
        };
    };
    
    private class Farms {
        
        private String activeFarmId = null;
        private List<String[]> all = new ArrayList<String[]>(); // [farmId, farmName]
        
        private Farms(String jsonStr) {
            try {
                if (jsonStr==null || jsonStr.length()==0) {
                    return;
                }
                JSONObject json = new JSONObject(jsonStr);
                // If error
                if (!json.getBoolean("ok")) {
                    return;
                }
                //Make list of farms
                JSONObject data = json.getJSONObject("data");
                activeFarmId = data.getString("farm_id");
                JSONArray jsonFarms = data.getJSONArray("farms");
                for (int i=0; i<jsonFarms.length(); i++) {
                    JSONObject jsonFarm = jsonFarms.getJSONObject(i);
                    all.add(new String[] { 
                            jsonFarm.getString("id"), 
                            jsonFarm.getString("name")
                    });
                }
                return;
            } catch (JSONException e) {
                // Should never happen. We expect correct data from server.
                throw new RuntimeException(e);
            }
        }
    }
    
}
