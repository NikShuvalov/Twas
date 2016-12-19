package shuvalov.nikita.twas.PoJos;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class Profile {
    private String mUID, mName, mBio, mGender, mPicURL;//ToDo: Consider changing DOB to long.
    private long mDOB;

    //ToDo: Add SoapBox message into profile? Or someplace else?

    public Profile(String uid, String name, String bio, long dobInMillis, String gender, String picURL){
        mUID = uid;
        mName = name;
        mBio = bio;
        mDOB = dobInMillis;
        mGender = gender;
        mPicURL = picURL;
    }

    public String getUID() {
        return mUID;
    }

    public String getName() {
        return mName;
    }

    public String getBio() {
        return mBio;
    }

    public long getDOB() {
        return mDOB;
    }

    public String getGender() {
        return mGender;
    }

    public String getPicURL() {
        return mPicURL;
    }
}
