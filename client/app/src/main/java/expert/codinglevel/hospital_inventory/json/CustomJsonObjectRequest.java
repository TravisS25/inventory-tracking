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


/**
 *  CustomJsonObjectRequest inherits from JsonObjectRequest and sole
 *  purpose is to be able to add headers to a json request// Request
 *  This class takes an extra parameter to the default constructor
 *  and overrides the getHeaders() function to return the passed headers
 */
public class CustomJsonObjectRequest extends JsonObjectRequest {
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
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            JSONObject jsonResponse = new JSONObject(jsonString);

            if(mHeaderNames != null){
                for(String headerName: mHeaderNames){
                    mHeaders.put(
                            headerName,
                            jsonResponse.getString(headerName)
                    );
                }
            }

            return Response.success(jsonResponse,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}