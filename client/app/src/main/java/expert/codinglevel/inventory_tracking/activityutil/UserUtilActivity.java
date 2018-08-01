package expert.codinglevel.inventory_tracking.activityutil;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import expert.codinglevel.inventory_tracking.LoginActivity;
import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.json.JsonResponses;
import expert.codinglevel.inventory_tracking.setting.Preferences;

/**
 *  UserUtilActivity is used for the sole purpose of making an http request to verify
 *  that the user is logged in and has a valid cookie
 *
 *  If not, user will be redirected back to login activity
 */
public class UserUtilActivity extends AppCompatActivity {
    protected ArrayList<String> mUserGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Context context = this;
        String userCookie = Preferences.getDefaults(this, getString(R.string.user_session));

        // Check if user a user cookie
        // If user does, make http request to validate that it is still good
        // Else redirect user back to login activity
        if(userCookie != null){
            RequestQueue queue = Volley.newRequestQueue(this);
            final HashMap<String, String> headers = new HashMap<>();
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    getString(R.string.host_url) + "/api/account/user/details/",
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String user = response.optString("user", "");
                            String groups = response.optString("groups", "");

                            if(user == null || user.equals("")){
                                Intent intent = new Intent(context, LoginActivity.class);
                                startActivity(intent);
                            } else{
                                try{
                                    JSONArray jsonArray = response.getJSONArray("groups");
                                    for(int i = 0; i < jsonArray.length(); i++){
                                        mUserGroups.add(jsonArray.optString(i, ""));
                                    }
                                }
                                catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            JsonResponses.volleyError(context, error);
                        }
                    }
            );

            queue.add(request);
        } else{
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }
}
