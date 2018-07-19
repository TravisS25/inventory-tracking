package expert.codinglevel.hospital_inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import expert.codinglevel.hospital_inventory.adapter.MachineDetailsAdapter;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.task.DeleteDatabaseTask;
import expert.codinglevel.hospital_inventory.task.RetrieveDatabaseTask;

/**
 *  MachineDeleteActivity is activity that allows user to delete
 *  machine scanned on their device
 */
public class MachineDeleteActivity extends AppCompatActivity {
    public static final String TAG = MachineDeleteActivity.class.getSimpleName();
    private AlertDialog mDialog;
    private SQLiteDatabase mDB;
    private Machine mMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "+++ onCreate +++");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);
        mMachine = getIntent().getParcelableExtra("machine");
        // init delete button
        initDeleteButton();

        // init delete dialog alert
        initAlertDialog();
    }

    @Override
    protected void onResume(){
        Log.i(TAG, "+++ onResume +++");
        super.onResume();

        // Init mDB instance and init list adapter
        new RetrieveDatabaseTask(
                this,
                new IAsyncResponse<SQLiteDatabase>() {
                    @Override
                    public void processFinish(SQLiteDatabase result) {
                        mDB = result;
                        initListAdapter();
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

    // initDeleteButton sets listener on delete button that activate dialog
    // when pressed
    private void initDeleteButton(){
        Button button = (Button) findViewById(R.id.action_button);
        button.setText(getText(R.string.delete));
        button.setBackgroundColor(Color.RED);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
            }
        });
    }

    // initListAdapter inits list adapter to display machine properties
    private void initListAdapter(){
        ArrayList<Machine.MachineProperty> properties = new ArrayList<>();
        Machine.MachineProperty machineName = new Machine.MachineProperty(
                getString(R.string.asset_tag_text),
                mMachine.getAssetTag().getText()
        );
        Machine.MachineProperty scannedTime = new Machine.MachineProperty(
                getString(R.string.scan_time),
                mMachine.getScannedTime()
        );
        Machine.MachineProperty buildingName = new Machine.MachineProperty(
                getString(R.string.building_text),
                mMachine.getBuilding().getText()
        );
        Machine.MachineProperty departmentName = new Machine.MachineProperty(
                getString(R.string.department_text),
                mMachine.getDepartment().getText()
        );
        Machine.MachineProperty floorName = new Machine.MachineProperty(
                getString(R.string.floor_text),
                mMachine.getFloor().getText()
        );
        Machine.MachineProperty machineStatusName = new Machine.MachineProperty(
                getString(R.string.machine_status_text),
                mMachine.getMachineStatus().getText()
        );

        properties.add(machineName);
        properties.add(scannedTime);
        properties.add(buildingName);
        properties.add(departmentName);
        properties.add(floorName);
        properties.add(machineStatusName);

        ListView listView = (ListView) findViewById(R.id.list_view);
        MachineDetailsAdapter adapter = new MachineDetailsAdapter(
                this,
                properties
        );
        listView.setAdapter(adapter);
    }

    // initAlertDialog inits dialog for when a user clicks the
    // delete button.  It verifies whether they want to delete
    // machine or not
    private void initAlertDialog(){
        final Activity activity = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message)
                .setTitle(R.string.delete);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String machineID = mMachine.getAssetTag().getValue();
                new DeleteDatabaseTask(
                        HospitalContract.TABLE_MACHINE_NAME,
                        "_id=?",
                        new String[]{machineID},
                        mDB,
                        new IAsyncResponse<Integer>() {
                            @Override
                            public void processFinish(Integer result) {
                                mDB.close();
                                Intent intent = new Intent(activity, MachineListActivity.class);
                                intent.putExtra("toast", "Machine Deleted");
                                startActivity(intent);
                            }
                        }
                ).execute();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        mDialog = builder.create();
    }
}
