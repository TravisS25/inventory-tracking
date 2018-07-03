package expert.codinglevel.hospital_inventory.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.CheckBox;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import expert.codinglevel.hospital_inventory.enums.MachineAttribute;
import expert.codinglevel.hospital_inventory.enums.ViewType;
import expert.codinglevel.hospital_inventory.interfaces.IMachine;
import expert.codinglevel.hospital_inventory.task.MultipleReadDBTask;
import expert.codinglevel.hospital_inventory.view.TextValue;


public class Machine implements Parcelable, IMachine {
    private TextValue mAssetTag;
    private TextValue mBuilding;
    private TextValue mDepartment;
    private TextValue mFloor;
    private TextValue mRoom;
    private TextValue mMachineStatus;
    private String mScannedTime;

    public Machine(Machine machine){
        setAssetTag(machine.getAssetTag());
        setBuilding(machine.getBuilding());
        setDepartment(machine.getDepartment());
        setFloor(machine.getFloor());
        setRoom(machine.getRoom());
        setMachineStatus(machine.getMachineStatus());
    }

    public Machine(){}
    private Machine(Parcel in) {
        mAssetTag = in.readParcelable(TextValue.class.getClassLoader());
        mBuilding = in.readParcelable(TextValue.class.getClassLoader());
        mFloor = in.readParcelable(TextValue.class.getClassLoader());
        mDepartment = in.readParcelable(TextValue.class.getClassLoader());
        mRoom = in.readParcelable(TextValue.class.getClassLoader());
        mMachineStatus = in.readParcelable(TextValue.class.getClassLoader());
        mScannedTime = in.readString();
    }

    public TextValue getAssetTag(){ return mAssetTag; }
    public TextValue getBuilding() { return mBuilding; }
    public TextValue getFloor() { return mFloor; }
    public TextValue getDepartment() { return mDepartment; }
    public TextValue getRoom() { return mRoom; }
    public TextValue getMachineStatus() { return mMachineStatus; }
    public String getScannedTime(){ return mScannedTime; }

    public void setAssetTag(TextValue machine){ mAssetTag = machine; }
    public void setBuilding(TextValue building){ mBuilding = building; }
    public void setFloor(TextValue floor){ mFloor = floor; }
    public void setDepartment(TextValue department){ mDepartment = department; }
    public void setRoom(TextValue room){ mRoom = room; }
    public void setMachineStatus(TextValue machineStatus){ mMachineStatus = machineStatus; }
    public void setScannedTime(String scannedTime){ mScannedTime = scannedTime; }

    @Override
    public String toString(){
        return mAssetTag.getText();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getAssetTag(), 0);
        dest.writeParcelable(getBuilding(), 0);
        dest.writeParcelable(getFloor(), 0);
        dest.writeParcelable(getDepartment(), 0);
        dest.writeParcelable(getRoom(), 0);
        dest.writeParcelable(getMachineStatus(), 0);
        dest.writeString(getScannedTime());
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

    public static class MachineProperty{
        private String mPropertyText;
        private String mPropertyValue;
        private CheckBox mCheckBox;
        private ArrayList<TextValue> mSpinnerArrayList;
        private ViewType mViewType;
        private MachineAttribute mMachineAttribute;

        public MachineProperty(String propertyText, String propertyValue){
            if(propertyText == null){
                throw new IllegalArgumentException("propertyText can't be null");
            }
            if(propertyValue == null){
                throw new IllegalArgumentException("propertyValue can't be null");
            }
            mPropertyText = propertyText;
            mPropertyValue = propertyValue;
        }

        public MachineProperty(
                String propertyText,
                ArrayList<TextValue> spinnerList,
                MachineAttribute machineAttribute
        ){
            if(propertyText == null){
                throw new IllegalArgumentException("propertyText can't be null");
            }
            mPropertyText = propertyText;
            mSpinnerArrayList = spinnerList;
            mViewType = ViewType.SPINNER;
            mMachineAttribute = machineAttribute;
        }

        public MachineProperty(
                String propertyText,
                CheckBox checkBox,
                MachineAttribute machineAttribute
        ){
            if(propertyText == null){
                throw new IllegalArgumentException("propertyText can't be null");
            }
            mPropertyText = propertyText;
            mCheckBox = checkBox;
            mViewType = ViewType.CHECKBOX;
            mMachineAttribute = machineAttribute;
        }

        public String getPropertyText(){ return mPropertyText; }
        public String getPropertyValue(){ return mPropertyValue; }
        public ViewType getViewType(){ return mViewType; }
        public MachineAttribute getMachineAttribute() { return mMachineAttribute; }
        public ArrayList<TextValue> getSpinnerArrayList(){ return mSpinnerArrayList; }
        public CheckBox getCheckbox() {return mCheckBox; }
    }
}
