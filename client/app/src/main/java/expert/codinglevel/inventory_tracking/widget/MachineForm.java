package expert.codinglevel.inventory_tracking.widget;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingFloorDropDownTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

public class MachineForm {
    public final static String TAG = MachineForm.class.getSimpleName();
    private static Map<String, Integer> mInitLoad;

    public MachineForm(){
        mInitLoad = new HashMap<>();
        mInitLoad.put(HospitalContract.TABLE_BUILDING_NAME, 0);
        mInitLoad.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, 0);
        mInitLoad.put(HospitalContract.TABLE_DEPARTMENT_NAME, 0);
        mInitLoad.put(HospitalContract.TABLE_ROOM_NAME, 0);
        mInitLoad.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, 0);
    }

    public static Map<String, Spinner> initSpinners(Activity activity){
        Spinner buildingSpinner = (Spinner) activity.findViewById(R.id.building_spinner);
        Spinner floorSpinner = (Spinner) activity.findViewById(R.id.floor_spinner);
        Spinner departmentSpinner = (Spinner) activity.findViewById(R.id.department_spinner);
        Spinner roomSpinner = (Spinner) activity.findViewById(R.id.room_spinner);
        Spinner machineStatusSpinner = (Spinner) activity.findViewById(R.id.machine_status_spinner);

        Map<String, Spinner> spinnerMap = new HashMap<>();
        spinnerMap.put(HospitalContract.TABLE_BUILDING_NAME, buildingSpinner);
        spinnerMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorSpinner);
        spinnerMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentSpinner);
        spinnerMap.put(HospitalContract.TABLE_ROOM_NAME, roomSpinner);
        spinnerMap.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, machineStatusSpinner);
        return spinnerMap;
    }

    private static void setAdapterPosition(
            Spinner spinner,
            String defaultValue
    ){
        for(int i = 0; i < spinner.getAdapter().getCount(); i ++){
            TextValue textValue = (TextValue) spinner.getAdapter().getItem(i);

            if(textValue.getValue().equals(defaultValue)){
                Log.i(TAG, "+++ found value at +++" + i);
                spinner.setSelection(i, false);
            }
        }
    }

    public static void initDefaultValues(
            final IMachine machine,
            final Map<String, Spinner> spinners
    ){
        Spinner buildingSpinner = spinners.get(HospitalContract.TABLE_BUILDING_NAME);
        Spinner floorSpinner = spinners.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME);
        Spinner departmentSpinner = spinners.get(HospitalContract.TABLE_DEPARTMENT_NAME);
        Spinner roomSpinner = spinners.get(HospitalContract.TABLE_ROOM_NAME);
        Spinner machineStatusSpinner = spinners.get(HospitalContract.TABLE_MACHINE_STATUS_NAME);

        setAdapterPosition(buildingSpinner, machine.getBuilding().getValue());
        setAdapterPosition(floorSpinner, machine.getFloor().getValue());
        setAdapterPosition(departmentSpinner, machine.getDepartment().getValue());
        setAdapterPosition(roomSpinner, machine.getRoom().getValue());
        setAdapterPosition(machineStatusSpinner, machine.getMachineStatus().getValue());
    }

    public void initDropdownListeners(
            final Context context,
            final Map<String, Spinner> spinners,
            final SQLiteDatabase db,
            final IMachine machine
    ){
        for(Map.Entry<String, Spinner> entry : spinners.entrySet()){
            switch(entry.getKey()){
                case HospitalContract.TABLE_BUILDING_NAME:
                    entry.getValue().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if(mInitLoad.get(HospitalContract.TABLE_BUILDING_NAME) >= 1){
                                Log.i(TAG, "+++ building selected +++");
                                TextValue item = (TextValue) adapterView.getSelectedItem();
                                machine.setBuilding(item);

                                new CascadingBuildingDropDownTask(
                                        item.getValue(),
                                        db,
                                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                                            @Override
                                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                                ArrayAdapter<TextValue> floorAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
                                                );

                                                ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                                );

                                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_ROOM_NAME)
                                                );

                                                floorAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );
                                                departmentAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );
                                                roomAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );

                                                spinners.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
                                                        .setAdapter(floorAdapter);
                                                spinners.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                                        .setAdapter(departmentAdapter);
                                                spinners.get(HospitalContract.TABLE_ROOM_NAME)
                                                        .setAdapter(roomAdapter);
                                            }
                                        }
                                ).execute();
                            } else{
                                int count = mInitLoad.get(HospitalContract.TABLE_BUILDING_NAME);
                                count++;
                                mInitLoad.put(HospitalContract.TABLE_BUILDING_NAME, count);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
                case HospitalContract.TABLE_BUILDING_FLOOR_NAME:
                    entry.getValue().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if(mInitLoad.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME) >= 1){
                                Log.i(TAG, "+++ floor selected +++");
                                TextValue item = (TextValue) adapterView.getSelectedItem();
                                machine.setFloor(item);

                                new CascadingFloorDropDownTask(
                                        item.getValue(),
                                        db,
                                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                                            @Override
                                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                                ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                                );

                                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_ROOM_NAME)
                                                );

                                                departmentAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );
                                                roomAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );

                                                spinners.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                                        .setAdapter(departmentAdapter);
                                                spinners.get(HospitalContract.TABLE_ROOM_NAME)
                                                        .setAdapter(roomAdapter);
                                            }
                                        }
                                ).execute();
                            } else{
                                int count = mInitLoad.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME);
                                count++;
                               mInitLoad.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, count);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
                case HospitalContract.TABLE_DEPARTMENT_NAME:
                    entry.getValue().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if(mInitLoad.get(HospitalContract.TABLE_DEPARTMENT_NAME) >= 1){
                                Log.i(TAG, "+++ department selected +++");
                                TextValue item = (TextValue) adapterView.getSelectedItem();
                                machine.setDepartment(item);

                                new CascadingFloorDropDownTask(
                                        item.getValue(),
                                        db,
                                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                                            @Override
                                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                                                        context,
                                                        android.R.layout.simple_spinner_item,
                                                        result.get(HospitalContract.TABLE_ROOM_NAME)
                                                );

                                                roomAdapter.setDropDownViewResource(
                                                        android.R.layout.simple_spinner_dropdown_item
                                                );

                                                spinners.get(HospitalContract.TABLE_ROOM_NAME)
                                                        .setAdapter(roomAdapter);
                                            }
                                        }
                                ).execute();
                            } else {
                                int count = mInitLoad.get(HospitalContract.TABLE_DEPARTMENT_NAME);
                                count++;
                                mInitLoad.put(HospitalContract.TABLE_DEPARTMENT_NAME, count);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
                case HospitalContract.TABLE_ROOM_NAME:
                    entry.getValue().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if(mInitLoad.get(HospitalContract.TABLE_ROOM_NAME) >= 1){
                                Log.i(TAG, "+++ room selected +++");
                                TextValue item = (TextValue) adapterView.getSelectedItem();
                                machine.setRoom(item);
                            } else{
                                int count = mInitLoad.get(HospitalContract.TABLE_ROOM_NAME);
                                count++;
                                mInitLoad.put(HospitalContract.TABLE_ROOM_NAME, count);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
                case HospitalContract.TABLE_MACHINE_STATUS_NAME:
                    entry.getValue().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            if(mInitLoad.get(HospitalContract.TABLE_MACHINE_STATUS_NAME) >= 1){
                                Log.i(TAG, "+++ machine status selected +++");
                                TextValue item = (TextValue) adapterView.getSelectedItem();
                                machine.setMachineStatus(item);
                            } else{
                                int count = mInitLoad.get(HospitalContract.TABLE_MACHINE_STATUS_NAME);
                                count++;
                                mInitLoad.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, count);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // your code here
                        }
                    });
                    break;
            }
        }
    }
}
