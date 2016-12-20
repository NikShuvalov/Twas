package shuvalov.nikita.twas.Helpers_Managers;

/**
 * Created by NikitaShuvalov on 12/16/16.
 */

public class NearbyManager {
    private boolean mGoogleApiConnected, mPublishing, mSubscribing;
    private String mSelfId; //Should probably moved this into SelfUserProfileUtils

    private static NearbyManager sMyNearbyManager;

    public static NearbyManager getInstance() {
        if(sMyNearbyManager ==null){
            sMyNearbyManager = new NearbyManager();
        }
        return sMyNearbyManager;
    }
    private NearbyManager() {
        mGoogleApiConnected = false;
        mPublishing = false;
        mSubscribing = false;
    }

    public boolean isGoogleApiConnected() {
        return mGoogleApiConnected;
    }

    public void setGoogleApiConnected(boolean googleApiConnected) {
        mGoogleApiConnected = googleApiConnected;
    }

    public boolean isPublishing() {
        return mPublishing;
    }

    public void setPublishing(boolean publishing) {
        mPublishing = publishing;
    }

    public boolean isSubscribing() {
        return mSubscribing;
    }

    public void setSubscribing(boolean subscribing) {
        mSubscribing = subscribing;
    }

    public String getSelfID() {
        return mSelfId;
    }

    public void setId(String id){
        mSelfId = id;
    }


}
