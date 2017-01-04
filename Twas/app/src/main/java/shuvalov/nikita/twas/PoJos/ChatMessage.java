package shuvalov.nikita.twas.PoJos;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatMessage {
    String userId; //THe user that submitted this message
    String roomID;  //The room to which this message was submitted
    String content; //The content of the message.
    long timeStamp; //The time the message was posted.

    public ChatMessage(String userId, String roomId, String content, long timeStamp) {
        this.userId = userId;
        this.roomID = roomId;
        this.content = content;
        this.timeStamp = timeStamp;
    }

    /**
     * This constructor will put in the current time.
     *
     * @param userId
     * @param roomId
     * @param content
     */
    public ChatMessage(String userId, String roomId, String content){
        this(userId, roomId, content, Calendar.getInstance().getTimeInMillis());
    }

    /**
     * This constructor is used specifically for my soapBoxMessages.
     * Doesn't require a roomId and will use current timeinMillis as timestamp.
     *
     * @param userId The Id of the user sending it.
     * @param content The Message String to be sent.
     */
    public ChatMessage(String userId, String content){
        this(userId, null, content);
    }

    /**
     * This constructor is used by the FirebaseDatabase Api.
     */
    public ChatMessage(){}

    public String getUserId() {
        return this.userId;
    }

    public String getRoomID() {
        return this.roomID;
    }

    public String getContent() {
        return this.content;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public static byte[] getBytesForSoapBox(ChatMessage chatMessage){
        String timeStampAsString = String.valueOf(chatMessage.getTimeStamp());
        String split = AppConstants.SOAPBOX_MESSAGE_DELIMITER;
        //[0] = uid, [1] = message, [2] = Timestamp
        String chatMessageAsString = String.format("%s"+split+"%s"+split+" %s",
                chatMessage.getUserId(),
                chatMessage.getContent(),
                timeStampAsString);
        return chatMessageAsString.getBytes();
    }

    public static ChatMessage getSoapBoxMessageFromBytes(byte[] bytes){
        String soapBoxAsString = null;
        try {
            soapBoxAsString = new String (bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(soapBoxAsString!=null){
            String[] soapBoxParams = soapBoxAsString.split(AppConstants.SOAPBOX_MESSAGE_DELIMITER);
            long timeStamp = Long.parseLong(soapBoxParams[2]);
            return new ChatMessage(soapBoxParams[0],null, soapBoxParams[1],timeStamp);
        }
        Log.d("SoapBoxMessage", "SoapBoxMessage didn't parse correctly");
        return new ChatMessage();
    }
}
