package shuvalov.nikita.twas.Helpers_Managers;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import shuvalov.nikita.twas.Activities.FirebaseLogInActivity;
import shuvalov.nikita.twas.Activities.MainActivity;
import shuvalov.nikita.twas.AppConstants;

/**
 * Created by NikitaShuvalov on 12/16/16.
 */

public class NearbyManager {
    private boolean mGoogleApiConnected, mPublishing, mSubscribing;
    private String mId;

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

    public String getId() {
        return mId;
    }

    public void setId(String id){
        mId = id;
    }


}
