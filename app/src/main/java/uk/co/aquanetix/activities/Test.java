package uk.co.aquanetix.activities;

import java.util.Collection;
import java.util.Date;

import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

/**
 * Run-once tests. Change the manifest to launch this activity, in order to
 * easily test a code snippet.
 * Perfectly safe to delete this class.
 */
public class Test extends Activity {

    String TAG = "dsadsa";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView txt = new TextView(this);
        txt.setText("OK");
        setContentView(txt);
        Log.e(TAG, "CREATE -------------------------------------------");
    }
    
    @SuppressWarnings("unused")
    private void testCageAllocationDB() {
        CageAllocationDB db = CageAllocationDB.get(this);
        db.forceDownload();
        
        Collection<CageAllocation> all =db.getCageAllocations();
        Log.e("dsad", "a:" + all.size());
        
        for (CageAllocation ca:all) {
            
            Log.e("dsad", ca.getCageAllocationId() + " " + new Date(ca.getTm()) + " " + ca.getTm());
        }
    }        
        
    @SuppressWarnings("unused")
    private void testScreen() {
        //--------------- DENSITY -------------------
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.e("dsada", "Density: " + metrics.density);
        Log.e("dsada", "Density DPI: " + metrics.densityDpi);
        Log.e("dsada", "Scaled density: " + metrics.scaledDensity);
        Log.e("dsada", "width: " + metrics.widthPixels);
        Log.e("dsada", "height: " + metrics.heightPixels);
        Log.e("dsada", "xdi: " + metrics.xdpi);
        Log.e("dsada", "ydi: " + metrics.ydpi);
        Log.e("ds", Build.BRAND + " " + Build.DEVICE + " " + Build.MODEL);
        Log.e("ds", Build.DISPLAY + " " + Build.PRODUCT + " " + Build.TYPE);
    }
}
