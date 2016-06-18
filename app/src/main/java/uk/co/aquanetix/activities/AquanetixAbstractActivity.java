package uk.co.aquanetix.activities;

import static android.os.Build.VERSION.SDK_INT;
import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.CrashManagerListener;
import uk.co.aquanetix.R;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import uk.co.aquanetix.network.RequestQueueSQL;
import uk.co.aquanetix.network.Sync;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * The abstract superclass of all Aquanetix Activities.
 * Contains code that otherwise should be repeated in more than one activities).
 */
public abstract class AquanetixAbstractActivity extends Activity {
    
	private static final String APP_ID = "de8731b2a2022e6b8bc52138fce8061b";
    private static final long CHECK_SYNC_ICON_EVERY = 3000; //3 secs
    
    protected AquanetixSharedPreferences prefs;
    //A custom font used throughout the app
    private static Typeface typeface;
    //The width of the components that need to occupy half the screen
    //in horizontal ScrollViews (eg UserSelect, CageSelect)
    //We only need to calculate it once since it does not change for the device
    protected static int halfWidth;
    //Used to switch-off sync icon
    private Handler h = new Handler();
    private View btSync;
    private boolean done;
    
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Screen orientation
        if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // Hide status bar for older android versions. For newer versions see onWindowFocusChanged() later on
        if (VERSION.SDK_INT < 16) {
            getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        }
        this.prefs = new AquanetixSharedPreferences(this);
        if (typeface==null) {
            //Load font
            typeface = Typeface.createFromAsset(getAssets(), "Exo-SemiBold.otf");
            //Custom sizing (half the screen, excluding navigation arrows)
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int dp = (int) getResources().getDimension(R.dimen.navArrow);
            int w = Math.max(metrics.widthPixels, metrics.heightPixels); //Orientation may not have been set yet
            halfWidth= (w - 2*dp) / 2;
        }
    }

    protected void setHeader() {
        setHeader(-1);
    }
    
    protected void setHeader(int headerResource) {
        //Set header (either from a resource string or more typically the username)
        String h; 
        if (headerResource==-1) {
            h = prefs.getUsername();
        } else {
            h = getString(headerResource);
        }
        ((TextView) findViewById(R.id.textUsername)).setText(h);
    }
    
    //Runs every few seconds and toggles the sync icon
    private Runnable toggleSyncIcon = new Runnable() {
        @Override
        public void run() {
            if (RequestQueueSQL.get(AquanetixAbstractActivity.this).size()==0) {
                btSync.setVisibility(View.INVISIBLE);
            } else {
                btSync.setVisibility(View.VISIBLE);
            }
            if (!done) {
                h.postDelayed(toggleSyncIcon, CHECK_SYNC_ICON_EVERY);
            }
        }
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        checkForCrashes();
        //Check if we need to sync with server
        if (prefs.needsUpdate()) {
            Intent i = new Intent(this, SplashActivity.class);
            //TODO: flag clear task ?
            startActivity(i);
            finish();
            return;
        }
        if (prefs.shouldUpdate()) {
            new Sync(this).updateAsync();
        }
        //Sync icon
        btSync = findViewById(R.id.btSyncStatus);
        if (btSync!=null) {
            done = false;
            toggleSyncIcon.run(); //Run it once 
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        //Sync icon
        done = true;
        h.removeCallbacks(toggleSyncIcon);
    }
    
    // Handles the full screen.
    // For SKD<16 see onCreate()
    // For SDK>=16 && SDK <19, hide the status bar, not the navigation bar
    // For SDK>19 use immersive (hides both status and navigation)
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && SDK_INT >= VERSION_CODES.JELLY_BEAN) { // API 16
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (SDK_INT >= VERSION_CODES.KITKAT) { // API 19
                flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION 
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    };
    
    //To hide the icon, pass the resource id for empty_subheader image
    protected void setSubheader(int drawable, int subheader) {
        ((TextView) findViewById(R.id.textSubheader)).setText(subheader);
        ((ImageView) findViewById(R.id.imageSubheader)).setImageResource(drawable);
    }
    
    public void onClickedBack(View view) {
        super.onBackPressed(); 
    }
    
    public void onClickedShowSynch(View view) {
        Intent i = new Intent(this, SynchroniseActivity.class);
        startActivity(i);
    }
    
    //The settings icon (top-right) at the header
    public void onClickedSettings(View view) {
        //First define a listener...
        DialogInterface.OnClickListener listenet = new DialogInterface.OnClickListener() {
            
            //Fail if no Internet connection available
            private boolean ensureInternet(int errorMsg) {
                NetworkInfo ni = ((ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                boolean hasInternet = ni!=null && ni.isConnected();
                if (!hasInternet) {
                    Toast.makeText(AquanetixAbstractActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
                return hasInternet;
            }
            
            public void onClick(DialogInterface dialog, int which) {
                Context ctx = AquanetixAbstractActivity.this;
                switch (which) {
                case 0:
                    // Change PIN or fail if no active user has been selected yet
                    if (AquanetixAbstractActivity.this instanceof UserSelectActivity ||
                            AquanetixAbstractActivity.this instanceof PinActivity) {
                        Toast.makeText(ctx, R.string.pin_change_no_user, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    // Start change-PIN process or fail if no Internet connection available
                    if (ensureInternet(R.string.pin_change_no_internet)) {
                        Intent i0 = new Intent(AquanetixAbstractActivity.this, PinChangeActivity.class);
                        startActivity(i0);
                    }
                    break;
                case 1:
                    // Start select-farm process or fail if no Internet connection available
                    String user_role = prefs.getRole();
                    if (ensureInternet(R.string.pin_change_no_internet) && (user_role.equals("administrator") || user_role.equals("manager")))  {
                        Intent i_1 = new Intent(AquanetixAbstractActivity.this, SelectFarmActivity.class);
                        startActivity(i_1);
                    }
                    break;
                case 2:
                    //Do nothing. It's "cancel"
                    break;
                case 3:
                    // Temporary code to force reloading. Not available from UI
                    Intent i_3 = new Intent(AquanetixAbstractActivity.this, SplashActivity.class);
                    startActivity(i_3);
                    break;
                    
//                    //Delete credentials
//                    prefs.setDeviceSecret(null);
//                    prefs.setCompanyId(0);
//                    //Go back to splash screen
//                    Intent i1 = new Intent(AquanetixAbstractActivity.this, SplashActivity.class);
//                    // TODO: verify how Intent.FLAG_ACTIVITY_CLEAR_TASK should work in 2.2
//                    i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(i1);
//                    break;
                }
            }
        };
        //Then create and show a dialog with setting options
        new AlertDialog.Builder(this).setItems(R.array.settings_options, listenet).show();
    }
    
    protected void applyCustomFontToAll() {
        View root = getWindow().getDecorView().findViewById(android.R.id.content);
        applyCustomFont(root);
    }
    
    protected void applyCustomFont(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyCustomFont(group.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            ((TextView) view).setTypeface(typeface);
        }
    }

    private void checkForCrashes() {
        CrashManager.register(this, APP_ID, new CrashManagerListener() {
            @Override
            public boolean shouldAutoUploadCrashes() {
                return true;
            }
        });
    }

}