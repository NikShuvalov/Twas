package shuvalov.nikita.twas;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import shuvalov.nikita.twas.Helpers_Managers.ConnectionsHelper;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.Helpers_Managers.FirebaseDatabaseUtils;
import shuvalov.nikita.twas.Helpers_Managers.NearbyManager;
import shuvalov.nikita.twas.Helpers_Managers.SelfUserProfileUtils;
import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.Profile;
import shuvalov.nikita.twas.RecyclersAndHolders.ProfileCollectionRecyclerAdapter;


/**
 * Created by NikitaShuvalov on 1/4/17.
 */

public class NearbyMessagesService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Handler mHandler;
    private String mSelfId;
    MessageListener mActiveListener; //This will have to be fed through another class.
    ProfileCollectionRecyclerAdapter mProfileRecAdapter; //This will have to be fed through another class*

    com.google.android.gms.nearby.messages.Message mActivemessage;

    private Message mHandlerMessage;

    GoogleApiClient mGoogleApiClient;

    public NearbyMessagesService(){
        mSelfId = SelfUserProfileUtils.getUserId(this);
        mProfileRecAdapter = ConnectionsHelper.getInstance().getProfileCollectionRecyclerAdapter();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Service", "onCreate Called ");
        HandlerThread handlerThread = new HandlerThread("MessagesHandler");
        handlerThread.start();


        Looper looper = handlerThread.getLooper();
        mHandler = new Handler(looper){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.obj.equals(AppConstants.START_PUBSUB)){
                    publish();
                    Nearby.Messages.subscribe(mGoogleApiClient,mActiveListener);
                }else if (msg.obj.equals(AppConstants.STOP_PUBSUB)){
                    Nearby.Messages.unpublish(mGoogleApiClient,mActivemessage);
                    Nearby.Messages.unsubscribe(mGoogleApiClient,mActiveListener);
                }
            }
        };

        mActiveListener = new MessageListener() {
            @Override
            public void onFound(com.google.android.gms.nearby.messages.Message message) {
                super.onFound(message);
                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if(!soapBoxMessage.getContent().equals("")){
                    ConnectionsSQLOpenHelper.getInstance(NearbyMessagesService.this).addSoapBoxMessage(soapBoxMessage);
                }
                String foundId = soapBoxMessage.getUserId();
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

                //ToDo: Figure out what I can store as a value for the stranger Connections, maybe a counter?
                //Idea: If using a counter 0-10 encounters = Stranger, 11-25 Familiar, 26-50 Regular, 51-99 Acquaintance,100-499 Friendly, 500+ whatever
                DatabaseReference selfConnectionRef = FirebaseDatabaseUtils.getUserProfileRef(firebaseDatabase,mSelfId);
                selfConnectionRef.child(soapBoxMessage.getUserId()).setValue(soapBoxMessage.getUserId()); //Adds stranger's UID to user's connectionsList.

                DatabaseReference strangerRef = FirebaseDatabaseUtils.getUserProfileRef(firebaseDatabase, foundId);

                //Gets the stranger's profile information.
                strangerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Profile strangerProfile = dataSnapshot.getValue(Profile.class);
                        ConnectionsSQLOpenHelper.getInstance(NearbyMessagesService.this).addNewConnection(strangerProfile); //Adds Stranger's info to local SQL DB.
                        ConnectionsHelper.getInstance().addProfileToCollection(strangerProfile); //Adds Stranger's info to Singleton.
                        mProfileRecAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("MainActivity", "on heard signal, failed attempt to D/L profile ");
                    }
                });

                //ToDo: Do a count that adds found users, to keep track of active publishing users.
            }

            @Override
            public void onLost(com.google.android.gms.nearby.messages.Message message) {
                super.onLost(message);

                //ToDo: Do a count that removes found users, to keep track of active publishing users.

            }

            @Override
            public void onDistanceChanged(com.google.android.gms.nearby.messages.Message
                                                  message, Distance distance) {
                super.onDistanceChanged(message, distance);
            }

            @Override
            public void onBleSignalChanged(com.google.android.gms.nearby.messages.Message
                                                   message, BleSignal bleSignal) {
                super.onBleSignalChanged(message, bleSignal);

                ChatMessage soapBoxMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                if(!soapBoxMessage.getContent().equals("")){
                    ConnectionsSQLOpenHelper.getInstance(NearbyMessagesService.this).addSoapBoxMessage(soapBoxMessage);
                }

                //ToDo: I think I need to put SoapBoxRecyclerAdapter here in order to update any changes.
                //ToDo: On second thought, this service is created on MainActivity and that activity doesn't have a reference to the SoapbBox adapter.
                // ToDo: Unless if I decide to create it in MainActivity and inject into both soapbox activity and this service.

            }
        };

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = mHandler.obtainMessage();
        switch(intent.getStringExtra(AppConstants.SERVICE_INTENT)){
            case AppConstants.START_PUBSUB:
                message.obj = AppConstants.START_PUBSUB;
                break;
            case AppConstants.STOP_PUBSUB:
                message.obj = AppConstants.STOP_PUBSUB;
                break;
            case AppConstants.SHOUTOUT_PUBLISH:
                message.obj = intent.getStringExtra(AppConstants.SHOUTOUT_MESSAGE);
                break;
        }
        mGoogleApiClient.connect();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void publish(){
        Log.d("Service", "publish: called");
        Intent intent = new Intent (this, NearbyMessagesService.class);
        PendingIntent stopSelf = PendingIntent.getService(this,0,intent, PendingIntent.FLAG_CANCEL_CURRENT);
        mActivemessage = new com.google.android.gms.nearby.messages.Message(ChatMessage.getBytesForSoapBox(new ChatMessage(mSelfId,"")));
        Nearby.Messages.publish(mGoogleApiClient, mActivemessage);
        NearbyManager.getInstance().setPublishing(true);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Twas is active")
                .setContentText("Click Here To Turn Off Discovery")
                .setOngoing(true)
                .setContentIntent(stopSelf)
                .build();
        }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mHandler.sendMessage(mHandlerMessage);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
