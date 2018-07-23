package expert.codinglevel.inventory_tracking.widget;

import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  CascadingDropDown is util class that allows us to apply form views (spinners, checkboxes etc)
 *  to an appropriate position in a list view
 */
public class CascadingDropDown {
    private CascadingDropDown(){}

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
