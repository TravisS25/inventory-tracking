package expert.codinglevel.inventory_tracking;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//import expert.codinglevel.inventory_tracking.adapter.MachineDetailsAdapter;
import expert.codinglevel.inventory_tracking.activityutil.MachineDetailsUtilActivity;


public class MachineDetailsActivity extends MachineDetailsUtilActivity {
    public static final String TAG = MachineDetailsActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        if (id == android.R.id.home) {
//            finish();
//            return  true;
//        }
//        return super.onOptionsItemSelected(item);
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_details);
        initViewValues(mMachine);
        //Machine machine = getIntent().getParcelableExtra("machine");
//        initView(machine);
        hideActionButton();

//        // Get machine from intent, insert as Machine.Property and display details
//        Machine.MachineProperty machineName = new Machine.MachineProperty(
//                getString(R.string.machine_name_text),
//                machine.getMachineName().getText()
//        );
//        Machine.MachineProperty status = new Machine.MachineProperty(
//                getString(R.string.machine_status_text),
//                machine.getMachineStatus().getText()
//        );
//        Machine.MachineProperty scannedTime = new Machine.MachineProperty(
//                getString(R.string.scan_time),
//                machine.getScannedTime()
//        );
//        Machine.MachineProperty building = new Machine.MachineProperty(
//                getString(R.string.building_text),
//                machine.getBuilding().getText()
//        );
//        Machine.MachineProperty department = new Machine.MachineProperty(
//                getString(R.string.department_text),
//                machine.getDepartment().getText()
//        );
//        Machine.MachineProperty floor = new Machine.MachineProperty(
//                getString(R.string.floor_text),
//                machine.getFloor().getText()
//        );
//        Machine.MachineProperty room = new Machine.MachineProperty(
//                getString(R.string.room_text),
//                machine.getRoom().getText()
//        );
//
//        ArrayList<Machine.MachineProperty> properties = new ArrayList<>();
//        properties.add(machineName);
//        properties.add(status);
//        properties.add(scannedTime);
//        properties.add(building);
//        properties.add(department);
//        properties.add(floor);
//        properties.add(room);
//
//        MachineDetailsAdapter adapter = new MachineDetailsAdapter(this, properties);
//        ListView listView = (ListView) findViewById(R.id.list_view);
//        listView.setAdapter(adapter);
    }

    private void hideActionButton(){
        Button button = (Button) findViewById(R.id.action_button);
        button.setVisibility(View.GONE);
    }
}
