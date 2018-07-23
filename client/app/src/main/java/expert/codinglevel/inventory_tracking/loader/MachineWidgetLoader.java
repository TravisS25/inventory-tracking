package expert.codinglevel.inventory_tracking.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.model.Machine;
import expert.codinglevel.inventory_tracking.view.TextValue;
import expert.codinglevel.inventory_tracking.widget.MachineWidget;

public class MachineWidgetLoader extends AsyncTaskLoader<MachineWidget> {
    private static final String TAG = DatabaseLoader.class.getSimpleName();
    private static final boolean DEBUG = true;
    private MachineWidget mMachineWidget;
    private Machine mMachine;

    public MachineWidgetLoader(Context context, Machine machine){
        super(context);
        mMachine = machine;
    }

    @Override
    public MachineWidget loadInBackground() {
        Log.i(TAG, "+++ MachineWidgetLoader#loadInBackground() called! +++");
        HospitalDbHelper dbHelper = HospitalDbHelper.getInstance(getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String buildingQuery = "select _id, building_name from building";
        String departmentQuery = "select _id, department_name from department where building_id=?";
        String floorQuery = "select _id, floor from floor where building_id=?";
        String machineStatusQuery = "select _id, status_name from machine_status";

        Cursor buildingCursor = db.rawQuery(buildingQuery, null);
        Cursor departmentCursor = db.rawQuery(
                departmentQuery,
                new String[]{mMachine.getBuilding().getValue()}
        );
        Cursor floorCursor = db.rawQuery(
                floorQuery,
                new String[]{mMachine.getBuilding().getValue()}
        );
        Cursor machineStatusCursor = db.rawQuery(machineStatusQuery, null);

        ArrayList<TextValue> buildingArray = new ArrayList<>(buildingCursor.getCount());
        ArrayList<TextValue> departmentArray = new ArrayList<>(departmentCursor.getCount());
        ArrayList<TextValue> floorArray = new ArrayList<>(floorCursor.getCount());
        ArrayList<TextValue> machineStatusArray = new ArrayList<>(machineStatusCursor.getCount());

        while(buildingCursor.moveToNext()){
            String id = buildingCursor.getString(buildingCursor.getColumnIndex("_id"));
            String buildingName = buildingCursor.getString(
                    buildingCursor.getColumnIndex("building_name")
            );
            buildingArray.add(new TextValue(buildingName, id));
        }
        while(departmentCursor.moveToNext()){
            String id = departmentCursor.getString(
                    departmentCursor.getColumnIndex("_id")
            );
            String departmentName = departmentCursor.getString(
                    departmentCursor.getColumnIndex("department_name")
            );
            departmentArray.add(new TextValue(departmentName, id));
        }
        while(floorCursor.moveToNext()){
            String id = floorCursor.getString(floorCursor.getColumnIndex("_id"));
            String floor = floorCursor.getString(floorCursor.getColumnIndex("floor"));
            floorArray.add(new TextValue(floor, id));
        }
        while(machineStatusCursor.moveToNext()){
            String id = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("_id"));
            String machineStatus = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("status_name")
            );
            machineStatusArray.add(new TextValue(machineStatus, id));
        }

        buildingCursor.close();
        departmentCursor.close();
        floorCursor.close();
        machineStatusCursor.close();

        return new MachineWidget(
                buildingArray,
                departmentArray,
                floorArray,
                machineStatusArray,
                db
        );
    }

    @Override
    public void deliverResult(MachineWidget machineWidget) {
        if (isReset()) {
            if (DEBUG) Log.w(TAG, "+++ Warning! An async query came in while the Loader was reset! +++");
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (machineWidget != null) {
                releaseResources();
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        MachineWidget oldMachineWidget = mMachineWidget;
        mMachineWidget = oldMachineWidget;

        if (isStarted()) {
            if (DEBUG) Log.i(TAG, "+++ Delivering results to the LoaderManager for" +
                    " the ListFragment to display! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(machineWidget);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldMachineWidget != null && oldMachineWidget != machineWidget) {
            if (DEBUG) Log.i(TAG, "+++ Releasing any old data associated with this Loader. +++");
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if (DEBUG) Log.i(TAG, "+++ onStartLoading() called! +++");

        if (mMachineWidget != null) {
            // Deliver any previously loaded data immediately.
            if (DEBUG) Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(mMachineWidget);
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

        // At this point we can release the resources
        if (mMachineWidget != null) {
            releaseResources();
        }
    }

    @Override
    public void onCanceled(MachineWidget db) {
        if (DEBUG) Log.i(TAG, "+++ onCanceled() called! +++");

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(mMachineWidget);

        // The load has been canceled, so we should release the resources
        // associated with 'mApps'.
        releaseResources();
    }

    private void releaseResources(){
        if(mMachineWidget.getDB().isOpen()){
            mMachineWidget.getDB().close();
        }
        mMachineWidget = null;
    }

}
