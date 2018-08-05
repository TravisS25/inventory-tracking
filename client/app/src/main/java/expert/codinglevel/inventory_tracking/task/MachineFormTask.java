package expert.codinglevel.inventory_tracking.task;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.view.TextValue;
import expert.codinglevel.inventory_tracking.widget.CascadingDropDown;

public class MachineFormTask extends AsyncTask<Void, Void, Map<String, ArrayAdapter<TextValue>>> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private Context mContext;
    private SQLiteDatabase mDB;
    private IMachine mMachine;
    private String mBuildingID;
    private IAsyncResponse<Map<String, ArrayAdapter<TextValue>>> mDelegate;

    /**
     * Constructor that includes which machine we want use as template for querying against db
     * @param machine - Machine that we wish to query its properties against in db
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public MachineFormTask(
            Context context,
            IMachine machine,
            SQLiteDatabase db,
            @Nullable IAsyncResponse<Map<String, ArrayAdapter<TextValue>>> delegate
    ){
        mContext = context;
        mMachine = machine;
        mDB = db;
        mDelegate = delegate;
    }

    public MachineFormTask(
            String buildingID,
            SQLiteDatabase db,
            IAsyncResponse<Map<String, ArrayAdapter<TextValue>>> delegate
    ){
        mBuildingID = buildingID;
        mDB = db;
        mDelegate = delegate;
    }

    @Override
    protected Map<String, ArrayAdapter<TextValue>> doInBackground(Void... params) {
        int counter = 0;

        HashMap<String, ArrayAdapter<TextValue>> textValueHashMap = new HashMap<>();

        String floorID = null;
        String departmentID = null;

        // Database cursors for each table
        Cursor buildingCursor;
        Cursor floorCursor;
        Cursor departmentCursor;
        Cursor roomCursor;
        Cursor machineStatusCursor;

        // Init arrays for values we will query from db
        ArrayList<TextValue> buildingArray = new ArrayList<>();
        ArrayList<TextValue> floorArray = new ArrayList<>();
        ArrayList<TextValue> departmentArray = new ArrayList<>();
        ArrayList<TextValue> roomArray = new ArrayList<>();
        ArrayList<TextValue> machineStatusArray = new ArrayList<>();

        // Get queries to use against db from our db helper
        String buildingQuery = HospitalDbHelper.getAllBuildingsQuery();
        String floorQuery = HospitalDbHelper.getFloorByBuildingQuery();
        String departmentQuery = HospitalDbHelper.getDepartmentByFloorQuery();
        String roomQuery = HospitalDbHelper.getRoomByDepartmentQuery();
        String machineStatusQuery = HospitalDbHelper.getAllMachineStatuses();

        buildingCursor = mDB.rawQuery(buildingQuery, null);

        // Loop through building cursor and add id and text value to array list
        while(buildingCursor.moveToNext()){
            String text = buildingCursor.getString(
                    buildingCursor.getColumnIndex("building_name")
            );
            String value = buildingCursor.getString(
                    buildingCursor.getColumnIndex("_id")
            );
            buildingArray.add(new TextValue(text, value));
        }
        buildingCursor.close();

        // If mMachine is null, then use the buildingID passed in constructor for query
        // Else use mMachine's building property
        if(mMachine == null){
            floorCursor = mDB.rawQuery(floorQuery, new String[]{mBuildingID});
        }
        else{
            floorCursor = mDB.rawQuery(floorQuery, new String[]{mMachine.getBuilding().getValue()});
        }

        // Loop through floor cursor and add id and text value to array list
        while(floorCursor.moveToNext()){
            counter++;
            String text = floorCursor.getString(
                    floorCursor.getColumnIndex("floor_name")
            );
            String value = floorCursor.getString(
                    floorCursor.getColumnIndex("_id")
            );
            floorArray.add(new TextValue(text, value));

            if(counter == 1){
                floorID = value;
            }
        }
        floorCursor.close();

        // If mMachine is null, then use the floorID gathered from above
        // Else use mMachine's building property
        if(mMachine == null){
            departmentCursor = mDB.rawQuery(departmentQuery, new String[]{floorID});
        }
        else{
            departmentCursor = mDB.rawQuery(
                    departmentQuery,
                    new String[]{mMachine.getFloor().getValue()}
            );
        }

        counter = 0;
        // Loop through department cursor and add id and text value to array list
        while(departmentCursor.moveToNext()){
            counter++;
            String text = departmentCursor.getString(
                    departmentCursor.getColumnIndex("department_name")
            );
            String value = departmentCursor.getString(
                    departmentCursor.getColumnIndex("_id")
            );
            departmentArray.add(new TextValue(text, value));

            if(counter == 1){
                departmentID = value;
            }
        }
        departmentCursor.close();

        // If mMachine is null, then use the departmentID gathered from above
        // Else use mMachine's building property
        if(mMachine == null){
            roomCursor = mDB.rawQuery(roomQuery, new String[]{departmentID});
        }
        else{
            roomCursor = mDB.rawQuery(roomQuery, new String[]{mMachine.getDepartment().getValue()});
        }

        // Loop through room cursor and add id and text value to array list
        while(roomCursor.moveToNext()){
            String text = roomCursor.getString(
                    roomCursor.getColumnIndex("room_name")
            );
            String value = roomCursor.getString(
                    roomCursor.getColumnIndex("_id")
            );
            roomArray.add(new TextValue(text, value));
        }
        roomCursor.close();

        machineStatusCursor = mDB.rawQuery(machineStatusQuery, null);

        while(machineStatusCursor.moveToNext()){
            String value = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex(HospitalContract.MachineStatus.ID)
            );
            String text = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex(HospitalContract.MachineStatus.MACHINE_STATUS_NAME)
            );
            machineStatusArray.add(new TextValue(text, value));
        }
        machineStatusCursor.close();

        ArrayAdapter<TextValue> buildingAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                buildingArray
        );
        ArrayAdapter<TextValue> floorAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                floorArray
        );
        ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                departmentArray
        );
        ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                roomArray
        );
        ArrayAdapter<TextValue> machineStatusAdapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                machineStatusArray
        );

        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        machineStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        textValueHashMap.put(HospitalContract.TABLE_BUILDING_NAME, buildingAdapter);
        textValueHashMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorAdapter);
        textValueHashMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentAdapter);
        textValueHashMap.put(HospitalContract.TABLE_ROOM_NAME, roomAdapter);
        textValueHashMap.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, machineStatusAdapter);

        return textValueHashMap;
    }

    @Override
    protected void onPostExecute(Map<String, ArrayAdapter<TextValue>> result) {
        if(mDelegate != null){
            mDelegate.processFinish(result);
        }
    }
}
