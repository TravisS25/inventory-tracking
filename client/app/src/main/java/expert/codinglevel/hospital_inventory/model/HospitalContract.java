package expert.codinglevel.hospital_inventory.model;

import android.provider.BaseColumns;


/**
 * This class is the single location for the database schema
 * Any updates needed to the database will be made here and any queries
 * required to reflect the updated schema will be made in the
 * migrations package.  This should me modified first before making
 * queries against the database
 *
 * */
public final class HospitalContract {
    private HospitalContract(){}

    // Table names
    public static final String TABLE_BUILDING_NAME = "building";
    public static final String TABLE_BUILDING_FLOOR_NAME = "building_floor";
    public static final String TABLE_DEPARTMENT_NAME = "department";
    public static final String TABLE_ROOM_NAME = "room";
    public static final String TABLE_MACHINE_NAME = "machine";
    public static final String TABLE_MACHINE_STATUS_NAME = "machine_status";

    // Defining database
    public static class Building implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_BUILDING_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_BUILDING_NAME;
        public static final String BUILDING_NAME = HospitalContract.TABLE_BUILDING_NAME + "_name";
    }

    public static class BuildingFloor implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_BUILDING_FLOOR_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_BUILDING_FLOOR_NAME;
        public static final String TABLE_BUILDING_NAME_ID = HospitalContract.TABLE_BUILDING_NAME + "_id";
        public static final String FLOOR_NAME =  "floor_name";
    }

    public static class Department implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_DEPARTMENT_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_DEPARTMENT_NAME;
        public static final String TABLE_FLOOR_NAME_ID = HospitalContract.TABLE_BUILDING_FLOOR_NAME + "_id";
        public static final String DEPARTMENT_NAME = HospitalContract.TABLE_DEPARTMENT_NAME + "_name";
    }

    public static class Room implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_ROOM_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_ROOM_NAME;
        public static final String TABLE_DEPARTMENT_NAME_ID = HospitalContract.TABLE_DEPARTMENT_NAME + "_id";
        public static final String ROOM_NAME = HospitalContract.TABLE_ROOM_NAME + "_name";
    }

    public static class MachineStatus implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_MACHINE_STATUS_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_MACHINE_STATUS_NAME;
        public static final String MACHINE_STATUS_NAME = "status_name";
    }

    public static class Machine implements BaseColumns{
        public static final String ID = HospitalContract.TABLE_MACHINE_NAME + "_id";
        public static final String TABLE_NAME = HospitalContract.TABLE_MACHINE_NAME;
        public static final String TABLE_BUILDING_NAME_ID = HospitalContract.TABLE_BUILDING_NAME + "_id";
        public static final String TABLE_DEPARTMENT_NAME_ID = HospitalContract.TABLE_DEPARTMENT_NAME + "_id";
        public static final String TABLE_FLOOR_NAME_ID = HospitalContract.TABLE_BUILDING_FLOOR_NAME + "_id";
        public static final String TABLE_MACHINE_STATUS_NAME_ID = HospitalContract.TABLE_MACHINE_STATUS_NAME + "_id";
        public static final String MACHINE_NAME = "machine_name";
        public static final String SCANNED_TIME = "scanned_time";
    }
}
