package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class SplashScreenActivity extends AppCompatActivity {
    String mId;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    DatabaseReference mSelfProfileRef;
    DatabaseReference mSelfConnectionsRef;
    DatabaseReference mSelfChatroomsRef;
    FirebaseDatabase mFirebaseDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        checkAuthOfUser();
        //Refer to this to remove this activity from back-stack when I get it going.
        //http://stackoverflow.com/questions/14112219/android-remove-activity-from-back-stack


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

    public ArrayList<String> checkForMissingProfiles(ArrayList<String> userIdList) {
        ArrayList<Profile> currentStoredProfiles = ConnectionsSQLOpenHelper.getInstance(this).getAllConnections();
        ConnectionsHelper.getInstance().addProfileConnectionsToCollection(currentStoredProfiles); //FixMe: This should probably go elsewhere
        ArrayList<String> currentStoredIds = new ArrayList<>();
        ArrayList<String> idsMissingProfiles = new ArrayList<>();
        for (Profile profile : currentStoredProfiles) {
            currentStoredIds.add(profile.getUID());
        }

        for (String uid : userIdList) {
            if (!currentStoredIds.contains(uid)) {
                idsMissingProfiles.add(uid);
            }
        }

        return idsMissingProfiles;
    }

    public void retrieveMissingProfiles(ArrayList<String> missingIdList) {
        for (String uid : missingIdList) {
            DatabaseReference profileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, uid);
            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Profile profile = dataSnapshot.getValue(Profile.class);
                    ConnectionsSQLOpenHelper.getInstance(SplashScreenActivity.this).addNewConnection(profile);
                    ConnectionsHelper.getInstance().addProfileToCollection(profile);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void getUsersFbdbInformation() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mId = SelfUserProfileUtils.getUserId(this);
        if (mId.equals(AppConstants.PREF_EMPTY)) {
            Log.d("MainActivity", "Either Awesome ID or No ID found in sharedPref");
            Throwable throwable = new Throwable("Accessed requires a User ID");//FixMe: Pretty sure this isn't the right way to do this.
            try {
                throw throwable;
            } catch (Throwable throwable1) {
                throwable1.printStackTrace();
            }
        }

        mSelfProfileRef = FirebaseDatabaseUtils.getUserProfileRef(mFirebaseDatabase, mId);
        mSelfConnectionsRef = FirebaseDatabaseUtils.getUserConnectionsRef(mFirebaseDatabase, mId);
        mSelfChatroomsRef = FirebaseDatabaseUtils.getUserChatroomsRef(mFirebaseDatabase, mId);

        //Check for logged-in user's profile information, in case it was updated on another device.
        mSelfProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile myProfile = dataSnapshot.getValue(Profile.class);
                if (myProfile != null) {
                    SelfUserProfileUtils.assignProfileToSharedPreferences(SplashScreenActivity.this, myProfile);
                    Log.d("Profile test", "onDataChange: " + myProfile.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Checking FBDB", "onCancelled: " + databaseError.getMessage());
            }
        });

        //ToDo: Check for logged-in user's chatroom associations.

        //ToDo: Check for logged-in user's connections list.
        mSelfConnectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> connectionsSnapshot = dataSnapshot.getChildren();
                ArrayList<String> connectionsList = new ArrayList<>();
                for (DataSnapshot connection : connectionsSnapshot) {
                    connectionsList.add(connection.getKey());
                }
                //ToDo: Use the list of connections and compare with what we have in the database, then fill in what's missing.
                ArrayList<String> missingIds = checkForMissingProfiles(connectionsList);
                if (!missingIds.isEmpty()) {
                    retrieveMissingProfiles(missingIds);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkAuthOfUser(){
        mAuth = FirebaseAuth.getInstance();

        //ToDo: Once I have a splash loading screen(& set as launch activity), do this check in that activity.
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!= null){
                    String userId = user.getUid();

                    //Check if the authenticated user's id matches the id we have stored in preferences. If not, clear preferences.
                    if(!SelfUserProfileUtils.compareStoredIdWithCurrentId(SplashScreenActivity.this,userId)){
                        SelfUserProfileUtils.clearUserProfile(SplashScreenActivity.this); //We clear the user Preferences if the IDs don't match. This is a back-up check, typically the preferences should be cleared on sign-out as well.
                        //ToDo: Check to see if user has a profile in FBDB, if so add that info to preferences, otherwise add only UserId to preferences.
                        SelfUserProfileUtils.setUserId(SplashScreenActivity.this, userId);

                    }
                    Toast.makeText(SplashScreenActivity.this, "Signed as "+ userId, Toast.LENGTH_SHORT).show();
                    Log.d("AuthStateChanged", "Logged in as "+ user.getUid());
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Log.d("AuthStateChanged", "Signed Out");
                }
            }
        };
    }
}
