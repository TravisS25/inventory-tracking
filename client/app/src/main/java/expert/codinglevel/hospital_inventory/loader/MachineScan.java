package expert.codinglevel.hospital_inventory.loader;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;

/**
 * This class is mainly used in conjunction with {@link ScanLoader}
 * This class is used to initiate a database and to hold content values
 * that will be used against the database
 *
 */
public class MachineScan {
    private SQLiteDatabase mDB;
    private ContentValues mContentValues;

    public MachineScan(Context context, ContentValues contentValues){
        HospitalDbHelper helper = HospitalDbHelper.getInstance(context);
        mContentValues = contentValues;
        mDB = helper.getReadableDatabase();
    }

    public ContentValues getContentValues(){return mContentValues;}
    public SQLiteDatabase getDB(){return mDB;}
}
