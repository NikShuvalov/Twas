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
                .putString(AppConstants.PREF_PICURL, profile.getPicURL())
                .commit();
    }


    /**
     * Adds user's picUrl in case it didn't exist when the profile was initially put into sharedPreferences.
     *
     * @param context
     * @param picUrl
     * @return True if url add successfully.
     */
    public static boolean addUserProfilePicUrl(Context context, String picUrl){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).edit().putString(AppConstants.PREF_PICURL,picUrl).commit();
    }

    /**
     * Use this function to clear the user preferences. Happens on sign-out.
     * @param context
     * @return True if cleared successfully.
     */
    public static boolean clearUserPreferences(Context context){
        return context.getSharedPreferences(AppConstants.PREF_SELF_USER_PROFILE,Context.MODE_PRIVATE).edit().clear().commit();
    }
}
