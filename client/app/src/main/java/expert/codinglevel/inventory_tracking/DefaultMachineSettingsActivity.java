package expert.codinglevel.inventory_tracking;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import expert.codinglevel.inventory_tracking.adapter.MachineEditAdapter;
import expert.codinglevel.inventory_tracking.activityutil.DBActivity;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
//import expert.codinglevel.inventory_tracking.model.MachineProperties;
import expert.codinglevel.inventory_tracking.interfaces.IDatabaseCallback;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.setting.MachineSettings;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.task.DatabaseTask;
import expert.codinglevel.inventory_tracking.task.MachineFormTask;
import expert.codinglevel.inventory_tracking.task.ReadDatabaseTask;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.inventory_tracking.view.TextValue;
import expert.codinglevel.inventory_tracking.widget.CascadingDropDown;
import expert.codinglevel.inventory_tracking.widget.MachineForm;

/**
 *  DefaultMachineSettingsActivity is activity that allows user to set
 *  defaults for when they scan bar codes such as default room, machine status etc.
 */
public class DefaultMachineSettingsActivity extends DBActivity {
    public static final String TAG = DefaultMachineSettingsActivity.class.getSimpleName();
    private MachineSettings mMachineSettings;
    private Map<String, Spinner> mSpinnerMap;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(getString(R.string.machine_settings), mMachineSettings);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine);

        if(savedInstanceState != null){
            setSettingsFromBundle(savedInstanceState);
        } else {
            setSettingsFromPref();
        }

        initMachineTitle();
        mSpinnerMap = MachineForm.initSpinners(this);
        initSettingsButton();
        initDB(new IDatabaseCallback() {
            @Override
            public void finished() {
                new MachineFormTask(
                        getBaseContext(),
                        mMachineSettings,
                        mDB,
                        new IAsyncResponse<Map<String, ArrayAdapter<TextValue>>>() {
                            @Override
                            public void processFinish(Map<String, ArrayAdapter<TextValue>> result) {
                                mSpinnerMap.get(HospitalContract.TABLE_BUILDING_NAME)
                                    .setAdapter(result.get(HospitalContract.TABLE_BUILDING_NAME));
                                mSpinnerMap.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME)
                                    .setAdapter(result.get(HospitalContract.TABLE_BUILDING_FLOOR_NAME));
                                mSpinnerMap.get(HospitalContract.TABLE_DEPARTMENT_NAME)
                                    .setAdapter(result.get(HospitalContract.TABLE_DEPARTMENT_NAME));
                                mSpinnerMap.get(HospitalContract.TABLE_ROOM_NAME)
                                    .setAdapter(result.get(HospitalContract.TABLE_ROOM_NAME));
                                mSpinnerMap.get(HospitalContract.TABLE_MACHINE_STATUS_NAME)
                                    .setAdapter(result.get(HospitalContract.TABLE_MACHINE_STATUS_NAME));

                                MachineForm machineForm = new MachineForm();
                                machineForm.initDropdownListeners(
                                        getBaseContext(),
                                        mSpinnerMap,
                                        mDB,
                                        mMachineSettings
                                );
                                MachineForm.initDefaultValues(
                                        mMachineSettings,
                                        mSpinnerMap
                                );
                            }
                        }
                ).execute();
            }
        });
    }

    private void initMachineTitle(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.title_layout);
        layout.setVisibility(View.GONE);
    }

    // saveSettings converts mMachineSettings instance into json format
    // and saves it to our apps preferences file and then redirects to
    // DashboardActivity
    public void saveSettings(View view){
        Gson gson = new Gson();
        String json = gson.toJson(mMachineSettings);
        Preferences.setDefaults(this, getString(R.string.machine_settings), json);

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra("toast", "Settings Edited");
        startActivity(intent);
    }

    // initSettingsButton sets attribute properties like text, color
    // background color etc along with attaching event handler to save
    // settings
    private void initSettingsButton(){
        Button button = (Button) findViewById(R.id.edit_button);
        button.setText(getText(R.string.save_settings));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings(v);
            }
        });
    }

    // setSettingsFromBundle grabs machine settings from bundle state
    // and applies to instance settings
    private void setSettingsFromBundle(Bundle bundle){
        mMachineSettings = bundle.getParcelable(getString(R.string.machine_settings));
        Log.i(TAG, "Room value from bundle " + mMachineSettings.getRoom().getValue());
    }

    // setSettingsFromPref sets instance settings from preference file
    private void setSettingsFromPref(){
        Gson gson = new Gson();
        String json = Preferences.getDefaults(this, getString(R.string.machine_settings));
        Log.i(TAG, "+++ json string +++");
        Log.i(TAG, json);
        mMachineSettings = gson.fromJson(json, MachineSettings.class);
        Log.i(TAG, "Room value from pref " + mMachineSettings.getRoom().getValue());
    }
}
