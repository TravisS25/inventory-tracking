package expert.codinglevel.inventory_tracking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import expert.codinglevel.inventory_tracking.activityutil.UserUtilActivity;
import expert.codinglevel.inventory_tracking.interfaces.IJsonRequestCallback;
import expert.codinglevel.inventory_tracking.json.CustomJsonArrayRequest;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.json.JsonRequest;
import expert.codinglevel.inventory_tracking.json.UserDetailsJsonCallback;
import expert.codinglevel.inventory_tracking.model.Filter;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.view.TextValue;

public class MachineLogFilterActivity extends UserUtilActivity implements DatePickerDialog.OnDateSetListener {
    private final static String TAG = MachineLogsActivity.class.getSimpleName();
    private final static String USER_FIELD = "entered_by.full_name";
    private final String mURL = getString(R.string.host_url) + "/api/log/index/";
    private final Context mContext = this;
    private RequestQueue mQueue = Volley.newRequestQueue(this);
    private Map<String, String> mHeaders = new HashMap<>();
    private Spinner mOperationSpinner;
    private String mSearchText;
    private EditText mStartDate, mEndDate;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestUserDetails();
        setContentView(R.layout.activity_machine_log_filter);
        initViews();
        initSearchText();
        initDatePickers();
        initOperationsList();
    }

    //
    private void requestUserDetails(){
        try{
            // This is a blocking call to http request
            JSONObject response = JsonRequest.getUserDetailsResponse(this, null);

            try{
                JSONObject user = response.getJSONObject("user");

                if(user == null){
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                }

            } catch (JSONException e){
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }

        } catch (InterruptedException | ExecutionException e){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void initSearchText(){
        mSearchText = getIntent().getStringExtra("search");
    }

    private void initViews(){
        mOperationSpinner = (Spinner) findViewById(R.id.operation);
        mStartDate = (EditText) findViewById(R.id.start_date);
        mEndDate = (EditText) findViewById(R.id.end_date);
    }

    private void initOperationsList(){
        // Set array list with http operations
        ArrayList<TextValue> spinnerArray = new ArrayList<>(4);
        spinnerArray.add(new TextValue("--Select Operation--", null));
        spinnerArray.add(new TextValue("POST", "POST"));
        spinnerArray.add(new TextValue("PUT", "PUT"));
        spinnerArray.add(new TextValue("DELETE", "DELETE"));
        ArrayAdapter<TextValue> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                spinnerArray
        );

        // Set spinner display dropdown and set adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOperationSpinner.setAdapter(adapter);

        // Add item select event listener where if the value selected does not equal null,
        // make http request with selected values
        mOperationSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Extract value from selected value
                TextValue item = (TextValue) adapterView.getSelectedItem();

                // If selected item is not null, gather all filter form values and make
                // http filter search request
                if(item.getValue() != null){
                    // Get other values
                    String operationValue = item.getValue();
                    String startDate = mStartDate.getText().toString();
                    String endDate = mEndDate.getText().toString();
                    JSONArray jsonArray = new JSONArray();

                    // If any value is not null or empty string, add to json array
                    if(operationValue != null){
                        jsonArray.put(new Filter("operation", "eq", operationValue));
                    }
                    if(startDate.equals("")){
                        jsonArray.put(new Filter("date_entered", "gte", startDate));
                    }
                    if(endDate.equals("")){
                        jsonArray.put(new Filter("date_entered", "lte", endDate));
                    }

                    //
                    String url = getString(R.string.host_url) + "/api/log/count-index/";
                    String userSession = Preferences.getDefaults(
                            mContext,
                            getString(R.string.user_session)
                    );
                    Map<String, String> headers = new HashMap<>();
                    headers.put(getString(R.string.cookie), userSession);
                    CustomJsonArrayRequest request = new CustomJsonArrayRequest(
                            Request.Method.GET,
                            url,
                            jsonArray,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            },
                            headers
                    );

                    mQueue.add(request);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initDatePickers(){
        EditText startDate = (EditText)findViewById(R.id.start_date);
        EditText endDate = (EditText) findViewById(R.id.end_date);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // API 21
            startDate.setShowSoftInputOnFocus(false);
            endDate.setShowSoftInputOnFocus(false);
        } else { // API 11-20
            startDate.setTextIsSelectable(true);
            endDate.setTextIsSelectable(true);
        }

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var1){
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        mContext,
                        MachineLogFilterActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View var1){
                Calendar now = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        mContext,
                        MachineLogFilterActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Log.i(TAG, Integer.toString(datePicker.getId()));
        Toast.makeText(this, "date set", Toast.LENGTH_SHORT).show();
    }
}
