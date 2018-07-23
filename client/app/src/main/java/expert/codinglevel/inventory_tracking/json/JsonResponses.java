package expert.codinglevel.inventory_tracking.json;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;

public class JsonResponses {
    public static final String TAG = JsonResponses.class.getSimpleName();
    private JsonResponses(){}

    public static void volleyError(Context context, VolleyError error){
        if(error.networkResponse != null){
            try{
                String stringError = new String(error.networkResponse.data, "UTF-8");
                Toast.makeText(
                        context,
                        stringError,
                        Toast.LENGTH_LONG).show();
                return;
            }
            catch (UnsupportedEncodingException ex){
                ex.printStackTrace();
            }
        }
        else{
            Toast.makeText(
                    context,
                    "Unexpected error has occurred, please try again later",
                    Toast.LENGTH_LONG).show();
            return;
        }
    }
}
