package expert.codinglevel.inventory_tracking.interfaces;

import android.database.sqlite.SQLiteDatabase;

public interface IAsyncResponse<T> {
    void processFinish(T result);
}
