package shuvalov.nikita.twas.PoJos;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatMessage {
    String mUserID; //THe user that submitted this message
    String mRoomID;  //The room to which this message was submitted
    String mContent; //The content of the message.
    long mTimeStamp; //The time the message was posted.

    public ChatMessage(String userID, String roomID, String content, long timeStamp) {
        mUserID = userID;
        mRoomID = roomID;
        mContent = content;
        mTimeStamp = timeStamp;
    }

    public ChatMessage(){}

    public String getUserID() {
        return mUserID;
    }

    public String getRoomID() {
        return mRoomID;
    }

    public String getContent() {
        return mContent;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }
}
