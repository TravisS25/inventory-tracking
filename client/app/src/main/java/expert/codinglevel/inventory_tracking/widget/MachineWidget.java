package expert.codinglevel.inventory_tracking.widget;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import expert.codinglevel.inventory_tracking.view.TextValue;


public class MachineWidget {
    private ArrayList<TextValue> mBuilding;
    private ArrayList<TextValue> mDepartment;
    private ArrayList<TextValue> mFloor;
    private ArrayList<TextValue> mMachineStatus;
    private SQLiteDatabase mDB;

    public MachineWidget(
            ArrayList<TextValue> building,
            ArrayList<TextValue> department,
            ArrayList<TextValue> floor,
            ArrayList<TextValue> machineStatus,
            SQLiteDatabase db
    ){
        mBuilding = building;
        mDepartment = department;
        mFloor = floor;
        mMachineStatus = machineStatus;
        mDB = db;
    }

    public ArrayList<TextValue> getBuildingArrayList() { return mBuilding; }
    public ArrayList<TextValue> getDepartmentArrayList() { return mDepartment; }
    public ArrayList<TextValue> getFloorArrayList() { return mFloor; }
    public ArrayList<TextValue> getMachineStatusArrayList() { return mMachineStatus; }
    public SQLiteDatabase getDB() { return mDB; }


//    private ArrayList<TextValue> mBuilding;
//    private ArrayList<TextValue> mDepartment;
//    private ArrayList<TextValue> mFloor;
//    private ArrayList<TextValue> mMachineStatus;
//    private SQLiteDatabase mDB;
//
//    public MachineWidget(
//            ArrayList<TextValue> building,
//            ArrayList<TextValue> department,
//            ArrayList<TextValue> floor,
//            ArrayList<TextValue> machineStatus,
//            SQLiteDatabase db
//    ){
//        mBuilding = building;
//        mDepartment = department;
//        mFloor = floor;
//        mMachineStatus = machineStatus;
//        mDB = db;
//    }
//
//    public ArrayList<TextValue> getBuildingArrayList() { return mBuilding; }
//    public ArrayList<TextValue> getDepartmentArrayList() { return mDepartment; }
//    public ArrayList<TextValue> getFloorArrayList() { return mFloor; }
//    public ArrayList<TextValue> getMachineStatusArrayList() { return mMachineStatus; }
//    public SQLiteDatabase getDB() { return mDB; }
}
