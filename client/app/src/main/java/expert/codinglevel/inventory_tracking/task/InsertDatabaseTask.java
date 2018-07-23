package expert.codinglevel.inventory_tracking.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;

public class InsertDatabaseTask extends AsyncTask<Void, Void, Long> {
    public static final String TAG = InsertDatabaseTask.class.getSimpleName();
    private IAsyncResponse<Long> mDelegate;
    private SQLiteDatabase mDB;
    private String mTableName;
    private String mNullColumnHack;
    private ContentValues mContentValues;

    public InsertDatabaseTask(
            String tableName,
            String nullColumnHack,
            ContentValues contentValues,
            SQLiteDatabase db,
            IAsyncResponse<Long> delegate
    ){
        mDB = db;
        mTableName = tableName;
        mNullColumnHack = nullColumnHack;
        mContentValues = contentValues;
        mDelegate = delegate;
    }

    @Override
    protected Long doInBackground(Void... params) {
        return mDB.insert(mTableName, mNullColumnHack, mContentValues);
    }

    @Override
    protected void onPostExecute(Long result) {
        mDelegate.processFinish(result);
    }
}
