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

import static human.hujackman.R.id.inbound_barcode;
import static human.hujackman.R.id.spinner_bizcomp;
import static human.hujackman.R.id.spinner_branch;
import static human.hujackman.R.id.spinner_bscitem;
import static human.hujackman.R.id.spinner_vendor;

public class InboundActivity extends AppCompatActivity {

    private String mErrorMessage;
    private String mBarCode = "";
    private String mMemo = "";

    // 통신사리스트
    ArrayList<String> mVendorList_Key = new ArrayList<String>();
    ArrayList<String> mVendorList_Value = new ArrayList<String>();

    // 거래처목록
    ArrayList<String> mBizCompList_Key = new ArrayList<String>();
    ArrayList<String> mBizCompList_Value = new ArrayList<String>();

    // 품목목록
    ArrayList<String> mBscItemList_Key = new ArrayList<String>();
    ArrayList<String> mBscItemList_Value = new ArrayList<String>();

    // 지점목록
    ArrayList<String> mBranchList_Key = new ArrayList<String>();
    ArrayList<String> mBranchList_Value = new ArrayList<String>();

    private Integer mIDX_VENDOR = -1;
    private Integer mIDX_BIZCOMP = -1;
    private Integer mIDX_BSCITEM = -1;
    private Integer mIDX_BRANCH = -1;

    // Alert
    android.app.AlertDialog.Builder mAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbound);

        // Alert 메시지박스
        mAlert = new AlertDialog.Builder(InboundActivity.this);
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

    // 이벤트정의
    private void initEvents()
    {
        Button btn = (Button) findViewById(R.id.inbound_barcode_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBarCode();
            }
        });

        Button btn_submit = (Button) findViewById(R.id.inbound_action_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });
    }

    // UI 메타데이타 SET
    private void initView()
    {
        setVendorSpinner(); // 벤더
        setBizCompSpinner(); // 거래처
        setBasicItemSpinner(); // 품목
        setBranchListSpinner(); // 지점목록
    }

    private void setVendorSpinner()
    {
        // 통신사 콤보박스 SET
        mVendorList_Key.add(getString(R.string.inbound_select_empty));
        mVendorList_Value.add(getString(R.string.inbound_select_empty));
        mVendorList_Key.add("10");
        mVendorList_Value.add(getString(R.string.inbound_vendor_skt));
        mVendorList_Key.add("20");
        mVendorList_Value.add(getString(R.string.inbound_vendor_kt));
        mVendorList_Key.add("30");
        mVendorList_Value.add(getString(R.string.inbound_vendor_lg));

        Spinner v_spinner_vendor  = (Spinner) findViewById(spinner_vendor);
        ArrayAdapter<String> spinnerAdapter_vendor = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mVendorList_Value);
        v_spinner_vendor.setAdapter(spinnerAdapter_vendor);
        v_spinner_vendor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mIDX_VENDOR = i;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
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

    private void setBasicItemSpinner()
    {
        // 품목 콤보박스 SET
        mBscItemList_Key.add(getString(R.string.inbound_select_empty));
        mBscItemList_Value.add(getString(R.string.inbound_select_empty));

        Spinner v_spinner_bscitem = (Spinner)findViewById( spinner_bscitem );
        ArrayAdapter<String> spinnerAdapter_bscitem = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mBscItemList_Value);
        v_spinner_bscitem.setAdapter(spinnerAdapter_bscitem);
        v_spinner_bscitem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mIDX_BSCITEM = i;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // SET URL
        String strUrl = String.format("%1$sbscitem_mas?comp_cd=%2$s&grp_cd=1000&bscitem_cd=&vendor=&use_yn=Y"
                , GlobalVal.getInstance().getAppConf().getSvrUrl()
                , GlobalVal.getInstance().getUserInfo().mCOMP_CD
        );

        JSONTask_GetBasicItem jsonTask_bscItem = new JSONTask_GetBasicItem();
        jsonTask_bscItem.execute(strUrl);
    }

    private void setBranchListSpinner()
    {

        // 지점목록 SET
        mBranchList_Key.add(getString(R.string.inbound_select_empty));
        mBranchList_Value.add(getString(R.string.inbound_select_empty));

        Spinner v_spinner_branch = (Spinner)findViewById( spinner_branch );
        ArrayAdapter<String> spinnerAdapter_branchList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mBranchList_Value);
        v_spinner_branch.setAdapter(spinnerAdapter_branchList);
        v_spinner_branch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mIDX_BRANCH = i;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        // SET URL
        String strUrl = String.format("%1$sbranch_mas?comp_cd=%2$s&br_nm=&use_yn=Y"
                , GlobalVal.getInstance().getAppConf().getSvrUrl()
                , GlobalVal.getInstance().getUserInfo().mCOMP_CD
        );

        JSONTask_GetBranchList jsonTask_branch = new JSONTask_GetBranchList();
        jsonTask_branch.execute(strUrl);
    }



    // 바코드 스캔
    private void getBarCode()
    {
        IntentIntegrator integrator = new IntentIntegrator( this );
        integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    private void submit()
    {
        // 유효성검사
        if(mIDX_BIZCOMP <= 0)
        {
            mAlert.setMessage(getString(R.string.inbound_select_bizcomp));
            mAlert.show();
            return;
        }

        if(mIDX_VENDOR <= 0)
        {
            mAlert.setMessage(getString(R.string.inbound_select_vendor));
            mAlert.show();
            return;
        }

        if(mIDX_BSCITEM <= 0)
        {
            mAlert.setMessage(getString(R.string.inbound_select_bscitem));
            mAlert.show();
            return;
        }

        if(mIDX_BRANCH <= 0)
        {
            mAlert.setMessage(getString(R.string.inbound_select_branch));
            mAlert.show();
            return;
        }

        TextView v_text = (TextView) findViewById(inbound_barcode);
        mBarCode = v_text.getText().toString();
        if(mBarCode.isEmpty())
        {
            mAlert.setMessage(getString(R.string.inbound_select_barcode));
            mAlert.show();
            return;
        }

        EditText v_EditText = (EditText) findViewById(R.id.editText_memo);
        mMemo = v_EditText.getText().toString();

        AppConf conf = GlobalVal.getInstance().getAppConf();
        JSONTask_Submit jsonTask = new JSONTask_Submit();
        jsonTask.execute(conf.getSvrUrl() + "biztrx_mas/");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                TextView v_text = (TextView) findViewById(inbound_barcode);
                v_text.setText(result.getContents());
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

    // I/F : 품목
    public class JSONTask_GetBasicItem extends AsyncTask<String, Integer, String> {

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
                    String BSCITEM_CD		  = jsonobject.getString("BSCITEM_CD"		);
                    String BSCITEM_NM		  = jsonobject.getString("BSCITEM_NM"		);

                    mBscItemList_Key.add(BSCITEM_CD);
                    mBscItemList_Value.add(BSCITEM_NM);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // I/F : 지점목록
    public class JSONTask_GetBranchList extends AsyncTask<String, Integer, String> {

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
                    String BR_CD		  = jsonobject.getString("BR_CD"		);
                    String BR_NM		  = jsonobject.getString("BR_NM"		);

                    mBranchList_Key.add(BR_CD);
                    mBranchList_Value.add(BR_NM);
                }
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
                jsonObject.accumulate("trx_cd"      , ""				);
                jsonObject.accumulate("inb_biz_cd"  , mBizCompList_Key.get(mIDX_BIZCOMP).toString() );
                jsonObject.accumulate("inb_dt"       , strToday	);
                jsonObject.accumulate("inb_br_cd"    , mBranchList_Key.get(mIDX_BRANCH).toString() );
                jsonObject.accumulate("bscitem_cd"   , mBscItemList_Key.get(mIDX_BSCITEM).toString() );
                jsonObject.accumulate("bar_cd"        , mBarCode	);
                jsonObject.accumulate("trx_st"        , "10" );
                jsonObject.accumulate("out_dt"        , ""	 );
                jsonObject.accumulate("out_biz_cd"   , "" );
                jsonObject.accumulate("usr_cd"        , GlobalVal.getInstance().getUserInfo().mUSR_CD );
                jsonObject.accumulate("use_yn"        , "Y" );
                jsonObject.accumulate("memo"          , mMemo);

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
                    mAlert.setMessage(getString(R.string.inbound_result_success));
                    mAlert.show();
                }
                else
                {
                    // 실패
                    if(strRet.trim().equals("-1001") == true)
                    {
                        mAlert.setMessage(getString(R.string.inbound_result_fail_1001));
                    }
                    else
                    {
                        mAlert.setMessage(getString(R.string.inbound_result_fail) + " [" + strRet + "]");
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
