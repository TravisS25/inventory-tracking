package expert.codinglevel.inventory_tracking.task;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;


public class DeleteDatabaseTask extends AsyncTask<Void, Void, Integer> {
    public static final String TAG = InsertDatabaseTask.class.getSimpleName();
    private IAsyncResponse<Integer> mDelegate;
    private SQLiteDatabase mDB;
    private String mTableName;
    private String mQuery;
    private String[] mArgs;

    public DeleteDatabaseTask(
            String tableName,
            String query,
            String[] args,
            SQLiteDatabase db,
            IAsyncResponse<Integer> delegate
    ){
        mDB = db;
        mTableName = tableName;
        mQuery = query;
        mArgs = args;
        mDelegate = delegate;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        return mDB.delete(mTableName, mQuery, mArgs);
    }

    @Override
    protected void onPostExecute(Integer result) {
        mDelegate.processFinish(result);
    }
}