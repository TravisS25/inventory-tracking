package expert.codinglevel.hospital_inventory.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

import expert.codinglevel.hospital_inventory.LoginActivity;
import expert.codinglevel.hospital_inventory.R;
import expert.codinglevel.hospital_inventory.json.CustomJsonObjectRequest;
import expert.codinglevel.hospital_inventory.json.JsonResponses;

/**
 *  UserActivity is used for the sole purpose of making an http request to verify
 *  that the user is logged in and has a valid cookie
 *
 *  If not, user will be redirected back to login activity
 */
public class UserActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        String userCookie = Preferences.getDefaults(this, "user");

        // Check if user a user cookie
        // If user does, make http request to validate that it is still good
        // Else redirect user back to login activity
        if(userCookie != null){
            RequestQueue queue = Volley.newRequestQueue(this);
            final HashMap<String, String> headers = new HashMap<>();
            final Context context = this;
            CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                    Request.Method.GET,
                    getString(R.string.host_url) + "/api/account/user/details/",
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // If headers received from response do not contain a user session,
                            // redirect back to login activity
                            if(headers.get("user") == null || headers.get("user").equals("")){
                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            JsonResponses.volleyError(context, error);
                        }
                    },
                    headers
            );

            queue.add(request);
        } else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
