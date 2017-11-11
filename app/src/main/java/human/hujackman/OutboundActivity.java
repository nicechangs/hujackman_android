package human.hujackman;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static human.hujackman.R.id.outbound_barcode;
import static human.hujackman.R.id.spinner_bizcomp;

public class OutboundActivity extends AppCompatActivity {

    String mTRX_CD			= "";
    String mINB_BIZ_CD		= "";
    String mBIZ_NM       	= "";
    String mINB_DT       	= "";
    String mINB_BR_CD    	= "";
    String mBR_NM        	= "";
    String mBSCITEM_CD   	= "";
    String mUSR_NM       	= "";
    String mMEMO         	= "";
    String mBarCode         	= "";



    // 거래처목록
    ArrayList<String> mBizCompList_Key = new ArrayList<String>();
    ArrayList<String> mBizCompList_Value = new ArrayList<String>();

    private Integer mIDX_BIZCOMP = -1;

    // Alert
    android.app.AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outbound);

        // Alert 메시지박스
        mAlert = new AlertDialog.Builder(OutboundActivity.this);
        mAlert.setPositiveButton("Notice", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });

        // 이벤트 초기화
        initEvents();

        // UI 메타데이타 SET
        initView();
    }

    // 바코드 스캔
    private void getBarCode()
    {
        IntentIntegrator integrator = new IntentIntegrator( this );
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private void searchBarcode()
    {

        // 바코드스캔
        EditText v_barcode = (EditText) findViewById(R.id.outbound_barcode);

        // SET URL
        // http://192.168.0.10:3000/biztrx_mas/gettrxdata?comp_cd=CO17-0000001&bar_cd=9999690000001&trx_st=10&use_yn=Y
        String strUrl = String.format("%1$sbiztrx_mas/gettrxdata?comp_cd=%2$s&bar_cd=%3$s&trx_st=10&use_yn=Y"
                , GlobalVal.getInstance().getAppConf().getSvrUrl()
                , GlobalVal.getInstance().getUserInfo().mCOMP_CD
                , v_barcode.getText().toString()
        );

        JSONTask_GetTrxInboundData jsonTask = new JSONTask_GetTrxInboundData();
        jsonTask.execute(strUrl);
    }

    private void submit()
    {
        if(mTRX_CD.isEmpty())
        {
            mAlert.setMessage(getString(R.string.outbound_nothing_trxcd));
            mAlert.show();
            return;
        }

        // 유효성검사
        if(mIDX_BIZCOMP <= 0)
        {
            mAlert.setMessage(getString(R.string.inbound_select_bizcomp));
            mAlert.show();
            return;
        }

        TextView v_text = (TextView) findViewById(outbound_barcode);
        mBarCode = v_text.getText().toString();
        if(mBarCode.isEmpty())
        {
            mAlert.setMessage(getString(R.string.inbound_select_barcode));
            mAlert.show();
            return;
        }

        AppConf conf = GlobalVal.getInstance().getAppConf();
        JSONTask_Submit jsonTask = new JSONTask_Submit();
        jsonTask.execute(conf.getSvrUrl() + "biztrx_mas/");
    }

    // 이벤트정의
    private void initEvents()
    {
        // 바코드스캔
        Button btn = (Button) findViewById(R.id.inbound_barcode_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBarCode();
            }
        });

        // summit
        Button btn_submit = (Button) findViewById(R.id.inbound_action_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        // 바코드로 내용 검색
        Button btn_barcode_search = (Button) findViewById(R.id.outbound_barcode_search);
        btn_barcode_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBarcode();
            }
        });
    }

    // UI 메타데이타 SET
    private void initView()
    {
        setBizCompSpinner(); // 거래처
    }

    private void setBizCompSpinner()
    {
        // 거래처 콤보박스 SET
        mBizCompList_Key.add(getString(R.string.inbound_select_empty));
        mBizCompList_Value.add(getString(R.string.inbound_select_empty));
        Spinner v_spninner = (Spinner) findViewById(spinner_bizcomp);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mBizCompList_Value);
        v_spninner.setAdapter(spinnerAdapter);
        v_spninner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mIDX_BIZCOMP = i;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // SET URL
        String strUrl = String.format("%1$sbizcomp_mas?comp_cd=%2$s&bizgrp_cd=&biz_nm=&use_yn=Y"
                , GlobalVal.getInstance().getAppConf().getSvrUrl()
                , GlobalVal.getInstance().getUserInfo().mCOMP_CD
        );
        JSONTask_GetBizcomp jsonTask = new JSONTask_GetBizcomp();
        jsonTask.execute(strUrl);
    }

    private void setInboundData(String strMsg)
    {
        EditText v_text = (EditText) findViewById(R.id.editText2);
        v_text.setText(strMsg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                TextView v_text = (TextView) findViewById(outbound_barcode);
                v_text.setText(result.getContents());
                mBarCode = result.getContents();
                // 바코드 검색
                searchBarcode();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // I/F : 거래처목록
    public class JSONTask_GetBizcomp extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
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

            if (result == null) {
                return;
            }

            try {
                JSONArray jarrary = new JSONArray(result);
                if (jarrary.length() <= 0) {
                    return;
                }

                for(int ix = 0; ix < jarrary.length(); ix++)
                {
                    JSONObject jsonobject = jarrary.getJSONObject(ix);

                    // 거래처유형 입고처여부 확인
                    String BIZCO_T		  = jsonobject.getString("BIZCO_T"		);
                    if( BIZCO_T.trim().equals("10") == false)
                        continue;

                    String BIZCO_CD		  = jsonobject.getString("BIZCO_CD"	);
                    String BIZ_NM		  = jsonobject.getString("BIZ_NM"		);

                    mBizCompList_Key.add(BIZCO_CD);
                    mBizCompList_Value.add(BIZ_NM);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // I/F : 입고내역조회
    public class JSONTask_GetTrxInboundData extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();

                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
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

            if (result == null) {
                return;
            }

            try {
                if( result.equals("[]") == true)
                {
                    setInboundData(getString(R.string.outbound_nothing));
                    return;
                }

                JSONArray jarrary = new JSONArray(result);
                if (jarrary.length() <= 0) {
                    return;
                }

                JSONObject jsonobject = jarrary.getJSONObject(0);
                mTRX_CD		= jsonobject.getString("TRX_CD"         );
                mINB_BIZ_CD	= jsonobject.getString("INB_BIZ_CD"    );
                mBIZ_NM       	= jsonobject.getString("BIZ_NM"         );

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat output = new SimpleDateFormat("yyyyMMddHHmmss");
                Date d = sdf.parse(jsonobject.getString("INB_DT"));
                mINB_DT = output.format(d);

                mINB_BR_CD    	= jsonobject.getString("INB_BR_CD"     );
                mBR_NM        	= jsonobject.getString("BR_NM"          );
                mBSCITEM_CD   	= jsonobject.getString("BSCITEM_CD"     );
                mUSR_NM       	= jsonobject.getString("USR_NM"         );
                mMEMO         	= jsonobject.getString("MEMO"           );

                String strMsg = getString(R.string.outbound_trx_cd) + " : " + mTRX_CD + "\r\n";
                strMsg += getString(R.string.outbound_biz_nm)       + " : " + mBIZ_NM + "\r\n";
                strMsg += getString(R.string.outbound_inb_dt)       + " : " + mINB_DT + "\r\n";
                strMsg += getString(R.string.outbound_br_nm)        + " : " + mBR_NM + "\r\n";
                strMsg += getString(R.string.outbound_bscitem_cd)  + " : " + mBSCITEM_CD + "\r\n";
                strMsg += getString(R.string.outbound_usr_nm)       + " : " + mUSR_NM + "\r\n";
                strMsg += getString(R.string.outbound_memo)         + " : " + mMEMO + "\r\n";

                setInboundData(strMsg);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // I/F : submit
    public class JSONTask_Submit extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... urls){
            try {
                UserInf uInfo = GlobalVal.getInstance().getUserInfo();

                Date today = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String strToday = sdf.format(today);

                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("comp_cd"	 , uInfo.mCOMP_CD );
                jsonObject.accumulate("trx_cd"      , mTRX_CD				);
                jsonObject.accumulate("inb_biz_cd"  , mINB_BIZ_CD );
                jsonObject.accumulate("inb_dt"       , mINB_DT	);
                jsonObject.accumulate("inb_br_cd"    , mINB_BR_CD );
                jsonObject.accumulate("bscitem_cd"   , mBSCITEM_CD );
                jsonObject.accumulate("bar_cd"        , mBarCode	);
                jsonObject.accumulate("trx_st"        , "20" );
                jsonObject.accumulate("out_dt"        , strToday	 );
                jsonObject.accumulate("out_biz_cd"   , mBizCompList_Key.get(mIDX_BIZCOMP).toString() );
                jsonObject.accumulate("usr_cd"        , GlobalVal.getInstance().getUserInfo().mUSR_CD );
                jsonObject.accumulate("use_yn"        , "Y" );
                jsonObject.accumulate("memo"          , mMEMO);

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    URL url = new URL(urls[0]);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
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
                return;
            }

            try
            {
                JSONArray jarrary = new JSONArray(result);
                if ( jarrary.length() <= 0)
                {
                    return;
                }

                // RESULT = 0 : 성공
                JSONObject jsonobject = jarrary.getJSONObject(0);
                String strRet		  = jsonobject.getString("@RESULT");
                if (strRet.trim().equals("0") == true)
                {
                    // 성공
                    mTRX_CD = "";
                    setInboundData(getString(R.string.outbound_result_success));
                    mAlert.setMessage(getString(R.string.outbound_result_success));
                    mAlert.show();
                }
                else
                {
                    // 실패
                    if(strRet.trim().equals("-1001") == true)
                    {
                        mAlert.setMessage(getString(R.string.outbound_result_fail));
                    }
                    else
                    {
                        mAlert.setMessage(getString(R.string.outbound_result_fail) + " [" + strRet + "]");
                    }

                    mAlert.show();
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
