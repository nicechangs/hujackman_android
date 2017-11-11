package human.hujackman;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity {

    private String mUserId;
    private String mPassword;
    private String mErrorMessage;

    // 메인액티비티 인턴트
    protected  Intent mMainIntent;
    // Alert
    android.app.AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mMainIntent = new Intent(this, MainActivity.class);

        // 임시코드
        // 사용자아이디
        AutoCompleteTextView v_userId = (AutoCompleteTextView) findViewById(R.id.user_id);
        v_userId.setText("nice");

        // 비밀번호
        EditText v_password = (EditText) findViewById(R.id.password);
        v_password.setText("0956");

        Button btn = (Button) findViewById(R.id.sign_in_button);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doLogin();
            }
        });

        // Alert 메시지박스
        mAlert = new AlertDialog.Builder(LoginActivity.this);
        mAlert.setPositiveButton("Notice", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
    }

    private void doLogin()
    {
        // 사용자아이디
        AutoCompleteTextView v_userId = (AutoCompleteTextView) findViewById(R.id.user_id);
        mUserId = v_userId.getText().toString();

        // 비밀번호
        EditText v_password = (EditText) findViewById(R.id.password);
        mPassword = v_password.getText().toString();

        // SET 글로벌 환경변수
        AppConf conf = GlobalVal.getInstance().getAppConf();

        // "http://192.168.0.10:3000/user_mas/login/"
        JSONTask jsonTask = new JSONTask();
        jsonTask.execute(conf.getSvrUrl() + "user_mas/login/");

    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public class JSONTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls){
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("comp_cd" , "CO17-0000001");
                jsonObject.accumulate("usr_id"  , mUserId);
                jsonObject.accumulate("usr_pwd" , mPassword);

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Cache-Control", "no-cache");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept", "text/html");
                    con.setConnectTimeout(5000);
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    OutputStream outStream = con.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    mErrorMessage = e.getMessage();
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if( result == null)
            {
                mAlert.setMessage(mErrorMessage);
                mAlert.show();
                return;
            }

            try
            {
                JSONArray jarrary = new JSONArray(result);
                if ( jarrary.length() <= 0)
                {
                    mAlert.setMessage(getString(R.string.failed_login_1001));
                    mAlert.show();
                    return;
                }

                // 로그인 성공시 글로벌변수 SET
                JSONObject jsonobject = jarrary.getJSONObject(0);
                UserInf uInfo = GlobalVal.getInstance().getUserInfo();
                uInfo.mCOMP_CD  		= jsonobject.getString("COMP_CD"	);
                uInfo.mBR_CD    		= jsonobject.getString("BR_CD"		);
                uInfo.mBR_NM    		= jsonobject.getString("BR_NM"		);
                uInfo.mUSR_ID   		= jsonobject.getString("USR_ID"	);
                uInfo.mUSR_CD   		= jsonobject.getString("USR_CD"	);
                uInfo.mUSR_PWD  		= jsonobject.getString("USR_PWD"	);
                uInfo.mUSR_NM   		= jsonobject.getString("USR_NM"	);
                uInfo.mUSR_DOB  		= jsonobject.getString("USR_DOB"	);
                uInfo.mUSR_TELNO		= jsonobject.getString("USR_TELNO");
                uInfo.mUSR_EMAIL		= jsonobject.getString("USR_EMAIL");
                uInfo.mUSR_ADDR 		= jsonobject.getString("USR_ADDR"	);
                uInfo.mUSR_GRANT		= jsonobject.getString("USR_GRANT");

                // 메인화면으로 이동.
                startActivity(mMainIntent);
                finish();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}

