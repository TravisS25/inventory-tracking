package expert.codinglevel.hospital_inventory.json;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import expert.codinglevel.hospital_inventory.R;
import expert.codinglevel.hospital_inventory.interfaces.IJsonRequestCallback;

public class JsonRequest {
    private JsonRequest(){}

    public static JsonObjectRequest getJSONRequestTokenObject(
            final Context context,
            final HashMap<String, String> headers,
//            final boolean displayToastError,
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
