package nctu.nol.account;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import nctu.nol.badmintonlogprogram.MainActivity;
import nctu.nol.badmintonlogprogram.R;

/**
 * Created by NOL on 2016/7/25.
 */
public class LoginPage extends Activity {
    //login page, this page should only be seen by uses that
    //  1.loged out 2.haven't logged in before 3.password change after last login
    // in other cases, user will be redircted to main page
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String result="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this,NetworkCheckService.class);
        startService(intent);
        sharedPreferences = getSharedPreferences(getString(R.string.PREFS_NAME),0);
        editor=sharedPreferences.edit();

        if(NetworkCheck.isNetworkConnected(this)){

            String account=sharedPreferences.getString("account",null);
            String passwd=sharedPreferences.getString("passwd",null);
            if((account!=null)&&(passwd!=null))
            {
                //test auto login
                //login(account,passwd);
                Log.d("TAG", "online ");
                Intent i = new Intent(getApplicationContext(), FakeLogin.class);
                startActivity(i);
                //if fail , wait for user login
            }
        }
        else{
            //test fake login
            if(offline_login())
            {
                Log.d("TAG","offline login");
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
            }
            else
            {
                Log.d("TAG","offline ");
                Toast.makeText(getApplicationContext(), "please connect to network and try again" + result, Toast.LENGTH_LONG).show();
                //  ( (Button)findViewById(R.id.loginbtn)).setEnabled(false);

            }
        }

        setContentView(R.layout.activity_loginpage);

        //check if just created account, auto fill in
        Bundle extras= getIntent().getExtras();
        if(extras!=null) {
            Log.d("Tag",extras.getString("account", ""));
            ((EditText) findViewById(R.id.account)).setText(extras.getString("account", ""));
        }
    }

    boolean offline_login()
    {
        String account = sharedPreferences.getString("account",null);
        String passwd=sharedPreferences.getString("passwd", null);
        Log.d("Tag", "fake login");
        return !((account==null)||(passwd==null));
    }

    public void login_click(final View view) {
        // Toast.makeText(view.getContext(),"loading...",Toast.LENGTH_SHORT).show();
        //check ac&pw not empty
        final String account =((EditText)findViewById(R.id.account)).getText().toString();
        String passwd=((EditText)findViewById(R.id.passwd)).getText().toString().trim();
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(account).matches())
        {
            Toast.makeText(view.getContext(),"account is a invaild email",Toast.LENGTH_LONG).show();
            return;
        }
        if(passwd.isEmpty()||passwd==null)
        {
            Toast.makeText(view.getContext(),"password can't be blank",Toast.LENGTH_LONG).show();
            return;
        }
        //TODO encrypt pw
        final String encry_pw=passwd;
        //sent to server

        login(account, encry_pw);
    }

    public void create_click(View view) {
        //switch to register page
        Intent i = new Intent(getApplicationContext(), RegisterPage.class);
        startActivity(i);
    }

    void login(final String account, final String passwd)
    {//auto jump to mainactivity if success,if fail pops out a toast and clears the passwd textbox
        Log.d("Tag", "login btn click");
        API.login(account, passwd, new ResponseListener() {
            public void onResponse(JSONObject response) {
                try {
                    result = response.getString("result");
                    if (result.equals("login succeed")) {
                        //login success
                        String token = response.getJSONObject("data").getString("token");
                        editor.putString("token", token);
                        editor.putString("account", account);
                        editor.putString("passwd", passwd);
                        editor.commit();
                        Log.d("Tag", "login success");
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "login fail, error code" + result, Toast.LENGTH_LONG).show();
                        ((EditText) findViewById(R.id.passwd)).setText("");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onErrorResponse(VolleyError error) {
                Log.d("Tag", "response error");
                Log.d("Tag", error.toString());
                Toast.makeText(getApplicationContext(), "login fail", Toast.LENGTH_LONG).show();
                ((EditText) findViewById(R.id.passwd)).setText("");
            }
        });

    }
}

