package expert.codinglevel.inventory_tracking.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.CheckBox;

import java.util.ArrayList;

import expert.codinglevel.inventory_tracking.enums.MachineAttribute;
import expert.codinglevel.inventory_tracking.enums.ViewType;
import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.setting.MachineSettings;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  Machine is the representation of what we will save to local db
 *  and upload to server
 *
 *  Machine implements Parcelable to be able to save state inbetween
 *  rotations
 */
public class Machine extends MachineSettings implements Parcelable, IMachine {
    private String machineName;
    private String scannedTime;
    private String buildingID;
    private String floorID;
    private String departmentID;
    private String roomID;
    private String machineStatusID;

    private TextValue mMachineName;
//    private TextValue mBuilding;
//    private TextValue mDepartment;
//    private TextValue mFloor;
//    private TextValue mRoom;
//    private TextValue mMachineStatus;

    public Machine(Machine machine){
        setMachineName(machine.getMachineName());
        setBuilding(machine.getBuilding());
        setDepartment(machine.getDepartment());
        setFloor(machine.getFloor());
        setRoom(machine.getRoom());
        setMachineStatus(machine.getMachineStatus());

        machineName = machine.getMachineName().getValue();
        scannedTime = machine.scannedTime;
        buildingID = machine.getBuilding().getValue();
        floorID = machine.getFloor().getValue();
        departmentID = machine.getDepartment().getValue();
        roomID = machine.getRoom().getValue();
        machineStatusID = machine.getMachineStatus().getValue();
    }

    public Machine(){}
    private Machine(Parcel in) {
        mMachineName = in.readParcelable(TextValue.class.getClassLoader());
        mBuilding = in.readParcelable(TextValue.class.getClassLoader());
        mFloor = in.readParcelable(TextValue.class.getClassLoader());
        mDepartment = in.readParcelable(TextValue.class.getClassLoader());
        mRoom = in.readParcelable(TextValue.class.getClassLoader());
        mMachineStatus = in.readParcelable(TextValue.class.getClassLoader());

        machineName = in.readString();
        scannedTime = in.readString();
        buildingID = in.readString();
        floorID = in.readString();
        departmentID = in.readString();
        roomID = in.readString();
        machineStatusID = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getMachineName(), 0);
        dest.writeParcelable(getBuilding(), 0);
        dest.writeParcelable(getFloor(), 0);
        dest.writeParcelable(getDepartment(), 0);
        dest.writeParcelable(getRoom(), 0);
        dest.writeParcelable(getMachineStatus(), 0);

        dest.writeString(machineName);
        dest.writeString(getScannedTime());
        dest.writeString(buildingID);
        dest.writeString(floorID);
        dest.writeString(departmentID);
        dest.writeString(roomID);
        dest.writeString(machineStatusID);
    }

    public TextValue getMachineName(){ return mMachineName; }
//    public TextValue getBuilding() { return mBuilding; }
//    public TextValue getFloor() { return mFloor; }
//    public TextValue getDepartment() { return mDepartment; }
//    public TextValue getRoom() { return mRoom; }
//    public TextValue getMachineStatus() { return mMachineStatus; }
    public String getScannedTime(){ return scannedTime; }

//    //public String getAssetTag(){ return mAssetTag; }
//    public String getBuildingID() { return buildingID; }
//    public String getFloorID() { return floorID; }
//    public String getDepartmentID() { return departmentID; }
//    public String getRoomID() { return roomID; }
//    public String getMachineStatusID() { return machineStatusID; }
//    public String getScannedTime(){ return scannedTime; }

    public void setMachineName(TextValue machine){ mMachineName = machine; }
//    public void setBuilding(TextValue building){ mBuilding = building; }
//    public void setFloor(TextValue floor){ mFloor = floor; }
//    public void setDepartment(TextValue department){ mDepartment = department; }
//    public void setRoom(TextValue room){ mRoom = room; }
//    public void setMachineStatus(TextValue machineStatus){ mMachineStatus = machineStatus; }
    public void setScannedTime(String time){ scannedTime = time; }

//    //public void setAssetTag(TextValue machine){ mAssetTag = machine; }
//    public void setBuildingID(String buildingID){ this.buildingID = buildingID; }
//    public void setFloorID(TextValue floor){ mFloor = floor; }
//    public void setDepartmentID(TextValue department){ mDepartment = department; }
//    public void setRoomID(TextValue room){ mRoom = room; }
//    public void setMachineStatusID(TextValue machineStatus){ mMachineStatus = machineStatus; }

    @Override
    public String toString(){
        return mMachineName.getText();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Machine> CREATOR
            = new Parcelable.Creator<Machine>() {
        public Machine createFromParcel(Parcel in) {
            return new Machine(in);
        }

        public Machine[] newArray(int size) {
            return new Machine[size];
        }
    };

    // MachineProperty is a representation of a form view of the different machine properties
//    public static class MachineProperty{
//        private String mPropertyText;
//        private String mPropertyValue;
//        private CheckBox mCheckBox;
//        private ArrayList<TextValue> mSpinnerArrayList;
//        private ViewType mViewType;
//        private MachineAttribute mMachineAttribute;
//
//        public MachineProperty(String propertyText, String propertyValue){
//            if(propertyText == null){
//                throw new IllegalArgumentException("propertyText can't be null");
//            }
//            if(propertyValue == null){
//                throw new IllegalArgumentException("propertyValue can't be null");
//            }
//            mPropertyText = propertyText;
//            mPropertyValue = propertyValue;
//        }
//
//        public MachineProperty(
//                String propertyText,
//                ArrayList<TextValue> spinnerList,
//                MachineAttribute machineAttribute
//        ){
//            if(propertyText == null){
//                throw new IllegalArgumentException("propertyText can't be null");
//            }
//            mPropertyText = propertyText;
//            mSpinnerArrayList = spinnerList;
//            mViewType = ViewType.SPINNER;
//            mMachineAttribute = machineAttribute;
//        }
//
//        public MachineProperty(
//                String propertyText,
//                CheckBox checkBox,
//                MachineAttribute machineAttribute
//        ){
//            if(propertyText == null){
//                throw new IllegalArgumentException("propertyText can't be null");
//            }
//            mPropertyText = propertyText;
//            mCheckBox = checkBox;
//            mViewType = ViewType.CHECKBOX;
//            mMachineAttribute = machineAttribute;
//        }
//
//        public String getPropertyText(){ return mPropertyText; }
//        public String getPropertyValue(){ return mPropertyValue; }
//        public ViewType getViewType(){ return mViewType; }
//        public MachineAttribute getMachineAttribute() { return mMachineAttribute; }
//        public ArrayList<TextValue> getSpinnerArrayList(){ return mSpinnerArrayList; }
//        public CheckBox getCheckbox() {return mCheckBox; }
//    }
}
