package shuvalov.nikita.twas.Helpers_Managers;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by NikitaShuvalov on 12/16/16.
 */

public class NearbyManager {
    private boolean mGoogleApiConnected, mPublishing, mSubscribing;
    public GoogleApiClient mGoogleApiClient;

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

    public void setGoogleApiClient(GoogleApiClient googleApiClient){
        mGoogleApiClient = googleApiClient;
    }
    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

}
