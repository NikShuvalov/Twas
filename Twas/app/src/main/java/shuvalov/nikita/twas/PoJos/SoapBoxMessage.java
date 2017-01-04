package shuvalov.nikita.twas.PoJos;

import com.google.android.gms.internal.zzavp;
import com.google.android.gms.nearby.messages.Message;

/**
 * Created by NikitaShuvalov on 1/3/17.
 */

public class SoapBoxMessage extends Message {
    String mShoutOut;

    public SoapBoxMessage(String shoutOut, byte[] bytes){
        super(bytes);
        mShoutOut = shoutOut;
    }
    public String getShoutOut(){
        return mShoutOut;
    }

    public SoapBoxMessage(byte[] bytes) {
        super(bytes);
    }

    public SoapBoxMessage(byte[] bytes, String s) {
        super(bytes, s);
    }

    public SoapBoxMessage(byte[] bytes, String s, String s1) {
        super(bytes, s, s1);
    }

    public SoapBoxMessage(byte[] bytes, String s, String s1, zzavp[] zzavps) {
        super(bytes, s, s1, zzavps);
    }

    public SoapBoxMessage(byte[] bytes, String s, String s1, zzavp[] zzavps, long l) {
        super(bytes, s, s1, zzavps, l);
    }
}
