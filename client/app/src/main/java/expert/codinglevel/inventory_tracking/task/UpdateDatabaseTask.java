package expert.codinglevel.inventory_tracking.task;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;


public class UpdateDatabaseTask extends AsyncTask<Void, Void, Integer> {
    public static final String TAG = UpdateDatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private IAsyncResponse<Integer> mDelegate;
    private String mTableName;
    private String mQuery;
    private String[] mArgs;
    private ContentValues mContentValues;

    public UpdateDatabaseTask(
            String tableName,
            String whereClause,
            String[] whereArgs,
            ContentValues contentValues,
            SQLiteDatabase db,
            IAsyncResponse<Integer> delegate
    ){
        mDB = db;
        mTableName = tableName;
        mQuery = whereClause;
        mArgs = whereArgs;
        mContentValues = contentValues;
        mDelegate = delegate;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return mDB.update(mTableName, mContentValues, mQuery, mArgs);
    }

    @Override
    protected void onPostExecute(Integer result) {
        mDelegate.processFinish(result);
    }
}
