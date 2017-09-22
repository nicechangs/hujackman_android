package human.hujackman;

/**
 * Created by KIMCG on 2017-09-22.
 * Defined Global Value.
 */
public class GlobalVal
{
    // singleton
    private static GlobalVal m_instance = null;
    public static GlobalVal getInstance()
    {
        if ( m_instance == null)
        {
            m_instance = new GlobalVal();
            m_appConf = new AppConf();
            m_userInf = new UserInf();
        }

        return m_instance;
    }

    // 환경설정
    private static AppConf m_appConf = null;
    public AppConf getAppConf() { return m_appConf; }

    // 사용자 정보.
    private static UserInf m_userInf = null;
    public UserInf getUserInfo() { return m_userInf; }
}

// 환경설정
class AppConf
{
    protected  String m_strSvrUrl;
    public void setSvrUrl(String p_strUrl) { m_strSvrUrl = p_strUrl;}
    public String getSvrUrl() {return m_strSvrUrl;}
}

// 사용자정보
class UserInf {
    protected String m_strUserID;
    protected String m_strCompanyNM;
    protected String m_strBranchNM;
    protected String m_strBranchID;

    // getter
    public String getUserID()       {return m_strUserID;}
    public String getCompanyNM()    {return m_strCompanyNM;}
    public String getBranchNM()     {return m_strBranchNM;}
    public String getBranchID()     {return m_strBranchID;}

    // setter
    public void setUserID( String p_strUserID)          { m_strUserID = p_strUserID; }
    public void setCompanyNM( String p_strCompanyNM)    { m_strCompanyNM = p_strCompanyNM; }
    public void setBranchNM( String p_strBranchNM)      { m_strBranchNM = p_strBranchNM; }
    public void setBranchID( String p_strBranchID)      { m_strBranchID = p_strBranchID; }
}
