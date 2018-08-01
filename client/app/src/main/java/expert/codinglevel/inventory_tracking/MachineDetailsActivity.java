package expert.codinglevel.inventory_tracking;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//import expert.codinglevel.inventory_tracking.adapter.MachineDetailsAdapter;
import expert.codinglevel.inventory_tracking.activityutil.MachineDetailsUtilActivity;


public class MachineDetailsActivity extends MachineDetailsUtilActivity {
    public static final String TAG = MachineDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_details);
        initViewValues(mMachine);
        hideActionButton();
    }

    private void hideActionButton(){
        Button button = (Button) findViewById(R.id.action_button);
        button.setVisibility(View.GONE);
    }
}
