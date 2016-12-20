package shuvalov.nikita.twas.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import shuvalov.nikita.twas.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Aside from keeping the user content with my animation screen, I'll also do my data loading here.
        /**
         * Step 1: Check if user is logged in.
         *
         * if Yes:
         Step 2: Check local database.
         Step 3: Get all items from local database and add to singleton.
         Step 4: Notify user that the initial load is complete and that they can navigate to next screen.
         Otherwise:
         Step 5: In background, check to see if any changes occurred for users.
         Step 6: Update local database.
         Step 7: Update singleton.
         Step 8: GoTo Step 5.

         Otherwise:
         Navigate to log-in screen.
         Come back here and start at step 2 after logging in.
         */
    }
}
