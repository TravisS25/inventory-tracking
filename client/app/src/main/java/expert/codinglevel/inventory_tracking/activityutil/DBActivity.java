package expert.codinglevel.inventory_tracking.activityutil;

import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.task.RetrieveDatabaseTask;

public class DBActivity extends AppCompatActivity {
    protected SQLiteDatabase mDB;
    protected boolean mHasDBStopped = false;

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
}
