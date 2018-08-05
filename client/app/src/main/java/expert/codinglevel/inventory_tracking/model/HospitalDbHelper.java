package expert.codinglevel.inventory_tracking.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.task.MultipleReadDBTask;
import expert.codinglevel.inventory_tracking.view.TextValue;


public class HospitalDbHelper extends SQLiteOpenHelper {
    public static final String TAG = HospitalDbHelper.class.getSimpleName();
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Hospital.db";
    public static final String BUILDING_JOIN = "building_join";
    private AssetManager mAssetManager;
    private static HospitalDbHelper mInstance = null;

    public static HospitalDbHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new HospitalDbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private HospitalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mAssetManager = context.getAssets();
    }
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "+++ helper create +++");
        executeSqlFile(db, "migrations/1_up.sql");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = oldVersion + 1; i <= newVersion; i++){
            executeSqlFile(db, "migrations/" + Integer.toString(i) + "_up.sql");
        }
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void executeSqlFile(SQLiteDatabase db, String filePath){
        InputStream inputStream;

        try{
            inputStream = mAssetManager.open(filePath);
        }
        catch (IOException e){
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
            return;
        }
//        File file = new File(filePath);
        BufferedReader reader = null;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String text;

            while ((text = reader.readLine()) != null) {
                stringBuilder.append(text);
            }
        }
        catch (IOException e) {
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.i(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        try{
            String[] queries = stringBuilder.toString().split(";");
            Log.i(TAG, Integer.toString(queries.length));

            for(String query : queries){
                Log.i(TAG, query);
                db.execSQL(query);
            }
        }
        catch (SQLException e){
            Log.i(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<MultipleReadDBTask.DatabaseRead> getMachineDatabaseReadList(IMachine machine){
//        String buildingQuery = "select _id, building_name from building";
//        String floorQuery = "select _id, floor_name from building_floor where building_id=?";
//        String departmentQuery = "select _id, department_name from department where building_id=?";
//        String roomQuery = "select _id, room_name from room where ";
        String machineStatusQuery = "select _id, status_name from machine_status";

        List<MultipleReadDBTask.DatabaseRead> databaseReadList = new ArrayList<>();
//        databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
//                HospitalContract.TABLE_BUILDING_NAME,
//                new MultipleReadDBTask.DatabaseQuery(buildingQuery, null)
//            )
//        );
//        databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
//                HospitalContract.TABLE_BUILDING_NAME,
//                new MultipleReadDBTask.DatabaseQuery(buildingQuery, null)
//        ));
//        databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
//                HospitalContract.TABLE_DEPARTMENT_NAME,
//                new MultipleReadDBTask.DatabaseQuery(
//                        departmentQuery,
//                        new String[]{machine.getBuilding().getValue()}
//                )
//        ));
//        databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
//                HospitalContract.TABLE_FLOOR_NAME,
//                new MultipleReadDBTask.DatabaseQuery(
//                        floorQuery,
//                        new String[]{machine.getBuilding().getValue()}
//                )
//        ));
        databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
                HospitalContract.TABLE_MACHINE_STATUS_NAME,
                new MultipleReadDBTask.DatabaseQuery(machineStatusQuery, null)
        ));

        return databaseReadList;
    }

    public static HashMap<String, ArrayList<TextValue>> getMachineDatabaseValues(HashMap<String, Cursor> cursors){
//        Cursor buildingCursor = cursors.get(HospitalContract.TABLE_BUILDING_NAME);
//        Cursor departmentCursor = cursors.get(HospitalContract.TABLE_DEPARTMENT_NAME);
//        Cursor floorCursor = cursors.get(HospitalContract.TABLE_FLOOR_NAME);
        Cursor allBuildingCursor = cursors.get(BUILDING_JOIN);
        Cursor machineStatusCursor = cursors.get(HospitalContract.TABLE_MACHINE_STATUS_NAME);

        ArrayList<TextValue> buildingArray = new ArrayList<>();
        ArrayList<TextValue> floorArray = new ArrayList<>();
        ArrayList<TextValue> departmentArray = new ArrayList<>();
        ArrayList<TextValue> roomArray = new ArrayList<>();
        ArrayList<TextValue> machineStatusArray = new ArrayList<>();

        while(allBuildingCursor.moveToNext()){
            String buildingID = allBuildingCursor.getString(
                allBuildingCursor.getColumnIndex("building_id")
            );
            String buildingName = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("building_name")
            );
            String floorID = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("building_floor_id")
            );
            String floorName = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("floor_name")
            );
            String departmentID = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("department_id")
            );
            String departmentName = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("department_name")
            );
            String roomID = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("room_id")
            );
            String roomName = allBuildingCursor.getString(
                    allBuildingCursor.getColumnIndex("room_name")
            );

            buildingArray.add(new TextValue(buildingName, buildingID));
            floorArray.add(new TextValue(floorName, floorID));
            departmentArray.add(new TextValue(departmentName, departmentID));
            roomArray.add(new TextValue(roomName, roomID));
        }

//        while(buildingCursor.moveToNext()){
//            String id = buildingCursor.getString(buildingCursor.getColumnIndex("_id"));
//            String buildingName = buildingCursor.getString(
//                    buildingCursor.getColumnIndex("building_name")
//            );
//            buildingArray.add(new TextValue(buildingName, id));
//        }
//        while(departmentCursor.moveToNext()){
//            String id = departmentCursor.getString(
//                    departmentCursor.getColumnIndex("_id")
//            );
//            String departmentName = departmentCursor.getString(
//                    departmentCursor.getColumnIndex("department_name")
//            );
//            departmentArray.add(new TextValue(departmentName, id));
//        }
//        while(floorCursor.moveToNext()){
//            String id = floorCursor.getString(floorCursor.getColumnIndex("_id"));
//            String floor = floorCursor.getString(floorCursor.getColumnIndex("floor"));
//            floorArray.add(new TextValue(floor, id));
//        }
        while(machineStatusCursor.moveToNext()){
            String id = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("_id"));
            String machineStatus = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("status_name")
            );
            machineStatusArray.add(new TextValue(machineStatus, id));
        }

//        buildingCursor.close();
//        departmentCursor.close();
//        floorCursor.close();
        machineStatusCursor.close();

        HashMap<String, ArrayList<TextValue>> hashMap = new HashMap<>();
        hashMap.put(HospitalContract.TABLE_BUILDING_NAME, buildingArray);
        hashMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorArray);
        hashMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentArray);
        hashMap.put(HospitalContract.TABLE_ROOM_NAME, roomArray);
        hashMap.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, machineStatusArray);

        return hashMap;
    }

    public static String getAllBuildingsQuery(){
        return "select * from building";
    }

    public static String getBuildingQuery(){
        return "select * from building where building._id = ?";
    }

    public static String getFloorByBuildingQuery(){
        return "select * from building_floor where building_floor.building_id = ?";
    }

    public static String getDepartmentByFloorQuery(){
        return "select * from department where department.building_floor_id = ?";
    }

    public static String getRoomByDepartmentQuery(){
        return "select * from room where room.department_id = ?";
    }

    public static String getAllMachineStatuses(){
        return "select _id as machine_status_id, status_name from machine_status";
    }

    public static String getBuildingJoinQuery(){
        return
            "select " +
                "building._id as building_id, " +
                "building.building_name, " +
                "building_floor._id as building_floor_id, " +
                "building_floor.floor_name, " +
                "department._id as department_id, " +
                "department.department_name, " +
                "room._id as room_id, " +
                "room.room_name " +
            "from " +
                "building " +
            "join " +
                "building_floor on building_floor.building_id = building._id " +
            "join " +
                "department on department.building_floor_id = building_floor._id " +
            "join " +
                "room on room.department_id = department._id " +
            "where " +
                "building._id = ?";
    }

    public static String getMachineListQuery(){
        return
            "select " +
                "machine._id as machine_id, " +
                "machine.machine_name, " +
                "machine.scanned_time, " +
                "building._id as building_id, " +
                "building.building_name, " +
                "building_floor._id as building_floor_id, " +
                "building_floor.floor_name, " +
                "department._id as department_id, " +
                "department.department_name, " +
                "room._id as room_id, " +
                "room.room_name, " +
                "machine_status._id as machine_status_id, " +
                "machine_status.status_name " +
            "from " +
                "building " +
            "JOIN " +
                "building_floor on building_floor.building_id = building._id " +
            "JOIN " +
                "department on department.building_floor_id = building_floor._id " +
            "JOIN " +
                "room on room.department_id = department._id " +
            "JOIN " +
                "machine on machine.room_id = room._id " +
            "JOIN " +
                "machine_status on machine.machine_status_id = machine_status._id " +
            "order by machine.scanned_time desc";
    }

    public static String getMachineByAssetTagQuery(){
        return "SELECT * FROM machine WHERE machine_name = ?";
    }
}
