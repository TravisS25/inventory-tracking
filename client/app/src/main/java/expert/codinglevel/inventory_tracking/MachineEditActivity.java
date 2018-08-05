package expert.codinglevel.inventory_tracking;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

//import expert.codinglevel.inventory_tracking.adapter.MachineEditAdapter;
import expert.codinglevel.inventory_tracking.activityutil.DBActivity;
import expert.codinglevel.inventory_tracking.interfaces.IDatabaseCallback;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.model.MachineJson;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.model.Machine;
//import expert.codinglevel.inventory_tracking.model.MachineProperties;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.task.UpdateDatabaseTask;
import expert.codinglevel.inventory_tracking.widget.CascadingDropDown;


public class MachineEditActivity extends DBActivity {
    public static final String TAG = MachineEditActivity.class.getSimpleName();
    //private SQLiteDatabase mDB;
    private String mUserSession;
    private boolean mServerEdit;
    private Machine mMachine;
    //private boolean mDBHasStopped = false;
    private Map<String, Spinner> mSpinnerMap;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("machine", mMachine);
        savedInstanceState.putBoolean("serverEdit", mServerEdit);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "+++ onCreate +++");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);

        if(savedInstanceState != null){
            mMachine = savedInstanceState.getParcelable("machine");
            mServerEdit = savedInstanceState.getBoolean("serverEdit");
        } else{
            mMachine = getIntent().getParcelableExtra("machine");
            mServerEdit = getIntent().getBooleanExtra("serverEdit", false);
        }

        if(mServerEdit){
            mUserSession = Preferences.getDefaults(
                    getApplicationContext(),
                    getString(R.string.user_session)
            );
        }

        initMachineTitle();
        mSpinnerMap = CascadingDropDown.initMachineSpinners(this);
        initEditButton();
        initDB(new IDatabaseCallback() {
            @Override
            public void finished() {
                CascadingDropDown.initDropdownListeners(
                        getApplicationContext(),
                        mSpinnerMap,
                        mDB,
                        mMachine
                );
            }
        });
    }

//    @Override
//    protected void onResume(){
//        Log.i(TAG, "+++ onResume +++");
//        super.onResume();
//
//        if(mDBHasStopped){
//            Log.i(TAG, "+++ retrieve db +++");
//            retrieveDB();
//        }
//
//        mDBHasStopped = false;
//    }
//
//    @Override
//    protected void onStop(){
//        Log.i(TAG, "+++ onStop +++");
//        super.onStop();
//        mDB.close();
//        mDBHasStopped = true;
//    }
//
//    private void retrieveDB(){
//        if(!mDB.isOpen()){
//            new RetrieveDatabaseTask(getApplicationContext(), new IAsyncResponse<SQLiteDatabase>() {
//                @Override
//                public void processFinish(SQLiteDatabase result) {
//                    mDB = result;
//                }
//            }).execute();
//        }
//    }

//    private void initCascadingSettings(){
//        new RetrieveDatabaseTask(
//            getApplicationContext(),
//            new IAsyncResponse<SQLiteDatabase>() {
//                @Override
//                public void processFinish(SQLiteDatabase result) {
//                    mDB = result;
//                    CascadingDropDown.initDropdownSettings(
//                        getApplicationContext(),
//                        mSpinnerMap,
//                        result,
//                        mMachine
//                    );
//                }
//            }
//        ).execute();
//    }

//    private void initSpinnerMap(){
//        Spinner buildingSpinner = (Spinner) findViewById(R.id.building_spinner);
//        Spinner floorSpinner = (Spinner) findViewById(R.id.floor_spinner);
//        Spinner departmentSpinner = (Spinner) findViewById(R.id.department_spinner);
//        Spinner roomSpinner = (Spinner) findViewById(R.id.room_spinner);
//        Spinner machineStatusSpinner = (Spinner) findViewById(R.id.machine_status_spinner);
//
//        mSpinnerMap.put(HospitalContract.TABLE_BUILDING_NAME, buildingSpinner);
//        mSpinnerMap.put(HospitalContract.TABLE_BUILDING_FLOOR_NAME, floorSpinner);
//        mSpinnerMap.put(HospitalContract.TABLE_DEPARTMENT_NAME, departmentSpinner);
//        mSpinnerMap.put(HospitalContract.TABLE_ROOM_NAME, roomSpinner);
//        mSpinnerMap.put(HospitalContract.TABLE_MACHINE_STATUS_NAME, machineStatusSpinner);
//    }

    private void initMachineTitle(){
        TextView machineTitle = (TextView) findViewById(R.id.machine_title);
        machineTitle.setText(mMachine.getMachineName().getValue());
    }

//    private void applyDropdownValues(){
//        new RetrieveDatabaseTask(
//                this,
//                new IAsyncResponse<SQLiteDatabase>() {
//                    @Override
//                    public void processFinish(SQLiteDatabase result) {
//                        mDB = result;
//                        new CascadingBuildingDropDownTask(
//                                mMachine,
//                                mDB,
//                                new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
//                                    @Override
//                                    public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
//                                        ArrayAdapter<TextValue> buildingAdapter = new ArrayAdapter<>(
//                                                getApplicationContext(),
//                                                android.R.layout.simple_spinner_item,
//                                                result.get(HospitalContract.TABLE_BUILDING_NAME)
//                                        );
//
//                                        ArrayAdapter<TextValue> floorAdapter = new ArrayAdapter<>(
//                                                getApplicationContext(),
//                                                android.R.layout.simple_spinner_item,
//                                                result.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
//                                        );
//
//                                        ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
//                                                getApplicationContext(),
//                                                android.R.layout.simple_spinner_item,
//                                                result.get(HospitalContract.TABLE_DEPARTMENT_NAME)
//                                        );
//
//                                        ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
//                                                getApplicationContext(),
//                                                android.R.layout.simple_spinner_item,
//                                                result.get(HospitalContract.TABLE_ROOM_NAME)
//                                        );
//
//                                        buildingAdapter.setDropDownViewResource(
//                                                android.R.layout.simple_spinner_dropdown_item
//                                        );
//                                        floorAdapter.setDropDownViewResource(
//                                                android.R.layout.simple_spinner_dropdown_item
//                                        );
//                                        departmentAdapter.setDropDownViewResource(
//                                                android.R.layout.simple_spinner_dropdown_item
//                                        );
//                                        roomAdapter.setDropDownViewResource(
//                                                android.R.layout.simple_spinner_dropdown_item
//                                        );
//
//                                        mBuildingSpinner.setAdapter(buildingAdapter);
//                                        mFloorSpinner.setAdapter(floorAdapter);
//                                        mDepartmentSpinner.setAdapter(departmentAdapter);
//                                        mRoomSpinner.setAdapter(roomAdapter);
//                                    }
//                                }
//                        ).execute();
//
//                        new ReadDatabaseTask(
//                                HospitalDbHelper.getAllMachineStatuses(),
//                                null,
//                                mDB,
//                                new IAsyncResponse<Cursor>() {
//                                    @Override
//                                    public void processFinish(Cursor result) {
//                                        ArrayList<TextValue> machineStatusArray = new ArrayList<>();
//
//                                        while(result.moveToNext()){
//                                            String text = result.getString(
//                                                    result.getColumnIndex("status_name")
//                                            );
//                                            String value = result.getString(
//                                                    result.getColumnIndex("_id")
//                                            );
//                                            machineStatusArray.add(new TextValue(text, value));
//                                        }
//
//                                        ArrayAdapter<TextValue> machineStatusAdapter = new ArrayAdapter<>(
//                                                getApplication(),
//                                                android.R.layout.simple_spinner_item,
//                                                machineStatusArray
//                                        );
//
//                                        mMachineStatusSpinner.setAdapter(machineStatusAdapter);
//                                    }
//                                }
//                        ).execute();
//                    }
//                }
//        ).execute();
//    }
//
//    // initDropdowns inits machine dropdown spinners along with adding
//    // event handlers on item select
//    private void initDropdowns(){
//        mBuildingSpinner = (Spinner) findViewById(R.id.building_spinner);
//        mFloorSpinner = (Spinner) findViewById(R.id.floor_spinner);
//        mDepartmentSpinner = (Spinner) findViewById(R.id.department_spinner);
//        mRoomSpinner = (Spinner) findViewById(R.id.room_spinner);
//        mMachineStatusSpinner = (Spinner) findViewById(R.id.machine_status_spinner);
//
//        mBuildingSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextValue item = (TextValue) adapterView.getSelectedItem();
//                mMachine.setBuilding(item);
//
//                new CascadingBuildingDropDownTask(
//                        item.getValue(),
//                        mDB,
//                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
//                            @Override
//                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
//                                ArrayAdapter<TextValue> floorAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
//                                );
//
//                                ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_DEPARTMENT_NAME)
//                                );
//
//                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_ROOM_NAME)
//                                );
//
//                                floorAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//                                departmentAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//                                roomAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//
//                                mFloorSpinner.setAdapter(floorAdapter);
//                                mDepartmentSpinner.setAdapter(departmentAdapter);
//                                mRoomSpinner.setAdapter(roomAdapter);
//                            }
//                        }
//                );
//            }
//        });
//
//        mFloorSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextValue item = (TextValue) adapterView.getSelectedItem();
//                mMachine.setFloor(item);
//
//                new CascadingBuildingDropDownTask(
//                        item.getValue(),
//                        mDB,
//                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
//                            @Override
//                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
//                                ArrayAdapter<TextValue> departmentAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_DEPARTMENT_NAME)
//                                );
//
//                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_ROOM_NAME)
//                                );
//
//                                departmentAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//                                roomAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//
//                                mDepartmentSpinner.setAdapter(departmentAdapter);
//                                mRoomSpinner.setAdapter(roomAdapter);
//                            }
//                        }
//                );
//            }
//        });
//
//        mDepartmentSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextValue item = (TextValue) adapterView.getSelectedItem();
//                mMachine.setDepartment(item);
//
//                new CascadingBuildingDropDownTask(
//                        item.getValue(),
//                        mDB,
//                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
//                            @Override
//                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
//                                ArrayAdapter<TextValue> roomAdapter = new ArrayAdapter<>(
//                                        getApplicationContext(),
//                                        android.R.layout.simple_spinner_item,
//                                        result.get(HospitalContract.TABLE_ROOM_NAME)
//                                );
//
//                                roomAdapter.setDropDownViewResource(
//                                        android.R.layout.simple_spinner_dropdown_item
//                                );
//
//                                mRoomSpinner.setAdapter(roomAdapter);
//                            }
//                        }
//                );
//            }
//        });
//
//        mRoomSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextValue item = (TextValue) adapterView.getSelectedItem();
//                mMachine.setRoom(item);
//            }
//        });
//
//        mMachineStatusSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextValue item = (TextValue) adapterView.getSelectedItem();
//                mMachine.setMachineStatus(item);
//            }
//        });
//    }

    // initEditButton adds event handler to edit button to
    // edit machine properties
    private void initEditButton(){
        Button button = (Button) findViewById(R.id.edit_button);
        button.setText("Edit");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editButton(v);
            }
        });
    }

    // editButton edits the current machine properties depending on whether
    // the current machine properties are local or from server
    public void editButton(final View view){
        final JSONObject jsonObject;
        Log.i(TAG, "+++ Server edit" + mServerEdit +" +++");
        final String id = mMachine.getMachineName().getValue();

        // If we got to this activity edit from a scan look up, that means the current
        // machine we are editing is from the server so convert current machine properties
        // to json format and send to server
        //
        // Else this edit is local so update local db with machine properties
        if(mServerEdit){
            final RequestQueue queue = Volley.newRequestQueue(this);
            Gson gson = new Gson();
            String jsonString = gson.toJson(mMachine, MachineJson.class);
            try{
                jsonObject = new JSONObject(jsonString);
            }
            catch (JSONException ex){
                ex.printStackTrace();
                return;
            }

            final Map<String, String> headers = new HashMap<>();
            headers.put(getString(R.string.user_session), mUserSession);
            CustomJsonObjectRequest getRequest = new CustomJsonObjectRequest(
                    Request.Method.GET,
                    getString(R.string.host_url) + "/api/machine/edit/" + id + "/",
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                                    Request.Method.POST,
                                    getString(R.string.host_url) + "/api/machine/edit/" + id + "/",
                                    jsonObject,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Machine edited",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(
                                                    getApplicationContext(),
                                                    LookUpActivity.class
                                            );
                                            intent.putExtra("machineJson", response.toString());
                                            startActivity(intent);
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if(error.getCause() == null){
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Unexpected error has occurred, please try again later",
                                                        Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            if(error.getCause() instanceof ConnectException){
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Error connecting to server, try again later",
                                                        Toast.LENGTH_LONG).show();
                                                return;
                                            }

                                            final int statusCode = error.networkResponse.statusCode;
                                            if(statusCode == HttpURLConnection.HTTP_NOT_FOUND){
                                                Toast.makeText(
                                                        getApplicationContext(),
                                                        "Machine not found",
                                                        Toast.LENGTH_SHORT).show();

                                                return;
                                            }

                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Unexpected error has occurred, please try again later",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    },
                                    headers
                            );
                            queue.add(request);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.getCause() == null){
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Unexpected error has occurred, please try again later",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            if(error.getCause() instanceof ConnectException){
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Error connecting to server, try again later",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            final int statusCode = error.networkResponse.statusCode;
                            if(statusCode == HttpURLConnection.HTTP_NOT_FOUND){
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Machine not found",
                                        Toast.LENGTH_SHORT).show();

                                return;
                            }

                            Toast.makeText(
                                    getApplicationContext(),
                                    "Unexpected error has occurred, please try again later",
                                    Toast.LENGTH_LONG).show();
                        }
                    },
                    headers,
                    new String[]{getString(R.string.csrf_token), getString(R.string.cookie)}
            );

            queue.add(getRequest);
        }
        else{
            ContentValues contentValues = new ContentValues();
            contentValues.put("room_id", mMachine.getRoom().getValue());
            contentValues.put("machine_status_id", mMachine.getMachineStatus().getValue());

            new UpdateDatabaseTask(
                    HospitalContract.TABLE_MACHINE_NAME,
                    "_id=?",
                    new String[]{id},
                    contentValues,
                    mDB,
                    new IAsyncResponse<Integer>() {
                        @Override
                        public void processFinish(Integer result) {
                            mDB.close();
                            Intent intent = new Intent(view.getContext(), MachineListActivity.class);
                            intent.putExtra("toast", "Machine Edited");
                            startActivity(intent);
                        }
                    }
            ).execute();
        }
    }
}
