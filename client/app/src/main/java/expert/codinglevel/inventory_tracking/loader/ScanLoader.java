package expert.codinglevel.inventory_tracking.loader;

//import android.support.v4.content.AsyncTaskLoader;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import expert.codinglevel.inventory_tracking.DefaultMachineSettingsActivity;


public class ScanLoader extends AsyncTaskLoader<MachineScan> {
    private static final String TAG = ScanLoader.class.getSimpleName();
    private static final boolean DEBUG = true;
    private MachineScan mMachineScan;

    public ScanLoader(Context context){
        super(context);
    }

    @Override
    public MachineScan loadInBackground() {
        if (DEBUG) Log.i(TAG, "+++ loadInBackground() called! +++");
        SharedPreferences settings = getContext().getSharedPreferences(DefaultMachineSettingsActivity.TAG, 0);
        ContentValues contentValues = new ContentValues();
        contentValues.put("building_id", settings.getInt("building_id", 1));
        contentValues.put("department_id", settings.getInt("department_id", 1));
        contentValues.put("floor_id", settings.getInt("floor_id", 1));
        contentValues.put("machine_status_id", settings.getInt("machine_status_id", 1));
        return new MachineScan(getContext(), contentValues);
    }

    @Override
    public void deliverResult(MachineScan machineScan) {
        if (DEBUG) Log.i(TAG, "+++ deliverResult() called! +++");
        if (isReset()) {
            if (DEBUG) Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (machineScan != null) {
                releaseResources(machineScan);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        MachineScan oldMachineScan = mMachineScan;
        mMachineScan = machineScan;

        if (isStarted()) {
            if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager for" +
                    " the ListFragment to display! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(machineScan);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldMachineScan != null && oldMachineScan != machineScan) {
            if (DEBUG) Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            releaseResources(oldMachineScan);
        }
    }

    @Override
    protected void onStartLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

        if (mMachineScan != null) {
            // Deliver any previously loaded data immediately.
            if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(mMachineScan);
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStopLoading() called! +++");

        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is; Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        if (DEBUG) Log.i(TAG, "+++ onReset() called! +++");

        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'apps'.
        if (mMachineScan != null) {
            releaseResources(mMachineScan);
            mMachineScan = null;
        }
    }

    @Override
    public void onCanceled(MachineScan machineScan) {
        if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(mMachineScan);

        // The load has been canceled, so we should release the resources
        // associated with 'mApps'.
        releaseResources(mMachineScan);
    }


    private void releaseResources(MachineScan machineScan){
        if (DEBUG) Log.i(TAG, "+++ releaseResources() called! +++");
        SQLiteDatabase db = machineScan.getDB();

        if(db.isOpen())
            if (DEBUG) Log.i(TAG, "+++ db.close() called! +++");
            db.close();

        mMachineScan = null;
    }
}
