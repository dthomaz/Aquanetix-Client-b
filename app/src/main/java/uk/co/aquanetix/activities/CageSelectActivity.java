package uk.co.aquanetix.activities;

import java.util.Collection;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.AquaLog;
import uk.co.aquanetix.android.CageSummaryUtils;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
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

/**
 * The list of cages.
 */
public class CageSelectActivity extends AquanetixAbstractActivity {
    
    public static boolean needsRefresh = false;
    private static final int HIGHLIGHT = Color.rgb(75, 75, 75);
    private LinearLayout root;
    private boolean filterByUser = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_select_cage);
        setHeader();
        
        //Get list of cage allocations
        needsRefresh = false;
        Collection<CageAllocation> cageAllocations = CageAllocationDB.get(this).getCageAllocations();
        
        //Calculate the height that is available for CageSummary
        //That is: screen height -android bar - header - subheader - empty bottom space
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int total = Math.min(metrics.widthPixels, metrics.heightPixels); //Orientation may not have been set yet
        int dpTop = (int) getResources().getDimension(R.dimen.headerHeight);
        int dpBottom = (int) getResources().getDimension(R.dimen.emptyBottomBar);
        int bar = (int) Math.ceil(30 * getResources().getDisplayMetrics().density);
        int height = total - 2*dpTop - dpBottom - bar;
        
        //Add them on UI
        root = (LinearLayout) findViewById(R.id.cageList);
        for (final CageAllocation ca:cageAllocations) {
            ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.common_cage_summary, root, false);
            //Add to layout
            root.addView(layout);
            layout.setTag(ca.getCageAllocationId()); //Used by updateUI() to filter cages and images
            layout.getLayoutParams().width = halfWidth;
            layout.getLayoutParams().height = height;
            
            //On click
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(CageSelectActivity.this, CageViewActivity.class);
                    i.putExtra("cageAllocationId", ca.getCageAllocationId());
                    startActivity(i);
                }
            });
        }
        
        //Scroll left button
        final HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.cageAllocScroller);
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
        if (needsRefresh) {
            // Just after a change of farm, we get out of the activity to force a complete re-load of cages
            super.onBackPressed();
            needsRefresh = false;
        } else {
            //Set correct style and data everywhere
            updateUI();
        }
    }

    public void onClickedFilterOne(View view) {
        filterByUser = true;
        updateUI();
    }
    
    public void onClickedFilterAll(View view) {
        filterByUser = false;
        updateUI();
    }
    
    private void updateUI() {
        //Highlight correct user filter
        ImageView one = (ImageView) findViewById(R.id.userFilterOne);
        ImageView all = (ImageView) findViewById(R.id.userFilterAll);
        if (filterByUser) {
            one.setBackgroundColor(HIGHLIGHT);
            one.setImageResource(R.drawable.person_blue);
            all.setBackgroundColor(Color.TRANSPARENT);
            all.setImageResource(R.drawable.person3_grey);
        } else {
            one.setBackgroundColor(Color.TRANSPARENT);
            one.setImageResource(R.drawable.person_grey);
            all.setBackgroundColor(HIGHLIGHT);
            all.setImageResource(R.drawable.person3_blue);
        }
        
        //Read all cage allocations for second time because they WILL have changed every time
        //onResume() is called (except the very first time)
        SparseArray<CageAllocation> allocs = CageAllocationDB.get(this).getCageAllocationsMap();
        
        //Update cage list
        AquanetixSharedPreferences prefs = new AquanetixSharedPreferences(this);
        int currentUserId = prefs.getUserId();
        for (int i=0; i<root.getChildCount(); i++) {
            View cageAllocView = root.getChildAt(i);
            int allocId = (Integer) cageAllocView.getTag();
            CageAllocation ca = allocs.get(allocId);
            //Hide/show cage allocations (user filter)
            if (ca==null) {
                // Probably the cage allocation was removed while the user was entering feeding
                // data, therefore we cannot update UI
                AquaLog.warn("Recovered from missing cage-allocation: " + allocId);
                cageAllocView.setVisibility(View.GONE);
                continue;
            } else if (filterByUser && ca.getUserId()!=currentUserId) {
                cageAllocView.setVisibility(View.GONE);
            } else {
                cageAllocView.setVisibility(View.VISIBLE);
            }
            //Update the red/blue icons
            CageSummaryUtils.updateView(this, cageAllocView, ca);
        }
    }

}