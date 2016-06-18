package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to enter/change user's pin.
 * It can run with any of the following targets:
 * TARGET_VERFIY: returns the userId if the PIN is the correct
 * TARGET_ENTER_ONE: first step of change PIN, returns the newly entered raw PIN
 * TARGET_ENTER_TWO: second step of change PIN, returns the newly entered raw PIN
 */
public class PinActivity extends AquanetixAbstractActivity {
    
    public static final int TARGET_VERFIY = 1;
    public static final int TARGET_ENTER_ONE = 2;
    public static final int TARGET_ENTER_TWO = 3;
    
    //We store each digits in an int[], where -1 means nothing entered
    //and the leftmost value (index 0) is the first digit entered
    //Index is the index in the array where the next value will be entered
    private int[] digits = new int[] {-1, -1, -1, -1}; //4 empty slots
    private int index=0;
    private TextView[] result;
    private int userId;
    private String validPin;
    private String enterPin = "----";
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);
        applyCustomFontToAll();
        
        Bundle xt = getIntent().getExtras();
        mode = xt.getInt("target");
        if (mode==TARGET_VERFIY) {
            userId = xt.getInt("userId");
            validPin = xt.getString("pincode");
            setHeader(R.string.pin_enter);
        } else if (mode==TARGET_ENTER_ONE) {
            setHeader(R.string.pin_change_first);
        } else if (mode==TARGET_ENTER_TWO) {
            setHeader(R.string.pin_change_second);
        } else {
            throw new RuntimeException("Please set the mode extra for PinActivity");
        }
        
        //Create a reference to result TextViews
        result = new TextView[4];
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
        } else if (index==4) {
            //Don't accept more than 4 digits
            return;
        } else {
            //Add a new digit
            digits[index++] = value;
        }
        
        //Shift digits (to align on right end of array)
        int[] offset = new int[] {-1, -1, -1, -1};
        System.arraycopy(digits, 0, offset, 4 - index, index);
        
        //Update result panel
        for (int i=0; i<4; i++) {
            String txt = offset[i]==-1 ? "-" : "*";
            result[i].setText(txt);
        }
        
        //Calculate new value
        enterPin = "";
        for (int i=0; i<4; i++) {
            enterPin += offset[i]==-1 ? "-" : offset[i];
        }
        
        //Update UI
        int img = digits[3]==-1 ? R.drawable.ok_dim : R.drawable.pressable_ok;
        ((ImageView) findViewById(R.id.btSubmit)).setImageResource(img);
    }
    
    public void onClickedSubmit(View view) {
        if (mode==TARGET_VERFIY) {
            verifyPin();
        } else if (mode==TARGET_ENTER_ONE || mode==TARGET_ENTER_TWO) {
            returnPin();
        }
    }
    
    private void resetUI() {
        for (int i=0; i<4; i++) {
            digits[i] = 0;
            index=0;
            result[i].setText("-");
            ((ImageView) findViewById(R.id.btSubmit)).setImageResource(R.drawable.ok_dim);
        }
    }
    
    private void verifyPin() {
        if (digits[0]==-1) { //Nothing entered yet
            return;
        }
        if (validPin.equals(enterPin)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("userId", userId);
            setResult(RESULT_OK,returnIntent);
            finish();
        } else {
            resetUI();
            Toast.makeText(this, R.string.pin_wrong, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void returnPin() {
        if (digits[0]==-1) { //Nothing entered yet
            return;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("pin", enterPin);
        setResult(RESULT_OK,returnIntent);
        finish();
    }
    
}
