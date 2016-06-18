package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Enter the health risk for a cage.
 */
public class EnterHealthRiskActivity extends AquanetixAbstractActivity {
    
    private String riskLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_health_risk);
        setHeader();
        setSubheader(R.drawable.empty_subheader, R.string.health_subheader);
        applyCustomFontToAll();
    }
    
    /** Called by android UI thread */
    public void onChangedRisk(View view) {
        //Change background (unselect all)
        findViewById(R.id.imageHealthRisk1).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.imageHealthRisk2).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.imageHealthRisk3).setBackgroundResource(R.drawable.border_right);
        //Read entered value
        int id = view.getId();
        if (id==R.id.imageHealthRisk1) {
            riskLevel = "non";
            findViewById(R.id.imageHealthRisk1).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.imageHealthRisk2) {
            riskLevel = "moderate";
            findViewById(R.id.imageHealthRisk2).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.imageHealthRisk3) {
            riskLevel = "high";
            findViewById(R.id.imageHealthRisk3).setBackgroundColor(Color.rgb(100, 100, 100));
        }
        //Change OK button
        ((ImageView) findViewById(R.id.btSubmit)).setImageResource(R.drawable.pressable_ok);
    }

    public void onClickSubmit(View view) {
        if (riskLevel==null) {
            return;
        }
        // Queue an HTTP request that stores the value
        int cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
        RequestDescription req = RequestDescription.makeAuthenticated(this, "POST", R.string.url_health,
                ":allocation_id", cageAllocationId);
        req.addParam("event[health_risk]", riskLevel);
        RequestQueue.addToQueue(this, req);
        //Update local DB that task has been completed
        CageAllocationDB db = CageAllocationDB.get(this);
        CageAllocation ca = db.getCageAllocation(cageAllocationId);
        ca.setHealthDone(true);
        db.update(ca);
        
        super.onBackPressed(); 
    }
    
}
