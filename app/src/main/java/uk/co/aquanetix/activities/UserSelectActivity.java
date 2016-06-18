package uk.co.aquanetix.activities;

import java.util.Collection;

import uk.co.aquanetix.R;
import uk.co.aquanetix.android.ClickAndHold;
import uk.co.aquanetix.model.User;
import uk.co.aquanetix.model.UserDB;
import uk.co.aquanetix.network.AquanetixSharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

/**
 * Activity to select user.
 */
public class UserSelectActivity extends AquanetixAbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_select_user);
        setHeader(R.string.userlist_title);
        
        //Scroll left button
        final HorizontalScrollView scroller = (HorizontalScrollView) findViewById(R.id.userListScroller);
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
        
        //Add list of users on UI
        Collection<User> users = UserDB.get(this).getUsers();
        ViewGroup root = (ViewGroup) findViewById(R.id.userList);
        root.removeAllViews();
        for (final User u:users) {
            if (u.isHideFromClient()) {
                continue;
            }
            ViewGroup  layout = (ViewGroup) getLayoutInflater().inflate(R.layout.common_user, root, false);
            ((TextView) layout.findViewById(R.id.textUsername)).setText(u.getUsername());
            root.addView(layout);
            layout.getLayoutParams().width = halfWidth;
            
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Open PIN activity
                    Intent i = new Intent(UserSelectActivity.this, PinActivity.class);
                    i.putExtra("target", PinActivity.TARGET_VERFIY);
                    i.putExtra("userId", u.getUserId());
                    i.putExtra("pincode", u.getPincode());
                    startActivityForResult(i, 1);
                }
            });
        }
    }
    
    //PIN activity returns
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1 && resultCode==RESULT_OK) {
            //Find user
            int userId = data.getExtras().getInt("userId");
            String username = UserDB.get(this).getUsername(userId);
            if (username==null) {
                return;
            }
            String role = UserDB.get(this).getRole(userId);
            if (role ==null) {
                return;
            }
            //Persist
            AquanetixSharedPreferences p = new AquanetixSharedPreferences(UserSelectActivity.this);
            p.setUserId(userId);
            p.setUsername(username);
            p.setRole(role);
            //Forward to main menu
            Intent i = new Intent(UserSelectActivity.this, HomeMenuActivity.class);
            startActivity(i);
        }
    }

}