package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class ProfilesHelper {
    ArrayList<Profile> mProfileConnections;

    private static ProfilesHelper mProfilesHelper;

    public static ProfilesHelper getInstance() {
        if (mProfilesHelper!= null){
            mProfilesHelper = new ProfilesHelper();
        }
        return mProfilesHelper;
    }
    private ProfilesHelper(){
        mProfileConnections = new ArrayList<>();
    }

    public ArrayList<Profile> getConnections() {
        return mProfileConnections;
    }

    public void addProfileToCollection(Profile profile){
        mProfileConnections.add(profile);
    }
}
