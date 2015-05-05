package com.saransh.smartsupper;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.saransh.smartsupper.library.DatabaseHandler;
import com.saransh.smartsupper.library.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;


public class SignUp extends ActionBarActivity {

    String name;
    String email;
    String phone;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
    }
    public void register(View view)
    {
        name = ((EditText) findViewById(R.id.fname)).getText().toString();
        email = ((EditText) findViewById(R.id.email)).getText().toString();
        phone = ((EditText) findViewById(R.id.phone)).getText().toString();
        password = ((EditText) findViewById(R.id.password)).getText().toString();
        new AttemptRegister().execute();

    }
    class AttemptRegister extends AsyncTask<String, String, String> {
        int success=0;
        @Override
        protected String doInBackground(String... params) {
            UserFunctions userFunctions=new UserFunctions();
            JSONObject result = userFunctions.registerUser(name, email, phone, password);
            if (result == null)
                return "There was some internal error. Please try again later";
            try {
                success = result.getInt("success");

                if(success==0)
                {
                    return result.getString("error_msg");
                }
                else
                {
                    userFunctions.logoutUser(getApplicationContext());
                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    JSONObject user = result.getJSONObject("user");
                    String name = user.getString("name");
                    String email = user.getString("email");
                    String id = user.getString("id");
                    String method = user.getString("method");
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
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
