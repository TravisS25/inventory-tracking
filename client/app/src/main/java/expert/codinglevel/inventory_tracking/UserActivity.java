package expert.codinglevel.inventory_tracking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.task.UserDetailsTask;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this;
        new UserDetailsTask(this, null, new IAsyncResponse<JSONObject>() {
            @Override
            public void processFinish(JSONObject result) {
                try{
                    JSONObject user = result.getJSONObject(getString(R.string.user));

                    if(user == null){
                        Intent intent = new Intent(context, LoginActivity.class);
                        startActivity(intent);
                    }

                } catch (JSONException e){
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                }

            }
        }).execute();
    }
}
