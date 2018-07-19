package expert.codinglevel.hospital_inventory.interfaces;

import android.content.Context;

import com.android.volley.VolleyError;

/**
 * Interface is used for making an  http request for json data
 * @param <T> - Any value from request, generally JSONObject
 */
public interface IJsonRequestCallback<T> {
    /**
     * This is used when there is successful http request
     * @param response - Any value from request, generally JSONObject
     */
    void successCallback(T response);

    /**
     * This is used when server returns error from http request
     * @param context - Context of the activity in which this is called
     * @param error - The error that is returned from the http request
     */
    void errorCallback(Context context, VolleyError error);
}
