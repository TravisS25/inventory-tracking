package expert.codinglevel.hospital_inventory;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.hospital_inventory.adapter.MachineEditAdapter;
import expert.codinglevel.hospital_inventory.enums.MachineAttribute;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.model.Machine;
import expert.codinglevel.hospital_inventory.model.MachineProperties;
import expert.codinglevel.hospital_inventory.setting.MachineSettings;
import expert.codinglevel.hospital_inventory.setting.Preferences;
import expert.codinglevel.hospital_inventory.task.MultipleReadDBTask;
import expert.codinglevel.hospital_inventory.task.RetrieveDatabaseTask;
import expert.codinglevel.hospital_inventory.task.cascadingdropdown.CascadingBuildingDropDownTask;
import expert.codinglevel.hospital_inventory.view.TextValue;
import expert.codinglevel.hospital_inventory.widget.CascadingDropDown;

public class DefaultMachineSettingsActivity extends AppCompatActivity {
    public static final String TAG = DefaultMachineSettingsActivity.class.getSimpleName();
    private SQLiteDatabase mDB;
    private Bundle mBundle;
    private MachineSettings mMachineSettings;
    private ArrayList<Machine.MachineProperty> mPropertyList = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(getString(R.string.machine_settings), mMachineSettings);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_machine_settings);
        mBundle = savedInstanceState;
        initSettingsButton();
    }

    @Override
    protected void onResume(){
        Log.i(TAG, "+++ onResume +++");
        super.onResume();

        new RetrieveDatabaseTask(
            this,
            new IAsyncResponse<SQLiteDatabase>() {
                @Override
                public void processFinish(SQLiteDatabase result) {
                mDB = result;
                if(mBundle == null){
                    Log.i(TAG, "+++ Within bundle +++");
                    setSettingsFromPref();
                }
                else{
                    Log.i(TAG, "+++ Else bundle +++");
                    setSettingsFromBundle();
                }
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
        Button button = (Button) findViewById(R.id.save_settings);
        button.setText(getText(R.string.save_settings));
        button.setBackgroundColor(Color.BLUE);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings(v);
            }
        });
    }

    // setSettingsFromBundle grabs machine settings from bundle state
    // and applies to instance settings
    private void setSettingsFromBundle(){
        mMachineSettings = mBundle.getParcelable(getString(R.string.machine_settings));
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

    // initListAdapter
    private void initListAdapter(){
        final Activity activity = this;

        new CascadingBuildingDropDownTask(
                mMachineSettings,
                mDB,
                new IAsyncResponse<HashMap<String, ArrayList<TextValue>>>() {
                    @Override
                    public void processFinish(HashMap<String, ArrayList<TextValue>> result) {
                        Log.i(TAG, result.toString());
                        MachineProperties.addCascadingProperties(activity, result, mPropertyList);
                        new MultipleReadDBTask(
                                HospitalDbHelper.getMachineDatabaseReadList(mMachineSettings),
                                mDB,
                                new IAsyncResponse<HashMap<String, Cursor>>() {
                                    @Override
                                    public void processFinish(HashMap<String, Cursor> result) {
                                        MachineProperties.addProperties(activity, result, mPropertyList);
                                        ListView listView = (ListView) findViewById(R.id.list_view);
                                        MachineEditAdapter adapter = new MachineEditAdapter(
                                                activity,
                                                mMachineSettings,
                                                mDB,
                                                mPropertyList
                                        );
                                        listView.setAdapter(adapter);
                                    }
                                }
                        ).execute();
                    }
                }
        ).execute();
    }
}
