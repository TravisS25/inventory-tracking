package expert.codinglevel.hospital_inventory.json;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import expert.codinglevel.hospital_inventory.R;

/**
 *  CustomJsonObjectRequest inherits from JsonObjectRequest and sole
 *  purpose is to be able to add headers to a json request// Request
 *  This class takes an extra parameter to the default constructor
 *  and overrides the getHeaders() function to return the passed headers
 */
public class CustomJsonObjectRequest extends JsonObjectRequest {
    private HashMap<String, String> mHeaders;
    private Context mContext;

    public CustomJsonObjectRequest(
            int method,
            String url,
            JSONObject jsonObject,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    )
    {
        super(method, url, jsonObject, listener, errorListener);
    }

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

    public CustomJsonObjectRequest(
            int method,
            String url,
            JSONObject jsonObject,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener,
            HashMap<String, String> headers,
            Context context
    )
    {
        super(method, url, jsonObject, listener, errorListener);
        mHeaders = headers;
        mContext = context;
    }

    /**
     *  Returns and sends the passed headers from the constructor
     *  to requests
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            JSONObject jsonResponse = new JSONObject(jsonString);
            mHeaders.put(
                    mContext.getString(R.string.csrf_token),
                    jsonResponse.getString(mContext.getString(R.string.csrf_token))
            );
            mHeaders.put(
                    mContext.getString(R.string.cookie),
                    jsonResponse.getString(mContext.getString(R.string.cookie))
            );
            return Response.success(jsonResponse,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
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