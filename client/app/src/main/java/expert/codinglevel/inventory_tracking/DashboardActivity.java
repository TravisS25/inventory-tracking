package expert.codinglevel.inventory_tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import expert.codinglevel.inventory_tracking.enums.ScanType;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.json.JsonRequest;
import expert.codinglevel.inventory_tracking.json.JsonResponses;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.setting.UserActivity;
import expert.codinglevel.inventory_tracking.task.UserDetailsTask;

/**
 *  DashboardActivity is "main" activity once logged in
 *  This activity allows user to redirect to many other activities
 *  including scanning, lookup, machine settings etc.
 */
public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private int mIsLoggedIn = -1;
    private boolean mIsAdmin = false;
    private Map<Integer, Boolean> mExceptionIDs = new HashMap<>();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("toastActivated", true);
        savedInstanceState.putInt("isLoggedIn", mIsLoggedIn);
        savedInstanceState.putBoolean("isAdmin", mIsAdmin);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean toastActivated = false;

        if(savedInstanceState != null){
            mIsLoggedIn = savedInstanceState.getInt("isLoggedIn");
            mIsAdmin = savedInstanceState.getBoolean("isAdmin");
            toastActivated = savedInstanceState.getBoolean("toastActivated");
        }
        //initButtons();
        String toastMessage = getIntent().getStringExtra("toast");

        if(toastMessage != null && !toastActivated){
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
        }

        setupView();
    }

    private void setExceptionIDs(){
        mExceptionIDs.put(R.id.logs_button, true);
    }

    private void setVisibility(ViewGroup viewGroup, boolean isLayoutVisible, Map<Integer, Boolean> exceptionIDS){
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            View v = viewGroup.getChildAt(i);

            if(!exceptionIDS.containsKey(v.getId())){
                if(v instanceof ProgressBar){
                    if(isLayoutVisible){
                        v.setVisibility(View.INVISIBLE);
                    } else{
                        v.setVisibility(View.VISIBLE);
                    }
                } else{
                    if(isLayoutVisible){
                        v.setVisibility(View.VISIBLE);
                    } else{
                        v.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private void setupView(){
        final Context context = getApplicationContext();
        setContentView(R.layout.activity_dashboard);
        setExceptionIDs();
        final ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.dashboard_layout);
        setVisibility(layout, false, mExceptionIDs);

        if(mIsLoggedIn == -1){
            Log.i(TAG, "+++ is -1 +++");
            new UserDetailsTask(this, null, new IAsyncResponse<JSONObject>() {
                @Override
                public void processFinish(JSONObject result) {
                    setVisibility(layout, true, mExceptionIDs);
                    initButtons();
                    final MaterialButton logButton = (MaterialButton) findViewById(R.id.logs_button);
                    final MaterialButton loginLogout = (MaterialButton) findViewById(R.id.login_logout_button);

                    if(result != null){
                        try{
                            JSONObject user = result.getJSONObject(getString(R.string.user));
                            JSONArray groups = result.getJSONArray(getString(R.string.user_groups));

                            if(user != null){
                                Log.i(TAG, "+++ is logged in +++");
                                mIsLoggedIn = 1;
                            } else{
                                mIsLoggedIn = 0;
                            }

                            for(int i = 0; i < groups.length(); i++){
                                if(groups.optString(i).equals("Admin")){
                                    Log.i(TAG, "+++ is admin +++");
                                    mIsAdmin = true;
                                    break;
                                }
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    } else{
                        mIsLoggedIn = 0;
                    }

                    if(mIsLoggedIn == 1){
                        setLogoutButton(context, loginLogout);
                    }
                    if(mIsLoggedIn == 0){
                        setLoginButton(context, loginLogout);
                    }
                    if(mIsAdmin){
                        logButton.setVisibility(View.VISIBLE);
                    }
                }
            }).execute();
        } else{
            setVisibility(layout, true, mExceptionIDs);
            initButtons();
            final MaterialButton logButton = (MaterialButton) findViewById(R.id.logs_button);
            final MaterialButton loginLogout = (MaterialButton) findViewById(R.id.login_logout_button);

            if(mIsLoggedIn == 1){
                setLogoutButton(context, loginLogout);
            }
            if(mIsLoggedIn == 0){
                setLoginButton(context, loginLogout);
            }
            if(mIsAdmin){
                logButton.setVisibility(View.VISIBLE);
            }
        }
    }

    // setLoginButton is helper function for setting login button text and
    // event handler to redirect to login
    private void setLoginButton(final Context context, MaterialButton button){
        button.setText(getString(R.string.login));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // event handler to redirect to login
    private void setLogoutButton(final Context context, MaterialButton button){
        button.setText(getString(R.string.logout));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestQueue queue = Volley.newRequestQueue(context);
                String userSession = Preferences.getDefaults(
                    context,
                    getString(R.string.user_session)
                );
                Map<String, String> headers = new HashMap<>();
                headers.put(getString(R.string.cookie), userSession);
                CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                        Request.Method.GET,
                        getString(R.string.host_url) + "/api/account/logout/",
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Preferences.setDefaults(
                                    context,
                                    getString(R.string.user_session),
                                    null
                                );

                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivity(intent);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                JsonResponses.volleyError(context, error);
                            }
                        },
                        headers
                );

                queue.add(request);
            }
        });
    }

    // initButtons inits both the login/logout button and the logs button
    //
    // The login/logout button's text is determined if the current user is logged in
    // If they are, will display "Logout", else will display "Login"
    //
    // The log button will display based on if the current user is logged in
    // and if that user is within "Admin" group
    private void initButtons(){
        MaterialButton scanButton = (MaterialButton) findViewById(R.id.scan_inventory_button);
        MaterialButton lookupInventoryButton = (MaterialButton) findViewById(R.id.look_up_inventory_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra("scanType", ScanType.INVENTORY);
                startActivity(intent);
            }
        });
        lookupInventoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                intent.putExtra("scanType", ScanType.LOOKUP);
                startActivity(intent);
            }
        });
    }

    // lookupInventory is redirect to new intent that allows user
    // to scan barcode to look it up
//    public void lookUpInventory(View view){
//        Intent intent = new Intent(this, LookUpActivity.class);
//        intent.putExtra("scanType", ScanType.LOOKUP);
//        startActivity(intent);
//    }

    // scanInventory is redirect to new intent that allows user
    // to scan barcode to save locally to be able to upload at
    // later time
//    public void scanInventory(View view){
//        Intent intent = new Intent(this, ScanActivity.class);
//        intent.putExtra("scanType", ScanType.INVENTORY);
//        startActivity(intent);
//    }

    // viewScannedInventory is redirect to new intent that allows user
    // to view all scanned items
    public void viewScannedInventory(View view){
        Intent intent = new Intent(this, MachineListActivity.class);
        startActivity(intent);
    }

    // defaultMachineSettings is redirect to new intent that allows user
    // to set new machine scan settings
    public void defaultMachineSettings(View view){
        Intent intent = new Intent(this, DefaultMachineSettingsActivity.class);
        startActivity(intent);
    }

    // machineLogs is redirect to new intent that allows admin user to see all
    // the actions that have been executed against the database
    public void machineLogs(View view){
        Intent intent = new Intent(this, MachineLogsActivity.class);
        startActivity(intent);
    }
}

