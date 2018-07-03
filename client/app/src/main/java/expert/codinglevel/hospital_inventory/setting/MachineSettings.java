package expert.codinglevel.hospital_inventory.setting;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import expert.codinglevel.hospital_inventory.interfaces.IMachine;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.view.TextValue;

public class MachineSettings implements Parcelable, IMachine {
    private static MachineSettings instance = null;
    private TextValue mBuilding;
    private TextValue mFloor;
    private TextValue mRoom;
    private TextValue mDepartment;
    private TextValue mMachineStatus;

    private MachineSettings(){}
    private MachineSettings(Parcel in) {
        mBuilding = in.readParcelable(TextValue.class.getClassLoader());
        mFloor = in.readParcelable(TextValue.class.getClassLoader());
        mDepartment = in.readParcelable(TextValue.class.getClassLoader());
        mRoom = in.readParcelable(TextValue.class.getClassLoader());
        mMachineStatus = in.readParcelable(TextValue.class.getClassLoader());
    }

    public static MachineSettings getInstance(){
        if(instance == null){
            instance = new MachineSettings();
        }

        return instance;
    }

    public TextValue getBuilding() { return mBuilding; }
    public TextValue getFloor() { return mFloor; }
    public TextValue getDepartment() { return mDepartment; }
    public TextValue getRoom() { return mRoom; }
    public TextValue getMachineStatus() { return mMachineStatus; }

    public void setBuilding(TextValue building){ mBuilding = building; }
    public void setFloor(TextValue floor){ mFloor = floor; }
    public void setDepartment(TextValue department){ mDepartment = department; }
    public void setRoom(TextValue room){ mRoom = room; }
    public void setMachineStatus(TextValue machineStatus){ mMachineStatus = machineStatus; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getBuilding(), 0);
        dest.writeParcelable(getFloor(), 0);
        dest.writeParcelable(getDepartment(), 0);
        dest.writeParcelable(getRoom(), 0);
        dest.writeParcelable(getMachineStatus(), 0);
    }

    @Override
    public String toString(){
        return getBuilding().getText();
    }

    public static final Parcelable.Creator<MachineSettings> CREATOR
            = new Parcelable.Creator<MachineSettings>() {
        public MachineSettings createFromParcel(Parcel in) {
            return new MachineSettings(in);
        }

        public MachineSettings[] newArray(int size) {
            return new MachineSettings[size];
        }
    };
}
