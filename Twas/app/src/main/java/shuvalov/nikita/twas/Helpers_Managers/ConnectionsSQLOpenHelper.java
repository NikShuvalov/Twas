package shuvalov.nikita.twas.Helpers_Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import shuvalov.nikita.twas.PoJos.ChatMessage;
import shuvalov.nikita.twas.PoJos.ChatRoom;
import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class ConnectionsSQLOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CONNECTION_COLLECTION_DB";
    public static final int DATABASE_VERSION = 5;

    public static final String PROFILE_TABLE_NAME = "PROFILE_LIST";
    public static final String CHATMESS_TABLE_NAME = "CHAT_MESSAGES_LIST";
    public static final String CHATROOM_TABLE_NAME = "CHAT_ROOM_LIST";
    public static final String SOAPBOX_TABLE_NAME = "SOAPBOX_MESSAGES_LIST";

    public static final String COLUMN_UID = "UID";
    public static final String COLUMN_NAME = "NAME";
//    public static final String COLUMN_PIC_URL = "PROFILE_PIC_URL";
    public static final String COLUMN_BIO = "BIO_SUMMARY";
    public static final String COLUMN_GENDER = "GENDER";
    public static final String COLUMN_BIRTHDATE = "BIRTH_DATE";

//    public static final String COLUMN_MESSAGE_ID = "_ID";
    public static final String COLUMN_ROOM_ID = "ROOM_ID";
    public static final String COLUMN_ROOM_NAME = "ROOM_NAME";
    public static final String COLUMN_TIMESTAMP = "TIMESTAMP";
    public static final String COLUMN_MESSAGE_CONTENT = "CONTENT";

    //This holds all of the user profiles, including self(? maybe store as sharePreferences).
    public static final String CREATE_PROFILE_TABLE_EXE = "CREATE TABLE "+ PROFILE_TABLE_NAME +
            " ("+ COLUMN_UID +" TEXT PRIMARY KEY,"+
            COLUMN_NAME + " TEXT,"+
            COLUMN_BIO + " TEXT,"+
            COLUMN_GENDER + " TEXT," +
            COLUMN_BIRTHDATE + " INTEGER)";

    //This holds all of the messages relative to the current user.
    //ToDo; Make Timestamp+Uid primary key. See SoapBoxMessages for the HOW.
    public static final String CREATE_CHATMESS_TABLE_EXE = "CREATE TABLE "+ CHATMESS_TABLE_NAME+
            " (" + COLUMN_ROOM_ID+ " TEXT,"+
            COLUMN_TIMESTAMP+ " INTEGER PRIMARY KEY,"+
            COLUMN_UID + " TEXT,"+
            COLUMN_MESSAGE_CONTENT + " TEXT)";

    //This simply keeps the chatmessages segregated by rooms.
    public static final String CREATE_CHATROOM_TABLE_EXE = "CREATE TABLE "+ CHATROOM_TABLE_NAME+
            " ("+ COLUMN_ROOM_ID+ " TEXT PRIMARY KEY,"+
            COLUMN_ROOM_NAME+ " TEXT)";

    public static final String CREATE_SOAPBOX_MESSAGES_TABLE_EXE = "CREATE TABLE "+ SOAPBOX_TABLE_NAME+
            " (" + COLUMN_TIMESTAMP+ " TEXT,"+
            COLUMN_MESSAGE_CONTENT+ " TEXT,"+
            COLUMN_UID+ " TEXT, "+
            "PRIMARY KEY ("+COLUMN_UID+", "+COLUMN_TIMESTAMP+"))";




    private static ConnectionsSQLOpenHelper sConnectionsSQLOpenHelper;

    public static ConnectionsSQLOpenHelper getInstance(Context context) {
        if (sConnectionsSQLOpenHelper == null) {
            sConnectionsSQLOpenHelper = new ConnectionsSQLOpenHelper(context.getApplicationContext());
        }
        return sConnectionsSQLOpenHelper;
    }

    private ConnectionsSQLOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PROFILE_TABLE_EXE);
        sqLiteDatabase.execSQL(CREATE_CHATMESS_TABLE_EXE);
        sqLiteDatabase.execSQL(CREATE_CHATROOM_TABLE_EXE);
        sqLiteDatabase.execSQL(CREATE_SOAPBOX_MESSAGES_TABLE_EXE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ PROFILE_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ CHATMESS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ CHATROOM_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ SOAPBOX_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    //ToDo: Search to retrieve all profiles from database.
    public ArrayList<Profile> getAllConnections(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(PROFILE_TABLE_NAME, null, null, null ,null, null, null);
        ArrayList<Profile> profiles = new ArrayList<>();
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast()){
                String uid = cursor.getString(cursor.getColumnIndex(COLUMN_UID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String bio = cursor.getString(cursor.getColumnIndex(COLUMN_BIO));
                String gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER));
                long dob = cursor.getLong(cursor.getColumnIndex(COLUMN_BIRTHDATE));
                profiles.add(new Profile(uid, name, bio, dob, gender));
                cursor.moveToNext();
            }

        }
        cursor.close();
        db.close();
        return profiles;
    }

    public Profile getConnectionById(String uid){
        Profile foundProfile;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(PROFILE_TABLE_NAME,null, COLUMN_UID+ " = ?",new String[]{uid},null,null,null);
        if(cursor.moveToFirst()){
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            String bio = cursor.getString(cursor.getColumnIndex(COLUMN_BIO));
            String gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER));
            long dob = cursor.getLong(cursor.getColumnIndex(COLUMN_BIRTHDATE));
            foundProfile = new Profile(uid, name, bio, dob, gender);
            return foundProfile;
        }
        return null;
    }

    //Clears database, idea is if user logs out, I wouldn't want other users on this phone to have access to that data.
    public void clearDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(PROFILE_TABLE_NAME, null, null);
        db.delete(CHATMESS_TABLE_NAME, null, null);
        db.delete(CHATROOM_TABLE_NAME, null, null);
        db.close();

    }

    //Adds a new profile connection to db. Gets called when user receives token from another phone.
    public long addNewConnection(Profile profile){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(COLUMN_UID, profile.getUID());
        content.put(COLUMN_NAME, profile.getName());
        content.put(COLUMN_BIO, profile.getBio());
        content.put(COLUMN_GENDER, profile.getGender());
        content.put(COLUMN_BIRTHDATE, profile.getDOB());

        long row=db.insertWithOnConflict(PROFILE_TABLE_NAME, null, content, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return row;//ToDo: Not sure why I'm returning a value, but might serve useful. (Remove if proves unnecessary)
    }

    //Adds a collection of userProfile connections to db. Gets called on log-in, if database is empty.
    public int addCollection(List<Profile> connections){
        int addedAmount = 0;
        SQLiteDatabase db = getWritableDatabase();
        for(Profile profile: connections){
            ContentValues content = new ContentValues();
            content.put(COLUMN_UID, profile.getUID());
            content.put(COLUMN_NAME, profile.getName());
            content.put(COLUMN_BIO, profile.getBio());
            content.put(COLUMN_GENDER, profile.getGender());
            content.put(COLUMN_BIRTHDATE, profile.getDOB());
            db.insertWithOnConflict(PROFILE_TABLE_NAME, null, content, SQLiteDatabase.CONFLICT_REPLACE); //ToDo: Consider changing this to updateWithOnConflict
            addedAmount++;
        }
        db.close();
        return addedAmount;
    }

    //Updates values, doesn't update UID or PIC_URL as that is immutable.
    public void updateUserInfo(Profile profile){
        SQLiteDatabase db = getWritableDatabase();


        ContentValues content = new ContentValues();
        content.put(COLUMN_NAME, profile.getName());
        content.put(COLUMN_BIO, profile.getBio());
        content.put(COLUMN_GENDER, profile.getGender());
        content.put(COLUMN_BIRTHDATE, profile.getDOB());

        String selection = COLUMN_UID+ " = ?";
        db.update(PROFILE_TABLE_NAME, content, selection, new String[]{profile.getUID()});
        db.close();
    }

    //Adds a single chatRoom to table, called upon creation of a chatroom
    public void addChatRoom(ChatRoom chatRoom){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put(COLUMN_ROOM_ID, chatRoom.getId());
        content.put(COLUMN_ROOM_NAME, chatRoom.getRoomName());
        db.insert(CHATROOM_TABLE_NAME, null, content);
        db.close();
    }

//    public ArrayList<ChatRoom> getChatRooms(){
//        ArrayList<ChatRoom> chatRooms = new ArrayList<>();
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = db.query(CHATROOM_TABLE_NAME,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null);
//        if(cursor.moveToFirst()){
//            while(!cursor.isAfterLast()){
//                cursor.get
//                cursor.moveToNext();
//            }
//        }
//    }
    //Adds collection of chatrooms. Called upon logging in and retrieving from FBDB.
    public void addAllChatRooms(List<ChatRoom> chatRooms){
        SQLiteDatabase db = getWritableDatabase();

        for(ChatRoom chatRoom: chatRooms){
            ContentValues content = new ContentValues();
            content.put(COLUMN_ROOM_ID, chatRoom.getId());
            content.put(COLUMN_ROOM_NAME, chatRoom.getRoomName());
            db.insert(CHATROOM_TABLE_NAME, null, content);
        }
        db.close();

    }
    public void addMessage(ChatMessage chatMessage){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();

        content.put(COLUMN_ROOM_ID, chatMessage.getRoomID());
        content.put(COLUMN_TIMESTAMP, chatMessage.getTimeStamp());
        content.put(COLUMN_UID, chatMessage.getUserId());
        content.put(COLUMN_MESSAGE_CONTENT, chatMessage.getContent());
        db.insertWithOnConflict(CHATMESS_TABLE_NAME, null, content,SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void addAllMessages(List<ChatMessage> chatMessages){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        for (ChatMessage chatMessage: chatMessages){
            content.put(COLUMN_ROOM_ID, chatMessage.getRoomID());
            content.put(COLUMN_TIMESTAMP, chatMessage.getTimeStamp());
            content.put(COLUMN_UID, chatMessage.getUserId());
            content.put(COLUMN_MESSAGE_CONTENT, chatMessage.getContent());

            db.insert(CHATMESS_TABLE_NAME, null, content);
        }

        db.close();
    }

    public void addSoapBoxMessage(ChatMessage soapBoxMessage){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(COLUMN_UID, soapBoxMessage.getUserId());
        String timeStampString = String.valueOf(soapBoxMessage.getTimeStamp());
        content.put(COLUMN_TIMESTAMP, timeStampString);
        content.put(COLUMN_MESSAGE_CONTENT, soapBoxMessage.getContent());

        db.insertWithOnConflict(SOAPBOX_TABLE_NAME, null, content,SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void addSoapBoxMessageList(ArrayList<ChatMessage> soapBoxMessages){
        SQLiteDatabase db = getWritableDatabase();
        for(ChatMessage soapBoxMessage: soapBoxMessages){
            ContentValues content = new ContentValues();
            content.put(COLUMN_UID, soapBoxMessage.getUserId());
            String timeStampString = String.valueOf(soapBoxMessage.getTimeStamp());
            content.put(COLUMN_TIMESTAMP, timeStampString);
            content.put(COLUMN_MESSAGE_CONTENT, soapBoxMessage.getContent());

            db.insert(SOAPBOX_TABLE_NAME, null, content);
        }

        db.close();
    }

    public ArrayList<ChatMessage> getSoapBoxMessages(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(SOAPBOX_TABLE_NAME, null, null, null, null, null, null);
        ArrayList<ChatMessage> soapBoxMessages = new ArrayList<>();
        int count = 0;
        if(cursor.moveToFirst()){
            while(!cursor.isAfterLast() && count<=100){
                count++;
                String userId = cursor.getString(cursor.getColumnIndex(COLUMN_UID));
                String timeStamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                String messageContent = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE_CONTENT));
                long longTime = Long.parseLong(timeStamp);

                soapBoxMessages.add(new ChatMessage(userId,null,messageContent,longTime));
                cursor.moveToNext();
            }
        }
        db.close();
        if(cursor.getCount()>250){
            messageHouseKeeping(SOAPBOX_TABLE_NAME, soapBoxMessages);
        }
        cursor.close();
        return soapBoxMessages;
    }


    private void messageHouseKeeping(String tableName, ArrayList<ChatMessage> keptList){
        SQLiteDatabase db = getWritableDatabase();
        if(tableName.equals(SOAPBOX_TABLE_NAME)){ //Might add this to private conversations as well, so keeping it general for now.
            db.execSQL("DROP TABLE IF EXISTS "+ tableName);
            addSoapBoxMessageList(keptList);
        }
        db.close();

    }

    //ToDo: Join search for ChatMessages that are relevant to each room.


}