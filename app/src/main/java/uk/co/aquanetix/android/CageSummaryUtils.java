package uk.co.aquanetix.android;

import java.util.Locale;

import uk.co.aquanetix.R;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.model.FeedDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Sets the correct data on the cage-summary widget. This widget is
 * visible in cage-list and cage-view.
 */
public class CageSummaryUtils {


    public static String getWeightLabel(String system) {
        String label = system.equals("metric") ? "kg" : "lb";
        return label;
    }

    public static float outputWeight(String system, float value) {
        return (system.equals("metric") ? value : (value / (float) 0.45359237));
    }

    public float inputWeight(String system, float value) {
        return (system.equals("metric") ? value : (value * (float) 0.45359237));
    }


    public static void updateView(Context context, View root, CageAllocation ca) {

        //Cage name
        ((TextView) root.findViewById(R.id.textCageName)).setText(ca.getCageName());
        
        //The four "done" icons on the left
        int feedImg = ca.isFeedDone() ? R.drawable.feed_round_blue : R.drawable.feed_round_red;
        ((ImageView) root.findViewById(R.id.imageFeedDone)).setImageResource(feedImg);
        int mortalitiesImg = ca.isMortalitiesDone() ? R.drawable.mortality_round_blue : R.drawable.mortality_round_red;
        ((ImageView) root.findViewById(R.id.imageMortalitiesDone)).setImageResource(mortalitiesImg);
        int healthImg = ca.isHealthDone() ? R.drawable.health_round_blue : R.drawable.health_round_red;
        ((ImageView) root.findViewById(R.id.imageHealthDone)).setImageResource(healthImg);
        // Special case for nets: if pond/tank turn this button to environmental
        int netImg;
        if ("cage".equals(ca.getCageType())) {
            netImg = ca.isNetDone() ? R.drawable.net_round_blue : R.drawable.net_round_red;
        } else {
            netImg = ca.isTemperatureDone() && ca.isOxygenDone() ? R.drawable.environmental_blue : R.drawable.environmental_red;
        }
        ((ImageView) root.findViewById(R.id.imageNetDone)).setImageResource(netImg);
        
        //The feed details
        AquanetixSharedPreferences p = new AquanetixSharedPreferences(context);
        boolean bags = UserDB.get(context).isBagsFlag(p.getUserId());
        ((TextView) root.findViewById(R.id.textFeedName)).setText(ca.getFeedType());
        String unit_system = UserDB.get(context).getUnitSystem(ca.getUserId());
        float wpu = outputWeight(unit_system ,(float) FeedDB.get(context).getFeedWeightPerUnit(ca.getFeedId()));
        float approved = outputWeight(unit_system ,ca.getFeedQuantityApproved());
        float used = outputWeight(unit_system , ca.getFeedQuantityUsed());

        String round_str = bags ? "%.1f" : approved == 0.0 ? "%.0f" : approved <= 0.10 ? "%.3f" : approved <= 1.00 ? "%.2f" : approved <= 20.0 ? "%.1f" : "%.0f";
        String round2_str = bags ? "%.1f" : used <= 0.0 ? "%.0f" : used <= 0.10 ? "%.3f" : used <= 1.00 ? "%.2f" : used <= 20.0 ? "%.1f" : "%.0f";
        String amount_approved = String.format(Locale.US, round_str, bags ? (wpu > 0 ? approved/wpu : 0.0) : approved);
        String amount_fed = String.format(Locale.US, round2_str, bags ? (wpu > 0 ? used/wpu : 0.0) : used);
        String units = bags ? "bags" : getWeightLabel(unit_system);
        Spanned s = Html.fromHtml( " <font color=#FFFFFF>" + amount_fed + "</font>/" + amount_approved + " <font color=#F87C6F>" + units + "</font>");
        ((TextView) root.findViewById(R.id.textFeedKg)).setText(s);
        String username = UserDB.get(context).getUsername(ca.getUserId());
        ((TextView) root.findViewById(R.id.textAllocatedTo)).setText(username);
    }

}
