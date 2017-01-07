package shuvalov.nikita.twas.PoJos;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class ChatRoom {
    String id, roomName; //Room ID is a unique identifier for the room, room name is the nickname for the room.
    ArrayList<String> userIds;
    public long unreadMessages;

    public static final String DEFAULT_NAME = "Generic Room"; //I want to use a random name generator. Just have to figure out the words to use.

    public ChatRoom(String id, @Nullable String roomName) {
        this.id = id;
        this.userIds = new ArrayList<>();
        if(roomName!=null){
            this.roomName = roomName;
        }else{
            this.roomName=DEFAULT_NAME;
        }
    }
    public ChatRoom(){
        this.userIds = new ArrayList<>();
        this.roomName = DEFAULT_NAME;
        unreadMessages = 0;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public void addUserToChatroom(String uid){
        userIds.add(uid);
    }

    public ArrayList<String> getUserIds() {
        return userIds;
    }
}
