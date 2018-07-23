package expert.codinglevel.inventory_tracking.task;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.List;

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
public class MultipleReadDBTask extends AsyncTask<Void, Void, HashMap<String, Cursor>> {
    public static final String TAG = DatabaseTask.class.getSimpleName();

    private SQLiteDatabase mDB;
    private List<DatabaseRead> mDatabaseReads;
    private IAsyncResponse<HashMap<String, Cursor>> mDelegate;


    /**
     * Constructor to use if we wish to query the database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public MultipleReadDBTask(
        List<DatabaseRead> databaseReads,
        SQLiteDatabase db,
        IAsyncResponse<HashMap<String, Cursor>> delegate
    ){
        mDatabaseReads = databaseReads;
        mDB = db;
        mDelegate = delegate;
    }


    @Override
    protected HashMap<String, Cursor> doInBackground(Void... params) {
        HashMap<String, Cursor> queryCursors = new HashMap<>();
        for(DatabaseRead read : mDatabaseReads){
            queryCursors.put(
                read.getTableName(),
                mDB.rawQuery(read.getQueries().getQuery(), read.getQueries().getArgs())
            );
        }

        return queryCursors;
    }

    @Override
    protected void onPostExecute(HashMap<String, Cursor> result) {
        mDelegate.processFinish(result);
    }

    public static class DatabaseRead{
        private String mTableName;
        private DatabaseQuery mQueries;

        public DatabaseRead(String tableName, DatabaseQuery queries){
            mTableName = tableName;
            mQueries = queries;
        }

        public String getTableName(){ return mTableName; }
        public DatabaseQuery getQueries(){ return mQueries; }
    }

    public static class DatabaseQuery{
        private String mQuery;
        private String[] mArgs;

        public DatabaseQuery(String query, String[] args){
            mQuery = query;
            mArgs = args;
        }

        public String getQuery(){ return mQuery; }
        public String[] getArgs(){ return mArgs; }
    }
}
