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

    public void addProfileToCollection(Profile profile){
        mProfileConnections.add(profile);
    }
}
