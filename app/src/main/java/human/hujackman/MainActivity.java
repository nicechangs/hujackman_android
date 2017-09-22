package human.hujackman;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserInf usr = GlobalVal.getInstance().getUserInfo();
        String LOGIN_USER_ID = usr.getUserID();
        String LOGIN_COMPANY_NM = usr.getCompanyNM();
        String LOGIN_BRANCH_NM = usr.getBranchNM();
        String LOGIN_BRANCH_ID = usr.getBranchID();

    }
}
