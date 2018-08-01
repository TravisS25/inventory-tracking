package expert.codinglevel.inventory_tracking;

//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.Loader;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import expert.codinglevel.inventory_tracking.enums.ScanType;
import expert.codinglevel.inventory_tracking.json.JsonResponses;
import expert.codinglevel.inventory_tracking.model.HospitalDbHelper;
import expert.codinglevel.inventory_tracking.setting.MachineSettings;
import expert.codinglevel.inventory_tracking.setting.Preferences;
import expert.codinglevel.inventory_tracking.model.HospitalContract;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.task.InsertDatabaseTask;
import expert.codinglevel.inventory_tracking.task.ReadDatabaseTask;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;


public class ScanActivity extends AppCompatActivity implements
        DecoratedBarcodeView.TorchListener {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int SCAN_DELAY = 1500; // 1.5 sec
    private static final boolean DEBUG = true;
    private static final int LOADER_ID = 1;
    private boolean mCanScan = true;
    private SQLiteDatabase mDB;
    private MachineSettings mMachineSettings;
    private ScanType mScanType;
    private DecoratedBarcodeView mBarcodeView;
    private BeepManager mBeepManager;
    private Button mSwitchFlashlightButton;
    private ScanActivity mActivity;
    private BarcodeCallback callback = new BarcodeCallback() {
        private long lastTimestamp = 0;
        private String lastMachineName;

        @Override
        public void barcodeResult(BarcodeResult result) {
            if (DEBUG) Log.i(TAG, "+++ barcodeResult() called! +++");
            if(
                    (result.getText() != null) &&
                            (System.currentTimeMillis() - lastTimestamp > SCAN_DELAY) &&
                            (mCanScan)
                    ){
                lastTimestamp = System.currentTimeMillis();
                mBeepManager.playBeepSoundAndVibrate();
                final String machineName = result.getText();

                if(mScanType == ScanType.INVENTORY){
                    Log.i(TAG, "+++ scanning inventory +++");
                    if (DEBUG) Log.i(TAG, machineName);

                    // Simply checking if previous scan was same machine number
                    // If it was, display message and return
                    if(machineName.equals(lastMachineName)) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Just Scanned Machine",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    lastMachineName = machineName;
                    insertMachineName(getContentValues(machineName));
                }

                else if(mScanType == ScanType.LOOKUP){
                    Log.i(TAG, "+++ looking up machine! +++");
                    mCanScan = false;
                    machineLookUp(machineName);
                }
            }
            else{
                Log.i(TAG, "+++ barcode ignored! +++");
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mActivity = this;
        mScanType = (ScanType) getIntent().getSerializableExtra("scanType");
        Log.i(TAG, mScanType.toString());

        mSwitchFlashlightButton = findViewById(R.id.switch_flashlight);
        mBeepManager = new BeepManager(this);
        mBeepManager.setBeepEnabled(false);
        mBeepManager.setVibrateEnabled(true);

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            mSwitchFlashlightButton.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new RetrieveDatabaseTask(this, new IAsyncResponse<SQLiteDatabase>() {
            @Override
            public void processFinish(SQLiteDatabase result) {
                mDB = result;
                mBarcodeView = findViewById(R.id.zxing_barcode_scanner);
                if(mScanType == ScanType.LOOKUP){
                    mBarcodeView.setStatusText("Machine Look Up...");
                }
                else if(mScanType == ScanType.INVENTORY){
                    mBarcodeView.setStatusText("Inventory Scanning...");
                }
                mBarcodeView.setTorchListener(mActivity);
                mBarcodeView.decodeContinuous(callback);
                mBarcodeView.resume();
                askForCameraPermission();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Gson gson = new Gson();
                        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                        String json = sharedPref.getString(
                                getString(R.string.machine_settings),
                                null
                        );
                        mMachineSettings = gson.fromJson(json, MachineSettings.class);
                    }
                }).start();
            }
        }).execute();
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBarcodeView.pause();
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if(mDB.isOpen()){
//            mDB.close();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void switchFlashlight(View view) {
        if (getString(R.string.turn_on_flashlight).equals(mSwitchFlashlightButton.getText())) {
            mBarcodeView.setTorchOn();
        } else {
            mBarcodeView.setTorchOff();
        }
    }

    @Override
    public void onTorchOn() {
        mSwitchFlashlightButton.setText(R.string.turn_off_flashlight);
    }

    @Override
    public void onTorchOff() {
        mSwitchFlashlightButton.setText(R.string.turn_on_flashlight);
    }

    private void askForCameraPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

    private void machineLookUp(String machineName){
        final Activity activity = this;
        final Toast searchingToast =  Toast.makeText(
                this,
                "Searching...",
                Toast.LENGTH_SHORT);
        searchingToast.show();

        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.host_url) + "/api/machine/look-up/" + machineName,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mCanScan = true;
                        searchingToast.cancel();
                        Intent intent = new Intent(activity, LookUpActivity.class);
                        intent.putExtra("machineJson", response.toString());
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mCanScan = true;
                        searchingToast.cancel();
                        JsonResponses.volleyError(activity, error);
                    }
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonRequest);
    }

    private ContentValues getContentValues(String machineName){
        DateFormat dateFormat = new SimpleDateFormat(
                getString(R.string.date_format)
        );
        Date currentDate = new Date();

        Gson gson = new Gson();
        String json = Preferences.getDefaults(mActivity, getString(R.string.machine_settings));
        MachineSettings machineSettings = gson.fromJson(json, MachineSettings.class);
        ContentValues contentValues = new ContentValues();
        contentValues.put(HospitalContract.Machine.MACHINE_NAME, machineName);
        contentValues.put(HospitalContract.Machine.SCANNED_TIME, dateFormat.format(currentDate));
        contentValues.put(
                HospitalContract.Room.ID,
                machineSettings.getFloor().getValue()
        );
        contentValues.put(
                HospitalContract.MachineStatus.ID,
                machineSettings.getMachineStatus().getValue()
        );

        return contentValues;
    }

    private void insertMachineName(final ContentValues contentValues){
        // Here we call the constructor that allows us to query the database
        // We are querying the current machine name that was just scanned
        // to see if it already exists
        // If it does, we display error message and return

        new ReadDatabaseTask(
                HospitalDbHelper.getMachineByAssetTagQuery(),
                new String[]{contentValues.getAsString(HospitalContract.Machine.MACHINE_NAME)},
                mDB,
                new IAsyncResponse<Cursor>(){
                    @Override
                    public void processFinish(Cursor outerCursor){
                        if(DEBUG) Log.i(TAG, "+++ query finished +++");
                        if(DEBUG) Log.i(TAG, Integer.toString(outerCursor.getCount()));

                        // If the cursor returns with no items, that means our query didn't find
                        // current machine name so it doesn't exists and we can safely insert
                        // Else machine name already exists so display message
                        if(outerCursor.getCount() == 0){
                            new InsertDatabaseTask(
                                    HospitalContract.Machine.TABLE_NAME,
                                    null,
                                    contentValues,
                                    mDB,
                                    new IAsyncResponse<Long>(){
                                        @Override
                                        public void processFinish(Long result){
                                            if(DEBUG) Log.i(TAG, "+++ insert finished +++");

                                            mBarcodeView.setStatusText(
                                                contentValues.getAsString(
                                                    HospitalContract.Machine.MACHINE_NAME
                                                )
                                            );

                                            Toast.makeText(
                                                    getApplicationContext(),
                                                    "Machine Scanned",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }
                            ).execute();
                        }
                        else{
                            while(outerCursor.moveToNext()){
                                Log.i(TAG, outerCursor.getString(
                                        outerCursor.getColumnIndex(HospitalContract.Machine.MACHINE_NAME))
                                );
                            }

                            Toast.makeText(
                                    getApplicationContext(),
                                    "Machine already saved",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                        outerCursor.close();
                    }
                }
        ).execute();
    }
}
