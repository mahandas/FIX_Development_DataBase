package client;


import client.FIX_MessageStructure.StructQuoteRequest;
import com.sun.rowset.CachedRowSetImpl;
import db_functions.DB_Functions_FIX;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.CFICode;
import quickfix.field.FutSettDate;
import quickfix.field.FutSettDate2;
import quickfix.field.MaturityDate;
import quickfix.field.NoPartyIDs;
import quickfix.field.NoRelatedSym;
import quickfix.field.OrderQty2;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.QuoteReqID;
import quickfix.field.SecurityID;
import quickfix.field.Symbol;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.QuoteRequest;

import util.DBUtil;
import util.DateUtil;


public class RequestQuote implements Runnable{

	public RequestQuote() {

	}

	private static final Logger LOG = Logger.getLogger(RequestQuote.class.getName());
	private Connection conn = null;
	private static final Properties config = ConfigReader.getConfigFile();



	@Override
	public void run() {
		

		System.out.println(" Thread Started "+ Thread.currentThread().getName());

		int pollInterval = 1000;
		try {
			pollInterval = Integer.parseInt(config.getProperty("Poll_Interval"));
		} catch (Exception e) {
			LOG.info("Loaded default Poll Interval :" + pollInterval + "ms");
		}
		while (true) {
			try {
				if (conn == null || conn.isClosed()) {
					conn = DBUtil.getDBConnection();
					LOG.info("New Connection initalized by " + Thread.currentThread().getName() + " thread.");

				}
				sendRequestForQuotation();

				try {
					Thread.sleep(pollInterval);
					
				} catch (InterruptedException ie) {
					LOG.severe("Error in RequestQuote Thread " + ie.getMessage());
				}
			} catch (Exception e) {
				LOG.severe("Error in RequestQuote thread DB connection " + e.getMessage());
			}
		}
	}
	
	public void sendRequestForQuotation() {

		try {
			CachedRowSet crs = new CachedRowSetImpl();
			try {
				
				crs.populate(DB_Functions_FIX.get_FX_RFQ_Details(FXClient.getEntityID(), FXClient.getPriceProvider_ID(),conn));	//Get the Details from Database
			} catch (Exception e) {
				LOG.severe("Error in get_FX_Details : " + e.getMessage());
				crs.close();
				return;
			}
			StructQuoteRequest objQuoteRequest = new StructQuoteRequest();
			while (crs != null && crs.next()) {
				try {            

					objQuoteRequest.setQuoteReqID(crs.getString("Request_ID")) ;

					objQuoteRequest.setNoRelatedSym(1);	 

					objQuoteRequest.setSymbol(crs.getString("Deal_Pair")) ;  //should be CCY1/CCY2 

					objQuoteRequest.setSecurityID(objQuoteRequest.getSymbol());
					objQuoteRequest.setCurrency(crs.getString("Quoted_Ccy"));

					if(crs.getString("FR_Deal_Type").compareToIgnoreCase("SPOT")==0)
						objQuoteRequest.setCFICode("SPOT") ;
					else if(crs.getString("FR_Deal_Type").compareToIgnoreCase("OUTRIGHT")==0)
					{	
						objQuoteRequest.setCFICode("OUTRIGHT");
						
					}
					
					objQuoteRequest.setOrderQty(crs.getDouble("Quoted_Amt"));
					objQuoteRequest.setExpireTime(DateUtil.getCurrentDateTime_Str());

					try {
						buildQuoteRequestAndSend(objQuoteRequest);
					} catch (Exception e) {
						e.printStackTrace();
						LOG.severe("Error in buildQuoteRequestAndSend for ID : " + objQuoteRequest.getQuoteReqID() +
								" : " + e.getMessage());
					}
				} catch (Exception e) {
					LOG.severe("Error in sendRequestForQuotation for ID : " + objQuoteRequest.getQuoteReqID() +
							" : " + e.getMessage());

				} finally {
					// Try to send a Quote only once. Don't resend even if send successfully or not.
					DB_Functions_FIX.update_Quote_Request_YN(objQuoteRequest.getQuoteReqID(), "N", conn);

				}
			}
			crs.close();
		} catch (Exception e) {
			LOG.severe("Error in sendRequestForQuotation : " + e.getMessage());
		}  


	}

	private void buildQuoteRequestAndSend(StructQuoteRequest objQuoteRequest) throws Exception {
		// TODO Auto-generated method stub
		if (!ApplicationImpl.isLoggedOn()) {
			throw new Exception("Unable get logon response from FIX server..");
		}

		QuoteRequest quoteRequest = new QuoteRequest();
		quoteRequest.set(new QuoteReqID(objQuoteRequest.getQuoteReqID()));		
	
		NewOrderSingle.NoPartyIDs grpPartyID = new NewOrderSingle.NoPartyIDs();
		grpPartyID.setField(new PartyID(config.getProperty("PartyID")));		
		grpPartyID.setField(new PartyIDSource('D'));		
		grpPartyID.setField(new PartyRole(13));				
		quoteRequest.addGroup(grpPartyID);
		
		quoteRequest.set(new NoRelatedSym(1));
		QuoteRequest.NoRelatedSym noRelatedSym = new QuoteRequest.NoRelatedSym();
		noRelatedSym.set(new Symbol(objQuoteRequest.getSymbol())); 
		noRelatedSym.set(new CFICode(objQuoteRequest.getCFICode()));


		quoteRequest.setDouble(38,objQuoteRequest.getOrderQty());
		if(objQuoteRequest.getCFICode().equalsIgnoreCase("FFCPNO")||objQuoteRequest.getCFICode().equalsIgnoreCase("FFCNNO"))
			quoteRequest.setString(64,objQuoteRequest.getFutSettDate());
		else if(objQuoteRequest.getCFICode().equalsIgnoreCase("FFCPNW")||objQuoteRequest.getCFICode().equalsIgnoreCase("FFCNNW"))
		{
			quoteRequest.setField(new FutSettDate(objQuoteRequest.getFutSettDate()));
			quoteRequest.setField(new FutSettDate2(objQuoteRequest.getFutSettDate2()));
			quoteRequest.setField(new OrderQty2(objQuoteRequest.getOrderQty2()));
		}
		quoteRequest.setString(15,objQuoteRequest.getCurrency());

		
		SessionID sessionID = UBSFXClient.getSessionID();
		if (sessionID != null && Session.doesSessionExist(sessionID)) {
			if (Session.sendToTarget(quoteRequest, sessionID)) { 
				LOG.info( "Quote request sent successfully for ID :" + objQuoteRequest.getQuoteReqID() );
			} else {
				throw new Exception("Error while sending quote.");
			}
		} else {
			throw new Exception("Session does not exist ... sessionId : "+sessionID);
		}
	}
}
