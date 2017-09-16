package human.hujackman;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그인 액티비티
        Intent intent = getIntent();
        String LOGIN_USER_ID = intent.getStringExtra("LOGIN_USER_ID");
        String LOGIN_COMPANY_NM = intent.getStringExtra("LOGIN_COMPANY_NM");
        String LOGIN_BRANCH_NM = intent.getStringExtra("LOGIN_BRANCH_NM");
        String LOGIN_BRANCH_ID = intent.getStringExtra("LOGIN_BRANCH_ID");

    }
}
