package uk.co.aquanetix.activities;

import java.util.Locale;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Enter the oxygen measurement.
 */
public class EnterOxygenActivity extends AquanetixAbstractActivity {

    private static final float minValue=0, maxValue=20;
    private int cageAllocationId = -1;
    private AquanetixSharedPreferences prefs;
    private float value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_float_number);
        setHeader();
        setSubheader(R.drawable.oxygen_small, R.string.environment_oxygen);
        ((TextView) findViewById(R.id.textUnitsDisplay)).setText(R.string.enter_oxygen_units);
        
        //Initial value and GUI text
        cageAllocationId = getIntent().hasExtra("cageAllocationId") ? getIntent().getExtras().getInt("cageAllocationId") : -1;
        prefs = new AquanetixSharedPreferences(this);
        value = prefs.getLastOxygen();
        changeValue(0);
        
        //The decrement button listener
        findViewById(R.id.btDecrement).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                changeValue(-0.1f);
            }
        });
        
        //The increment button listener
        findViewById(R.id.btIncrement).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                changeValue(0.1f);
            }
        });
        
        //Apply fonts
        applyCustomFontToAll();
    }
    
    private void changeValue(float diff) {
        //The value
        value += diff;
        value = Math.round(value*10) / 10f; //f is needed to avoid implicit casting to int
        value = Math.min(maxValue, Math.max(minValue, value));
        //The GUI text
        ((TextView) findViewById(R.id.textValueDisplay)).setText(String.format(Locale.US, "%.1f", value));
    }
    
    public void onClickSubmit(View view) {
        // Queue an HTTP request that stores the value
        RequestDescription req = RequestDescription.makeAuthenticated(this, "POST", R.string.url_oxygen);
        req.addParam("event[dissolved_oxygen]", String.format(Locale.US, "%.1f", value));
        if (cageAllocationId>0) {
            req.addParam("event[cage_allocation_id]", Integer.toString(cageAllocationId) );
        }
        RequestQueue.addToQueue(this, req);
        //Persist to provide initial value next time
        prefs.setLastOxygen(value);
        prefs.setLastOxygenTm(System.currentTimeMillis());
        //Update local DB that task has been completed
        if (cageAllocationId>0) {
            CageAllocationDB db = CageAllocationDB.get(this);
            CageAllocation ca = db.getCageAllocation(cageAllocationId);
            ca.setOxygenDone(true);
            db.update(ca);
        }
        //Go to previous screen
        super.onBackPressed(); 
    }
    
}
