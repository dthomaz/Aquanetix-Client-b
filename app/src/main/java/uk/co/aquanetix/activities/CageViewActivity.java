package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.CageSummaryUtils;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * View the details of the cage.
 */
public class CageViewActivity extends AquanetixAbstractActivity {
    
    private CageAllocation ca;
    private int cageAllocationId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_cage);
        setHeader();
        applyCustomFontToAll();
        
        //The left column acts as back button
        View cageSummaryView = findViewById(R.id.cageSummaryColumn);
        cageSummaryView.setBackgroundResource(R.drawable.pressable);
        cageSummaryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CageViewActivity.super.onBackPressed();
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        //Find cage allocation
        cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
        ca = CageAllocationDB.get(this).getCageAllocation(cageAllocationId);
        // Accommodate for the case that the alloc has been deleted or that the device was re-attached to different farm
        if (ca==null) {
            super.onBackPressed();
            return;
        }
        //Update UI (show correct red-blue circles etc) on left side of screen
        CageSummaryUtils.updateView(this, findViewById(R.id.cageSummaryColumn), ca);
        //Update UI (show correct red-blue icons) on right side of screen
        int feedImg = ca.isFeedDone() ? R.drawable.feed_rect_blue : R.drawable.feed_rect_red;
        ((ImageView) findViewById(R.id.imageDidFeed)).setImageResource(feedImg);
        int mortalitiesImg = ca.isMortalitiesDone() ? R.drawable.mortality_rect_blue : R.drawable.mortality_rect_red;
        ((ImageView) findViewById(R.id.imageDidMortalities)).setImageResource(mortalitiesImg);
        int healthImg = ca.isHealthDone() ? R.drawable.health_rect_blue : R.drawable.health_rect_red;
        ((ImageView) findViewById(R.id.imageDidHealth)).setImageResource(healthImg);
        // Special case for nets: if pond/tank turn this button to environmental
        int netImg, netText;
        if ("cage".equals(ca.getCageType())) {
            netImg = ca.isNetDone() ? R.drawable.net_rect_blue : R.drawable.net_rect_red;
            netText = R.string.cage_net;
        } else {
            netImg = ca.isTemperatureDone() && ca.isOxygenDone() ? R.drawable.environmental_blue : R.drawable.environmental_red;
            netText = R.string.cage_environmental;
        }
        ((ImageView) findViewById(R.id.imageDidNet)).setImageResource(netImg);
        ((TextView) findViewById(R.id.textLegendNet)).setText(netText);
    }

    public void onClickedFeed(View view) {
        Intent i = new Intent(this, EnterFeedFishLevelActivity.class);
        i.putExtra("cageAllocationId", cageAllocationId);
        i.putExtra("type", EnterFeedFishLevelActivity.BEFORE);
        startActivity(i);
    }
    
    public void onClickedMortalities(View view) {
        Intent i = new Intent(this, EnterMortalitiesActivity.class);
        i.putExtra("cageAllocationId", cageAllocationId);
        startActivity(i);
    }
    
    public void onClickedNet(View view) {
        // If pond/tank go to environmental
        Class<?> next = "cage".equals(ca.getCageType()) ? EnterNetCheckActivity.class : EnvironmentalContainerMenuActivity.class; 
        Intent i = new Intent(this, next);
        i.putExtra("cageAllocationId", cageAllocationId);
        startActivity(i);
    }
    
    public void onClickedHealth(View view) {
        Intent i = new Intent(this, EnterHealthRiskActivity.class);
        i.putExtra("cageAllocationId", cageAllocationId);
        startActivity(i);
    }
    
}
