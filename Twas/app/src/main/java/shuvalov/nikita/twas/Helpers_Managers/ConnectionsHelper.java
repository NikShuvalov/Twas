package shuvalov.nikita.twas.Helpers_Managers;

import java.util.ArrayList;

import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class ConnectionsHelper {
    ArrayList<Profile> mProfileConnections;

    private static ConnectionsHelper sMConnectionsHelper;

    public static ConnectionsHelper getInstance() {
        if (sMConnectionsHelper != null){
            sMConnectionsHelper = new ConnectionsHelper();
        }
        return sMConnectionsHelper;
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
