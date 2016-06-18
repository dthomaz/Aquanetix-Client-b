package uk.co.aquanetix.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collection;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.android.CageSummaryUtils;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.model.Feed;
import uk.co.aquanetix.model.FeedDB;
import uk.co.aquanetix.model.User;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;

/**
 * Created by diogothomaz on 13/12/15.
 */
public class FeedViewActivity extends AquanetixAbstractActivity {

    private CageAllocation ca;
    private int cageAllocationId;
    private CageAllocationDB cal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_select_feed);
        setHeader(R.string.feedlist_title);

        //Scroll left button
        final HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.feedListScroller);
        findViewById(R.id.arrowLeft).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                scroller.scrollBy(-halfWidth, 0);
            }
        });

        //Scroll right button
        findViewById(R.id.arrowRight).setOnTouchListener(new ClickAndHold() {
            @Override
            public void onChange(int steps) {
                scroller.scrollBy(halfWidth, 0);
            }
        });

        //Apply fonts
        applyCustomFontToAll();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
        cal = CageAllocationDB.get(this);
        ca = CageAllocationDB.get(this).getCageAllocation(cageAllocationId);
        int start_feed_id = ca.getFeedId();

        //Add list of feeds on UI
        Collection<Feed> feeds = FeedDB.get(this).getFeeds();
        //double start_feed_min_weight = FeedDB.get(this).getFeedMinWeight(start_feed_id);
        //double start_feed_max_weight = FeedDB.get(this).getFeedMaxWeight(start_feed_id);
        double fish_mw = ca.getMeanWeightGr();
        ViewGroup root = (ViewGroup) findViewById(R.id.feedList);
        root.removeAllViews();

        for (final Feed fe:feeds) {
            /*if (u.isHideFromClient()) {
                continue;
            }
            if (fe.getMinWeight() > start_feed_min_weight || fe.getMaxWeight() < start_feed_max_weight) {
                continue;
            }*/

            if (fe.getMinWeight() > fish_mw || fe.getMaxWeight() < fish_mw) {
                continue;
            }

            ViewGroup  layout = (ViewGroup) getLayoutInflater().inflate(R.layout.feed_in_list, root, false);
            ((TextView) layout.findViewById(R.id.textFeedname)).setText(fe.getFeedname());
            root.addView(layout);
            layout.getLayoutParams().width = halfWidth;

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ca.setFeedType(fe.getFeedname());
                    ca.setFeedId(fe.getFeedId());
                    cal.update(ca);
                    FeedViewActivity.super.onBackPressed();
                }
            });
        }
    }

}
