package uk.co.aquanetix.activities;

import org.json.JSONException;
import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.network.RequestDescription;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

/**
 * This activity has no UI. It just calls PinActivity twice.
 */
public class PinChangeActivity extends AquanetixAbstractActivity {
    
    private String enterPin1 = "";
    private String enterPin2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Start 1st PIN activity
        Intent i = new Intent(this, PinActivity.class);
        i.putExtra("target", PinActivity.TARGET_ENTER_ONE);
        startActivityForResult(i, 1);
    }
    
    //PIN activity returns
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1 && resultCode==RESULT_OK) {
            //Start 2nd PIN activity
            enterPin1 = data.getStringExtra("pin");
            Intent i = new Intent(this, PinActivity.class);
            i.putExtra("target", PinActivity.TARGET_ENTER_TWO);
            startActivityForResult(i, 2);
            
        } else if (requestCode==2 && resultCode==RESULT_OK) {
            enterPin2 = data.getStringExtra("pin");
            if (enterPin1.equals(enterPin2)) {
                // Initiate PIN change
                changePinTask.execute(enterPin1);
            } else {
                Toast.makeText(this, R.string.pin_change_no_match, Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }
            
        } else {
            super.onBackPressed();
        }
    }
    
    // Changes the PIN at the server and reloads all users (to refresh local copy of PIN)
    private final AsyncTask<String, Void, Boolean> changePinTask = new AsyncTask<String, Void, Boolean>() {
        
        final private Context ctx = PinChangeActivity.this;

        @Override
        protected void onPreExecute() {
            Toast.makeText(ctx, R.string.pin_change_start, Toast.LENGTH_SHORT).show();
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            RequestDescription req = RequestDescription.makeAuthenticated(ctx, "POST", R.string.url_changePin);
            req.addParam("pincode", enterPin1);
            String result = req.sendSync();
            try {
                return result!=null 
                        && new JSONObject(result).getBoolean("ok") 
                        && UserDB.get(ctx).forceDownload();
            } catch (JSONException e) {
                AquaLog.warn("Recovered. Failed to change PIN.", e);
                return false;
            }
        }
        
        @Override
        protected void onPostExecute(Boolean success) {
            int msg = success ? R.string.pin_change_done : R.string.pin_change_failed;
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
            PinChangeActivity.super.onBackPressed();
        };
        
    };
    
}
