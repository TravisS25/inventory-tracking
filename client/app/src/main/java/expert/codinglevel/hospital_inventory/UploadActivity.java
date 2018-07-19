package expert.codinglevel.hospital_inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.adapter.MachineUploadAdapter;
import expert.codinglevel.hospital_inventory.interfaces.IJsonRequestCallback;
import expert.codinglevel.hospital_inventory.json.CustomJsonObjectRequest;
import expert.codinglevel.hospital_inventory.json.JsonRequest;
import expert.codinglevel.hospital_inventory.json.JsonResponses;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.model.MachineJson;
import expert.codinglevel.hospital_inventory.setting.Preferences;
import expert.codinglevel.hospital_inventory.task.DeleteDatabaseTask;
import expert.codinglevel.hospital_inventory.task.ReadDatabaseTask;
import expert.codinglevel.hospital_inventory.task.RetrieveDatabaseTask;
import expert.codinglevel.hospital_inventory.view.TextValue;


public class UploadActivity extends AppCompatActivity{
    public static final String TAG = UploadActivity.class.getSimpleName();
    private static final boolean DEBUG = true;
    private static final int LOADER_ID = 1;
    private HashMap<String, String> mHeaders = new HashMap<>();
    private AlertDialog mDialog;
    private SQLiteDatabase mDB;
    private MachineUploadAdapter mAdapter = null;
    private ArrayList<Machine> mMachineList;
    private ArrayList<MachineJson> mMachineJsonList;
    private RequestQueue mQueue;
    private String mCSRFToken;
    private String mCookie;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("machineList", mMachineList);
        savedInstanceState.putParcelableArrayList("machineJsonList", mMachineJsonList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);
        initAlertDialog();
        mQueue = Volley.newRequestQueue(this);

        if(savedInstanceState != null){
            mMachineList = savedInstanceState.getParcelableArrayList("machineList");
        }

        if(mMachineList == null){
            new RetrieveDatabaseTask(
                    this,
                    new IAsyncResponse<SQLiteDatabase>() {
                        @Override
                        public void processFinish(SQLiteDatabase result) {
                            mDB = result;
                            queryMachineList();
                            initButtonListener();
                        }
                    }
            ).execute();
        }
        else{
            mAdapter = new MachineUploadAdapter(this, mMachineList);
            initButtonListener();
            initListView();
        }
    }

    // initAlertDialog creates instance of dialog to use when the user
    // clicks the upload button as the dialog will verify whether they want
    // to upload or not
    private void initAlertDialog(){
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.upload_message)
                .setTitle(R.string.upload);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Gson gson = new Gson();
                JSONObject jsonObject = new JSONObject();
                String session;
                String jsonMachineList = gson.toJson(mMachineJsonList);

                try{
                    jsonObject.put("machineList", jsonMachineList);
                    session = Preferences.getDefaults(
                            activity,
                            activity.getString(R.string.user_session)
                    );
                    jsonObject.put(getString(R.string.user_session), session);
                }
                catch (JSONException e){
                    e.printStackTrace();
                    return;
                }

                Toast.makeText(
                        getApplicationContext(),
                        "Uploading...",
                        Toast.LENGTH_LONG).show();

                CustomJsonObjectRequest jsonObjectRequest = new CustomJsonObjectRequest(
                        Request.Method.POST,
                        getString(R.string.host_url) + "/api/machine/upload",
                        jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Machines uploaded",
                                        Toast.LENGTH_LONG).show();

                                new DeleteDatabaseTask(
                                        HospitalContract.TABLE_MACHINE_NAME,
                                        null,
                                        null,
                                        mDB,
                                        new IAsyncResponse<Integer>() {
                                            @Override
                                            public void processFinish(Integer result) {
                                                mDB.close();
                                                Intent intent = new Intent(
                                                        activity,
                                                        DashboardActivity.class
                                                );
                                                startActivity(intent);
                                            }
                                        }
                                ).execute();
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

                                Toast.makeText(
                                        getApplicationContext(),
                                        error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        },
                        mHeaders
                );
                mQueue.add(jsonObjectRequest);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        mDialog = builder.create();
    }

    private void initButtonListener() {
        final Activity activity = this;
        Button button = (Button) findViewById(R.id.action_button);
        button.setText(getString(R.string.upload));
        button.setBackgroundColor(Color.GREEN);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hostUrl = activity.getString(R.string.host_url);
                String uploadUrl = hostUrl + "/api/machine/upload/";
                JsonObjectRequest requestObject = JsonRequest.getJSONRequestTokenObject(
                    activity,
                    mHeaders,
                    new IJsonRequestCallback<JSONObject>() {
                        @Override
                        public void successCallback(JSONObject response) {
                            mDialog.show();
                        }

                        @Override
                        public void errorCallback(Context context, VolleyError error) {
                            JsonResponses.volleyError(context, error);
                        }
                    },
                    uploadUrl
                );
                mQueue.add(requestObject);
            }
        });
    }

    private void initListView(){
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Machine machine = (Machine) parent.getAdapter().getItem(position);

                Intent detailIntent = new Intent(
                        getApplicationContext(),
                        MachineDetailsActivity.class
                );

                detailIntent.putExtra("machine", machine);
                startActivity(detailIntent);
            }
        });
    }

    private void queryMachineList(){
        final Activity activity = this;
        String query =
                "select " +
                    "machine._id, " +
                    "machine.building_id, " +
                    "machine.department_id, " +
                    "machine.floor_id, " +
                    "machine.machine_status_id, " +
                    "machine.machine_name, " +
                    "machine_status.status_name, " +
                    "machine.scanned_time, " +
                    "building.building_name, " +
                    "department.department_name, " +
                    "floor.floor " +
                "from " +
                    "machine " +
                "join " +
                    "building on machine.building_id = building._id " +
                "join " +
                    "department on machine.department_id = department._id " +
                "join " +
                    "floor on machine.floor_id = floor._id " +
                "join " +
                    "machine_status on machine.machine_status_id = machine_status._id " +
                "order by machine.scanned_time desc";

        new ReadDatabaseTask(
            query,
            null,
            mDB,
            new IAsyncResponse<Cursor>() {
                @Override
                public void processFinish(Cursor result) {
                    mMachineList = new ArrayList<>(result.getCount());
                    mMachineJsonList = new ArrayList<>(result.getCount());
                    while(result.moveToNext()){
                        String id = result.getString(result.getColumnIndex("_id"));
                        String buildingID = result.getString(
                                result.getColumnIndex("building_id")
                        );
                        String floorID = result.getString(
                                result.getColumnIndex("floor_id")
                        );
                        String departmentID = result.getString(
                                result.getColumnIndex("department_id")
                        );
                        String roomID = result.getString(
                                result.getColumnIndex("room_id")
                        );
                        String machineStatusID = result.getString(
                                result.getColumnIndex("machine_status_id")
                        );
                        String machineName = result.getString(
                                result.getColumnIndex("machine_name")
                        );
                        String buildingName = result.getString(
                                result.getColumnIndex("building_name")
                        );
                        String floor = result.getString(
                                result.getColumnIndex("floor")
                        );
                        String departmentName = result.getString(
                                result.getColumnIndex("department_name")
                        );
                        String roomName = result.getString(
                                result.getColumnIndex("room_name")
                        );
                        String machineStatusName = result.getString(
                                result.getColumnIndex("status_name")
                        );
                        String scannedName = result.getString(
                                result.getColumnIndex("scanned_time")
                        );

                        Machine machine = new Machine();
                        machine.setAssetTag(new TextValue(machineName, id));
                        machine.setBuilding(new TextValue(buildingName, buildingID));
                        machine.setFloor(new TextValue(floor, floorID));
                        machine.setDepartment(new TextValue(departmentName, departmentID));
                        machine.setRoom(new TextValue(roomName, roomID));
                        machine.setMachineStatus(new TextValue(machineStatusName, machineStatusID));
                        mMachineList.add(machine);

                        MachineJson machineJson = new MachineJson(
                                machineName,
                                scannedName,
                                buildingID,
                                floorID,
                                departmentID,
                                roomID,
                                machineStatusID
                        );
                        mMachineJsonList.add(machineJson);
                    }
                    result.close();

                    mAdapter = new MachineUploadAdapter(activity, mMachineList);
                    initListView();
                }
            }
        ).execute();
    }
}




