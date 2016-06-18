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
 * Enter the net check result for a cage.
 */
public class EnterNetCheckActivity extends AquanetixAbstractActivity {
    
    private String cloggValue, lossValue;
    private int cageAllocationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_net_check);
        setHeader();
        setSubheader(R.drawable.empty_subheader, R.string.net_health_subheader);
        applyCustomFontToAll();
        
        //Find specific cage
        cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
    }
    
    public void onClickedClogg(View view) {
        //Change background (unselect all)
        findViewById(R.id.textNetClogg1).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.textNetClogg2).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.textNetClogg3).setBackgroundResource(R.drawable.border_right);
        //Read entered value
        int id = view.getId();
        if (id==R.id.textNetClogg1) {
            cloggValue = "non";
            findViewById(R.id.textNetClogg1).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.textNetClogg2) {
            cloggValue = "moderate";
            findViewById(R.id.textNetClogg2).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.textNetClogg3) {
            cloggValue = "high";
            findViewById(R.id.textNetClogg3).setBackgroundColor(Color.rgb(100, 100, 100));
        }
        //Change OK button
        if (lossValue!=null) {
            ((ImageView) findViewById(R.id.btSubmit)).setImageResource(R.drawable.pressable_ok);
        }
    }
    
    public void onClickedLoss(View view) {
        //Change background (unselect all)
        findViewById(R.id.textNetLoss1).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.textNetLoss2).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.textNetLoss3).setBackgroundResource(R.drawable.border_right);
        //Read entered value
        int id = view.getId();
        if (id==R.id.textNetLoss1) {
            lossValue = "non";
            findViewById(R.id.textNetLoss1).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.textNetLoss2) {
            lossValue = "moderate";
            findViewById(R.id.textNetLoss2).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.textNetLoss3) {
            lossValue = "high";
            findViewById(R.id.textNetLoss3).setBackgroundColor(Color.rgb(100, 100, 100));
        }
        //Change OK button
        if (cloggValue!=null) {
            ((ImageView) findViewById(R.id.btSubmit)).setImageResource(R.drawable.pressable_ok);
        }
    }

    public void onClickSubmit(View view) {
        if (cloggValue==null || lossValue==null) {
            return;
        }
        // Queue an HTTP request that stores the value        
        RequestDescription req = RequestDescription.makeAuthenticated(this, "POST", R.string.url_net,
                ":allocation_id", cageAllocationId);
        req.addParam("event[clog_risk]", cloggValue);
        req.addParam("event[loss_risk]", lossValue);
        RequestQueue.addToQueue(this, req);
        //Update local DB that task has been completed
        CageAllocationDB db = CageAllocationDB.get(this);
        CageAllocation ca = db.getCageAllocation(cageAllocationId);
        ca.setNetDone(true);
        db.update(ca);
        
        super.onBackPressed(); 
    }
    
}
