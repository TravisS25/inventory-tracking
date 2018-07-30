package expert.codinglevel.inventory_tracking.json;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.interfaces.IJsonRequestCallback;
import expert.codinglevel.inventory_tracking.setting.Preferences;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

/**
 *  JsonRequest is util class for making http request and extracting
 *  the headers from response, mainly for tokens to use on future requests
 */
public class JsonRequest {
    private final static String TAG = JsonRequest.class.getSimpleName();
    private JsonRequest(){}

    /**
     * @param context
     * @return - The return will indicate current
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static JSONObject getUserDetailsResponse(
        final Context context,
        @Nullable String userSession
    ) throws InterruptedException, ExecutionException {
        String url = context.getString(R.string.host_url) + "/api/account/user/details/";
        RequestQueue queue = Volley.newRequestQueue(context);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        if(userSession == null){
            userSession = Preferences.getDefaults(context, context.getString(R.string.user_session));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(context.getString(R.string.cookie), userSession);
        final String mURL = "https://google.com";

        CustomJsonObjectRequest request = new CustomJsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            future,
            future,
            headers
        );
//        JsonObjectRequest request = new JsonObjectRequest(
//                Request.Method.GET,
//                mURL,
//                new JSONObject(),
//                future,
//                future
//        );
        queue.add(request);

        try{
            JSONObject foo = future.get(3, TimeUnit.SECONDS); // this will block
            Log.i(TAG, "+++ after future request +++");
            return foo;
        } catch (TimeoutException e){
            e.printStackTrace();
        }

        return null;
    }

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
