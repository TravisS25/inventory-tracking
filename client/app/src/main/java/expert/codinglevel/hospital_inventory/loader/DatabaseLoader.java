package expert.codinglevel.hospital_inventory.loader;

//import android.support.v4.content.AsyncTaskLoader;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;

public class DatabaseLoader extends AsyncTaskLoader<SQLiteDatabase> {
    private static final String TAG = DatabaseLoader.class.getSimpleName();
    private static final boolean DEBUG = true;
    private SQLiteDatabase mDB;

    public DatabaseLoader(Context context){
        super(context);
    }

    @Override
    public SQLiteDatabase loadInBackground() {
        Log.i(TAG, "+++ load in background +++");
        HospitalDbHelper dbHelper = HospitalDbHelper.getInstance(getContext());
        return dbHelper.getWritableDatabase();
    }

    @Override
    public void deliverResult(SQLiteDatabase db) {
        if (isReset()) {
            if (DEBUG) Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (db != null) {
                releaseResources(db);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        SQLiteDatabase oldDB = mDB;
        mDB = db;

        if (isStarted()) {
            if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager for" +
                    " the ListFragment to display! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(db);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldDB != null && oldDB != db) {
            if (DEBUG) Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            releaseResources(oldDB);
        }
    }

    @Override
    protected void onStartLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

        if (mDB != null) {
            // Deliver any previously loaded data immediately.
            if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(mDB);
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStopLoading() called! +++");

        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();
    }

    @Override
    protected void onReset() {
        if (DEBUG) Log.i(TAG, "+++ onReset() called! +++");

        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'apps'.
        if (mDB != null) {
            releaseResources(mDB);
            mDB = null;
        }
    }

    @Override
    public void onCanceled(SQLiteDatabase db) {
        if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(mDB);

        // The load has been canceled, so we should release the resources
        // associated with 'mApps'.
        releaseResources(mDB);
    }

    private void releaseResources(SQLiteDatabase db){
        db.close();
    }
}
