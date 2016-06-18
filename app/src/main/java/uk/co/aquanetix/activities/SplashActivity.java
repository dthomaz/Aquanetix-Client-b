package uk.co.aquanetix.activities;

import uk.co.aquanetix.R;
import uk.co.aquanetix.network.Sync;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * The splash screen. Ensures the app has downloaded all the data
 * required to function properly.
 */
public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        new EnsureData().execute();
    }
    
    public void onClickedTryAgain(View view) {
        showMessage(-1);
        new EnsureData().execute();
    }
    
    //Toggles the bottom-of-screen message between "Loading" and errorMsg
    private void showMessage(int errorMsg) {
        TextView textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
        if (errorMsg < 0) {
            findViewById(R.id.textLoading).setVisibility(View.VISIBLE);
            textErrorMessage.setVisibility(View.GONE);
            findViewById(R.id.btTryAgainInternet).setVisibility(View.GONE);
        } else {
            findViewById(R.id.textLoading).setVisibility(View.GONE);
            textErrorMessage.setText(errorMsg);
            textErrorMessage.setVisibility(View.VISIBLE);
            findViewById(R.id.btTryAgainInternet).setVisibility(View.VISIBLE);
        }
    }
    
    //Asynchronously, we make sure that all data needed for the off-line functionality
    //of the app is downloaded.
    //The doInBackground() runs on the background thread and returns a status code.
    //The onPostExecute runs on the main thread and based on the status code it refreshes the UI.
    private class EnsureData extends AsyncTask<Void, Void, Integer> {
        
        @Override
        protected Integer doInBackground(Void... params) {
            return new Sync(SplashActivity.this).update();
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "Splash screen action: " + result);
            switch (result) {
            case Sync.OK:
                startActivity(new Intent(SplashActivity.this, UserSelectActivity.class));
                finish();
                break;
            case Sync.REGISTER_DEVICE:
                startActivity(new Intent(SplashActivity.this, RegistrationActivity.class));
                finish();
                break;
            case Sync.NO_INTERNET:
                showMessage(R.string.splash_no_internet);
                break;
            case Sync.USER_LIST_ERROR:
                showMessage(R.string.splash_user_error);
                break;
            case Sync.CAGE_LIST_ERROR:
                showMessage(R.string.splash_cage_error);
                break;
            default:
                throw new RuntimeException();
            }
        }
    };

}