package expert.codinglevel.hospital_inventory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import expert.codinglevel.hospital_inventory.interfaces.IJsonRequestCallback;
import expert.codinglevel.hospital_inventory.json.CustomJsonObjectRequest;
import expert.codinglevel.hospital_inventory.json.JsonRequest;
import expert.codinglevel.hospital_inventory.json.JsonResponses;
import expert.codinglevel.hospital_inventory.model.HospitalContract;
import expert.codinglevel.hospital_inventory.model.HospitalDbHelper;
import expert.codinglevel.hospital_inventory.interfaces.IAsyncResponse;
import expert.codinglevel.hospital_inventory.setting.MachineSettings;
import expert.codinglevel.hospital_inventory.setting.Preferences;
import expert.codinglevel.hospital_inventory.task.MultipleReadDBTask;
import expert.codinglevel.hospital_inventory.task.RetrieveDatabaseTask;
import expert.codinglevel.hospital_inventory.view.TextValue;

/**
 *  LoginActivity is activity that allows user to login
 */
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = LoginActivity.class.getSimpleName();

    // mHeaders represents http headers that will be used for csrf tokens
    private HashMap<String, String> mHeaders = new HashMap<>();
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
        savedInstanceState.putString(
            getString(R.string.csrf_token),
            mHeaders.get(getString(R.string.csrf_token))
        );
        savedInstanceState.putString(
            getString(R.string.cookie),
            mHeaders.get(getString(R.string.cookie))
        );
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
        mQueue = Volley.newRequestQueue(this);
        mURL = getString(R.string.host_url) + "/";

        // If no saved instance state, create json object request
        // which will be used to make request to get tokens and
        // be inserted into the mHeaders
        //
        // Else use tokens from saved instance state
        if(savedInstanceState == null){
            JsonObjectRequest request = JsonRequest.getJSONRequestTokenObject(
                    this,
                    mHeaders,
                    null,
                    mURL
            );
            mQueue.add(request);
        }
        else{
            mHeaders.put(
                getString(R.string.csrf_token),
                savedInstanceState.getString(getString(R.string.csrf_token))
            );
            mHeaders.put(
                getString(R.string.cookie),
                savedInstanceState.getString(getString(R.string.cookie))
            );

            mEmailErrorView.setText(savedInstanceState.getString(mEmailError));
            mPasswordErrorView.setText(savedInstanceState.getString(mPasswordError));
            mErrorView.setText(savedInstanceState.getString(mError));

            Log.i(TAG, mHeaders.toString());
        }

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

    // attemptLogin is used to attempt to login a user based
    // on email and password given from the user
    // If successful, will receive session token to be
    // used throughout the app
    public void attemptLogin(View view){
        final Context context = this;
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

        // Request
        CustomJsonObjectRequest jsonRequest = new CustomJsonObjectRequest(
                Request.Method.POST,
                mURL,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "positive response");
                        String user;
                        String expire;

                        try{
                            user = response.getString("user");
                            expire = response.getString("expire");
                        }
                        catch (JSONException ex){
                            ex.printStackTrace();
                            return;
                        }

                        Log.i(TAG, user + ";" + expire);

                        // Get user session token along with expiration from response
                        // and add to app's preferences file
                        Preferences.setDefaults(
                            context,
                            context.getString(R.string.user_session),
                            user + ";" + expire
                        );
                        Toast.makeText(
                                getApplicationContext(),
                                "Successfully Logged In",
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context, DashboardActivity.class);
                        context.startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String csrfToken = "csrfToken";
                        String cookie = "cookie";
                        String generalError = "Error";
                        String email = "Email";
                        String password = "Password";
                        String jsonString;
//                        TextView emailErrorView = (TextView) findViewById(R.id.email_error);
//                        TextView passwordErrorView = (TextView) findViewById(R.id.password_error);
//                        emailErrorView.setTextColor(Color.RED);
//                        passwordErrorView.setTextColor(Color.RED);

                        mEmailErrorView.setText("");
                        mPasswordErrorView.setText("");
                        mErrorView.setText("");

                        try{
                            if(error.networkResponse != null){
                                try{
                                    jsonString = new String(
                                        error.networkResponse.data,
                                        "UTF-8"
                                    );
                                }
                                catch (UnsupportedEncodingException ex){
                                    ex.printStackTrace();
                                    return;
                                }

                                Log.i(TAG, jsonString);
                                JSONObject jsonObject = new JSONObject(jsonString);
                                if (jsonObject.has(email)){
                                    mEmailErrorView.setText(jsonObject.getString(email));
                                }
                                if(jsonObject.has(password)){
                                    mPasswordErrorView.setText(jsonObject.getString(password));
                                }
                                if(jsonObject.has(generalError)){
                                    mErrorView.setText(jsonObject.getString(generalError));
                                }
                            }
                            else{
                                mErrorView.setText(
                                    "Unexpected error has occurred, please try again later"
                                );
                            }
                        }
                        catch (JSONException ex){
                            ex.printStackTrace();
                            mErrorView.setText("Unexpected error has occurred");
                        }
                    }
                },
                mHeaders
        );

        Log.i(TAG, "added to queue");
        mQueue.add(jsonRequest);
    }

    private void initErrorTextViews(){
        mEmailErrorView = (TextView) findViewById(R.id.email_error);
        mPasswordErrorView = (TextView) findViewById(R.id.password_error);
        mErrorView = (TextView) findViewById(R.id.error);

        mEmailErrorView.setTextColor(Color.RED);
        mPasswordErrorView.setTextColor(Color.RED);
        mErrorView.setTextColor(Color.RED);
    }

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

            List<MultipleReadDBTask.DatabaseRead> databaseReadList = new ArrayList<>();
            databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
                    HospitalDbHelper.BUILDING_JOIN,
                    new MultipleReadDBTask.DatabaseQuery(query, new String[]{"1"})
            ));
            databaseReadList.add(new MultipleReadDBTask.DatabaseRead(
                    HospitalContract.TABLE_MACHINE_STATUS_NAME,
                    new MultipleReadDBTask.DatabaseQuery(machineStatusQuery, new String[]{"1"})
            ));

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

                        queryCursor.close();
                        machineStatusCursor.close();

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
