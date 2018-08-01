package expert.codinglevel.inventory_tracking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.net.ConnectException;

import expert.codinglevel.inventory_tracking.adapter.MachineListAdapter;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.model.Machine;
import expert.codinglevel.inventory_tracking.model.MachineJson;
import expert.codinglevel.inventory_tracking.task.DeleteDatabaseTask;
import expert.codinglevel.inventory_tracking.task.ReadDatabaseTask;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  MachineListActivity is list activity that displays all the machine bar codes
 *  that have been scanned along with ability to upload all of them to server
 */
public class MachineListActivity extends AppCompatActivity {
    public static final String TAG = MachineListActivity.class.getSimpleName();
    private boolean mIsSavedInstance = false;
    private boolean mDBHasStopped = false;
    private SQLiteDatabase mDB;
    private RequestQueue mQueue;
   // private ArrayList<MachineJson> mMachineJsonList;
    private MachineListAdapter mAdapter = null;
    private ArrayList<Machine> mMachineList = null;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // This is set to true so every time screen rotates, we know its already
        // been activated
        savedInstanceState.putBoolean("toastActivated", true);
        savedInstanceState.putParcelableArrayList("machineList", mMachineList);
        //savedInstanceState.putParcelableArrayList("machineJsonList", mMachineJsonList);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mQueue = Volley.newRequestQueue(this);
        boolean toastActivated = false;
        String toastMessage = getIntent().getStringExtra("toast");

        if(savedInstanceState != null){
            toastActivated = savedInstanceState.getBoolean("toastActivated");
            mIsSavedInstance = true;
            mMachineList = savedInstanceState.getParcelableArrayList("machineList");
            //mMachineJsonList = savedInstanceState.getParcelableArrayList("machineJsonList");
        }

        // This is to check if toastr has already been activated previously (due to changing
        // screen rotation).  Have to check this or every time user changes screen rotation
        // after initial toastr, message will pop up
        if(toastMessage != null && !toastActivated){
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
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
            mAdapter = new MachineListAdapter(this, mMachineList);
            initButtonListener();
            initListView();
        }
    }

    @Override
    protected void onResume(){
        Log.i(TAG, "+++ onResume +++");
        super.onResume();

        if(mDBHasStopped){
            Log.i(TAG, "+++ retrieve db +++");
            retrieveDB();
        }

        mDBHasStopped = false;
    }

    private void retrieveDB(){
        new RetrieveDatabaseTask(
                getApplicationContext(),
                new IAsyncResponse<SQLiteDatabase>() {
                    @Override
                    public void processFinish(SQLiteDatabase result) {
                        mDB = result;
                    }
                }
        );
    }

    private void initAlertDialog(){
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.upload_message)
                .setTitle(R.string.upload);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Gson gson = new Gson();
                JSONArray jsonArray;
                String machineList = gson.toJson(mMachineList);
                //Log.i(TAG, jsonMachineList);

                try{
                    jsonArray = new JSONArray(machineList);
                }
                catch (JSONException e){
                    e.printStackTrace();
                    return;
                }

                final Toast uploadingToast =  Toast.makeText(
                        getApplicationContext(),
                        "Uploading...",
                        Toast.LENGTH_SHORT
                );
                uploadingToast.show();

                JsonArrayRequest jsonRequestObject = new JsonArrayRequest(
                        Request.Method.POST,
                        getString(R.string.host_url) + "/api/machine/upload",
                        jsonArray,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                uploadingToast.cancel();
                                Toast.makeText(
                                        getApplicationContext(),
                                        "Machines uploaded",
                                        Toast.LENGTH_SHORT).show();

                                Log.i(TAG, "+++ on response +++");

                                if(!mIsSavedInstance){
                                    execDeleteDatabaseTask();
                                }
                                else{
                                    new RetrieveDatabaseTask(
                                            activity,
                                            new IAsyncResponse<SQLiteDatabase>() {
                                                @Override
                                                public void processFinish(SQLiteDatabase result) {
                                                    mDB = result;
                                                    execDeleteDatabaseTask();
                                                }
                                            }
                                    ).execute();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i(TAG, "+++ on error +++");

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

                                Toast.makeText(
                                        getApplicationContext(),
                                        error.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                );
                mQueue.add(jsonRequestObject);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // initButtonListener inits button listener
    private void initButtonListener() {
        Button button = (Button) findViewById(R.id.action_button);
        button.setText(getString(R.string.upload));
        button.setBackgroundColor(Color.GREEN);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            initAlertDialog();
            }
        });
    }

    // execDeleteDatabaseTask executes deleting all scanned bar code
    // machines from db
    private void execDeleteDatabaseTask(){
        final Activity activity = this;
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

    // initListView inits list view along with setting event listener
    // for list to allow user to navigate to detail activity
    private void initListView(){
        ListView listView = findViewById(R.id.list_view);
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

    // queryMachineList queries all machines from device's db
    // for list view
    private void queryMachineList(){
        final Activity activity = this;

        new ReadDatabaseTask(
                HospitalDbHelper.getMachineListQuery(),
                null,
                mDB,
                new IAsyncResponse<Cursor>() {
                    @Override
                    public void processFinish(Cursor result) {
                        mMachineList = new ArrayList<>(result.getCount());
//                        mMachineJsonList = new ArrayList<>(result.getCount());
                        while(result.moveToNext()){
                            String machineID = result.getString(
                                    result.getColumnIndex(HospitalContract.Machine.ID)
                            );
                            String buildingID = result.getString(
                                    result.getColumnIndex(HospitalContract.Building.ID)
                            );
                            String floorID = result.getString(
                                    result.getColumnIndex(HospitalContract.BuildingFloor.ID)
                            );
                            String departmentID = result.getString(
                                    result.getColumnIndex(HospitalContract.Department.ID)
                            );
                            String roomID = result.getString(
                                    result.getColumnIndex(HospitalContract.Room.ID)
                            );
                            String machineStatusID = result.getString(
                                    result.getColumnIndex(HospitalContract.MachineStatus.ID)
                            );
                            String assetTag = result.getString(
                                    result.getColumnIndex(HospitalContract.Machine.MACHINE_NAME)
                            );
                            String buildingName = result.getString(
                                    result.getColumnIndex(HospitalContract.Building.BUILDING_NAME)
                            );
                            String floor = result.getString(
                                    result.getColumnIndex(HospitalContract.BuildingFloor.FLOOR_NAME)
                            );
                            String departmentName = result.getString(
                                    result.getColumnIndex(HospitalContract.Department.DEPARTMENT_NAME)
                            );
                            String roomName = result.getString(
                                    result.getColumnIndex(HospitalContract.Room.ROOM_NAME)
                            );
                            String machineStatusName = result.getString(
                                    result.getColumnIndex(HospitalContract.MachineStatus.MACHINE_STATUS_NAME)
                            );
                            String scannedTime = result.getString(
                                    result.getColumnIndex(HospitalContract.Machine.SCANNED_TIME)
                            );

                            Machine machine = new Machine();
                            machine.setMachineName(new TextValue(assetTag, machineID));
                            machine.setBuilding(new TextValue(buildingName, buildingID));
                            machine.setFloor(new TextValue(floor, floorID));
                            machine.setDepartment(new TextValue(departmentName, departmentID));
                            machine.setRoom(new TextValue(roomName, roomID));
                            machine.setMachineStatus(new TextValue(machineStatusName, machineStatusID));
                            machine.setScannedTime(scannedTime);
                            mMachineList.add(machine);

//                            MachineJson machineJson = new MachineJson(
//                                    assetTag,
//                                    scannedTime,
//                                    buildingID,
//                                    floorID,
//                                    departmentID,
//                                    roomID,
//                                    machineStatusID
//                            );
//                            mMachineJsonList.add(machineJson);
                        }
                        result.close();

                        mAdapter = new MachineListAdapter(activity, mMachineList);
                        initListView();
                    }
                }
        ).execute();
    }
}




