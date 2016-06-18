package uk.co.aquanetix.activities;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Enter fish level in a cage, before and after feeding.
 * This activity generates TWO pages on the UI.
 */
public class EnterFeedFishLevelActivity extends AquanetixAbstractActivity {
    
    public static final int BEFORE = 1;
    public static final int AFTER = 2;
    
    private CageAllocation ca;
    private int type;
    private String fishLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_fish_level);
        setHeader();
        
        // Find cage allocation
        int cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
        ca = CageAllocationDB.get(this).getCageAllocation(cageAllocationId);
        
        //Distinguish between "before" and "after" feeding
        int textSubheader;
        type = getIntent().getExtras().getInt("type");
        if (type==BEFORE) {
            ((TextView) findViewById(R.id.textSubheader)).setText("BEFORE");
            textSubheader = R.string.feed_before;
        } else if (type==AFTER) {
            ((TextView) findViewById(R.id.textSubheader)).setText("AFTER");
            textSubheader = R.string.feed_after;
        } else {
            throw new RuntimeException("Unknown type. Check that the intent has correct extras");
        }
        
        // Being paranoid with Bluefarm lost data
        AquaLog.info("Enter, FishLevel for " + ca.getCageAllocationId() + " " + ca.getFeedingId() + " " + type);
        
        setSubheader(R.drawable.empty_subheader, textSubheader);
        applyCustomFontToAll();
    }
    
    /** Called by android UI thread */
    public void onChangedFishLevel(View view) {
        //Change background (unselect all)
        findViewById(R.id.imageFishLevel1).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.imageFishLevel2).setBackgroundResource(R.drawable.border_right);
        findViewById(R.id.imageFishLevel3).setBackgroundResource(R.drawable.border_right);
        //Read entered value
        int id = view.getId();
        if (id==R.id.imageFishLevel1) {
            fishLevel = "top";
            findViewById(R.id.imageFishLevel1).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.imageFishLevel2) {
            fishLevel = "middle";
            findViewById(R.id.imageFishLevel2).setBackgroundColor(Color.rgb(100, 100, 100));
        } else if (id==R.id.imageFishLevel3) {
            fishLevel = "bottom";
            findViewById(R.id.imageFishLevel3).setBackgroundColor(Color.rgb(100, 100, 100));
        }
        //Change OK button
        ((ImageView) findViewById(R.id.btSubmit)).setImageResource(R.drawable.pressable_ok);
    }

    public void onClickSubmit(View view) {
        if (fishLevel==null) {
            return;
        }
        // Being paranoid with Bluefarm lost data
        AquaLog.info("OK, FishLevel for " + ca.getCageAllocationId() + " " + ca.getFeedingId());
        // Create an HTTP request that stores the value
        RequestDescription req = RequestDescription.makeAuthenticated(this, "PUT", R.string.url_feed,
                ":allocation_id", ca.getCageAllocationId(), ":feeding_id", ca.getFeedingId());
        if (type==BEFORE) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK);
            req.addParam("event[feeding_start]", sdf.format(new Date()));
            req.addParam("event[species_position_start]", fishLevel);
            RequestQueue.addToQueue(this, req);
            // Forward to next page or return
            Intent i = new Intent(this, EnterFeedAmountActivity .class);
            i.putExtra("cageAllocationId", ca.getCageAllocationId());
            startActivity(i);
        } else if (type==AFTER) {
            req.addParam("event[species_position_end]", fishLevel);
            RequestQueue.addToQueue(this, req);
            //Update local DB that task has been completed
            CageAllocationDB db = CageAllocationDB.get(this);
            ca.setFeedDone(true);
            db.update(ca);
            //Go back to cage view
            Intent i = new Intent(this, CageViewActivity .class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Do not add to back stack, instead pop all activities until cage view
            i.putExtra("cageAllocationId", ca.getCageAllocationId());
            startActivity(i);
        } else {
            throw new RuntimeException();
        }
    }
    
}
