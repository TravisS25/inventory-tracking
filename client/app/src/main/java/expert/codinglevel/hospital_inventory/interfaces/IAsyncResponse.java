package expert.codinglevel.hospital_inventory.interfaces;

import android.database.sqlite.SQLiteDatabase;

public interface IAsyncResponse<T> {
    void processFinish(T result);
}
