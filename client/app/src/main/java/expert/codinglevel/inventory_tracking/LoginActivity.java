package expert.codinglevel.inventory_tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.json.CustomStringRequest;
import expert.codinglevel.inventory_tracking.json.JsonRequest;
import expert.codinglevel.inventory_tracking.json.JsonResponses;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.setting.MachineSettings;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.task.MultipleReadDBTask;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

/**
 *  LoginActivity is activity that allows user to login
 */
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    // mHeaders represents http headers that will be used for csrf tokens
    //private HashMap<String, String> mHeaders = new HashMap<>();
    private RequestQueue mQueue;
    private String mURL;
    private String mEmailError = "emailError";
    private String mPasswordError = "passwordError";
    private String mError = "error";
    private TextView mEmailErrorView;
    private TextView mPasswordErrorView;
    private TextView mErrorView;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putString(
//            getString(R.string.csrf_token),
//            mHeaders.get(getString(R.string.csrf_token))
//        );
//        savedInstanceState.putString(
//            getString(R.string.cookie),
//            mHeaders.get(getString(R.string.cookie))
//        );
        savedInstanceState.putString(mEmailError, mEmailErrorView.getText().toString());
        savedInstanceState.putString(mPasswordError, mPasswordErrorView.getText().toString());
        savedInstanceState.putString(mError, mErrorView.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initErrorTextViews();
        initLoginButton();
        mQueue = Volley.newRequestQueue(this);
        mURL = getString(R.string.host_url) + "/api/account/login/";

        // Query and set default machine settings
        new RetrieveDatabaseTask(
            this,
            new IAsyncResponse<SQLiteDatabase>() {
                    @Override
                    public void processFinish(SQLiteDatabase result) {
                        initDefaultMachineSettings(result);
                    }
                }
        ).execute();
    }

    public void skipLogin(View view){
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    private void initLoginButton(){
        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                attemptLogin(view);
            }
        });
    }

    // attemptLogin is used to attempt to login a user based
    // on email and password given from the user
    // If successful, will receive session token to be
    // used throughout the app
    public void attemptLogin(View view){
        final Context context = this;
        final HashMap<String, String> headers = new HashMap<>();
        EditText email = (EditText)findViewById(R.id.email);
        EditText password = (EditText)findViewById(R.id.password);

        // Validate that fields are not empty
        if(email.getText().toString().equals("") || password.getText().toString().equals("")){
            mErrorView.setText("Email and password are required");
            return;
        }

        final JSONObject jsonObject = new JSONObject();

        // Add email and password fields to json object
        try{
            jsonObject.put("email", email.getText().toString());
            jsonObject.put("password", password.getText().toString());
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }

        Log.i(TAG, "Passed jsonobject");
        Log.i(TAG, "url " + mURL);


        // If validation passes, then make GET request to obtain token
        // and on success request make a POST request to login and get
        // session token to use for rest of app
        CustomStringRequest jsonRequest = new CustomStringRequest(
                Request.Method.GET,
                mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "+++ Got to get response +++");

                        for (Map.Entry<String, String> entry : headers.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();

                            Log.i(TAG, "key " + key);
                            Log.i(TAG, "value " + value);
                        }

                        // Extract "Set-Cookie" header value, add "Cookie" header with extracted value
                        // and then delete "Set-Cookie" header key
                        String cookie = headers.get(getString(R.string.set_cookie));
                        headers.put(getString(R.string.cookie), cookie);
                        headers.remove(getString(R.string.set_cookie));

                        CustomJsonObjectRequest postRequest = new CustomJsonObjectRequest(
                                Request.Method.POST,
                                mURL,
                                jsonObject,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // Upon successful response (meaning email and password were correct),
                                        // the response will send back user cookie which is written
                                        // to our "mHeaders" variable in which we store in the
                                        // app's preferences file for later use and then redirect
                                        // to dashboard
                                        Preferences.setDefaults(
                                            context,
                                            getString(R.string.user_session),
                                            headers.get(getString(R.string.set_cookie))
                                        );

                                        Intent intent = new Intent(context, DashboardActivity.class);
                                        startActivity(intent);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.i(TAG, "+++ Got to post error response +++");
                                        if(error.networkResponse != null){
                                            Log.i(TAG, error.networkResponse.toString());
                                            if(error.networkResponse.statusCode == 406){
                                                JSONObject jsonResponse;

                                                try{
                                                    Log.i(TAG, "+++" + new String(error.networkResponse.data) + "+++");
                                                    jsonResponse = new JSONObject(new String(error.networkResponse.data));
                                                } catch (JSONException e){
                                                    e.printStackTrace();
                                                    JsonResponses.volleyError(context, error);
                                                    return;
                                                }

                                                String emailError = jsonResponse.optString("email", "");
                                                String passwordError = jsonResponse.optString("password", "");
                                                String errorMessage = jsonResponse.optString("errorMessage", "");

                                                mEmailErrorView.setText(emailError);
                                                mPasswordErrorView.setText(passwordError);
                                                mErrorView.setText(errorMessage);

                                                return;
                                            }
                                        }
                                        JsonResponses.volleyError(context, error);
                                    }
                                },
                                headers
                        );

                        mQueue.add(postRequest);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "+++ Got to get error response +++");
                        Log.i(TAG, error.getMessage());
                        JsonResponses.volleyError(context, error);
                    }
                },
                headers,
                new String[]{getString(R.string.csrf_token), getString(R.string.set_cookie)}
        );

        Log.i(TAG, "added to queue");
        mQueue.add(jsonRequest);
    }

    // initErrorTextViews inits the text color of error field to be red
    private void initErrorTextViews(){
        mEmailErrorView = (TextView) findViewById(R.id.email_error);
        mPasswordErrorView = (TextView) findViewById(R.id.password_error);
        mErrorView = (TextView) findViewById(R.id.error);

        mEmailErrorView.setTextColor(Color.RED);
        mPasswordErrorView.setTextColor(Color.RED);
        mErrorView.setTextColor(Color.RED);
    }

    // initDefaultMachineSettings tries to get machine settings from preferences app file
    // If settings do not exist (generally when user first uses app) then create defaults
    private void initDefaultMachineSettings(SQLiteDatabase db){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String json = sharedPref.getString(getString(R.string.machine_settings), null);
        final Activity activity = this;

        if(json == null){
            Log.i(TAG, "+++ querying database +++");

            String query = HospitalDbHelper.getBuildingJoinQuery() + " limit 1";
            String machineStatusQuery =
                "select _id as machine_status_id, status_name " +
                "from machine_status where _id = ?";

            // Init array of MultipleReadDBTask.DatabaseRead and add query to retrieve
            // the first building in db to use as default for settings along with joining
            // other tables connected to building to use for defaults
            //
            // Also add query for machine status to use for default
            List<MultipleReadDBTask.DatabaseRead> databaseReadList = new ArrayList<>();
            databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
                    HospitalDbHelper.BUILDING_JOIN,
                    new MultipleReadDBTask.DatabaseQuery(query, new String[]{"1"})
            ));
            databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
                    HospitalContract.TABLE_MACHINE_STATUS_NAME,
                    new MultipleReadDBTask.DatabaseQuery(machineStatusQuery, new String[]{"1"})
            ));

            // Exec the multiple queries from our list above and extract the returned values
            // from cursor
            // Use the extracted values to put into MachineSettings singleton and convert the
            // settings to json to store in preferences file
            new MultipleReadDBTask(
                databaseReadList,
                db,
                new IAsyncResponse<HashMap<String, Cursor>>() {
                    @Override
                    public void processFinish(HashMap<String, Cursor> cursors) {
                        Cursor queryCursor = cursors.get(HospitalDbHelper.BUILDING_JOIN);
                        Cursor machineStatusCursor = cursors.get(HospitalContract.TABLE_MACHINE_STATUS_NAME);

                        Log.i(TAG, Integer.toString(machineStatusCursor.getCount()));

                        queryCursor.moveToFirst();
                        machineStatusCursor.moveToFirst();

                        // Extract values from cursor based on table
                        String buildingID = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Building.ID)
                        );
                        String buildingName = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Building.BUILDING_NAME)
                        );
                        String floorID = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.BuildingFloor.ID)
                        );
                        String floor = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.BuildingFloor.FLOOR_NAME)
                        );
                        String departmentID = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Department.ID)
                        );
                        String departmentName = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Department.DEPARTMENT_NAME)
                        );
                        String roomID = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Room.ID)
                        );
                        String roomName = queryCursor.getString(
                                queryCursor.getColumnIndex(HospitalContract.Room.ROOM_NAME)
                        );
                        String machineStatusID = machineStatusCursor.getString(
                                machineStatusCursor.getColumnIndex(HospitalContract.MachineStatus.ID)
                        );
                        String machineStatus = machineStatusCursor.getString(
                                machineStatusCursor.getColumnIndex(
                                    HospitalContract.MachineStatus.MACHINE_STATUS_NAME
                            )
                        );

                        // Close cursors
                        queryCursor.close();
                        machineStatusCursor.close();

                        // Put extracted values in machine settings and convert to json to put
                        // into preferences file
                        MachineSettings machineSettings = MachineSettings.getInstance();
                        machineSettings.setBuilding(new TextValue(buildingName, buildingID));
                        machineSettings.setFloor(new TextValue(floor, floorID));
                        machineSettings.setDepartment(new TextValue(departmentName, departmentID));
                        machineSettings.setRoom(new TextValue(roomName, roomID));
                        machineSettings.setMachineStatus(new TextValue(machineStatus, machineStatusID));

                        Gson gson = new Gson();
                        String json = gson.toJson(machineSettings);

                        Preferences.setDefaults(activity, getString(R.string.machine_settings), json);
                    }
                }
            ).execute();
        }
    }
}
