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

public class CascadingFloorDropDownTask extends AsyncTask<Void, Void, HashMap<String, ArrayList<TextValue>>> {
    public static final String TAG = DatabaseTask.class.getSimpleName();
    private SQLiteDatabase mDB;
    private IMachine mMachine;
    private String mFloorID;
    private IAsyncResponse <HashMap<String, ArrayList<TextValue>>> mDelegate;


    /**
     * Constructor to use if we wish to query the database
     * @param db - The underlying database we will query against
     * @param delegate - The callback that will be called on onPostExecute
     */
    public CascadingFloorDropDownTask(
            String floorID,
            SQLiteDatabase db,
            IAsyncResponse<HashMap<String, ArrayList<TextValue>>>  delegate
    ){
//        mMachine = machine;
        mFloorID = floorID;
        mDB = db;
        mDelegate = delegate;
    }

    @Override
    protected HashMap<String, ArrayList<TextValue>> doInBackground(Void... params) {
        int counter = 0;

        HashMap<String, ArrayList<TextValue>> hashMap = new HashMap<>();

        String departmentID = null;

        ArrayList<TextValue> departmentArray = new ArrayList<>();
        ArrayList<TextValue> roomArray = new ArrayList<>();

        String departmentQuery = HospitalDbHelper.getDepartmentByFloorQuery();
        String roomQuery = HospitalDbHelper.getRoomByDepartmentQuery();

        Cursor departmentCursor = mDB.rawQuery(departmentQuery, new String[]{mFloorID});

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
        Cursor roomCursor = mDB.rawQuery(roomQuery, new String[]{departmentID});

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

        hashMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentArray);
        hashMap.put(HospitalContract.TABLE_ROOM_NAME, roomArray);

        return hashMap;
    }

    @Override
    protected void onPostExecute(HashMap<String, ArrayList<TextValue>>  result) {
        mDelegate.processFinish(result);
    }
}
