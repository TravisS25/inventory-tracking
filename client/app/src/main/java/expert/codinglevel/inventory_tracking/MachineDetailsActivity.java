package expert.codinglevel.inventory_tracking;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import expert.codinglevel.inventory_tracking.adapter.MachineDetailsAdapter;
import expert.codinglevel.inventory_tracking.activityutil.MachineDetailsUtilActivity;


public class MachineDetailsActivity extends MachineDetailsUtilActivity {
    public static final String TAG = MachineDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideActionButton();
        setTitle();
    }

    private void hideActionButton(){
        Button button = (Button) findViewById(R.id.action_button);
        button.setVisibility(View.GONE);
    }

    private void setTitle(){
        TextView title = (TextView) findViewById(R.id.machine_title);
        title.setText(mMachine.getMachineName().getText());
    }
}
