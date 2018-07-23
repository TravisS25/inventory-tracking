package expert.codinglevel.inventory_tracking.json;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.Map;

public class CustomStringRequest extends StringRequest {
    private final static String TAG = CustomStringRequest.class.getSimpleName();
    private Map<String, String> mHeaders;
    private String[] mHeaderNames;

    public CustomStringRequest(
            int method,
            String url,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener,
            Map<String, String> headers,
            String[] headerNames
    )
    {
        super(method, url, listener, errorListener);
        mHeaders = headers;
        mHeaderNames = headerNames;
    }

    public CustomStringRequest(
            int method,
            String url,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener,
            Map<String, String> headers
    )
    {
        super(method, url, listener, errorListener);
        mHeaders = headers;
    }

    public CustomStringRequest(
            String url,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener,
            Map<String, String> headers
    )
    {
        super(url, listener, errorListener);
        mHeaders = headers;
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
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        Log.i(TAG, "+++ Made to parse network +++");
        if(mHeaderNames != null){
            for(String headerName: mHeaderNames){
                mHeaders.put(
                        headerName,
                        response.headers.get(headerName)
                );
            }
        } else{
            for(Map.Entry<String, String> entry: response.headers.entrySet()){
                mHeaders.put(entry.getKey(), entry.getValue());
            }
        }

        return super.parseNetworkResponse(response);
    }
}
