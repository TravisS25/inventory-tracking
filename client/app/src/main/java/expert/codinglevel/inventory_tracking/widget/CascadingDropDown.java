package expert.codinglevel.inventory_tracking.widget;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.Machine;
import expert.codinglevel.inventory_tracking.setting.MachineSettings;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingFloorDropDownTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  CascadingDropDown is util class that allows us to apply form views (spinners, checkboxes etc)
 *  to an appropriate position in a list view
 */
public class CascadingDropDown {
    public final static String TAG = CascadingDropDown.class.getSimpleName();
    private CascadingDropDown(){}

    public static void execBuildingCascade(
        final Context context,
        final Map<String, Spinner> spinners,
        final SQLiteDatabase db,
        final MachineSettings machine
    ){
        new CascadingBuildingDropDownTask(
                machine.getBuilding().getValue(),
                db,
                new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                    @Override
                    public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                        Log.i(TAG, "+++ Exec building cascade +++");
                        ArrayAdapter<TextValue> buildingAdapter = new ArrayAdapter<>(
                                context,
                                android.R.layout.simple_spinner_item,
                                result.get(HospitalContract.TABLE_BUILDING_NAME)
                        );

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

                        buildingAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
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

                        spinners.get(HospitalContract.TABLE_BUILDING_NAME)
                                .setAdapter(buildingAdapter);
                        spinners.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
                                .setAdapter(floorAdapter);
                        spinners.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                .setAdapter(departmentAdapter);
                        spinners.get(HospitalContract.TABLE_ROOM_NAME)
                                .setAdapter(roomAdapter);
                    }
                }
        ).execute();
    }

    public static Map<String, Spinner> initMachineSpinners(Activity activity){
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

    public static void initDropdownListeners(
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
                            Log.i(TAG, "+++ room selected +++");
                            TextValue item = (TextValue) adapterView.getSelectedItem();
                            machine.setRoom(item);
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
                            Log.i(TAG, "+++ machine status selected +++");
                            TextValue item = (TextValue) adapterView.getSelectedItem();
                            machine.setMachineStatus(item);
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

    /**
     * Takes passed {@param result} parameter and applies the results into the appropriate
     * list view slot found in {@param widgetPosition} and then applies that to the overall
     * list view of {@param viewGroup}
     *
     * applyBuildingCascade is used to cascade from building table
     * @param viewGroup - List view representation
     * @param widgetPosition - Hashmap where table name is key and position in list view is value
     * @param result - Hashmap
     */
    public static void applyBuildingCascade(
            ViewGroup viewGroup,
            HashMap<String, Integer> widgetPosition,
            HashMap<String, ArrayList<TextValue>> result
    )
    {
        // Property spinners
        Spinner floorSpinner;
        Spinner departmentSpinner;
        Spinner roomSpinner;

        // Extract array of text values based on table
        ArrayList<TextValue> floorArray = result.get(
                HospitalContract.TABLE_BUILDING_FLOOR_NAME
        );
        ArrayList<TextValue> departmentArray = result.get(
                HospitalContract.TABLE_DEPARTMENT_NAME
        );
        ArrayList<TextValue> roomArray = result.get(
                HospitalContract.TABLE_ROOM_NAME
        );

        // Get list view position based on table
        View floorView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
        );
        View departmentView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_DEPARTMENT_NAME)
        );
        View roomView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_ROOM_NAME)
        );

        // Apply text values extracted above
        ArrayAdapter<TextValue> floorAdapter = new ArrayAdapter<>(
                floorView.getContext(),
                android.R.layout.simple_spinner_item,
                floorArray
        );
        ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
                departmentView.getContext(),
                android.R.layout.simple_spinner_item,
                departmentArray
        );
        ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                roomView.getContext(),
                android.R.layout.simple_spinner_item,
                roomArray
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

        floorSpinner = floorView.findViewById(R.id.spinner);
        departmentSpinner = departmentView.findViewById(R.id.spinner);
        roomSpinner = roomView.findViewById(R.id.spinner);

        floorSpinner.setAdapter(floorAdapter);
        departmentSpinner.setAdapter(departmentAdapter);
        roomSpinner.setAdapter(roomAdapter);
    }

    /**
     * Takes passed {@param result} parameter and applies the results into the appropriate
     * list view slot found in {@param widgetPosition} and then applies that to the overall
     * list view of {@param viewGroup}
     *
     * applyFloorCascade is used to cascade from floor table
     * @param viewGroup - List view representation
     * @param widgetPosition - Hashmap where table name is key and position in list view is value
     * @param result - Hashmap
     */
    public static void applyFloorCascade(
            ViewGroup viewGroup,
            HashMap<String, Integer> widgetPosition,
            HashMap<String, ArrayList<TextValue>> result
    ){
        Spinner departmentSpinner;
        Spinner roomSpinner;

        ArrayList<TextValue> departmentArray = result.get(
                HospitalContract.TABLE_DEPARTMENT_NAME
        );
        ArrayList<TextValue> roomArray = result.get(
                HospitalContract.TABLE_ROOM_NAME
        );

        View departmentView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_DEPARTMENT_NAME)
        );
        View roomView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_ROOM_NAME)
        );


        ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
                departmentView.getContext(),
                android.R.layout.simple_spinner_item,
                departmentArray
        );
        ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                roomView.getContext(),
                android.R.layout.simple_spinner_item,
                roomArray
        );


        departmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        roomAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        departmentSpinner = departmentView.findViewById(R.id.spinner);
        roomSpinner = roomView.findViewById(R.id.spinner);

        departmentSpinner.setAdapter(departmentAdapter);
        roomSpinner.setAdapter(roomAdapter);
    }

    /**
     * Takes passed {@param result} parameter and applies the results into the appropriate
     * list view slot found in {@param widgetPosition} and then applies that to the overall
     * list view of {@param viewGroup}
     *
     * applyDepartmentCascade is used to cascade from department table
     * @param viewGroup - List view representation
     * @param widgetPosition - Hashmap where table name is key and position in list view is value
     * @param result - Hashmap
     */
    public static void applyDepartmentCascade(
            ViewGroup viewGroup,
            HashMap<String, Integer> widgetPosition,
            Cursor result
    ){
        Spinner roomSpinner;
        ArrayList<TextValue> roomArray = new ArrayList<>(result.getCount());

        while(result.moveToNext()){
            String text = result.getString(
                    result.getColumnIndex("room_name")
            );
            String value = result.getString(
                    result.getColumnIndex("_id")
            );

            roomArray.add(new TextValue(text, value));
        }

        View roomView = viewGroup.getChildAt(
                widgetPosition.get(HospitalContract.TABLE_ROOM_NAME)
        );

        ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
                roomView.getContext(),
                android.R.layout.simple_spinner_item,
                roomArray
        );

        roomAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        roomSpinner = roomView.findViewById(R.id.spinner);
        roomSpinner.setAdapter(roomAdapter);
    }
}
