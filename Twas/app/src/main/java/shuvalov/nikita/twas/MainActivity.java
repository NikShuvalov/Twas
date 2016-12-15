package shuvalov.nikita.twas;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    Message mActiveMessage;
    MessageListener mActiveListener;

    Button mSendButt, mRetrieveButton;
    EditText mEditText, mBioEntry, mDobEntry;
    TextView mDisplayText;
    boolean googleAPIconnected, publishing, subscribing;



    Profile mProfile;
    DatabaseReference mRef;
    FirebaseDatabase mFirebaseDatabase;

    private static final String MESSAGES_API_KEY = "AIzaSyB19mT541M39gddQhee5ehQy45G3FpV3MU";
    public static final String HTC_PHONE_ANDROID_ID = "41cc7ed0cfee3d4c";
    public static final String SAMSUNG_PHONE_ANDROID_ID = "669ec9813b4c140";

    String mId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mRef = mFirebaseDatabase.getReference(mId);

        googleAPIconnected=false;
        publishing=false;
        subscribing=false;

        mSendButt = (Button)findViewById(R.id.send_butt);
        mRetrieveButton = (Button)findViewById(R.id.retrieve_button);
        mEditText = (EditText)findViewById(R.id.enter_text);
        mDisplayText = (TextView)findViewById(R.id.display_text);
        mDobEntry = (EditText)findViewById(R.id.dob_entry);
        mBioEntry = (EditText)findViewById(R.id.bio_entry);


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
                String received = new String(message.getContent());
                mDisplayText.setText(received);
                Toast.makeText(MainActivity.this, received, Toast.LENGTH_SHORT).show();
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
                mActiveMessage = new Message(mEditText.getText().toString().getBytes());
                mProfile = new Profile(mEditText.getText().toString(),mBioEntry.getText().toString(),mDobEntry.getText().toString());
                mRef.setValue(mProfile);
            }
        });

        mRetrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, ProfileDetailActivity.class);
                startActivity(intent);


                //Go to Next Activity
                //Retrieve Profile data
                //AsyncTask in new activity
                //Make a class to hold a profile or singleton that holds all the profiles you have access to.
            }
        });

    }


    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        googleAPIconnected=true;
        publish();
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleAPIconnected=false;

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
        Message message = new Message(mId.getBytes());
        if(googleAPIconnected){
            Nearby.Messages.publish(mGoogleApiClient, message);
            publishing=true;
        }else{
            Toast.makeText(this, "Not connected to Google Cloud", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "publish: failed");
        }
    }
    public void subscribe(){
        Nearby.Messages.subscribe(mGoogleApiClient, mActiveListener);
        subscribing=true;
    }

    @Override
    protected void onStop() {
        if(publishing){
            Nearby.Messages.unpublish(mGoogleApiClient,mActiveMessage);
        }
        if(subscribing){
            Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
        }
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}
