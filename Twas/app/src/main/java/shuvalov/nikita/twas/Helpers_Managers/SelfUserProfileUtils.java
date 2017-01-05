package shuvalov.nikita.twas.Helpers_Managers;

import android.content.Context;
import android.content.SharedPreferences;

import shuvalov.nikita.twas.AppConstants;
import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/20/16.
 */

public class SelfUserProfileUtils {

    /**
     * Adds user's profile information to shared preferences.
     * @param context Activity context
     * @param profile Self-User's profile
     * @return True if added to sharedPreferences successfully.
     */
    public static boolean assignProfileToSharedPreferences(Context context, Profile profile){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, Context.MODE_PRIVATE);
         return sharedPreferences.edit()
                .putString(AppConstants.PREF_ID,profile.getUID())
                .putString(AppConstants.PREF_NAME, profile.getName())
                .putString(AppConstants.PREF_BIO, profile.getBio())
                .putLong(AppConstants.PREF_DOB, profile.getDOB())
                .putString(AppConstants.PREF_GENDER, profile.getGender())
                .commit();
    }

//    /**
//     * Adds user's picUrl in case it didn't exist when the profile was initially put into sharedPreferences.
//     *
//     * @param context
//     * @param picUrl
//     * @return True if url add successfully.
//     */
//    public static boolean addUserProfilePicUrl(Context context, String picUrl){
//        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).edit().putString(AppConstants.PREF_PICURL,picUrl).commit();
//    }

    /**
     * Clears the user preferences. Called on sign-out.
     * @param context
     * @return True if cleared successfully.
     */
    public static boolean clearUserProfile(Context context){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).edit().clear().commit();
    }

    /**
     * Adds Self-User ID to sharedPreferences.
     *
     * @param context
     * @param uid
     * @return
     */
    public static boolean setUserId(Context context, String uid){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).edit().putString(AppConstants.PREF_ID,uid).commit();
    }

    public static String getUserId(Context context){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, Context.MODE_PRIVATE).getString(AppConstants.PREF_ID,AppConstants.PREF_EMPTY);
    }

    public static boolean compareStoredIdWithCurrentId(Context context, String uid){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).getString(AppConstants.PREF_ID, AppConstants.PREF_EMPTY).equals(uid);
    }

    public static Profile getUsersInfoAsProfile(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, Context.MODE_PRIVATE);
        String selfUID = sharedPreferences.getString(AppConstants.PREF_ID, "");
        String selfName = sharedPreferences.getString(AppConstants.PREF_NAME, "");
        String selfBio = sharedPreferences.getString(AppConstants.PREF_BIO, "");
        long selfDOB = sharedPreferences.getLong(AppConstants.PREF_DOB, 0);
        String selfGender = sharedPreferences.getString(AppConstants.PREF_GENDER, "");
        return new Profile(selfUID, selfName, selfBio,selfDOB, selfGender);
    }

    public static void setNewSoapBoxMessage(Context context, String soapBoxString){
        context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, Context.MODE_PRIVATE).edit().putString(AppConstants.PREF_SOAPBOX_MESSAGE,soapBoxString).commit();
    }

    public static String getSoapBoxMessage(Context context){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE, Context.MODE_PRIVATE).getString(AppConstants.PREF_SOAPBOX_MESSAGE, "");
    }


}
