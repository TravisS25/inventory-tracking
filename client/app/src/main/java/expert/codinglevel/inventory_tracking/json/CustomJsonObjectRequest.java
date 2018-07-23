package expert.codinglevel.inventory_tracking.json;

import android.content.Context;
import android.util.Log;

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


/**
 *  CustomJsonObjectRequest inherits from JsonObjectRequest and sole
 *  purpose is to be able to add headers to a json request// Request
 *  This class takes an extra parameter to the default constructor
 *  and overrides the getHeaders() function to return the passed headers
 */
public class CustomJsonObjectRequest extends JsonObjectRequest {
    private final static String TAG = CustomJsonObjectRequest.class.getSimpleName();
    private HashMap<String, String> mHeaders;
    private String[] mHeaderNames;

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
            String[] headerNames
    )
    {
        super(method, url, jsonObject, listener, errorListener);
        mHeaders = headers;
        mHeaderNames = headerNames;
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
        String jsonString;
        JSONObject jsonResponse;

        try {
            if (response.data != null && !new String(response.data, "UTF-8").equals("")){
                Log.i(TAG, "+++ response data +++ " + new String(response.data, "UTF-8"));
                jsonString = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                Log.i(TAG, "String here" + jsonString);
                jsonResponse = new JSONObject(jsonString);

                if(mHeaderNames != null){
                    for(String headerName: mHeaderNames){
                        mHeaders.put(
                                headerName,
                                jsonResponse.getString(headerName)
                        );
                    }
                }

                return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));
            }

            jsonResponse = new JSONObject("{}");
            return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException | JSONException e) {
            Log.i(TAG, "+++ error parsing response");
            e.printStackTrace();
            return Response.error(new ParseError(e));
        }
    }
}