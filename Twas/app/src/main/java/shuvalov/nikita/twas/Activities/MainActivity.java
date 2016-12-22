package shuvalov.nikita.twas.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ProfileCollectionRecyclerAdapter;
import shuvalov.nikita.twas.SelfProfileRecyclerAdapter;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String FOUND_ID_INTENT = "Found id";

    Toolbar mToolbar;
    Button mSendButt, mRetrieveButton, mSignOutButton, mProfileButton;
    TextView mDisplayText;
    String mFoundId;

    RecyclerView mRecyclerView;

    NearbyManager mNearbyManager;
    ProfileCollectionRecyclerAdapter mProfileRecAdapter;

    GoogleApiClient mGoogleApiClient;
    Message mFindMeMessage;
//    Message mActiveMessage;
    MessageListener mActiveListener;


    Profile mProfile;
    DatabaseReference mRef;
    FirebaseDatabase mFirebaseDatabase;

    String mId;
//    String[] navOptions= new String[]{"Profile","Home", "Settings","SoapBox Feed","Invite Friends", "Donate", "About"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNearbyManager = NearbyManager.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mId = SelfUserProfileUtils.getUserId(this);
        if(mId.equals(AppConstants.PREF_EMPTY)){
            Log.d("MainActivity", "Either Awesome ID or No ID found in sharedPref");
            Throwable throwable = new Throwable("Accessed requires a User ID");//FixMe: Pretty sure this isn't the right way to do this.
            try {
                throw throwable;
            } catch (Throwable throwable1) {
                throwable1.printStackTrace();
            }
        }
        mRef = mFirebaseDatabase.getReference(mId);

        //ToDo: Check if user has profile on FBDB that we can pull.

        findViews();
        setUpRecyclerView();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Profile myProfile = dataSnapshot.getValue(Profile.class);
                if(myProfile!=null){
                    SelfUserProfileUtils.assignProfileToSharedPreferences(MainActivity.this, myProfile);
                    Log.d("Profile test", "onDataChange: "+myProfile.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Checking FBDB", "onCancelled: "+ databaseError.getMessage());
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                super.onFound(message);
                mFoundId = new String(message.getContent());
                //ToDo: The ID should be stored in some way so that the user can access that id's profile. DO THIS SOMEWHERE ELSE!!!!!!
                DatabaseReference strangerRef = mFirebaseDatabase.getReference(mFoundId).child(AppConstants.FIREBASE_USER_CHILD_PROFILE);
                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                        ConnectionsSQLOpenHelper.getInstance(MainActivity.this).addNewConnection(strangerProfile);
                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile);
                        mProfileRecAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Did not load correctly", Toast.LENGTH_SHORT).show();
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });
                mDisplayText.setText(mFoundId);
                //ToDo: After storing the ID retrieve the profile data from FBDB and add to ConnectionsHelper  && local db.
                Toast.makeText(MainActivity.this, mFoundId, Toast.LENGTH_SHORT).show();


                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                Toast.makeText(MainActivity.this, "Lost the signal", Toast.LENGTH_SHORT).show();

                //ToDo: Do a count that removes found users, to keep track of active publishing users.

            }

            @Override
            public void onDistanceChanged(Message message, Distance distance) {
                super.onDistanceChanged(message, distance);
            }

            @Override
            public void onBleSignalChanged(Message message, BleSignal bleSignal) {
                super.onBleSignalChanged(message, bleSignal);
                String received = new String(message.getContent());
                mDisplayText.setText(received);
                Toast.makeText(MainActivity.this, received, Toast.LENGTH_SHORT).show();

            }
        };

        mSendButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mActiveMessage = new Message(mEditText.getText().toString().getBytes());
//                mProfile = new Profile(mEditText.getText().toString(),
//                        mBioEntry.getText().toString(),
//                        mDobEntry.getText().toString(),
//                        null,
//                        null,
//                        null);
                mRef.setValue(mProfile);
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();

                //ToDo: Clear users information in SharedPref and Database, unless if I keep database info tied to specific Users.
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
//                ConnectionsSQLOpenHelper.getInstance(MainActivity.this).clearDatabase();
                Intent intent = new Intent(MainActivity.this, FirebaseLogInActivity.class);
                startActivity(intent);
            }
        });


        //ToDo: After making prototype, instead of button, probably using a recyclerView, populate the profile blurbs and add onClickListeners to that.
        mRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, ProfileDetailActivity.class);
                intent.putExtra(FOUND_ID_INTENT,mFoundId);//ToDo: Once I have some way of displaying each Id's blurb, this will take in the id for that profile to be passed to the detail activity.
                startActivity(intent);
            }
        });

        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(MainActivity.this, SelfProfileActivity.class);
                startActivity(intent);
            }
        });

    }


    public void findViews(){
        mSendButt = (Button)findViewById(R.id.send_butt);
        mRetrieveButton = (Button)findViewById(R.id.retrieve_button);
        mDisplayText = (TextView)findViewById(R.id.display_text);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mSignOutButton = (Button)findViewById(R.id.sign_out_button);
        mProfileButton = (Button)findViewById(R.id.self_profile_button);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
    }

    public void setUpRecyclerView(){
        mRecyclerView = (RecyclerView)findViewById(R.id.profiles_recyclerview);

        mProfileRecAdapter = new ProfileCollectionRecyclerAdapter(ConnectionsHelper.getInstance().getConnections());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mProfileRecAdapter);
    }


    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        NearbyManager.getInstance().setGoogleApiConnected(true);
        publish();
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        NearbyManager.getInstance().setGoogleApiConnected(false);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.connect();
        }
    }

    public void publish(){
        mFindMeMessage = new Message(mId.getBytes());
        if(mNearbyManager.isGoogleApiConnected()){
            Nearby.Messages.publish(mGoogleApiClient, mFindMeMessage);
            mNearbyManager.setPublishing(true);
        }else{
            Toast.makeText(this, "Not connected to Google Cloud", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "publish: failed");
        }
    }
    public void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mActiveListener);
        mNearbyManager.setSubscribing(true);
    }


    @Override
    protected void onDestroy() {
        //ToDo: Find out if this is even necessary
//        if(mNearbyManager.isPublishing()){
//            Nearby.Messages.unpublish(mGoogleApiClient,mFindMeMessage);
//        }
//        if(mNearbyManager.isSubscribing()){
//            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
//        }
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }
}
