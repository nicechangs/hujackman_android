package human.hujackman;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button ib_btn = (Button) findViewById(R.id.btn_input_item);
        ib_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInboundAcitvity();
            }
        });

        Button ob_btn = (Button) findViewById(R.id.btn_output_item);
        ob_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOutboundAcitvity();
            }
        });

        Button hist_btn = (Button) findViewById(R.id.btn_hist_item);
        hist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openIOHistoryActivity();
            }
        });

        TextView v_txtUserInfo = (TextView) findViewById(R.id.userInfo);
        v_txtUserInfo.setText(GlobalVal.getInstance().getUserInfo().mBR_NM + " / " + GlobalVal.getInstance().getUserInfo().mUSR_NM);

    }

    // 입고관리 액티비티 생성
    private void openInboundAcitvity()
    {
        Intent intent = new Intent(this, InboundActivity.class);
        startActivity(intent);
    }

    // 출고관리 액티비티 생성
    private void openOutboundAcitvity()
    {
        Intent intent = new Intent(this,  OutboundActivity.class);
        startActivity(intent);
    }

    // 입출고이력
    private void openIOHistoryActivity()
    {
        Intent intent = new Intent(this,  IOHistory.class);
        startActivity(intent);
    }


}
