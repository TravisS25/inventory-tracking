package expert.codinglevel.inventory_tracking;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//import expert.codinglevel.inventory_tracking.adapter.MachineDetailsAdapter;
import expert.codinglevel.inventory_tracking.activityutil.MachineDetailsUtilActivity;
import expert.codinglevel.inventory_tracking.enums.ScanType;
import expert.codinglevel.inventory_tracking.model.Machine;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  LookUpActivity is activity that allows user to view scanned item
 *  that came from server
 *
 *  Along with looking up a bar code, once a bar code is scanned,
 *  user will have the ability to edit machine or swap machine that
 *  will directly update to server
 */
public class LookUpActivity extends MachineDetailsUtilActivity {
    public static final String TAG = LookUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_lookup);
        initViewValues(mMachine);
        initButtons();
        //initListAdapter();
    }

    private void initButtons(){
        MaterialButton editBtn = findViewById(R.id.edit_button);
        MaterialButton swapMachineBtn = findViewById(R.id.swap_machine_button);
        MaterialButton continueScanningBtn = findViewById(R.id.continue_scanning_button);
        MaterialButton dashboardBtn = findViewById(R.id.dashboard_button);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MachineEditActivity.class);
                Machine machine = getIntent().getParcelableExtra("machine");
                intent.putExtra("serverEdit", true);
                intent.putExtra("machine", machine);
                startActivity(intent);
            }
        });
        swapMachineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra("scanType", ScanType.SWAP);
                startActivity(intent);
            }
        });
        continueScanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra("scanType", ScanType.LOOKUP);
                startActivity(intent);
            }
        });
        dashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                startActivity(intent);
            }
        });
    }

//    private void setupView(){
//        new User
//    }

    // initListAdapter inits list adapter with machine info that
    // was sent to this intent
//    private void initListAdapter(){
//        // Get json string from intent
//        String jsonString = getIntent().getStringExtra("machineJson");
//        JSONObject machineJson;
//
//        Log.i(TAG, jsonString);
//
//        try{
//            // Get machine json string and extract machine info
//            machineJson = new JSONObject(jsonString);
//            JSONObject buildingJson = machineJson.getJSONObject("building");
//            JSONObject floorJson = machineJson.getJSONObject("floor");
//            JSONObject departmentJson = machineJson.getJSONObject("department");
//            JSONObject roomJson = machineJson.getJSONObject("room");
//            JSONObject machineStatusJson = machineJson.getJSONObject("machineStatus");
//
//            // Assign machine info from json into TextValue to use
//            // for dropdown values
//            TextValue machine = new TextValue(
//                    machineJson.getString("machineName"),
//                    machineJson.getString("id")
//            );
//            TextValue building = new TextValue(
//                    buildingJson.getString("text"),
//                    buildingJson.getString("value")
//            );
//            TextValue floor = new TextValue(
//                    floorJson.getString("text"),
//                    floorJson.getString("value")
//            );
//            TextValue department = new TextValue(
//                    departmentJson.getString("text"),
//                    departmentJson.getString("value")
//            );
//            TextValue room = new TextValue(
//                    roomJson.getString("text"),
//                    roomJson.getString("value")
//            );
//            TextValue machineStatus = new TextValue(
//                    machineStatusJson.getString("text"),
//                    machineStatusJson.getString("value")
//            );
//
//            // Set TextValues to machine instance
//            // This is used when moving to edit or swap activities
//            mMachine.setMachineName(machine);
//            mMachine.setBuilding(building);
//            mMachine.setFloor(floor);
//            mMachine.setDepartment(department);
//            mMachine.setRoom(room);
//            mMachine.setMachineStatus(machineStatus);
//            mMachine.setScannedTime(machineJson.getString("scannedTime"));
//        }
//        catch(JSONException ex){
//            ex.printStackTrace();
//            return;
//        }
//
//        // Create machine properties of each machine settings
//        // These will used as individual fields in the list view
//        Machine.MachineProperty machine = new Machine.MachineProperty(
//                getString(R.string.machine_name_text),
//                mMachine.getMachineName().getText()
//        );
//        Machine.MachineProperty status = new Machine.MachineProperty(
//                getString(R.string.machine_status_text),
//                mMachine.getMachineStatus().getText()
//        );
//        Machine.MachineProperty building = new Machine.MachineProperty(
//                getString(R.string.building_text),
//                mMachine.getBuilding().getText()
//        );
//        Machine.MachineProperty floor = new Machine.MachineProperty(
//                getString(R.string.floor_text),
//                mMachine.getFloor().getText()
//        );
//        Machine.MachineProperty department = new Machine.MachineProperty(
//                getString(R.string.department_text),
//                mMachine.getDepartment().getText()
//        );
//        Machine.MachineProperty room = new Machine.MachineProperty(
//                getString(R.string.room_text),
//                mMachine.getRoom().getText()
//        );
//
//        // Create array and add machine properties to list
//        ArrayList<Machine.MachineProperty> properties = new ArrayList<>();
//        properties.add(machine);
//        properties.add(status);
//        properties.add(building);
//        properties.add(floor);
//        properties.add(department);
//        properties.add(room);
//
//        // Init details adapter, pass array and set adapter for view
//        MachineDetailsAdapter adapter = new MachineDetailsAdapter(this, properties);
//        ListView listView = (ListView) findViewById(R.id.list_view);
//        listView.setAdapter(adapter);
//    }

//    // editMachine redirects user to activity that can edit machine
//    public void editMachine(View view){
//        Intent intent = new Intent(this, MachineEditActivity.class);
//        intent.putExtra("serverEdit", true);
//        intent.putExtra("machine", mMachine);
//        startActivity(intent);
//    }
//
//    // swapMachine redirects user to activity to scan another machine to swap
//    // with current machine
//    public void swapMachine(View view){
//        Intent intent = new Intent(this, ScanActivity.class);
//        intent.putExtra("scanType", ScanType.SWAP);
//        startActivity(intent);
//    }
//
//    // continueScanning redirects user to scan activity to scan another lookup
//    public void continueScanning(View view){
//        Intent intent = new Intent(this, ScanActivity.class);
//        intent.putExtra("scanType", ScanType.LOOKUP);
//        startActivity(intent);
//    }
//
//    // dashboard redirects user back to dashboard activity
//    public void dashboard(View view){
//        Intent intent = new Intent(this, DashboardActivity.class);
//        startActivity(intent);
//    }
}
