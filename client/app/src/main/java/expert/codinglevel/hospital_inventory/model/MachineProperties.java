package expert.codinglevel.hospital_inventory.model;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.R;
import expert.codinglevel.hospital_inventory.enums.MachineAttribute;
import expert.codinglevel.hospital_inventory.view.TextValue;

public class MachineProperties {
    private MachineProperties(){}

    public static void addCascadingProperties(
            Context context,
            HashMap<String, ArrayList<TextValue>> result,
            ArrayList<Machine.MachineProperty> propertyList
    )
    {
        Machine.MachineProperty buildingProperty = new Machine.MachineProperty(
                context.getString(R.string.building_text),
                result.get(HospitalContract.TABLE_BUILDING_NAME),
                MachineAttribute.BUILDING
        );
        Machine.MachineProperty floorProperty = new Machine.MachineProperty(
                context.getString(R.string.floor_text),
                result.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME),
                MachineAttribute.FLOOR
        );
        Machine.MachineProperty departmentProperty = new Machine.MachineProperty(
                context.getString(R.string.department_text),
                result.get(HospitalContract.TABLE_DEPARTMENT_NAME),
                MachineAttribute.DEPARTMENT
        );
        Machine.MachineProperty roomProperty = new Machine.MachineProperty(
                context.getString(R.string.room_text),
                result.get(HospitalContract.TABLE_ROOM_NAME),
                MachineAttribute.ROOM
        );

        propertyList.add(buildingProperty);
        propertyList.add(floorProperty);
        propertyList.add(departmentProperty);
        propertyList.add(roomProperty);
    }

    public static void addProperties(
            Context context,
            HashMap<String, Cursor> result,
            ArrayList<Machine.MachineProperty> propertyList
    )
    {
        Cursor machineStatusCursor = result.get(
                HospitalContract.TABLE_MACHINE_STATUS_NAME
        );

        ArrayList<TextValue> machineStatusArray = new ArrayList<>(
                machineStatusCursor.getCount()
        );

        while(machineStatusCursor.moveToNext()){
            String text = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("status_name")
            );
            String value = machineStatusCursor.getString(
                    machineStatusCursor.getColumnIndex("_id")
            );
            machineStatusArray.add(new TextValue(text, value));
        }

        Machine.MachineProperty machineStatusProperty = new Machine.MachineProperty(
                context.getString(R.string.machine_status_text),
                machineStatusArray,
                MachineAttribute.MACHINE_STATUS
        );
        propertyList.add(machineStatusProperty);
    }
}
