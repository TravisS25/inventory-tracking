package expert.codinglevel.inventory_tracking.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


public class MachineJson implements Parcelable {
    private String machineName;
    private String scannedTime;
    private String buildingID;
    private String floorID;
    private String departmentID;
    private String roomID;
    private String machineStatusID;

    public MachineJson(
        @NonNull String pMachineName,
        @NonNull String pScannedTime,
        @NonNull String pBuildingID,
        @NonNull String pFloorID,
        @NonNull String pDepartmentID,
        @NonNull String pRoomID,
        @NonNull String pMachineStatusID
    ){
        machineName = pMachineName;
        scannedTime = pScannedTime;
        buildingID = pBuildingID;
        floorID = pFloorID;
        departmentID = pDepartmentID;
        roomID = pRoomID;
        machineStatusID = pMachineStatusID;
    }

    private MachineJson(Parcel in) {
        machineName = in.readString();
        scannedTime = in.readString();
        buildingID = in.readString();
        floorID = in.readString();
        departmentID = in.readString();
        machineStatusID = in.readString();
    }

    public String getMachineName(){ return machineName; }
    public String getScannedTime(){ return scannedTime; }
    public String getBuildingID(){ return buildingID; }
    public String getFloorID(){ return floorID; }
    public String getDepartmentID(){ return departmentID; }
    public String getRoomID(){ return roomID; }
    public String getMachineStatusID(){ return machineStatusID; }

    @Override
    public String toString(){
        return machineName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(machineName);
        dest.writeString(scannedTime);
        dest.writeString(buildingID);
        dest.writeString(floorID);
        dest.writeString(departmentID);
        dest.writeString(machineStatusID);
    }

    public static final Parcelable.Creator<MachineJson> CREATOR
            = new Parcelable.Creator<MachineJson>() {
        public MachineJson createFromParcel(Parcel in) {
            return new MachineJson(in);
        }

        public MachineJson[] newArray(int size) {
            return new MachineJson[size];
        }
    };
}
