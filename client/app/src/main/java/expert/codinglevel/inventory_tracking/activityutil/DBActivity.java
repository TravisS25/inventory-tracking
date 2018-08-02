package expert.codinglevel.inventory_tracking.activityutil;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;

public class DBActivity extends AppCompatActivity {
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

    @Override
    protected void onResume(){
        super.onResume();

        if(mDBHasStopped){
            retrieveDB();
        }

        mDBHasStopped = false;
    }

    @Override
    protected void onStop(){
        super.onStop();
        mDB.close();
        mDBHasStopped = true;
    }
}
