package shuvalov.nikita.twas;

import java.util.ArrayList;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class ProfilesHelper {
    ArrayList<Profile> mProfileCollection;

    private static ProfilesHelper mProfilesHelper;

    public static ProfilesHelper getInstance() {
        if (mProfilesHelper!= null){
            mProfilesHelper = new ProfilesHelper();
        }
        return mProfilesHelper;
    }
    private ProfilesHelper(){
        mProfileCollection = new ArrayList<>();
    }

    public ArrayList<Profile> getStalkedProfiles() {
        return mProfileCollection;
    }

    public void addProfileToCollection(Profile profile){
        mProfileCollection.add(profile);
    }
}
