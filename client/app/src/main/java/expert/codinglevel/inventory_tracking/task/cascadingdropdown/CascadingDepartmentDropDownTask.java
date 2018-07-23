package expert.codinglevel.inventory_tracking.task.cascadingdropdown;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.task.DatabaseTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

public class CascadingDepartmentDropDownTask extends AsyncTask<Void, Void, HashMap<String, ArrayList<TextValue>>> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private String mDepartmentID;
    private IAsyncResponse<HashMap<String, ArrayList<TextValue>>> mDelegate;


    /**
     * Constructor to use if we wish to query the database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public CascadingDepartmentDropDownTask(
            String departmentID,
            SQLiteDatabase db,
            IAsyncResponse<HashMap<String, ArrayList<TextValue>>>  delegate
    ){
        mDepartmentID = departmentID;
        mDB = db;
        mDelegate = delegate;
    }

    @Override
    protected HashMap<String, ArrayList<TextValue>> doInBackground(Void... params) {
        int counter = 0;

        HashMap<String, ArrayList<TextValue>> hashMap = new HashMap<>();

        String floorID = null;
        String departmentID = null;

        ArrayList<TextValue> floorArray = new ArrayList<>();
        ArrayList<TextValue> departmentArray = new ArrayList<>();
        ArrayList<TextValue> roomArray = new ArrayList<>();

        String floorQuery = HospitalDbHelper.getFloorByBuildingQuery();
        String departmentQuery = HospitalDbHelper.getDepartmentByFloorQuery();
        String roomQuery = HospitalDbHelper.getRoomByDepartmentQuery();


        Cursor roomCursor = mDB.rawQuery(roomQuery, new String[]{mDepartmentID});

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

        hashMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorArray);
        hashMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentArray);
        hashMap.put(HospitalContract.TABLE_ROOM_NAME, roomArray);

        return hashMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, ArrayList<TextValue>>  result) {
        mDelegate.processFinish(result);
    }
}
