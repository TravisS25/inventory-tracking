package expert.codinglevel.inventory_tracking.json;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.interfaces.IJsonRequestCallback;

import com.android.volley.RequestQueue;

/**
 *  JsonRequest is util class for making http request and extracting
 *  the headers from response, mainly for tokens to use on future requests
 */
public class JsonRequest {
    private JsonRequest(){}

    /**
     * Wrapper function for requesting json data and adding returned headers to {@param headers}
     * along with calling a callback
     * @param context - Context of activity this called on
     * @param headers - Hashmap in which to add headers to from returned response
     * @param callback - Callback interface that allows user to do what they want with
     *                 a successful or error response
     * @param url - Url in which you want to request token for
     * @return
     * Returns a JsonObjectRequest which you will generally add to {@link RequestQueue}
     */
    public static JsonObjectRequest getJSONRequestTokenObject(
            final Context context,
            final HashMap<String, String> headers,
            final IJsonRequestCallback callback,
            String url
    )
    {
        return new JsonObjectRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        headers.put(
                            context.getString(R.string.csrf_token),
                            response.getString("csrfToken")
                        );
                        headers.put(
                            context.getString(R.string.cookie),
                            response.getString("cookie")
                        );
                    }
                    catch (JSONException ex){
                        ex.printStackTrace();
                    }

                    if(callback != null){
                        callback.successCallback(response);
                    }
                }
                },
                new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
//                    if(displayToastError){
//                        JsonResponses.volleyError(context, error);
//                    }
                    if(callback != null){
                        callback.errorCallback(context, error);
                    }
//                    else{
//                        JsonResponses.volleyError(context, error);
//                    }
                }
            }
        );
    }
}
