package expert.codinglevel.hospital_inventory.task.cascadingdropdown;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.interfaces.IMachine;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.task.DatabaseTask;
import expert.codinglevel.hospital_inventory.view.TextValue;

public class CascadingBuildingDropDownTask extends AsyncTask<Void, Void, HashMap<String, ArrayList<TextValue>>> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private IMachine mMachine;
    private String mBuildingID;
    private IAsyncResponse <HashMap<String, ArrayList<TextValue>>> mDelegate;

    /**
     * Constructor to use if we wish to query the database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public CascadingBuildingDropDownTask(
            IMachine machine,
            SQLiteDatabase db,
            IAsyncResponse<HashMap<String, ArrayList<TextValue>>> delegate
    ){
        mMachine = machine;
        mDB = db;
        mDelegate = delegate;
    }

    public CascadingBuildingDropDownTask(
            String buildingID,
            SQLiteDatabase db,
            IAsyncResponse<HashMap<String, ArrayList<TextValue>>> delegate
    ){
        mBuildingID = buildingID;
        mDB = db;
        mDelegate = delegate;
    }

    @Override
    protected HashMap<String, ArrayList<TextValue>> doInBackground(Void... params) {
        int counter = 0;

        HashMap<String, ArrayList<TextValue>> textValueHashMap = new HashMap<>();

        String floorID = null;
        String departmentID = null;

        // Database cursors for each table
        Cursor buildingCursor;
        Cursor floorCursor;
        Cursor departmentCursor;
        Cursor roomCursor;

        // Init arrays for values we will query from db
        ArrayList<TextValue> buildingArray = new ArrayList<>();
        ArrayList<TextValue> floorArray = new ArrayList<>();
        ArrayList<TextValue> departmentArray = new ArrayList<>();
        ArrayList<TextValue> roomArray = new ArrayList<>();

        // Get queries to use against db from our db helper
        String buildingQuery = HospitalDbHelper.getAllBuildingsQuery();
        String floorQuery = HospitalDbHelper.getFloorByBuildingQuery();
        String departmentQuery = HospitalDbHelper.getDepartmentByFloorQuery();
        String roomQuery = HospitalDbHelper.getRoomByDepartmentQuery();

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

        textValueHashMap.put(HospitalContract.TABLE_BUILDING_NAME, buildingArray);
        textValueHashMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorArray);
        textValueHashMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentArray);
        textValueHashMap.put(HospitalContract.TABLE_ROOM_NAME, roomArray);

        return textValueHashMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, ArrayList<TextValue>> result) {
        mDelegate.processFinish(result);
    }
}
