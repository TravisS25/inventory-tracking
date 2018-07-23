package expert.codinglevel.inventory_tracking.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;

/**
 * This class is used for making async calls to our underling database
 * The two constructors are for either querying data or inserting data
 * If you wish to query data, use constructor:
 *    {@code public MultipleReadDBTask(String query, String[] args, SQLiteDatabase db, AsyncResponse delegate) }
 * Else use:
 *    {@code public MultipleReadDBTask(
 *          String tableName,
 *          String nullColumnHack,
 *          ContentValues contentValues,
 *          SQLiteDatabase db,
 *          AsyncResponse delegate
 *       )
 *     }
 *
 *
 */
public class ReadDatabaseTask extends AsyncTask<Void, Void, Cursor> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private String mQuery;
    private String[] mArgs;
    private IAsyncResponse<Cursor> mDelegate;


    /**
     * Constructor to use if we wish to query the database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public ReadDatabaseTask(
            String query,
            String[] args,
            SQLiteDatabase db,
            IAsyncResponse<Cursor> delegate
    ){
        mQuery = query;
        mArgs = args;
        mDB = db;
        mDelegate = delegate;
    }


    @Override
    protected Cursor doInBackground(Void... params) {
        return mDB.rawQuery(mQuery, mArgs);
    }

    @Override
    protected void onPostExecute(Cursor result) {
        mDelegate.processFinish(result);
    }
}
