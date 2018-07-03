package expert.codinglevel.hospital_inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import expert.codinglevel.hospital_inventory.adapter.MachineDetailsAdapter;
import expert.codinglevel.hospital_inventory.enums.ScanType;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.view.TextValue;


public class LookUpActivity extends AppCompatActivity {
    public static final String TAG = LookUpActivity.class.getSimpleName();
    private Machine mMachine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_lookup);
        mMachine = new Machine();
        initListAdapter();
    }

    private void initListAdapter(){
        String jsonString = getIntent().getStringExtra("machineJson");
        JSONObject machineJson;

        Log.i(TAG, jsonString);

        try{
            machineJson = new JSONObject(jsonString);
            JSONObject buildingJson = machineJson.getJSONObject("building");
            JSONObject floorJson = machineJson.getJSONObject("floor");
            JSONObject departmentJson = machineJson.getJSONObject("department");
            JSONObject roomJson = machineJson.getJSONObject("room");
            JSONObject machineStatusJson = machineJson.getJSONObject("machineStatus");
            TextValue machine = new TextValue(
                    machineJson.getString("machineName"),
                    machineJson.getString("id")
            );
            TextValue building = new TextValue(
                    buildingJson.getString("text"),
                    buildingJson.getString("value")
            );
            TextValue floor = new TextValue(
                    floorJson.getString("text"),
                    floorJson.getString("value")
            );
            TextValue department = new TextValue(
                    departmentJson.getString("text"),
                    departmentJson.getString("value")
            );
            TextValue room = new TextValue(
                    roomJson.getString("text"),
                    roomJson.getString("value")
            );
            TextValue machineStatus = new TextValue(
                    machineStatusJson.getString("text"),
                    machineStatusJson.getString("value")
            );

            mMachine.setAssetTag(machine);
            mMachine.setBuilding(building);
            mMachine.setFloor(floor);
            mMachine.setDepartment(department);
            mMachine.setRoom(room);
            mMachine.setMachineStatus(machineStatus);
            mMachine.setScannedTime(machineJson.getString("scannedTime"));
        }
        catch(JSONException ex){
            ex.printStackTrace();
            return;
        }

        Machine.MachineProperty machine = new Machine.MachineProperty(
                getString(R.string.asset_tag_text),
                mMachine.getAssetTag().getText()
        );
        Machine.MachineProperty status = new Machine.MachineProperty(
                getString(R.string.machine_status_text),
                mMachine.getMachineStatus().getText()
        );
        Machine.MachineProperty building = new Machine.MachineProperty(
                getString(R.string.building_text),
                mMachine.getBuilding().getText()
        );
        Machine.MachineProperty floor = new Machine.MachineProperty(
                getString(R.string.floor_text),
                mMachine.getFloor().getText()
        );
        Machine.MachineProperty department = new Machine.MachineProperty(
                getString(R.string.department_text),
                mMachine.getDepartment().getText()
        );
        Machine.MachineProperty room = new Machine.MachineProperty(
                getString(R.string.room_text),
                mMachine.getRoom().getText()
        );

        ArrayList<Machine.MachineProperty> properties = new ArrayList<>();
        properties.add(machine);
        properties.add(status);
        properties.add(building);
        properties.add(floor);
        properties.add(department);
        properties.add(room);

        MachineDetailsAdapter adapter = new MachineDetailsAdapter(this, properties);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(adapter);
    }

    public void editMachine(View view){
        Intent intent = new Intent(this, MachineEditActivity.class);
        intent.putExtra("serverEdit", true);
        intent.putExtra("machine", mMachine);
        startActivity(intent);
    }

    public void swapMachine(View view){
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("scanType", ScanType.SWAP);
        startActivity(intent);
    }

    public void continueScanning(View view){
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("scanType", ScanType.LOOKUP);
        startActivity(intent);
    }

    public void dashboard(View view){
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}
