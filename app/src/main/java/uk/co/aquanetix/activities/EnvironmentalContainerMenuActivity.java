package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * The menu to select between "Temperature" and "Oxygen" when recording per container (not per farm)
 */
public class EnvironmentalContainerMenuActivity extends AquanetixAbstractActivity {

    private int cageAllocationId = -1;
    private CageAllocation ca;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_environmental);
        setHeader();
        applyCustomFontToAll();
        //Find cageAllocation
        cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ca = CageAllocationDB.get(this).getCageAllocation(cageAllocationId);
        if (ca==null) { // Was deleted
            super.onBackPressed();
            return;
        }
        // Set colour of icons
        int tImg = ca.isTemperatureDone() ? R.drawable.temperature_blue : R.drawable.temperature_red;
        ((ImageView) findViewById(R.id.btEnvTemperature)).setImageResource(tImg);
        int oImg = ca.isOxygenDone() ? R.drawable.oxygen_blue : R.drawable.oxygen_red;
        ((ImageView) findViewById(R.id.btEnvOxygen)).setImageResource(oImg);
    }
    
    public void onClickTemperature(View view) {
        Intent i = new Intent(this, EnterTemperatureActivity.class);
        i.putExtra("cageAllocationId", cageAllocationId);
        startActivity(i);
    }
    
    public void onClickOxygen(View view) {
        Intent i = new Intent(this, EnterOxygenActivity.class);
        i.putExtra("cageAllocationId", cageAllocationId);
        startActivity(i);
    }
}
