package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class ConnectionsHelper {
    private ArrayList<Profile> mProfileConnections;

    private static ConnectionsHelper sConnectionsHelper;

    public static ConnectionsHelper getInstance() {
        if (sConnectionsHelper == null){
            sConnectionsHelper = new ConnectionsHelper();
        }
        return sConnectionsHelper;
    }
    private ConnectionsHelper(){
        mProfileConnections = new ArrayList<>();
    }

    public ArrayList<Profile> getConnections() {
        return mProfileConnections;
    }

    //ToDo: Optimize
    public int addProfileToCollection(Profile profile){
        boolean alreadyIn =false;
        for(Profile storedProfile: mProfileConnections){
            if(storedProfile.getUID().equals(profile.getUID())){
                alreadyIn=true;
            }
        }
        if(!alreadyIn){
            mProfileConnections.add(profile);
        }
        return mProfileConnections.size();
    }

    //Should call this after pulling data from database... But no other time, otherwise it'll end up giving duplicates.
    public void addProfileConnectionsToCollection(ArrayList<Profile> profileList){
        mProfileConnections.addAll(profileList);
    }

    public Profile getProfileByPosition(int i){
        return mProfileConnections.get(i);
    }
}
