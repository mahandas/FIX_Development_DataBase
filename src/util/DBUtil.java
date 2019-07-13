package util;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static Connection conn = null;
    private static String DB_FINIQ_COMMON, DB_QUICKFIX;


    // TODO hardcoded Values .. remove later
    private static String URL, USERNAME, PWD, AUTHMODE, SERVER_NAME, INSTANCE_NAME;


    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public static synchronized Connection getDBConnection() throws Exception {
        final String url = URL;
        final String username = USERNAME;
        final String pwd = PWD;
        return getDBConnection(url, username, pwd);

    }

    public static Connection getDBConnection(String url, String user, String password) throws ClassNotFoundException,
                                                                                              SQLException,
                                                                                              InstantiationException,
                                                                                              IllegalAccessException {
        Class.forName(DRIVER).newInstance();
        conn =
            AUTHMODE.toUpperCase().contains("WINDOW") ? DriverManager.getConnection(url) :
            DriverManager.getConnection(url, user, password);
        //DriverManager.getConnection(url, user, password);
        return conn;
    }

    public static void setDB_FINIQ_COMMON(String DB_FINIQ_COMMON) {
        DBUtil.DB_FINIQ_COMMON = DB_FINIQ_COMMON;
    }

    public static String getDB_FINIQ_COMMON() {
        return DB_FINIQ_COMMON;
    }

    public static void setURL(String URL) {
        DBUtil.URL = URL;
    }

    public String getURL() {
        return URL;
    }

    public static void setUSERNAME(String USERNAME) {
        DBUtil.USERNAME = USERNAME;
    }

    public static String getUSERNAME() {
        return USERNAME;
    }

    public static void setPWD(String PWD) {
        DBUtil.PWD = PWD;
    }

    public static String getPWD() {
        return PWD;
    }

    public static void setAUTHMODE(String AUTHMODE) {
        DBUtil.AUTHMODE = AUTHMODE;
    }

    public static String getAUTHMODE() {
        return AUTHMODE;
    }

    public static void setDB_QUICKFIX(String DB_QUICKFIX) {
        DBUtil.DB_QUICKFIX = DB_QUICKFIX;
    }

    public static void setSERVER_NAME(String SERVER_NAME) {
        DBUtil.SERVER_NAME = SERVER_NAME;
    }

    public static void setINSTANCE_NAME(String INSTANCE_NAME) {
        DBUtil.INSTANCE_NAME = INSTANCE_NAME;
    }

    public static String getDB_QUICKFIX() {
        return DB_QUICKFIX;
    }

    public static String getSERVER_NAME() {
        return SERVER_NAME;
    }

    public static String getINSTANCE_NAME() {
        return INSTANCE_NAME;
    }

    public static SQLServerDataSource getSQLDataSource() {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(SERVER_NAME);
        ds.setDatabaseName(DB_QUICKFIX);
        try {
            ds.setPortNumber(Integer.parseInt(INSTANCE_NAME));
        } catch (Exception e) {
            ds.setInstanceName(INSTANCE_NAME);
        }
        ds.setIntegratedSecurity(AUTHMODE.toUpperCase().contains("WINDOW") ? true : false);
        return ds;
    }

}
