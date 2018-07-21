package expert.codinglevel.hospital_inventory.model;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.ArrayList;

public class MachineExclusionStrategy implements ExclusionStrategy {
    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes f) {
        ArrayList<String> machineFields = new ArrayList<>();
        machineFields.add("machineName");
        machineFields.add("scannedTime");
        machineFields.add("buildingID");
        machineFields.add("floorID");
        machineFields.add("departmentID");
        machineFields.add("roomID");
        machineFields.add("machineStatusID");

        return !machineFields.contains(f.getName());
    }
}
