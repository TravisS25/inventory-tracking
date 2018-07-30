package expert.codinglevel.inventory_tracking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import expert.codinglevel.inventory_tracking.adapter.MachineUploadAdapter;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.model.Filter;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;
import expert.codinglevel.inventory_tracking.view.TextValue;

public class MachineLogsActivity extends UserActivity {
    private final static String TAG = MachineLogsActivity.class.getSimpleName();
    private final static String QUERY = "?skip=0&take=20";
    private final static String USER_FIELD = "entered_by.full_name";
    private final Context mContext = this;
    private RequestQueue mQueue;
    private String mURL = getString(R.string.host_url) + "/api/log/count-index/";
    private Map<String, String> mHeaders = new HashMap<>();
    private ArrayAdapter<TextValue> mArrayAdapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item
    );
    private AutoCompleteTextView mSearch;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_logs);
        initViews();
    }

    private void initViews(){
        mSearch = (AutoCompleteTextView) findViewById(R.id.search);
        mSearch.setAdapter(mArrayAdapter);
        mSearch.addTextChangedListener(new TextWatcher() {
            long lastPress = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(System.currentTimeMillis() - lastPress > 500){
                    lastPress = System.currentTimeMillis();
                    Log.i(TAG, "+++ Passed pressed time +++");
//                    CustomJsonObjectRequest request = new CustomJsonObjectRequest(
//                            Request.Method.GET,
//                            mURL,
//                            null,
//                            new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject response) {
//
//                                }
//                            },
//                            new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//
//                                }
//                            },
//                            mHeaders
//                    );
//                    mQueue.add(request);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


}
