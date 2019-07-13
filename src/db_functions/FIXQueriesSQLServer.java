package db_functions;

import util.DBUtil;

public class FIXQueriesSQLServer extends FIXQueriesBase {

    protected final String DBNAME = DBUtil.DBNAME() + "..";

    @Override
    public String get_FX_RFQ_Details() {
        return "exec " + FINIQ_COMMON + "usp ?,?";
    }

    @Override
    public String get_NewOrderSingle_Details_FX() {
        return "exec " + FINIQ_COMMON + "usp123 ?,?";
    }

}
