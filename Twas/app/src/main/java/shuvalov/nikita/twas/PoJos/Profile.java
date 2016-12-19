package shuvalov.nikita.twas.PoJos;

/**
 * Created by NikitaShuvalov on 12/15/16.
 */

public class Profile {
    private String mName, mBio, mDOB, mGender, mSoapBoxMessage;//ToDo: Consider changing DOB to long.

    public Profile(String name, String bio, String dob, String gender, String soapBoxMessage){
        mName = name;
        mBio = bio;
        mDOB = dob;
        mGender = gender;
        mSoapBoxMessage = soapBoxMessage;
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

    public String getGender() {
        return mGender;
    }

    public String getSoapBoxMessage() {
        return mSoapBoxMessage;
    }

    public void setSoapBoxMessage(String soapBoxMessage) {
        mSoapBoxMessage = soapBoxMessage;
    }
}
