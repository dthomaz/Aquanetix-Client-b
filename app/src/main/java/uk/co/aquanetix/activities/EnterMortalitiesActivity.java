package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.model.CageAllocation;
import uk.co.aquanetix.model.CageAllocationDB;
import uk.co.aquanetix.network.RequestDescription;
import uk.co.aquanetix.network.RequestQueue;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Activity to enter mortality number.
 */
public class EnterMortalitiesActivity extends AquanetixAbstractActivity {
    
    //We store each digits in an int[], where -1 means nothing entered
    //and the leftmost value (index 0) is the first digit entered
    //Index is the index in the array where the next value will be entered
    private int[] digits = new int[] {-1, -1, -1, -1, -1}; //5 empty slots
    private int index=0;
    private TextView[] result;
    private int mortalities = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_mortalities);
        setHeader();
        setSubheader(R.drawable.empty_subheader, R.string.mortalities_subheader);
        applyCustomFontToAll();
        
        //Create a reference to result TextViews
        result = new TextView[5];
        ViewGroup resultRoot = (ViewGroup) findViewById(R.id.resultNumbers);
        for (int i=0; i<resultRoot.getChildCount(); i++) {
            result[i] = (TextView) resultRoot.getChildAt(i); 
        }
    }
    
    public void onClickedNumber(View v) {
        int value = Integer.parseInt((String) v.getTag());
        
        //Update the digits table
        if (value<0 && index==0) {
            //User clicked "delete last digit", but all digits already deleted
            return;
        } else if (value<0) {
            //Delete last digit
            digits[--index] = -1;
        } else if (index==5) {
            //Don't accept more than 5 digits
            return;
        } else {
            //Add a new digit
            digits[index++] = value;
        }
        
        //Shift digits (to align on right end of array)
        int[] offset = new int[] {-1, -1, -1, -1, -1};
        System.arraycopy(digits, 0, offset, 5 - index, index);
        
        //Update result panel
        for (int i=0; i<5; i++) {
            String txt = offset[i]==-1 ? "-" : "" + offset[i];
            result[i].setText(txt);
        }
        
        //Calculate new value
        int total = 0;
        for (int i=0; i<5; i++) {
            total += offset[i]==-1 ? 0 : offset[i] * Math.pow(10, 4-i);
        }
        mortalities = total;
        
        //Update UI
        int img = digits[0]==-1 ? R.drawable.ok_dim : R.drawable.pressable_ok;
        ((ImageView) findViewById(R.id.btSubmit)).setImageResource(img);
    }
    
    public void onClickedSubmit(View view) {
        if (digits[0]==-1) { //Nothing entered yet
            return;
        }
        // Queue an HTTP request that stores the value
        int cageAllocationId = getIntent().getExtras().getInt("cageAllocationId");
        RequestDescription req = RequestDescription.makeAuthenticated(this, "POST", R.string.url_mortalities,
                ":allocation_id", cageAllocationId);
        req.addParam("event[mortalities]", "" + mortalities);
        RequestQueue.addToQueue(this, req);
        //Update local DB that task has been completed
        CageAllocationDB db = CageAllocationDB.get(this);
        CageAllocation ca = db.getCageAllocation(cageAllocationId);
        ca.setMortalitiesDone(true);
        db.update(ca);
        
        super.onBackPressed(); 
    }
    
}
