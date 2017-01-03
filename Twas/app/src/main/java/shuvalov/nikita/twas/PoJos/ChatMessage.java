package shuvalov.nikita.twas.PoJos;

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
}
