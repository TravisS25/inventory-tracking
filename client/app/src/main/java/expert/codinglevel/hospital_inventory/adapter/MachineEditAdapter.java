package expert.codinglevel.hospital_inventory.adapter;

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

import expert.codinglevel.hospital_inventory.R;
import expert.codinglevel.hospital_inventory.enums.MachineAttribute;
import expert.codinglevel.hospital_inventory.enums.ViewType;
import expert.codinglevel.hospital_inventory.interfaces.IMachine;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.task.ReadDatabaseTask;
import expert.codinglevel.hospital_inventory.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.hospital_inventory.task.cascadingdropdown.CascadingFloorDropDownTask;
import expert.codinglevel.hospital_inventory.view.TextValue;
import expert.codinglevel.hospital_inventory.widget.CascadingDropDown;

public class MachineEditAdapter extends BaseAdapter {
    public final static String TAG = MachineEditAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private ArrayList<Machine.MachineProperty> mDataSource;
    private IMachine mMachine;
    private HashMap<String, Integer> mWidgetPosition;
    private SQLiteDatabase mDB;
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

        if(spinnerHolder != null){
            spinnerHolder.mPropertyTextView.setText(machineProperty.getPropertyText());
            ArrayList<TextValue> spinnerArray = machineProperty.getSpinnerArrayList();
            Log.i(TAG, spinnerHolder.mPropertyTextView.getText().toString());
            ArrayAdapter<TextValue> adapter = new ArrayAdapter<>(
                    convertView.getContext(),
                    android.R.layout.simple_spinner_item,
                    spinnerArray
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerHolder.mSpinner.setAdapter(adapter);

            if(machineProperty.getMachineAttribute() == MachineAttribute.BUILDING){
                spinnerHolder.mSpinner.setOnItemSelectedListener(
                    new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final AdapterView<?> parent, View view, int position, long id) {
                            if(!mInitLoad.get(HospitalContract.TABLE_BUILDING_NAME)){
                                Log.i(TAG, "+++ building selected +++");
                                TextValue item = (TextValue) parent.getSelectedItem();
                                mMachine.setBuilding(item);

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
                            TextValue item = (TextValue) parent.getSelectedItem();
                            mMachine.setFloor(item);

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
                            TextValue item = (TextValue) parent.getSelectedItem();
                            mMachine.setDepartment(item);

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
