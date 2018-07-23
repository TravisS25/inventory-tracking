package expert.codinglevel.inventory_tracking.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.enums.MachineAttribute;
import expert.codinglevel.inventory_tracking.enums.ViewType;
import expert.codinglevel.inventory_tracking.interfaces.IMachine;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.model.Machine;
import expert.codinglevel.inventory_tracking.task.ReadDatabaseTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingFloorDropDownTask;
import expert.codinglevel.inventory_tracking.view.TextValue;
import expert.codinglevel.inventory_tracking.widget.CascadingDropDown;

/**
 *  MachineEditAdapter is list adapter used to view machine properties
 *  and have the ability to edit them
 */
public class MachineEditAdapter extends BaseAdapter {
    public final static String TAG = MachineEditAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private ArrayList<Machine.MachineProperty> mDataSource;
    private IMachine mMachine;
    private HashMap<String, Integer> mWidgetPosition;
    private SQLiteDatabase mDB;

    // mInitLoad is used as a "work around" for the cascading dropdowns as the onItemSelected
    // event is activated on initialization even when we don't select item which in turn
    // messes with the cascading dropdowns
    // We use this variable for each table(key) that we need a cascade dropdown for and skip
    // the initialization trigger by setting the value "true" for each table and when the code
    // runs and skips the trigger, we set them all to "false" so future selections will work
    private HashMap<String, Boolean> mInitLoad;

    public MachineEditAdapter(
            Context context,
            IMachine machine,
            SQLiteDatabase db,
            ArrayList<Machine.MachineProperty> items
    )
    {
        mMachine = machine;
        mDataSource = items;
        mDB = db;
        mWidgetPosition = new HashMap<>();
        mInitLoad = new HashMap<>();

        // Set the initial
        mInitLoad.put(HospitalContract.TABLE_BUILDING_NAME, true);
        mInitLoad.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, true);
        mInitLoad.put(HospitalContract.TABLE_DEPARTMENT_NAME, true);
        mInitLoad.put(HospitalContract.TABLE_ROOM_NAME, true);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup outerParent) {
        DefaultViewHolder defaultHolder = null;
        SpinnerViewHolder spinnerHolder = null;
        CheckboxViewHolder checkboxHolder = null;
        final Resources resources = Resources.getSystem();

        // Get corresponding machine property for row
        Machine.MachineProperty machineProperty = (Machine.MachineProperty) getItem(position);

        // check if the view already exists if so, no need to inflate and findViewById again!
        if (convertView == null) {
//            Log.i(TAG, "+++ convertview null +++");

            // Check what view type each machine property has and then display
            // the appropriate layout
            if(machineProperty.getViewType() == ViewType.SPINNER){
                spinnerHolder = new SpinnerViewHolder();
                convertView = mInflater.inflate(R.layout.machine_spinner_item, outerParent, false);
                spinnerHolder.mPropertyTextView = convertView.findViewById(R.id.property_text);
                spinnerHolder.mSpinner = convertView.findViewById(R.id.spinner);
                convertView.setTag(spinnerHolder);
            }
            else if(machineProperty.getViewType() == ViewType.CHECKBOX){
                checkboxHolder = new CheckboxViewHolder();
                convertView = mInflater.inflate(R.layout.machine_checkbox_item, outerParent, false);
                checkboxHolder.mPropertyTextView = convertView.findViewById(R.id.property_text);
                checkboxHolder.mCheckBox = convertView.findViewById(R.id.checkbox);
                convertView.setTag(checkboxHolder);
            }
            else{
                defaultHolder = new DefaultViewHolder();
                convertView = mInflater.inflate(R.layout.machine_detail_item, outerParent, false);
                defaultHolder.mPropertyTextView = convertView.findViewById(R.id.property_text);
                defaultHolder.mPropertyValueView = convertView.findViewById(R.id.property_value);
                convertView.setTag(defaultHolder);
            }
        }
        else {
            // If layout already inflated, still have to check what view type
            // to cast the layout to
            if(machineProperty.getViewType() == ViewType.SPINNER){
                spinnerHolder = (SpinnerViewHolder) convertView.getTag();
            }
            else if(machineProperty.getViewType() == ViewType.CHECKBOX){
                checkboxHolder = (CheckboxViewHolder) convertView.getTag();
            }
            else{
                defaultHolder = (DefaultViewHolder) convertView.getTag();
            }
        }

        // If spinnerHolder is not null, then current layout view contains spinner
        if(spinnerHolder != null){
            spinnerHolder.mPropertyTextView.setText(machineProperty.getPropertyText());
            ArrayList<TextValue> spinnerArray = machineProperty.getSpinnerArrayList();
            //Log.i(TAG, spinnerHolder.mPropertyTextView.getText().toString());

            // ArrayAdapter is used for dropdown lists
            ArrayAdapter<TextValue> adapter = new ArrayAdapter<>(
                    convertView.getContext(),
                    android.R.layout.simple_spinner_item,
                    spinnerArray
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHolder.mSpinner.setAdapter(adapter);

            // Check what type machine attribute current machine property has to apply
            // proper cascading dropdown selection
            if(machineProperty.getMachineAttribute() == MachineAttribute.BUILDING){
                spinnerHolder.mSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                            if(!mInitLoad.get(HospitalContract.TABLE_BUILDING_NAME)){
                                Log.i(TAG, "+++ building selected +++");

                                // Extract value from selected value
                                TextValue item = (TextValue) parent.getSelectedItem();
                                mMachine.setBuilding(item);

                                // Perform db query task against selected building item and all
                                // tables attached to building
                                new CascadingBuildingDropDownTask(
                                        item.getValue(),
                                        mDB,
                                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                                            @Override
                                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                                CascadingDropDown.applyBuildingCascade(
                                                        outerParent,
                                                        mWidgetPosition,
                                                        result
                                                );
                                            }
                                        }
                                ).execute();
                            }
                            else{
                                mInitLoad.put(HospitalContract.TABLE_BUILDING_NAME, false);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
            }
            else if(machineProperty.getMachineAttribute() == MachineAttribute.FLOOR){
                mWidgetPosition.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, position);
                spinnerHolder.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!mInitLoad.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)){
                            Log.i(TAG, "+++ floor selected +++");

                            // Extract value from selected value
                            TextValue item = (TextValue) parent.getSelectedItem();
                            mMachine.setFloor(item);

                            // Perform db query task against selected floor item and all
                            // tables attached to floor
                            new CascadingFloorDropDownTask(
                                    item.getValue(),
                                    mDB,
                                    new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                                        @Override
                                        public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                            CascadingDropDown.applyFloorCascade(
                                                    outerParent,
                                                    mWidgetPosition,
                                                    result
                                            );
                                        }
                                    }
                            ).execute();
                        }
                        else{
                            mInitLoad.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, false);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            else if(machineProperty.getMachineAttribute() == MachineAttribute.DEPARTMENT){
                mWidgetPosition.put(HospitalContract.TABLE_DEPARTMENT_NAME, position);
                spinnerHolder.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!mInitLoad.get(HospitalContract.TABLE_DEPARTMENT_NAME)){
                            Log.i(TAG, "+++ department selected +++");

                            // Extract value from selected value
                            TextValue item = (TextValue) parent.getSelectedItem();
                            mMachine.setDepartment(item);

                            // Perform db read task based on selected department to get all the
                            // rooms connected to department instance selected
                            new ReadDatabaseTask(
                                    HospitalDbHelper.getRoomByDepartmentQuery(),
                                    new String[]{item.getValue()},
                                    mDB,
                                    new IAsyncResponse<Cursor>() {
                                        @Override
                                        public void processFinish(Cursor result) {
                                            CascadingDropDown.applyDepartmentCascade(
                                                    outerParent,
                                                    mWidgetPosition,
                                                    result
                                            );
                                        }
                                    }
                            ).execute();
                        }
                        else{
                            mInitLoad.put(HospitalContract.TABLE_DEPARTMENT_NAME, false);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            else if(machineProperty.getMachineAttribute() == MachineAttribute.ROOM){
                mWidgetPosition.put(HospitalContract.TABLE_ROOM_NAME, position);
                spinnerHolder.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(!mInitLoad.get(HospitalContract.TABLE_ROOM_NAME)){
                            Log.i(TAG, "+++ room selected +++");
                            TextValue item = (TextValue) parent.getSelectedItem();
                            mMachine.setRoom(item);
                        }
                        else{
                            mInitLoad.put(HospitalContract.TABLE_ROOM_NAME, false);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
            else if(machineProperty.getMachineAttribute() == MachineAttribute.MACHINE_STATUS){
                spinnerHolder.mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.i(TAG, "+++ machine status selected +++");
                        TextValue machineStatusValue = (TextValue) parent.getSelectedItem();
                        mMachine.setMachineStatus(machineStatusValue);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            // Loop through current spinner and verify which spinner we are currently on
            // by checking the machine attribute property and then set the spinner value based
            // on the machine passed
            for(int i = 0; i < spinnerArray.size(); i++){
//                Log.i(TAG, mMachine.toString());
                if(machineProperty.getMachineAttribute() == MachineAttribute.BUILDING){
                    if(spinnerArray.get(i).getValue().equals(mMachine.getBuilding().getValue())){
                        spinnerHolder.mSpinner.setSelection(i, false);
                        //spinnerHolder.mSpinner.setSelection(i);
                        break;
                    }
                }
                if(machineProperty.getMachineAttribute() == MachineAttribute.FLOOR){
                    if(spinnerArray.get(i).getValue().equals(mMachine.getFloor().getValue())){
                        spinnerHolder.mSpinner.setSelection(i, false);
                        //spinnerHolder.mSpinner.setSelection(i);
                        break;
                    }
                }
                if(machineProperty.getMachineAttribute() == MachineAttribute.DEPARTMENT){
                    if(spinnerArray.get(i).getValue().equals(mMachine.getDepartment().getValue())){
                        spinnerHolder.mSpinner.setSelection(i, false);
                        //spinnerHolder.mSpinner.setSelection(i);
                        break;
                    }
                }
                if(machineProperty.getMachineAttribute() == MachineAttribute.ROOM){
                    if(spinnerArray.get(i).getValue().equals(mMachine.getRoom().getValue())){
                        spinnerHolder.mSpinner.setSelection(i, false);
                        //spinnerHolder.mSpinner.setSelection(i);
                        break;
                    }
                }
                if(machineProperty.getMachineAttribute() == MachineAttribute.MACHINE_STATUS){
                    if(spinnerArray.get(i).getValue().equals(mMachine.getMachineStatus().getValue())){
                        spinnerHolder.mSpinner.setSelection(i, false);
                       //spinnerHolder.mSpinner.setSelection(i);
                        break;
                    }
                }
            }

        }
        else if(checkboxHolder != null){
            checkboxHolder.mPropertyTextView.setText(machineProperty.getPropertyText());
            checkboxHolder.mCheckBox.setText(resources.getString(R.string.yes));
        }
        else if(defaultHolder != null){
            defaultHolder.mPropertyTextView.setText(machineProperty.getPropertyText());
            defaultHolder.mPropertyValueView.setText(machineProperty.getPropertyValue());
        }

        return convertView;
    }

    private static class DefaultViewHolder {
        private TextView mPropertyTextView;
        private TextView mPropertyValueView;
    }

    private static class SpinnerViewHolder{
        private TextView mPropertyTextView;
        private Spinner mSpinner;
    }

    private static class CheckboxViewHolder{
        private TextView mPropertyTextView;
        private CheckBox mCheckBox;
    }
}
