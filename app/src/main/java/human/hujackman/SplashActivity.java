package human.hujackman;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 레이아웃을 사용하지 않고 스플래시를 만든다.
        // setContentView(R.layout.activity_splash);

        // 앱초기화
        if( init() == false ) {
            // 로그..
            finish();
        }

        // 로그인 액티비티 생성
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finish();
    }

    // 초기화 작업수행
    // 환경설정및 네트워크등 체크
    protected boolean init()
    {
        // SET 글로벌 환경변수
        AppConf conf = GlobalVal.getInstance().getAppConf();
        conf.setSvrUrl("http://192.168.0.10:3000/");

        return true;
    }
}
