package shuvalov.nikita.twas.PoJos;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import shuvalov.nikita.twas.AppConstants;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatMessage implements Serializable{
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
        Log.d("LIT", "Sent TimeStamp: " + timeStampAsString);
        String split = AppConstants.SOAPBOX_MESSAGE_DELIMITER;
        //[0] = uid, [1] = message, [2] = Timestamp
        String chatMessageAsString = String.format("%s"+split+"%s"+split+"%s",
                chatMessage.getUserId(),
                chatMessage.getContent(),
                timeStampAsString);
        return chatMessageAsString.getBytes();
    }

    //ToDo: Serialize it into JsonArray, and Deserialize from that JsonArray if time-permits.
//    public static ChatMessage getSoapBoxMessageFromBytesJson(byte[] bytes){
//        JSONArray jsonArray;
//        try {
//            jsonArray = new JSONArray(new String(bytes));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ChatMessage chatMessage = jsonArray.get
//    }

    public static ChatMessage getSoapBoxMessageFromBytes(byte[] bytes){
        String soapBoxAsString = new String(bytes);
        Log.d("Log all the things", soapBoxAsString);


        if(!soapBoxAsString.isEmpty()){
            String[] soapBoxParams = soapBoxAsString.split(AppConstants.SOAPBOX_MESSAGE_DELIMITER);
            Log.d("Log all the things", soapBoxParams[0]);
            Log.d("Log all the things", soapBoxParams[1]);
            Log.d("Log all the things", soapBoxParams[2]);

            long timeStamp = Long.parseLong(soapBoxParams[2]);
            Log.d("LIT", "Retrieved TimeStamp: "+timeStamp);
            return new ChatMessage(soapBoxParams[0],null, soapBoxParams[1],timeStamp);
        }
        Log.d("SoapBoxMessage", "SoapBoxMessage didn't parse correctly");
        return new ChatMessage();
    }
}
