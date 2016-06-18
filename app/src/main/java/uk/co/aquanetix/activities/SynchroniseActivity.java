package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.network.RequestQueue;
import uk.co.aquanetix.network.RequestQueueSQL;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Allows the user to synchronise the data in RequestQueue with the server.
 */
public class SynchroniseActivity extends AquanetixAbstractActivity {

    private RequestQueueSQL sql;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronise);
        setHeader();
        
        findViewById(R.id.btSyncStatus).setVisibility(View.INVISIBLE);
        
        sql = RequestQueueSQL.get(this);
        String s = sql.size() + " " + getString(R.string.sync_pending);
        ((TextView) findViewById(R.id.textCountPending)).setText(s);
    }
    
    public void onClickedSynchroniseNow(View v) {
        //Check for Internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        @SuppressWarnings("deprecation")
        boolean hasInternet = cm.getBackgroundDataSetting() && ni!=null && ni.isConnected();
        //Start synchronise process or fail
        if (hasInternet) {
            Intent i = new Intent(this, RequestQueue.class);
            startService(i);
            Toast.makeText(this, R.string.sync_started, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.sync_no_internet, Toast.LENGTH_SHORT).show();
        }
    }
    
    public void onClickedMore(View v) {
        Toast.makeText(this, R.string.sync_logged, Toast.LENGTH_SHORT).show();
        sql.logAll();
    }
    
}
