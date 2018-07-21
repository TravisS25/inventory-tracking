package expert.codinglevel.hospital_inventory.json;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class CustomJsonArrayRequest extends JsonArrayRequest {
    private HashMap<String, String> mHeaders;
    //private String[] mHeaderNames;

    public CustomJsonArrayRequest(
            int method,
            String url,
            JSONArray jsonArray,
            Response.Listener<JSONArray> listener,
            Response.ErrorListener errorListener,
            HashMap<String, String> headers
    )
    {
        super(method, url, jsonArray, listener, errorListener);
        mHeaders = headers;
    }

//    public CustomJsonArrayRequest(
//            int method,
//            String url,
//            JSONArray jsonArray,
//            Response.Listener<JSONArray> listener,
//            Response.ErrorListener errorListener,
//            HashMap<String, String> headers,
//            String[] headerNames
//    )
//    {
//        super(method, url, jsonArray, listener, errorListener);
//        mHeaders = headers;
//        //mHeaderNames = headerNames;
//    }

    /**
     *  Returns and sends the passed headers from the constructor
     *  to requests
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return mHeaders;
    }

//    @Override
//    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
//        try {
//            String jsonString = new String(response.data,
//                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
//            JSONArray jsonResponse = new JSONArray(jsonString);
//
//            if(mHeaderNames != null){
//                for(String headerName: mHeaderNames){
//                    mHeaders.put(
//                            headerName,
//                            jsonResponse.getString(headerName)
//                    );
//                }
//            }
//
//            return Response.success(jsonResponse,
//                    HttpHeaderParser.parseCacheHeaders(response));
//        } catch (UnsupportedEncodingException e) {
//            return Response.error(new ParseError(e));
//        } catch (JSONException je) {
//            return Response.error(new ParseError(je));
//        }
//    }
}
