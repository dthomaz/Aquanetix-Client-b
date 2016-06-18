package uk.co.aquanetix.activities;

import java.util.Locale;

import uk.co.aquanetix.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A TEMPORARY activity to show the device's details.
 * Used to debug various devices.
 */
public class SystemInfo extends Activity {

    private ViewGroup root;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_info);
        
        root = (ViewGroup) findViewById(R.id.root);
        
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        
        addValue("Density",         metrics.density);
        addValue("Density DPI",     metrics.densityDpi);
        addValue("Scaled density",  metrics.scaledDensity);
        addValue("width",           metrics.widthPixels);
        addValue("height",          metrics.heightPixels);
        addValue("xdi",             metrics.xdpi);
        addValue("ydi",             metrics.ydpi);
      
        addValue("Brand",           Build.BRAND);
        addValue("Devive",          Build.DEVICE);
        addValue("Model",           Build.MODEL);
    }
    
    private void addValue(String caption, float value) {
        addValue(caption, String.format(Locale.US, "%.3f", value));
    }
    
    private void addValue(String caption, String value) {
        TextView txt1 = new TextView(this);
        txt1.setText(caption);
        txt1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        txt1.setGravity(Gravity.LEFT);
        root.addView(txt1);
        
        TextView txt2 = new TextView(this);
        txt2.setText(value);
        txt2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        txt2.setGravity(Gravity.RIGHT);
        txt2.setBackgroundResource(R.drawable.border_bottom);
        root.addView(txt2);
    }

    public void onCLickedBack(View v) {
        super.onBackPressed();
    }
    
    public void onCLickedException(View v) {
        throw new RuntimeException("Test exception. Ignore it.");
    }
    
}
