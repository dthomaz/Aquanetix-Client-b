package uk.co.aquanetix.activities;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.model.FeedDB;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * Enter the amount of feed used in a cage.
 */
public class EnterFeedAmountActivity extends AquanetixAbstractActivity {
  
    private CageAllocation ca;
    private int feedingId;
    private float approved_qty;
    private float fed_qty;
    private float wpu;
    private boolean bags;
    private String units;
    private String round_str;
    private float round_size;
    private double round;
    public String unit_system;

    public String getWeightLabel(String system) {
        String label = system.equals("metric") ? "kg" : "lb";
        return label;
    }

    public float outputWeight(String system, float value) {
        return (system.equals("metric") ? value : (value / (float) 0.45359237));
    }

    public float inputWeight(String system, float value) {
        return (system.equals("metric") ? value : (value * (float) 0.45359237));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AquanetixSharedPreferences p = new AquanetixSharedPreferences(EnterFeedAmountActivity.this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_feed_quantity);
        setHeader();
        
        //Find cageAllocation
        int caId = getIntent().getExtras().getInt("cageAllocationId");
        ca = CageAllocationDB.get(this).getCageAllocation(caId);
        unit_system = UserDB.get(EnterFeedAmountActivity.this).getUnitSystem(ca.getUserId());
        bags = UserDB.get(this).isBagsFlag(p.getUserId());
        fed_qty = 0;
        wpu = outputWeight(unit_system , (float) FeedDB.get(this).getFeedWeightPerUnit(ca.getFeedId()));
        units = bags ? "bags" : getWeightLabel(unit_system);
        // Being paranoid with Bluefarm lost data
        AquaLog.info("Enter, FishFeed " + ca.getCageAllocationId() + " " + ca.getFeedingId());
        
        //Initial value and UI setup
        float approved = outputWeight(unit_system , ca.getFeedQuantityApproved());
        approved_qty = bags ? approved/wpu : approved;
        round = bags ? 1 : approved < 0.101 ? 3 : approved < 1.01 ? 2 : approved < 20.1 ? 1 : 0;
        round_size = bags ? 65 : approved < 0.1 ? 40 : 65;
        round_str = bags ? "#.1" : approved < 0.11 ? "#.###" : approved < 1 ? "#.##" : approved < 20 ? "#.#" : "#";
        //approved_qty = (int) ca.getFeedQuantityApproved();
        ((TextView) findViewById(R.id.textFeedName)).setText(ca.getFeedType());
        ((TextView) findViewById(R.id.textCageName)).setText(ca.getCageName());
        changeValue(0);

        ((TextView) findViewById(R.id.textUnitsDisplay)).setText(units);
        setFeedApproved();

        //Click on feedName to open list of alternative feeds
        findViewById(R.id.textFeedBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(EnterFeedAmountActivity.this, FeedViewActivity.class);
                i.putExtra("cageAllocationId", ca.getCageAllocationId());
                startActivity(i);
            }

        });


        //The decrement button listener
        findViewById(R.id.btDecrement).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                changeValue(-1);
            }
        });
        
        //The increment button listener
        findViewById(R.id.btIncrement).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                changeValue(1);
            }
        });

        //Apply fonts
        applyCustomFontToAll();
    }


    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        float previous_wpu = wpu;
        int caId = getIntent().getExtras().getInt("cageAllocationId");
        ca = CageAllocationDB.get(this).getCageAllocation(caId);
        ((TextView) findViewById(R.id.textFeedName)).setText(ca.getFeedType());
        wpu = outputWeight(unit_system , (float) FeedDB.get(this).getFeedWeightPerUnit(ca.getFeedId()));
        if (fed_qty > 0) {
            approved_qty = fed_qty;
            ((TextView) findViewById(R.id.textValueDisplay)).setText(formatKg());
        } else {
            approved_qty = bags ? outputWeight(unit_system , ca.getFeedQuantityApproved()) / wpu : outputWeight(unit_system , ca.getFeedQuantityApproved());
            ((TextView) findViewById(R.id.textValueDisplay)).setText(formatKg());
        }
        setFeedApproved();
    }

    private String formatKg() {
        round_str = bags ? "#.1" : approved_qty <= 0.10 ? "0.000" : approved_qty <= 1.00 ? "0.00" : approved_qty <= 20.0 ? "0.0" : "#";
        if (bags) {
            return new DecimalFormat("#.1").format(approved_qty);
        } else {
            return new DecimalFormat(round_str).format(approved_qty);
        }
    }

    private void changeValue(float diff) {
        round = bags ? 1 : approved_qty <= 0.10001 ? 3 : approved_qty <= 1.00001 ? 2 : approved_qty <= 20.0001 ? 1 : 0;
        if ( !(bags) ) {
            diff *= Math.pow(0.1, round);
        }

        if (bags) {
            diff *=0.5;
        }

        approved_qty += diff;

        approved_qty = Math.max(0, approved_qty);
        round_str = bags ? "#.1" : approved_qty <= 0.10 ? "0.000" : approved_qty <= 1.00 ? "0.00" : approved_qty <= 20.0 ? "0.0" : "#";
        round = bags ? 1 : approved_qty <= 0.10 ? 3 : approved_qty <= 1.00 ? 2 : approved_qty <= 20.0 ? 1 : 0;
        round_size = bags ? 65 : approved_qty <= 0.1 ? 40 : 65;
        approved_qty = bags ? (float) Math.round(approved_qty * 2) / 2 : (float) Math.round(approved_qty * 1000) / 1000;
        ((TextView) findViewById(R.id.textValueDisplay)).setTextSize(TypedValue.COMPLEX_UNIT_SP, round_size);
        ((TextView) findViewById(R.id.textValueDisplay)).setText(formatKg());
        if (diff != 0) {
            fed_qty = approved_qty;
            setFeedApproved();
        }
    }
    
    public void onClickSubmit(View view) {
        // Being paranoid with Bluefarm lost data
        AquaLog.info("OK, FishFeed for " + ca.getCageAllocationId() + " " + ca.getFeedingId());

        wpu = outputWeight(unit_system , (float) FeedDB.get(this).getFeedWeightPerUnit(ca.getFeedId()));
        float quantity_fed = bags ? inputWeight(unit_system , approved_qty * wpu) : inputWeight(unit_system , approved_qty);
        // Queue an HTTP request that stores the value
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.UK);
        RequestDescription req = RequestDescription.makeAuthenticated(this, "PUT", R.string.url_feed,
                ":allocation_id", ca.getCageAllocationId(), ":feeding_id", ca.getFeedingId());
        String round2_str = bags ? "%.1f" : quantity_fed <= 0.10 ? "%.3f" : quantity_fed <= 1.00 ? "%.2f" : quantity_fed <= 20.0 ? "%.1f" : "%.0f";
        req.addParam("event[quantity_fed]", String.format(Locale.US , "%.5f", quantity_fed));
        req.addParam("event[feed_id]", String.valueOf(ca.getFeedId()));
        req.addParam("event[feeding_end]", sdf.format(new Date()));
        RequestQueue.addToQueue(this, req);

        ca.setFeedQuantityUsed(quantity_fed);
        CageAllocationDB cal = CageAllocationDB.get(this);
        cal.update(ca);
        //Forward to next page
        Intent i = new Intent(this, EnterFeedFishLevelActivity.class);
        i.putExtra("cageAllocationId", ca.getCageAllocationId());
        i.putExtra("type", EnterFeedFishLevelActivity.AFTER);
        startActivity(i);
    }

    public void setFeedApproved() {

        TextView approved = (TextView) findViewById(R.id.textFeedApproved);
        wpu = outputWeight(unit_system , (float) FeedDB.get(this).getFeedWeightPerUnit(ca.getFeedId()));
        String quantity_fed = bags ? new DecimalFormat("#.1").format(fed_qty) : new DecimalFormat(round_str).format(fed_qty);
        String quantity_approved = bags ? new DecimalFormat("#.1").format(outputWeight( unit_system , ca.getFeedQuantityApproved()) / wpu) : new DecimalFormat(round_str).format(outputWeight(unit_system , ca.getFeedQuantityApproved()));
        approved.setText(Html.fromHtml("<font color=#FFFFFF>" + quantity_fed + "</font>" + "/" + quantity_approved + "<font color=#F87C6F>" + units + "</font>"));
    }
}
