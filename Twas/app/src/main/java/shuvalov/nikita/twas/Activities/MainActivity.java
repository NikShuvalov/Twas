package shuvalov.nikita.twas.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.R;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String FOUND_ID_INTENT = "Found id";

    Toolbar mToolbar;
    Button mSendButt, mRetrieveButton, mSignOutButton, mProfileButton;
    EditText mEditText, mBioEntry, mDobEntry;
    TextView mDisplayText;
    String mFoundId;

    NearbyManager mNearbyManager;

    GoogleApiClient mGoogleApiClient;
    Message mFindMeMessage;
//    Message mActiveMessage;
    MessageListener mActiveListener;


    Profile mProfile;
    DatabaseReference mRef;
    FirebaseDatabase mFirebaseDatabase;


    //ToDo: Remove these three
    private static final String MESSAGES_API_KEY = "AIzaSyB19mT541M39gddQhee5ehQy45G3FpV3MU";
    public static final String HTC_PHONE_ANDROID_ID = "41cc7ed0cfee3d4c";
    public static final String SAMSUNG_PHONE_ANDROID_ID = "669ec9813b4c140";

    String mId;
//    String[] navOptions= new String[]{"Profile","Home", "Settings","SoapBox Feed","Invite Friends", "Donate", "About"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNearbyManager = NearbyManager.getInstance();

        Log.d("MainActivity", "onCreate: "+ mNearbyManager.getSelfID());

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mId = getIntent().getStringExtra(AppConstants.SELF_USER_ID);

        mRef = mFirebaseDatabase.getReference(mId);

        findViews();


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
                //ToDo: The ID should be stored in some way so that the user can access that id's profile.
                mDisplayText.setText(mFoundId);
                //ToDo: After storing the ID retrieve the profile data from FBDB and add to ConnectionsHelper
                Toast.makeText(MainActivity.this, mFoundId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                Toast.makeText(MainActivity.this, "Lost the signal", Toast.LENGTH_SHORT).show();
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
                mProfile = new Profile(mEditText.getText().toString(),
                        mBioEntry.getText().toString(),
                        mDobEntry.getText().toString(),
                        null,
                        null);
                mRef.setValue(mProfile);
            }
        });

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
                //ToDo:Drop SQL table
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
        mEditText = (EditText)findViewById(R.id.enter_text);
        mDisplayText = (TextView)findViewById(R.id.display_text);
        mDobEntry = (EditText)findViewById(R.id.dob_entry);
        mBioEntry = (EditText)findViewById(R.id.bio_entry);
        mToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        mSignOutButton = (Button)findViewById(R.id.sign_out_button);
        mProfileButton = (Button)findViewById(R.id.self_profile_button);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
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
