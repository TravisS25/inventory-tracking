package expert.codinglevel.hospital_inventory;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.adapter.MachineEditAdapter;
import expert.codinglevel.hospital_inventory.enums.MachineAttribute;
import expert.codinglevel.hospital_inventory.json.CustomJsonObjectRequest;
import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;
import expert.codinglevel.hospital_inventory.model.MachineJson;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.model.MachineProperties;
import expert.codinglevel.hospital_inventory.setting.UserActivity;
import expert.codinglevel.hospital_inventory.task.MultipleReadDBTask;
import expert.codinglevel.hospital_inventory.task.RetrieveDatabaseTask;
import expert.codinglevel.hospital_inventory.task.UpdateDatabaseTask;
import expert.codinglevel.hospital_inventory.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.hospital_inventory.view.TextValue;


public class MachineEditActivity extends UserActivity{
    public static final String TAG = MachineEditActivity.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int LOADER_ID = 1;
    private SQLiteDatabase mDB;
    private ArrayList<Machine.MachineProperty> mPropertyList = new ArrayList<>();
    private HashMap<String, String> mHeaders = new HashMap<>();
    private String mCSRFToken, mCookie;
    private Bundle mBundle;
    private Machine mMachine;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Machine machine = new Machine(mMachine);
        savedInstanceState.putString(getString(R.string.csrf_token), mCSRFToken);
        savedInstanceState.putString(getString(R.string.cookie), mCookie);
        savedInstanceState.putParcelable("machine", machine);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "+++ onCreate +++");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);
        mBundle = savedInstanceState;

//        if (savedInstanceState != null){
//            mHeaders.put(getString(R.string.csrf_token), savedInstanceState.getString(getString(R.string.csrf_token)));
//            mHeaders.put(getString(R.string.cookie), savedInstanceState.getString(getString(R.string.cookie)));
//
//        } else{
//            mBundle = null;
//        }

        initMachine();
        initEditButton();
    }

    @Override
    protected void onResume(){
        Log.i(TAG, "+++ onResume +++");
        super.onResume();
        final Activity activity = this;

        // Retrieve database instance and use that instance
        // to retrieve our machine instance that we want along with
        // querying for our cascading drop downs
        new RetrieveDatabaseTask(
            this,
            new IAsyncResponse<SQLiteDatabase>() {
                @Override
                public void processFinish(SQLiteDatabase result) {
                    mDB = result;
                    new CascadingBuildingDropDownTask(
                        mMachine,
                        mDB,
                        new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                            @Override
                            public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                                Log.i(TAG, result.toString());

                                // Adds result to mPropertyList
                                MachineProperties.addCascadingProperties(
                                    activity,
                                    result,
                                    mPropertyList
                                );

                                // This is query statuses from db and add to property list
                                new MultipleReadDBTask(
                                    HospitalDbHelper.getMachineDatabaseReadList(mMachine),
                                    mDB,
                                    new IAsyncResponse<HashMap<String, Cursor>>() {
                                        @Override
                                        public void processFinish(HashMap<String, Cursor> result) {
                                            // Adds result to mPropertyList
                                            MachineProperties.addProperties(
                                                    activity,
                                                    result,
                                                    mPropertyList
                                            );
                                            initListAdapter();
                                        }
                                    }
                                ).execute();
                            }
                        }
                    ).execute();
                }
            }
        ).execute();
    }

    @Override
    protected void onStop(){
        Log.i(TAG, "+++ onStop +++");
        super.onStop();
        mDB.close();
    }

    // initMachine gets machine properties from intent and
    // wraps the asset tag and scan time property to Machine Property
    private void initMachine(){
        mMachine = getIntent().getParcelableExtra("machine");
        mPropertyList.add(new Machine.MachineProperty(
                getString(R.string.machine_name_text),
                mMachine.getMachineName().getText()
            )
        );
        mPropertyList.add(new Machine.MachineProperty(
                getString(R.string.scan_time),
                mMachine.getScannedTime()
            )
        );
    }

    // initEditButton adds event handler to edit button to
    // edit machine properties
    private void initEditButton(){
        Button button = (Button) findViewById(R.id.action_button);
        button.setText(getText(R.string.edit));
        button.setBackgroundColor(Color.BLUE);
        button.setTextColor(Color.WHITE);
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
        final Activity activity = this;
        boolean serverEdit = getIntent().getBooleanExtra("serverEdit", false);
        Log.i(TAG, "+++ Server edit" + serverEdit +" +++");
        String id = mMachine.getMachineName().getValue();
        final String machineName = mMachine.getMachineName().getText();
        String buildingID = mMachine.getBuilding().getValue();
        String floorID = mMachine.getFloor().getValue();
        String departmentID = mMachine.getDepartment().getValue();
        String roomID = mMachine.getRoom().getValue();
        String machineStatusID = mMachine.getMachineStatus().getValue();

        // If we got to this activity edit from a scan look up, that means the current
        // machine we are editing is from the server so convert current machine properties
        // to json format and send to server
        //
        // Else this edit is local so update local db with machine properties
        if(serverEdit){
            final RequestQueue queue = Volley.newRequestQueue(this);
            Gson gson = new Gson();
            MachineJson machine = new MachineJson(
                    machineName,
                    "",
                    buildingID,
                    floorID,
                    departmentID,
                    roomID,
                    machineStatusID
            );
            String jsonString = gson.toJson(machine, MachineJson.class);
            try{
                jsonObject = new JSONObject(jsonString);
            }
            catch (JSONException ex){
                ex.printStackTrace();
                return;
            }

            CustomJsonObjectRequest getRequest = new CustomJsonObjectRequest(
                    Request.Method.GET,
                    getString(R.string.host_url) + "/api/machine/edit/" + machineName,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                                    Request.Method.POST,
                                    getString(R.string.host_url) + "/api/machine/edit/" + machineName,
                                    jsonObject,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Machine edited",
                                                    Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(activity, LookUpActivity.class);
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
                                    mHeaders
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
                    mHeaders,
                    new String[]{getString(R.string.csrf_token), getString(R.string.cookie)}
            );

            queue.add(getRequest);
        }
        else{
            ContentValues contentValues = new ContentValues();
            contentValues.put("room_id", roomID);
            contentValues.put("machine_status_id", machineStatusID);

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

    private void initListAdapter(){
        if(mBundle != null){
            Log.i(TAG, "+++ Bundle NOT null +++");
            Machine savedMachine = mBundle.getParcelable("machine");
            mMachine.setBuilding(savedMachine.getBuilding());
            mMachine.setFloor(savedMachine.getFloor());
            mMachine.setDepartment(savedMachine.getDepartment());
            mMachine.setRoom(savedMachine.getRoom());
            mMachine.setMachineStatus(savedMachine.getMachineStatus());
        }

        ListView listView = (ListView) findViewById(R.id.list_view);
        MachineEditAdapter adapter = new MachineEditAdapter(
                this,
                mMachine,
                mDB,
                mPropertyList
        );
        listView.setAdapter(adapter);
    }
}
