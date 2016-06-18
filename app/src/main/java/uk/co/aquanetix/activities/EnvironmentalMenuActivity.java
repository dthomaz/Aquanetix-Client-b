package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * The menu to select between "Temperature" and "Oxygen"
 */
public class EnvironmentalMenuActivity extends AquanetixAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_environmental);
        setHeader();
        applyCustomFontToAll();
    }
    
    public void onClickTemperature(View view) {
        Intent i = new Intent(this, EnterTemperatureActivity.class);
        startActivity(i);
    }
    
    public void onClickOxygen(View view) {
        Intent i = new Intent(this, EnterOxygenActivity.class);
        startActivity(i);
    }
    
}
