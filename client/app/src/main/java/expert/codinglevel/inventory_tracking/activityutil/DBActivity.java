package expert.codinglevel.inventory_tracking.activityutil;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.interfaces.IDatabaseCallback;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;

public class DBActivity extends AppCompatActivity {
    public final static String TAG = DBActivity.class.getSimpleName();
    protected SQLiteDatabase mDB;
    protected boolean mDBHasStopped = false;

    protected void retrieveDB(){
        if(mDB != null){
            if(!mDB.isOpen()){
                new RetrieveDatabaseTask(getApplicationContext(), new IAsyncResponse<SQLiteDatabase>() {
                    @Override
                    public void processFinish(SQLiteDatabase result) {
                        mDB = result;
                    }
                }).execute();
            }
        }
    }

    protected void initDB(final @Nullable IDatabaseCallback callback){
        new RetrieveDatabaseTask(
                getApplicationContext(),
                new IAsyncResponse<SQLiteDatabase>() {
                    @Override
                    public void processFinish(SQLiteDatabase result) {
                        mDB = result;

                        if(callback != null){
                            callback.finished();
                        }
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "+++ has resumed +++");

        if(mDBHasStopped){
            Log.i(TAG, "+++ retrieve db +++");
            retrieveDB();
        }

        mDBHasStopped = false;
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "+++ has stopped +++");

        if(mDB != null){
            mDB.close();
        }
        mDBHasStopped = true;
    }
}
