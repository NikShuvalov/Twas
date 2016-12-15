package shuvalov.nikita.twas;

import java.util.ArrayList;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class ProfilesHelper {
    ArrayList<Profile> mStalkedProfiles;

    private static ProfilesHelper mProfilesHelper;

    public static ProfilesHelper getInstance() {
        if (mProfilesHelper!= null){
            mProfilesHelper = new ProfilesHelper();
        }
        return mProfilesHelper;
    }
    private ProfilesHelper(){
        mStalkedProfiles = new ArrayList<>();
    }

    public ArrayList<Profile> getStalkedProfiles() {
        return mStalkedProfiles;
    }
}
