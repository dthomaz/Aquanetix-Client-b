package uk.co.aquanetix.android;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * An OnTouchListener wrapper that generates click-and-hold events.
 * Use each instance for only one view.
 */
public abstract class ClickAndHold implements OnTouchListener, Runnable {
    
    private final Handler mHandler = new Handler();;
    private int steps;
    private boolean done;
    
    @Override
    public void run() {
        if (done) {
            return;
        }
        if (steps==1) {
            onChange(steps++);
            mHandler.postDelayed(this, 500);
        } else if (steps<10) {
            onChange(steps++);
            mHandler.postDelayed(this, 200);
        } else {
            onChange(steps++);
            mHandler.postDelayed(this, 100);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked()==MotionEvent.ACTION_DOWN) {
            v.setPressed(true);
            done = false;
            steps = 1;
            mHandler.post(this);
            return true;
        } else if (event.getActionMasked()==MotionEvent.ACTION_UP || 
                event.getActionMasked()==MotionEvent.ACTION_CANCEL) { //Stream of events is inconsistent
            v.setPressed(false);
            done = true;
            mHandler.removeCallbacks(this);
            return true;
        }
        return false;
    }
    
    public abstract void onChange(int steps);
}
