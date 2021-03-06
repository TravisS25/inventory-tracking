package expert.codinglevel.inventory_tracking.task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;

/**
 *  RetrieveDatabaseTask is aysnc task used to get app database instance
 */
public class RetrieveDatabaseTask extends AsyncTask<Void, Void, SQLiteDatabase> {
    public static final String TAG = RetrieveDatabaseTask.class.getSimpleName();
    private IAsyncResponse<SQLiteDatabase> mDelegate;
    private HospitalDbHelper mDbHelper;

    public RetrieveDatabaseTask(Context context, IAsyncResponse<SQLiteDatabase> delegate){
        mDbHelper = HospitalDbHelper.getInstance(context);
        mDelegate = delegate;
    }

    @Override
    protected SQLiteDatabase doInBackground(Void... params) {
        return mDbHelper.getWritableDatabase();
    }

    @Override
    protected void onPostExecute(SQLiteDatabase result) {
        mDelegate.processFinish(result);
    }

}
