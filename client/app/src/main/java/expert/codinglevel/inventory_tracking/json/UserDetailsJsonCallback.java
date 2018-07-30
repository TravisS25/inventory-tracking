package expert.codinglevel.inventory_tracking.json;

import android.content.Context;
import android.content.Intent;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import expert.codinglevel.inventory_tracking.LoginActivity;
import expert.codinglevel.inventory_tracking.interfaces.IJsonRequestCallback;

public class UserDetailsJsonCallback implements IJsonRequestCallback<JSONObject> {
    private Context mContext;
    private boolean mIsUser;
    private boolean mIsAdmin;
    private boolean mRedirectToLogin;

    public UserDetailsJsonCallback(Context context, boolean redirectToLogin){
        mContext = context;
        mRedirectToLogin = redirectToLogin;
    }

    private void redirectToLogin(){
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }

    public boolean isUser(){
        return mIsUser;
    }

    public boolean isAdmin(){
        return mIsAdmin;
    }

    @Override
    public void successCallback(JSONObject response) {
        try{
            JSONObject user = response.getJSONObject("user");
            JSONArray groups = response.getJSONArray("groups");

            if(user != null){
                mIsUser = true;
            } else{
                if(mRedirectToLogin){
                    redirectToLogin();
                }
            }

            for(int i = 0; i < groups.length(); i++){
                String groupName = groups.optString(i);

                if(groupName.equals("Admin")){
                    mIsAdmin = true;
                    break;
                }
            }

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void errorCallback(Context context, VolleyError error) {
        if(mRedirectToLogin){
            redirectToLogin();
        }

        mIsUser = false;
        mIsAdmin = false;
    }
}
