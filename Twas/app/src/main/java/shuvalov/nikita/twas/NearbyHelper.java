package shuvalov.nikita.twas;

/**
 * Created by NikitaShuvalov on 12/16/16.
 */

public class NearbyHelper {


    private boolean mGoogleApiConnected, mPublishing, mSubscribing;
    private String mId;

    private static NearbyHelper myNearbyHelper;

    public static NearbyHelper getInstance() {
        if(myNearbyHelper==null){
            myNearbyHelper = new NearbyHelper();
        }
        return myNearbyHelper;
    }
    private NearbyHelper(){
        mGoogleApiConnected=false;
        mPublishing=false;
        mSubscribing= false;
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

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
}
