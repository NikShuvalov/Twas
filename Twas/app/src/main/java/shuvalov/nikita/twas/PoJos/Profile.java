package shuvalov.nikita.twas.PoJos;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class Profile {
    private String mUID, mName, mBio, mGender;
//    private String mPicURL;
    private long mDOB;
//    private ArrayList<ChatRoom> mActiveChatRooms;

    //ToDo: Add SoapBox message into profile? Or someplace else?
    public Profile(){
//        mActiveChatRooms = new ArrayList<>();
    }

    public Profile(String uid, String name, String bio, long dobInMillis, String gender){
        mUID = uid;
        mName = name;
        mBio = bio;
        mDOB = dobInMillis;
        mGender = gender;
//        mPicURL = picURL;
//        mActiveChatRooms = new ArrayList<>();
    }

//    public void addActiveChatRoom(ChatRoom chatRoom){
//        mActiveChatRooms.add(chatRoom);
//    }
//    public void addChatRoomList(List<ChatRoom> chatRooms){
//        for (ChatRoom chatroom :chatRooms) {
//            addActiveChatRoom(chatroom);
//        }
//    }

//    public ArrayList<ChatRoom> getActiveChatRooms() {
//        return mActiveChatRooms;
//    }
//
//    public void setActiveChatRooms(ArrayList<ChatRoom> activeChatRooms) {
//        mActiveChatRooms = activeChatRooms;
//    }

    public String getUID() {
        return mUID;
    }

    public String getName() {
        return mName;
    }

    public String getBio() {
        return mBio;
    }

    public long getDOB() {
        return mDOB;
    }

    public String getGender() {
        return mGender;
    }



    public void setUID(String UID) {
        mUID = UID;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setBio(String bio) {
        mBio = bio;
    }

    public void setGender(String gender) {
        mGender = gender;
    }

    //Since the image reference is standardized storing image URL strings is abso-fucking-lutely unnecessary. Just use the UID and formatting.
//    public void setPicURL(String picURL) {
//        mPicURL = picURL;
//    }

//    public String getPicURL() {
//        return mPicURL;
//    }

    public void setDOB(long DOB) {
        mDOB = DOB;
    }
}
