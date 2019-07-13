package db_functions;


import client.ConfigReader;
import client.FIX_MessageStructure.StructExecutionReport;
import client.FIX_MessageStructure.StructMassQuoteAck;
import client.FIX_MessageStructure.StructQuote;
import client.FIX_MessageStructure.StructQuoteRequestReject;
import db_functions.dto.TradeDetailsXML.ExecReportXML;
import db_functions.dto.TradeDetailsXML.QuoteXML;
import util.CommonUtil;
import com.sun.rowset.CachedRowSetImpl;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.rowset.CachedRowSet;


public class DB_Functions_FIX {

	private static final FIXQueriesBase QUERY = FIXQueriesFactory.getInstance();

	public static boolean updateWithDeadlockRetry(CallableStatement stmt) throws SQLException {
		boolean isUpdated = false;
		int retryCount = 1;
		try {
			do {
				try {
					isUpdated = stmt.execute();
					if (retryCount > 1)
						System.out.println("INFO : Execute successfull in " + retryCount + " re-attempt .");
					return isUpdated;
				} catch (SQLException sqle) {
					if (sqle.getErrorCode() == 1205 || sqle.getErrorCode() == 1204) {
						System.out.println("SEVERE : DEADLOCK at execute attempt : " + (retryCount++) +
								" : Error Code : " + sqle.getErrorCode());

					} else
						throw sqle;
				}
			} while (!isUpdated &&
					retryCount <= Integer.parseInt(ConfigReader.getConfigFile().getProperty("Deadlock_Retry", "3")));
			if (!isUpdated)
				throw new SQLException("Update Failed due to deadlock");
		} finally {
			if (stmt != null)
				stmt.close();
		}
		return false;
	}
	public static void update_Expired_FX_Quote_Request_N(String entityId, String ppID,
			Connection conn) throws SQLException {

		CallableStatement stmt = conn.prepareCall(QUERY.get_Update_Expired_FX_RFQ_Quote_Request_N());
		stmt.setString(1, entityId);
		stmt.setString(2, ppID);
		updateWithDeadlockRetry(stmt);
	}
	public static CachedRowSet get_FX_RFQ_Details(String entityID,String ppID,
			Connection conn) throws Exception {
		CallableStatement stmt=conn.prepareCall(QUERY.get_FX_RFQ_Details(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	
		stmt.setString(1, entityID);
		stmt.setString(2, ppID);
		CachedRowSet crs = new CachedRowSetImpl();
		crs.populate(stmt.executeQuery());
		if (stmt != null)
			stmt.close();
		return crs;

	}
	public static void update_Quote_Request_YN(String strRFQ_Request_Id, String strQuoteRequest_YN,
			Connection conn) throws Exception {

		CallableStatement stmt = conn.prepareCall(QUERY.update_Quote_Request_YN());
		stmt.setString(1, strRFQ_Request_Id);
		stmt.setString(2, strQuoteRequest_YN);
		updateWithDeadlockRetry(stmt);
	}
	public static String get_Price_Provider_ID(String ppcode, Connection conn) throws SQLException {
		PreparedStatement stmt = conn.prepareCall(QUERY.get_Price_Provider_ID());
		stmt.setString(1, ppcode);
		CachedRowSet crs = new CachedRowSetImpl();
		crs.populate(stmt.executeQuery());
		String ppid = "";
		while (crs != null && crs.next()) {
			ppid = crs.getString("");
		}
		crs.close();
		return ppid;
	}
	public static CachedRowSet get_NewOrderSingle_Details_FX(String entityID, String ppID,
			Connection conn) throws Exception {
		PreparedStatement stmt =
				conn.prepareStatement(QUERY.get_NewOrderSingle_Details_FX(), ResultSet.TYPE_SCROLL_INSENSITIVE);
		stmt.setString(1, entityID);
		stmt.setString(2, ppID);
		CachedRowSet crs = new CachedRowSetImpl();
		crs.populate(stmt.executeQuery());
		if (stmt != null)
			stmt.close();
		return crs;
	}
	public static void update_Order_Requested_YN(String strRFQ_Request_Id, String strOrderRequested_YN,
			Connection conn) throws Exception {

		CallableStatement stmt = conn.prepareCall(QUERY.update_Order_Requested_YN());
		stmt.setString(1, strRFQ_Request_Id);
		stmt.setString(2, strOrderRequested_YN);
		//            stmt.execute();
		updateWithDeadlockRetry(stmt);
	}
}
