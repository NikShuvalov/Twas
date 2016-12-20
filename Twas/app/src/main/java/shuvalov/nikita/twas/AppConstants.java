package shuvalov.nikita.twas;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class AppConstants {
    public static final String SELF_USER_ID = "Self User ID";

    public static final String FIREBASE_IMAGE_BUCKET = "gs://twas-fef4a.appspot.com";
    public static final String FIREBASE_USER_PROFILE_IMAGE = "images/%s-profile-pic.jpg";
    public static final long FIREBASE_MAX_PHOTO_SIZE = 10*1024*1024; //n number of megabytes where n(1024^2)

    public static final String ORIGIN_ACTIVITY = "where is intent coming from?";
    public static final String ORIGIN_MAIN = "main activity";
    public static final String ORIGIN_LOG_IN= "login activity";

    public static final String PREF_SELF_USER_PROFILE = "Self_user_profile";
    public static final String PREF_ID = "Self_id";
    public static final String PREF_NAME ="Self_name";
    public static final String PREF_DOB = "Self_dob";
    public static final String PREF_BIO = "Self_bio";
    public static final String PREF_GENDER = "Self_gender";
    public static final String PREF_PICURL = "Self_picurl";


}
