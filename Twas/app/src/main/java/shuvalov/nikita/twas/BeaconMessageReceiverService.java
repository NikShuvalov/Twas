package shuvalov.nikita.twas;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import shuvalov.nikita.twas.Activities.FirebaseLogInActivity;
import shuvalov.nikita.twas.Helpers_Managers.ConnectionsSQLOpenHelper;
import shuvalov.nikita.twas.PoJos.ChatMessage;

/**
 * Created by NikitaShuvalov on 1/5/17.
 */

public class BeaconMessageReceiverService extends IntentService {
    private static final int MESSAGES_NOTIFICATION_ID = 1;

    public BeaconMessageReceiverService() {
        super("BeaconMessageReceiverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent!=null){
            Nearby.Messages.handleIntent(intent, new MessageListener() {
                @Override
                public void onFound(Message message) {
                    ChatMessage chatMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                    long currentMissed = ConnectionsSQLOpenHelper.getInstance(BeaconMessageReceiverService.this).addMissedConnection(chatMessage);
                    updateNotification(currentMissed);
                }

                @Override
                public void onLost(Message message) {
                    super.onLost(message);
                }

                @Override
                public void onBleSignalChanged(Message message, BleSignal bleSignal) {
                    ChatMessage chatMessage = ChatMessage.getSoapBoxMessageFromBytes(message.getContent());
                    long currentMissed = ConnectionsSQLOpenHelper.getInstance(BeaconMessageReceiverService.this).addMissedConnection(chatMessage);
                    updateNotification(currentMissed);
                    Log.d("Intent Service Test", "Found this many: "+ currentMissed);
                }
            });
        }
    }
    public void updateNotification(long missedMessages){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent launchIntent = new Intent(getApplicationContext(), FirebaseLogInActivity.class);//FixMe: Will have to change to launcher activity
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent turnOffIntent = new Intent(this, BeaconMessageReceiverService.class);
        PendingIntent turnOff = PendingIntent.getService(this, 0, turnOffIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(this);
        noteBuilder.setContentTitle("Nearby currently Scanning")
                .setContentText(missedMessages+" missed connections/messages. But don't worry you can view them once you start the app up.")
                .setOngoing(true)
                .setContentIntent(turnOff)
                .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
                .setSubText("Click to turn off scan");
        notificationManager.notify(MESSAGES_NOTIFICATION_ID, noteBuilder.build());
    }
}
