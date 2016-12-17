package shuvalov.nikita.twas.PoJos;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class Profile {
    private String mName, mBio, mDOB;

    public Profile(String name, String bio, String dob){
        mName = name;
        mBio = bio;
        mDOB = dob;
    }

    public String getName() {
        return mName;
    }

    public String getBio() {
        return mBio;
    }

    public String getDOB() {
        return mDOB;
    }

}
