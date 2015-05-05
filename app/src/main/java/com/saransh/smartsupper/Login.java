package com.saransh.smartsupper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.saransh.smartsupper.library.DatabaseHandler;
import com.saransh.smartsupper.library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;


public class Login extends ActionBarActivity {
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;

    int facebook_flag = 0;

    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        login_facebook_background();

        setContentView(R.layout.activity_login);
    }

    public void login(View v) {

        email = ((EditText) findViewById(R.id.email)).getText().toString();
        password = ((EditText) findViewById(R.id.password)).getText().toString();

        new AttemptLogin().execute();

    }

    public void login_facebook(View v) {

        facebook_flag = 1;
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
            }
        };

    }

    class AttemptLogin extends AsyncTask<String, String, String> {

        int success = 0;
        @Override
        protected String doInBackground(String... params) {

            UserFunctions userFunctions = new UserFunctions();
            JSONObject result = userFunctions.loginUser(email, password);

            if (result == null)
                return "There was some internal error. Please try again later";
            try {
                success = result.getInt("success");

                if(success == 0) {
                    return result.getString("error_msg");
                } else {
                    userFunctions.logoutUser(getApplicationContext());
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                    JSONObject user = result.getJSONObject("user");

                    String name = user.getString("name");
                    String email = user.getString("email");
                    String id = user.getString("id");
                    String method = user.getString("login_method");

                    db.addUser(id, name, email, method);

                    db.close();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String params) {
            if(success != 1) {
                Toast.makeText(getApplicationContext(), params, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void login_facebook_background() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.d("JSON", String.valueOf(object));
                                try {
                                    String id = object.getString("id");
                                    String name = object.getString("name");
                                    String email = object.getString("email");

                                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                                    db.addUser(id, name, email, "facebook");

                                    db.close();

                                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(i);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                request.executeAsync();

            }

            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }

        });
    }

    public void register_intent(View v) {
        Intent i = new Intent(getApplicationContext(), SignUp.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(facebook_flag == 1)
            accessTokenTracker.stopTracking();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
