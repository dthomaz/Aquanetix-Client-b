package uk.co.aquanetix.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;

/**
 * Enter the temperature measurement.
 */
public class EnterTemperatureActivity extends AquanetixAbstractActivity {
    
    private float minValue= 0, maxValue= 40;
    private int cageAllocationId = -1;
    private AquanetixSharedPreferences prefs;
    private float value;
    private String unit_system;

    public int getTemperatureLabel(String system) {
        int label = system.equals("metric") ? R.string.enter_temperature_units : R.string.enter_temperature_units_F;
        return label;
    }

    public float outputTemperature(String system, float value) {
        return (system.equals("metric") ? value : (float) (value * 9.0 / 5.0 + 32.0));
    }

   public float inputTemperature(String system, float value) {
        return (system.equals("metric") ? value : (float) ((value - 32) / 9.0 * 5.0));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_float_number);
        setHeader();
        setSubheader(R.drawable.temperature_small, R.string.environment_temperature);


        //Initial value and GUI text
        cageAllocationId = getIntent().hasExtra("cageAllocationId") ? getIntent().getExtras().getInt("cageAllocationId") : -1;
        prefs = new AquanetixSharedPreferences(this);
        unit_system = UserDB.get(this).getUnitSystem();
        minValue= unit_system.equals("metric") ? 0 : 32;
        maxValue= unit_system.equals("metric") ? 40 : 105;
        value = outputTemperature(unit_system, prefs.getLastTemperature());
        ((TextView) findViewById(R.id.textUnitsDisplay)).setText(getTemperatureLabel(unit_system));
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
        RequestDescription req = RequestDescription.makeAuthenticated(this, "POST", R.string.url_temperature);
        req.addParam("event[temperature]", String.format(Locale.US, "%.1f", inputTemperature(unit_system , value)));
        if (cageAllocationId>0) {
            req.addParam("event[cage_allocation_id]", Integer.toString(cageAllocationId) );
        }
        RequestQueue.addToQueue(this, req);
        //Persist to provide initial value next time
        prefs.setLastTemperature(inputTemperature(unit_system , value));
        prefs.setLastTemperatureTm(System.currentTimeMillis());
        //Update local DB that task has been completed
        if (cageAllocationId>0) {
            CageAllocationDB db = CageAllocationDB.get(this);
            CageAllocation ca = db.getCageAllocation(cageAllocationId);
            ca.setTemperatureDone(true);
            db.update(ca);
        }
        //Go to previous screen
        super.onBackPressed(); 
    }
    
}
