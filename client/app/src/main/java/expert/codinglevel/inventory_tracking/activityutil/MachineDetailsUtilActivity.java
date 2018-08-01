package expert.codinglevel.inventory_tracking.activityutil;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.model.Machine;

public class MachineDetailsUtilActivity extends DBActivity {
    protected Machine mMachine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_details);
        mMachine = getIntent().getParcelableExtra("machine");
        initViewValues(mMachine);
    }

    protected void initViewValues(Machine machine){
        TextView buildingValue = (TextView) findViewById(R.id.building_value);
        TextView floorValue = (TextView) findViewById(R.id.floor_value);
        TextView departmentValue = (TextView) findViewById(R.id.department_value);
        TextView roomValue = (TextView) findViewById(R.id.room_value);
        TextView machineStatusValue = (TextView) findViewById(R.id.machine_status_value);
        TextView scanValue = (TextView) findViewById(R.id.scan_value);

        buildingValue.setText(machine.getMachineName().getText());
        floorValue.setText(machine.getFloor().getText());
        departmentValue.setText(machine.getDepartment().getText());
        roomValue.setText(machine.getRoom().getText());
        machineStatusValue.setText(machine.getMachineStatus().getText());
        scanValue.setText(machine.getScannedTime());
    }
}
