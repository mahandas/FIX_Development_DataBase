package client;


import db_functions.DB_Functions_FIX;
import finiq.FinIQApp;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.JdbcLogFactory;
import quickfix.JdbcStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.fix44.MessageFactory;
import util.DBUtil;


public class FXClient {

	public FXClient() {
	}

	private static final Logger LOG = Logger.getLogger(FXClient.class.getName());
	private static String entityID = "";
	private static String ppID = "";
	private static SessionID sessionID;
	static Initiator initiator;

	private static final ApplicationImpl application = new ApplicationImpl();

	/**
	 * @param args
	 *
	 * Command line args : 0. Properties File path
	 * eg : "..\\Dat\\UBS FX\\UBSFX_Config.properties"
	 */
	public static void main(String[] args) {
		FXClient client = new FXClient();
		client.initializeParams(args);
		client.startClient();
	}

	/**
	 *  Establishes login to the FX server and starts RequestQuote and OrderProcessing thread.
	 */
	private void startClient() {
		//--------------------------------------------------------------------
		// Create a JVMShutdownHook to trap jvm exit event
		final JVMShutdownHook jvmShutdownHook = new JVMShutdownHook();
		Runtime.getRuntime().addShutdownHook(jvmShutdownHook);
		//--------------------------------------------------------------------
		if (login()) {
			try {
				if (ConfigReader.getConfigFile().getProperty("Quote_Thread", "Y").toUpperCase().contains("Y")) {
					Thread rq = new Thread(new RequestQuote(), "RequestQuote");
					rq.start();

				}
				if (ConfigReader.getConfigFile().getProperty("Order_Thread", "Y").toUpperCase().contains("Y")) {
					Thread op = new Thread(new OrderProcessing(), "OrderProcessing");
					op.start();

				}
				//          
				//                }
			} catch (Exception e) {
				LOG.severe("Error in startClient : Error while staring threads :: " + e.getMessage());
			}
		} else {
			LOG.info("Unable to login to FX FIX Server");
			return;
		}
	}

	/**
	 * Starts the intiator and establishes connection with the FIX server.
	 *
	 * @return true if login successful else false
	 */

	public static boolean login() {
		try {
			LOG.info("Attempting login ...");
			final SessionSettings settings = new SessionSettings(ConfigReader.getConfigFile().getProperty("Initiator"));
			settings.setString("SocketKeyStorePassword", "Password!23");
			final String beginString = settings.get().getString("BeginString");
			final String senderCompID = settings.get().getString("SenderCompID");
			final String targetCompID = settings.get().getString("TargetCompID");
			sessionID = new SessionID(beginString, senderCompID, targetCompID);

			// Changed By Sarun for JDBC Message Store 23/6/2015
			MessageStoreFactory storeFactory;
			LogFactory logFactory;
			if (ConfigReader.getConfigFile().getProperty("Message_Store", "FILE").trim().equalsIgnoreCase("FILE")) {
				storeFactory = new FileStoreFactory(settings);
				logFactory = new FileLogFactory(settings);
			} else {
				//                storeFactory = new JdbcStoreFactory(settings);
				//                logFactory = new JdbcLogFactory(settings);
				JdbcStoreFactory objJSF = new JdbcStoreFactory(settings);
				JdbcLogFactory objJLF = new JdbcLogFactory(settings);
				DataSource ds = DBUtil.getSQLDataSource();
				objJSF.setDataSource(ds);
				objJLF.setDataSource(ds);
				storeFactory = objJSF;
				logFactory = objJLF;
			}
			
			MessageFactory messageFactory = new MessageFactory();
			initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);

			LOG.info("Initiate login request ...");
			int waitForLogon = 1000;
			try {
				waitForLogon = Integer.parseInt(ConfigReader.getConfigFile().getProperty("Wait_For_Logon"));
			} catch (Exception e) {
				LOG.info("Loaded default wait for logon :" + waitForLogon + "ms");
			}
			int loginAttempts;
			try {
				loginAttempts = Integer.parseInt(ConfigReader.getConfigFile().getProperty("Login_Attempts"));
			} catch (NumberFormatException nfe) {
				loginAttempts = -1;
			}
			initiator.start();
			Thread.sleep(waitForLogon);
			int attemptCount = 1;
			do {
				Thread.sleep(waitForLogon);
				if (application.isLoggedOn()) {
					LOG.info("Logged in to FX FIX Server");
					return true;
				}
				LOG.info("Login attempt " + attemptCount++ + " failed. Retrying...");
			} while (loginAttempts == -1 || loginAttempts >= attemptCount);
		} catch (Exception e) {
			LOG.severe("Error in loginQuote : " + e.getMessage());
		}
		return false;
	}


	private void initializeParams(String[] args) {
		try {
			ConfigReader.readConfigs(args[FXConstants.Params_FI.PROP_FILE_PATH]);
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			try {
				// Display error message and wait for 15 seconds and exit
				Thread.sleep(15000);
			} catch (InterruptedException f) {
				f.getMessage();
			}
			System.exit(1); // Exit program if failed to load properties file
		}
		DBUtil.setSERVER_NAME(ConfigReader.getConfigFile().getProperty("DB_HOST"));
		DBUtil.setINSTANCE_NAME(ConfigReader.getConfigFile().getProperty("DB_INSTANCE", "1433"));
		DBUtil.setDB_QUICKFIX(ConfigReader.getConfigFile().getProperty("QUICKFIX_DB", "QuickFIX"));
		DBUtil.setDB_COMMON(ConfigReader.getConfigFile().getProperty("COMMON_DB"));
		String authenticationMode = ConfigReader.getConfigFile().getProperty("Authentication_Mode", "SERVER");
	
		DBUtil.setAUTHMODE(authenticationMode);
		if (authenticationMode.toUpperCase().contains("WINDOW")) {
			// DBUtil.setURL("jdbc:sqlserver://" + DBUtil.getSERVER_NAME() + ";databaseName=" + DBUtil.getDB_COMMON() + ";integratedSecurity=true");
			try {
				Integer.parseInt(DBUtil.getINSTANCE_NAME());
				DBUtil.setURL("jdbc:sqlserver://" + DBUtil.getSERVER_NAME() + ":" + DBUtil.getINSTANCE_NAME() +
						";databaseName=" + DBUtil.getDB_COMMON() + ";integratedSecurity=true");
			} catch (NumberFormatException efe) {
				DBUtil.setURL("jdbc:sqlserver://" + DBUtil.getSERVER_NAME() + ";InstanceName=" +
						DBUtil.getINSTANCE_NAME() + ";databaseName=" + DBUtil.getDB_COMMON() +
						";integratedSecurity=true");
			}
		} else {
			try {
				Integer.parseInt(DBUtil.getINSTANCE_NAME());
				DBUtil.setURL("jdbc:sqlserver://" + DBUtil.getSERVER_NAME() + ":" + DBUtil.getINSTANCE_NAME() +
						";databaseName=" + DBUtil.getDB_COMMON());
			} catch (NumberFormatException efe) {
				DBUtil.setURL("jdbc:sqlserver://" + DBUtil.getSERVER_NAME() + ";InstanceName=" +
						DBUtil.getINSTANCE_NAME() + ";databaseName=" + DBUtil.getDB_COMMON());
			}
			try {
				if (ConfigReader.getConfigFile().getProperty("DB_Password", "").isEmpty())
				{	
				}
				else
					DBUtil.setPWD(ConfigReader.getConfigFile().getProperty("DB_Password"));
			} catch (Exception e) {
				LOG.severe("Unable to get Password :- " + e.getMessage());
			}
			DBUtil.setUSERNAME(ConfigReader.getConfigFile().getProperty("DB_USER"));
		}
		entityID = ConfigReader.getConfigFile().getProperty("ENt");
		ppID = ConfigReader.getConfigFile().getProperty("ID");
		Connection conn = null;
		try {
			conn = DBUtil.getDBConnection();
			ppID = DB_Functions_FIX.get_Price_Provider_ID(ConfigReader.getConfigFile().getProperty("ID"), conn);
			if (ppID.trim().isEmpty()) {
				LOG.severe("Error while fetching ID");
				System.exit(0);
			}
		} catch (Exception e) {
			LOG.severe("Error while fetching ID : " + e.getMessage());
			System.exit(0);
		} finally {
			try {
				conn.close();
			} catch (SQLException sqle) {
				LOG.severe("Error while closing connection to fetch ID : " + sqle.getMessage());
				System.exit(0);
			}
		}

	}

	public static SessionID getSessionID() {
		return sessionID;
	}

	public static String getEntityID() {
		return entityID;
	}

	public static String getPriceProvider_ID() {
		return ppID;
	}

	/**
	 * Traps a JVM shutdown event to send logout message
	 */
	class JVMShutdownHook extends Thread {
		public void run() {
			System.out.println("Exit process ... ");
			initiator.stop();
		}
	}

}
