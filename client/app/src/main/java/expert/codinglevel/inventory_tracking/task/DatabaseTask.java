package expert.codinglevel.inventory_tracking.task;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import expert.codinglevel.inventory_tracking.enums.OperationType;
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
public class DatabaseTask extends AsyncTask<Void, Void, Cursor> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private HashMap<String, String[]> mQueries;
    private OperationType mOperationType;
    private String mTableName;
    private String mNullColumnHack;
    private String mQuery;
    private String[] mArgs;
    private ContentValues mContentValues;
    private HashMap<String, ContentValues> mTableContents;
    private IAsyncResponse<Cursor> mDelegate;

    /**
     * Constructor to use if we wish to query the database
     * @param query - Query to call against the database
     * @param args - Database arguments that go with query
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public DatabaseTask(String query, String[] args, SQLiteDatabase db, IAsyncResponse<Cursor> delegate){
        mOperationType = OperationType.QUERY;
        mDB = db;
        mQuery = query;
        mArgs = args;
        mDelegate = delegate;
    }

    public DatabaseTask(
            HashMap<String, String[]> queries,
            SQLiteDatabase db,
            IAsyncResponse<Cursor> delegate
    ){
        mOperationType = OperationType.QUERY;
        mQueries = queries;
        mDB = db;
        mDelegate = delegate;
    }

    /**
     * Constructor to use if we wish to insert into the database
     * @param tableName - Name of table to insert into
     * @param nullColumnHack - {@link SQLiteDatabase#insert(String, String, ContentValues)}
     * @param contentValues - Content values that will be inserted into database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public DatabaseTask(
        String tableName,
        String nullColumnHack,
        ContentValues contentValues,
        SQLiteDatabase db,
        IAsyncResponse<Cursor> delegate
    ){
        mOperationType = OperationType.INSERT;
        mDB = db;
        mTableName = tableName;
        mNullColumnHack = nullColumnHack;
        mContentValues = contentValues;
        mDelegate = delegate;
    }

    public DatabaseTask(
            String nullColumnHack,
            HashMap<String, ContentValues> tableContents,
            SQLiteDatabase db,
            IAsyncResponse<Cursor> delegate
    ){
        mNullColumnHack = nullColumnHack;
        mOperationType = OperationType.INSERT;
        mTableContents = tableContents;
        mDB = db;
        mDelegate = delegate;
    }

    /**
     * Constructor to use if we wish to edit the database
     * @param tableName - Name of table to insert into
     * @param whereClause - Where clause for the edit
     * @param whereArgs - Arguments used in where clause
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public DatabaseTask(
            String tableName,
            String whereClause,
            String[] whereArgs,
            ContentValues contentValues,
            SQLiteDatabase db,
            IAsyncResponse<Cursor> delegate
    ){
        mOperationType = OperationType.UPDATE;
        mDB = db;
        mTableName = tableName;
        mQuery = whereClause;
        mArgs = whereArgs;
        mContentValues = contentValues;
        mDelegate = delegate;
    }

    public DatabaseTask(
            String tableName,
            String whereClause,
            String[] whereArgs,
            SQLiteDatabase db,
            IAsyncResponse<Cursor> delegate
    ){
        mOperationType = OperationType.DELETE;
        mTableName = tableName;
        mQuery = whereClause;
        mArgs = whereArgs;
        mDB = db;
        mDelegate = delegate;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        if (mOperationType == OperationType.QUERY){
            Log.i(TAG, "+++ Querying data... +++");
//            if(mQueries != null){
//                ArrayList<Cursor> cursor
//                for(Map.Entry<String, String[]> query: mQueries.entrySet()){
//
//                }
//            }
            return mDB.rawQuery(mQuery, mArgs);
        }
        else if(mOperationType == OperationType.UPDATE){
            Log.i(TAG, "+++ Updating data... +++");
            mDB.update(mTableName, mContentValues, mQuery, mArgs);
            return null;
        }
        else if(mOperationType == OperationType.DELETE){
            Log.i(TAG, "+++ Deleting data... +++");
            mDB.update(mTableName, mContentValues, mQuery, mArgs);
            return null;
        }
        else{
            Log.i(TAG, "+++ Inserting data... +++");
            mDB.insert(mTableName, mNullColumnHack, mContentValues);
//            if(mTableContents != null){
//                for(Map.Entry<String, ContentValues> entry: mTableContents.entrySet()){
//                    mDB.insert(entry.getKey(), mNullColumnHack, entry.getValue());
//                }
//            }
//            else{
//                mDB.insert(mTableName, mNullColumnHack, mContentValues);
//            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(Cursor result) {
        mDelegate.processFinish(result);
    }
}
