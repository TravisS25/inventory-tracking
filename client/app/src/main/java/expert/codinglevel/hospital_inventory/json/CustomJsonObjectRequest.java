package expert.codinglevel.hospital_inventory.json;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class CustomJsonObjectRequest extends JsonObjectRequest {
    private HashMap<String, String> mHeaders;

    public CustomJsonObjectRequest(
        int method,
        String url,
        JSONObject jsonObject,
        Response.Listener<JSONObject> listener,
        Response.ErrorListener errorListener,
        HashMap<String, String> headers
    )
    {
        super(method, url, jsonObject, listener, errorListener);
        mHeaders = headers;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

//    public static String getJsonString(String jsonString, String key){
//        JSONObject temp;
//        String value;
//        try {
//            temp = new JSONObject(jsonString);
//            value = temp.getString(key);
//        } catch (JSONException e) {
//            value = null;
//            e.printStackTrace();
//
//        }
//        return value;
//    }

}