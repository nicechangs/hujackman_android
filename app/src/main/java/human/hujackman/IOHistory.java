package human.hujackman;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IOHistory extends AppCompatActivity {

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iohistory);

        mListView = (ListView)findViewById(R.id.listView);

        dataSetting();
    }

    private void dataSetting()
    {
        // SET URL
        // http://172.16.1.254:3000/biztrx_his?comp_cd=CO17-0000001&start_date=20171111&end_date=20171111
        String strUrl = String.format("%1$sbiztrx_his?comp_cd=%2$s&start_date=2017110&end_date=20171130"
                , GlobalVal.getInstance().getAppConf().getSvrUrl()
                , GlobalVal.getInstance().getUserInfo().mCOMP_CD
        );

        JSONTask_GetIOHistory jsonTask = new JSONTask_GetIOHistory();
        jsonTask.execute(strUrl);
    }

    // I/F : 입/출고내역조회
    public class JSONTask_GetIOHistory extends AsyncTask<String, Integer, String> {

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

                MyAdapter mMyAdapter = new MyAdapter();
                for(int ix = 0; ix < jarrary.length(); ix++)
                {
                    JSONObject jsonobject = jarrary.getJSONObject(ix);

                    String TRX_CD		  = jsonobject.getString("TRX_CD"		);
                    String TRX_ST		  = jsonobject.getString("TRX_ST"		);
                    if(TRX_ST.equals("10"))
                    {
                        TRX_ST = getString(R.string.IOHistory_info_inbound);
                    }
                    else if(TRX_ST.equals("20"))
                    {
                        TRX_ST = getString(R.string.IOHistory_info_outbound);
                    }





                    String INB_BIZ_NM		  = jsonobject.getString("INB_BIZ_NM"		);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = sdf.parse(jsonobject.getString("REG_DT"));
                    String REG_DT = output.format(d);
                    String BSCITEM_CD	= jsonobject.getString("BSCITEM_CD"	);
                    String BAR_CD		= jsonobject.getString("BAR_CD"		);
                    String OUT_BIZ_NM	= jsonobject.getString("OUT_BIZ_NM"	);
                    String USR_NM		= jsonobject.getString("USR_NM"		);

                    String strTitle =  getString(R.string.IOHistory_info_trx_cd) + " [" +TRX_CD +"]";
                    strTitle += "\t" + getString(R.string.IOHistory_info_reg_dt)  + " / [" +REG_DT +"]";
                    strTitle += "\t" + getString(R.string.IOHistory_info_trx_st)  + " / [" +TRX_ST +"]";

                    String strContents = getString(R.string.IOHistory_info_inb_biz_nm)  + ":" + INB_BIZ_NM + " / ";
                    strContents += getString(R.string.IOHistory_info_bscitem_cd)        + ":" + BSCITEM_CD + " / ";
                    strContents += getString(R.string.IOHistory_info_bar_cd)             + ":" + BAR_CD + " / ";
                    strContents += getString(R.string.IOHistory_info_out_biz_nm)        + ":" + OUT_BIZ_NM + " / ";
                    strContents += getString(R.string.IOHistory_info_usr_nm)             + ":" + USR_NM + " / ";
                    mMyAdapter.addItem(strTitle, strContents);
                }

                mListView.setAdapter(mMyAdapter);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
