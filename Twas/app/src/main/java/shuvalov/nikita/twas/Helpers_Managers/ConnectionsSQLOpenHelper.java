package shuvalov.nikita.twas.Helpers_Managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

import shuvalov.nikita.twas.PoJos.Profile;

/**
 * Created by NikitaShuvalov on 12/17/16.
 */

public class ConnectionsSQLOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CONNECTION_COLLECTION_DB";
    public static final String TABLE_NAME = "PROFILE_LIST";

    public static final String COLUMN_UID = "UID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_PIC_URL = "PROFILE_PIC_URL";
    public static final String COLUMN_BIO = "BIO_SUMMARY";
    public static final String COLUMN_GENDER = "GENDER";
    public static final String COLUMN_BIRTHDATE = "BIRTH_DATE";

    public static final String CREATE_TABLE_EXE = "CREATE TABLE "+ TABLE_NAME +
            " ("+ COLUMN_UID +" TEXT"+
            COLUMN_NAME + " TEXT"+
            COLUMN_PIC_URL + " TEXT"+
            COLUMN_BIO + " TEXT"+
            COLUMN_GENDER + " TEXT" +
            COLUMN_BIRTHDATE + " INTEGER)";

    private static ConnectionsSQLOpenHelper sConnectionsSQLOpenHelper;

    public static ConnectionsSQLOpenHelper getInstance(Context context) {
        if (sConnectionsSQLOpenHelper == null) {
            sConnectionsSQLOpenHelper = new ConnectionsSQLOpenHelper(context.getApplicationContext());
        }
        return sConnectionsSQLOpenHelper;
    }

    private ConnectionsSQLOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, 0);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { sqLiteDatabase.execSQL(CREATE_TABLE_EXE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    //Clears database, idea is if user logs out, I wouldn't want other users on this phone to have access to that data.
    public void clearDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
    }


    public long addNewConnection(Profile profile){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues content = new ContentValues();
        content.put(COLUMN_UID, profile.getUID());
        content.put(COLUMN_NAME, profile.getName());
        content.put(COLUMN_BIO, profile.getBio());
        content.put(COLUMN_GENDER, profile.getGender());
        content.put(COLUMN_BIRTHDATE, profile.getDOB());
        content.put(COLUMN_PIC_URL, profile.getPicURL());

        long row = db.insert(TABLE_NAME, null, content);
        db.close();
        return row;//ToDo: Not sure why I'm returning a value, but might serve useful. (Remove if proves unncessary)
    }
    //Uses the above method to take in a full list of connection
    public int addCollection(ArrayList<Profile> connections){
        int addedAmount = 0;
        for(Profile connection: connections){
            addNewConnection(connection);
            addedAmount++;
        }
        return addedAmount;
    }


    public void updateUserInfo(Profile profile){//Updates values, doesn't update UID or PIC_URL as that is immutable.
        SQLiteDatabase db = getWritableDatabase();


        ContentValues content = new ContentValues();
        content.put(COLUMN_NAME, profile.getName());
        content.put(COLUMN_BIO, profile.getBio());
        content.put(COLUMN_GENDER, profile.getGender());
        content.put(COLUMN_BIRTHDATE, profile.getDOB());

        String selection = COLUMN_UID+ " = ?";
        db.update(TABLE_NAME, null, selection, new String[]{profile.getUID()});
        db.close();
    }



}
