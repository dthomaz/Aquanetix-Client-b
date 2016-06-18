package uk.co.aquanetix.activities;

import org.json.JSONObject;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.OnResult;
import uk.co.aquanetix.network.RequestDescription;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The login (username, password) activity.
 */
public class RegistrationActivity extends Activity {

    private ProgressDialog progress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
    }
    
    public void onClickedSubmit(View view) {
        //Show a waiting message
        progress = new ProgressDialog(this);
        progress.setTitle(R.string.login_wait_title);
        progress.setMessage(getResources().getString(R.string.login_wait_message));
        progress.show();
        //Prepare authentication credentials
        AquanetixSharedPreferences p = new AquanetixSharedPreferences(this);
        String uuid = p.getUUID();
        String username = ((EditText) findViewById(R.id.textUsername)).getText().toString();
        String password = ((EditText) findViewById(R.id.textPassword)).getText().toString();
        //Create a URL
        String companyId = "" + p.getCompanyId();
        String url = getString(R.string.url_server) + getString(R.string.url_signin);
        url = url.replaceAll(":company_id", companyId);
        //Create a request object
        RequestDescription req = new RequestDescription(url, "POST");
        req.addParam("login", username);
        req.addParam("password", password);
        req.addParam("uuid", uuid);
        //Send request to server
        req.sendAsync(new OnResult<String>() {
            @Override
            public void getResult(String result) {
                try {
                    //Clear dialog
                    progress.dismiss();
                    //Parse response
                    if (result==null) {
                        showFailMsg();
                        return; // Probably network down
                    }
                    JSONObject json = new JSONObject(result);
                    //If wrong password
                    if (!json.getBoolean("ok")) {
                        Toast.makeText(RegistrationActivity.this, R.string.login_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Persist data 
                    AquanetixSharedPreferences p = new AquanetixSharedPreferences(RegistrationActivity.this);
                    p.setDeviceSecret(json.getString("secret"));
                    p.setCompanyId(json.getInt("company_id"));
                    //Forward to next screen (select user)
                    Intent i = new Intent(RegistrationActivity.this, SplashActivity.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    showFailMsg();
                    AquaLog.warn("Recovered. Wrong signin response from server:" +  result);
                }
            }
            
            private void showFailMsg() {
                Toast.makeText(RegistrationActivity.this, R.string.login_error_server, Toast.LENGTH_SHORT).show();
            }
        });
    }
}