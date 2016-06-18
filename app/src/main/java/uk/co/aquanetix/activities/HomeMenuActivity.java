package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * The very first menu of the app. Select between "Environmental"
 * and "Cages" categories.
 */
public class HomeMenuActivity extends AquanetixAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_home);
        setHeader();
        applyCustomFontToAll();
    }
    
    public void onClickedEnvironmental(View view) {
        Intent i = new Intent(this, EnvironmentalMenuActivity.class);
        startActivity(i);
    }
    
    public void onClickedCages(View view) {
        Intent i = new Intent(this, CageSelectActivity.class);
        startActivity(i);
    }
    
}