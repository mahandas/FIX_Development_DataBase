package db_functions;

public class FIXQueriesFactory {
    //    private static final String DB_TYPE = "SQLSERVER"; // TODO hardcoded

    public static FIXQueriesBase getInstance() {
        //        if (DB_TYPE.equalsIgnoreCase("SQLSERVER"))
        return new FIXQueriesSQLServer();
        // Currently using only SQL Server. Provision made for adding other Data Bases
        //        else if (DB_TYPE.equalsIgnoreCase("ORACLE"))
        //            return new FIXQueriesOracle();

    }
}
