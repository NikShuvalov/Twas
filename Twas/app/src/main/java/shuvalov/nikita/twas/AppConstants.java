package shuvalov.nikita.twas;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class AppConstants {
    public static final String SELF_USER_ID = "Self User ID";

    public static final String FIREBASE_IMAGE_BUCKET = "gs://twas-fef4a.appspot.com";
    public static final String FIREBASE_USER_PROFILE_IMAGE = "images/%s-profile-pic.jpg"; //Use in Conjunction with String.format{uid};
    public static final long FIREBASE_MAX_PHOTO_SIZE = 10*1024*1024; //n number of megabytes where n(1024^2)
    public static final String FIREBASE_USER_CHILD_PROFILE= "Profile";
    public static final String FIREBASE_USER_CHILD_CHATROOMS = "Chatrooms";
    public static final String FIREBASE_USER_CHILD_CONNECTIONS = "Connections";
    public static final String FIREBASE_CHATROOMS = "Chatrooms";
    public static final String FIREBASE_CHATROOM_MESSSAGES = "Messages";
    public static final String FIREBASE_USERS = "Users";

    public static final String ORIGIN_ACTIVITY = "where is intent coming from?";
    public static final String ORIGIN_MAIN = "main activity";
    public static final String ORIGIN_LOG_IN= "login activity";
    public static final String ORIGIN_CHATROOMS = "chatrooms activity";
    public static final String ORIGIN_PROFILE_DETAIL = "profile detail activity";
    public static final String ORIGIN_SOAPBOX_FEED = "coming from soapbox activity";

    public static final String PREF_SELF_USER_PROFILE = "Self_user_profile";
    public static final String PREF_ID = "Self_id";
    public static final String PREF_NAME ="Self_name";
    public static final String PREF_DOB = "Self_dob";
    public static final String PREF_BIO = "Self_bio";
    public static final String PREF_GENDER = "Self_gender";
//    public static final String PREF_PICURL = "Self_picurl";
    public static final String PREF_CHATROOM = "Chatroom";
    public static final String PREF_SOAPBOX_MESSAGE = "My SoapBoxMessage";
    public static final String PREF_HELPER_POSITION = "Position in singleton";

    public static final String PREF_EMPTY = "Duh-dah, duh-dah, duhhh! For whom the bell tolls!";

    public static final String USER_ID = "Id";

    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int TAKE_IMAGE_REQUEST = 2;
//
//    public static final int SIGN_UP_REQUESTCODE= 666;
//    public static final String SIGN_UP_USERNAME="user's user'sname";
//    public static final String SIGN_UP_PASSWORD = "";

    public static final String SOAPBOX_MESSAGE_DELIMITER = "Œ&œ§×Å¾¶¤";

}
