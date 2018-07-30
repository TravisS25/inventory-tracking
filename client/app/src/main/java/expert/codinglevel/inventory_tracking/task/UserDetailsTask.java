package expert.codinglevel.inventory_tracking.task;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import expert.codinglevel.inventory_tracking.R;
import expert.codinglevel.inventory_tracking.interfaces.IAsyncResponse;
import expert.codinglevel.inventory_tracking.json.CustomJsonObjectRequest;
import expert.codinglevel.inventory_tracking.setting.Preferences;

public class UserDetailsTask extends AsyncTask<Void, Void, JSONObject> {
    private final static String TAG = UserDetailsTask.class.getSimpleName();
    private Context mContext;
    private String mUserSession;
    private IAsyncResponse<JSONObject> mCallback;

    public UserDetailsTask(
        Context context,
        @Nullable String session,
        IAsyncResponse<JSONObject> callback
    ){
        mContext = context;
        mUserSession = session;
        mCallback = callback;
    }

    @Override
    protected JSONObject doInBackground(Void... params) {
        String url = mContext.getString(R.string.host_url) + "/api/account/user/details/";
        RequestQueue queue = Volley.newRequestQueue(mContext);
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        if(mUserSession == null){
            mUserSession = Preferences.getDefaults(mContext, mContext.getString(R.string.user_session));
        }

        Map<String, String> headers = new HashMap<>();
        headers.put(mContext.getString(R.string.cookie), mUserSession);

        CustomJsonObjectRequest request = new CustomJsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                future,
                future,
                headers
        );
        queue.add(request);

        try{
            Log.i(TAG, "+++ before future request +++");
            JSONObject foo = future.get(3, TimeUnit.SECONDS); // this will block
            Log.i(TAG, "+++ after future request +++");
            return foo;
        } catch (InterruptedException | ExecutionException | TimeoutException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        mCallback.processFinish(result);
    }
}
