package expert.codinglevel.hospital_inventory.interfaces;

import android.content.Context;

import com.android.volley.VolleyError;

/**
 *
 * @param <T> -
 */
public interface IJsonRequestCallback<T> {
    void successCallback(T response);
    void errorCallback(Context context, VolleyError error);
}
