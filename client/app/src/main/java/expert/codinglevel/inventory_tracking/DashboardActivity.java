package expert.codinglevel.inventory_tracking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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

import expert.codinglevel.inventory_tracking.enums.ScanType;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.json.JsonResponses;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.setting.UserActivity;

/**
 *  DashboardActivity is "main" activity once logged in
 *  This activity allows user to redirect to many other activities
 *  including scanning, lookup, machine settings etc.
 */
public class DashboardActivity extends AppCompatActivity {
    private Boolean mIsLoggedIn = null;
    private boolean mIsAdmin = false;
    private String mUserSession;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("toastActivated", true);
        savedInstanceState.putBoolean("isLoggedIn", mIsLoggedIn);
        savedInstanceState.putBoolean("isAdmin", mIsAdmin);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        boolean toastActivated = false;

        if(savedInstanceState != null){
            mIsLoggedIn = savedInstanceState.getBoolean("isLoggedIn");
            mIsAdmin = savedInstanceState.getBoolean("isAdmin");
            toastActivated = savedInstanceState.getBoolean("toastActivated");
        }
        initButtons();
        String toastMessage = getIntent().getStringExtra("toast");

        if(toastMessage != null && !toastActivated){
            Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    private void initButtons(){
        final MaterialButton logButton = (MaterialButton) findViewById(R.id.log_button);
        final MaterialButton loginLogout = (MaterialButton) findViewById(R.id.login_logout_button);

        if(mIsLoggedIn == null){
            String mUserSession = Preferences.getDefaults(this, getString(R.string.user_session));

            if(mUserSession != null){
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = getString(R.string.host_url) + "/api/user/details/";
                JsonObjectRequest request = new JsonObjectRequest(
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                mIsLoggedIn = true;
                                loginLogout.setText(getString(R.string.logout));

                                try{
                                    JSONArray groups = response.getJSONArray("groups");
                                    for(int i = 0; i < groups.length(); i++){
                                        if(groups.optString(i, "").equals("Admin")){
                                            mIsAdmin = true;
                                            logButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                } catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mIsLoggedIn = false;
                                mIsAdmin = false;
                                loginLogout.setText(getString(R.string.login));
                            }
                        }
                );

                queue.add(request);
            } else{
                mIsLoggedIn = false;
                mIsAdmin = false;
                loginLogout.setText(getString(R.string.login));
            }
        } else{
            if(mIsLoggedIn){
                mIsLoggedIn = true;
                loginLogout.setText(getString(R.string.logout));

                if(mIsAdmin){
                    logButton.setVisibility(View.VISIBLE);
                }
            } else{
                mIsLoggedIn = true;
                loginLogout.setText(getString(R.string.login));
            }
        }
    }

    private void initLogButton(){
        String userSession = Preferences.getDefaults(this, getString(R.string.user_session));


    }

    // Determines whether our button for login/logout displays "Login" or "Logout"
    // depending on if user session exists in our shared preference file
    private void initLoginLogoutButton(){
        if(mIsLoggedIn == null){
            final MaterialButton button = (MaterialButton) findViewById(R.id.login_logout_button);

            // Grab user session, if exists, from our shared preferences file
            mUserSession = Preferences.getDefaults(this, getString(R.string.user_session));

            // If user session exists, we check the expire date substring to make sure the expire
            // date of the cookie is past the current date
            // If user session does not exist, then user is not logged in and display "Log In"
            // for our button
            if (mUserSession != null){
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = getString(R.string.host_url) + "/api/user/details/";
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject user = response.getJSONObject("user");

                                    if (user != null) {
                                        mIsLoggedIn = true;
                                        button.setText(getString(R.string.logout));
                                    } else {
                                        mIsLoggedIn = false;
                                        button.setText(getString(R.string.login));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mIsLoggedIn = false;
                            }
                        }
                );

                queue.add(request);
            }
            else{
                button.setText(getString(R.string.login));
                mIsLoggedIn = false;
            }
        }
    }

    // lookupInventory is redirect to new intent that allows user
    // to scan barcode to look it up
    public void lookUpInventory(View view){
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("scanType", ScanType.LOOKUP);
        startActivity(intent);
    }

    // scanInventory is redirect to new intent that allows user
    // to scan barcode to save locally to be able to upload at
    // later time
    public void scanInventory(View view){
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra("scanType", ScanType.INVENTORY);
        startActivity(intent);
    }

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

    // Action function for the login/logout button
    public void loginLogout(View view){
        // If user is logged in, then the button text will display "Logout" and
        // when pressed, an api call to "/logout" is called which deletes session off
        // server and we delete the user session from our shared preference file
        //
        // Else user is not logged in and display text for button is "Login" which
        // simply redirects user to login activity
        if(mIsLoggedIn){
            final Activity activity = this;
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = getString(R.string.host_url) + "/logout";
            String user = mUserSession.split(";")[0];
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Cookie", user);

            CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Sets the user session to null
                        Preferences.setDefaults(
                            activity,
                            activity.getString(R.string.user_session),
                            null
                        );

                        Toast.makeText(activity, "Logged out", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(activity, LoginActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        JsonResponses.volleyError(activity, error);
                    }
                },
                headers
            );
            queue.add(request);
        }
        else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}

