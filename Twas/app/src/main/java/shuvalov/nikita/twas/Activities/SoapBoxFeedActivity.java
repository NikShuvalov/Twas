package shuvalov.nikita.twas.Activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.R;
import shuvalov.nikita.twas.RecyclersAndHolders.ChatMessagesRecyclerAdapter;

public class SoapBoxFeedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Button mSend;
    private EditText mMessageEntry;
    private RecyclerView mRecyclerView;
    private ChatMessagesRecyclerAdapter mMessagesRecyclerAdapter;
    String mSelfId;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap_box_feed);

        mSelfId = SelfUserProfileUtils.getUserId(this);
        findViews();
        recyclerLogic();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addOnConnectionFailedListener(this)
                .build();

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMessageEntry.getText().toString().equals("")){
                    mMessageEntry.setError("Can't send an empty message");
                }else{
                    String shoutOut = mMessageEntry.getText().toString();
                    mMessageEntry.setText("");

                    ChatMessage soapBoxMessage = new ChatMessage(mSelfId, shoutOut);
                    byte[] soapBoxBytes = ChatMessage.getBytesForSoapBox(soapBoxMessage);

                    Message nearByShoutout = new Message(soapBoxBytes);
                    if(NearbyManager.getInstance().isGoogleApiConnected()){
                        PublishOptions blastOptions = new PublishOptions.Builder().setStrategy(
                                new Strategy.Builder().setTtlSeconds(60).build()).build();
                        Nearby.Messages.publish(mGoogleApiClient,nearByShoutout, blastOptions);
                        //Turn this off after some time. Or immediately?
                    }
                    //ToDo: Publish this! Blast it out for everyone near by to hear.

                }
            }
        });
    }

    public void findViews(){
        mSend = (Button)findViewById(R.id.send_butt);
        mMessageEntry = (EditText)findViewById(R.id.message_entry);
        mRecyclerView = (RecyclerView)findViewById(R.id.soapbox_feed_recycler);
    }

    public void recyclerLogic(){
        mMessagesRecyclerAdapter = new ChatMessagesRecyclerAdapter(ConnectionsSQLOpenHelper.getInstance(this).getSoapBoxMessages(),mSelfId);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);

        mRecyclerView.setAdapter(mMessagesRecyclerAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        NearbyManager.getInstance().setGoogleApiConnected(true);
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
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onDestroy() {
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();

    }
}
