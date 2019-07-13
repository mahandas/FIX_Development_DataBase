package client;


import client.FIX_MessageStructure.StructDontKnowTrade;
import client.FIX_MessageStructure.StructExecutionReport;
import client.FIX_MessageStructure.StructQuote;
import client.FIX_MessageStructure.StructQuoteRequestReject;
import client.FIX_MessageStructure.StructMassQuoteAck;
import db_functions.DB_Functions_FIX;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import com.sun.rowset.CachedRowSetImpl;
import quickfix.Application;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.AvgPx;
import quickfix.field.BidForwardPoints;
import quickfix.field.BidForwardPoints2;
import quickfix.field.BidPx;
import quickfix.field.BidSize;
import quickfix.field.BidSpotRate;
import quickfix.field.BidSwapPoints;
import quickfix.field.CFICode;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.Currency;
import quickfix.field.DKReason;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.FutSettDate;
import quickfix.field.LastForwardPoints;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastSpotRate;
import quickfix.field.MaturityDate;
import quickfix.field.MsgType;
import quickfix.field.OfferForwardPoints;
import quickfix.field.OfferForwardPoints2;
import quickfix.field.OfferPx;
import quickfix.field.OfferSize;
import quickfix.field.OfferSpotRate;
import quickfix.field.OfferSwapPoints;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Password;
import quickfix.field.Price;
import quickfix.field.QuoteID;
import quickfix.field.QuoteReqID;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.Username;
import quickfix.field.ValidUntilTime;
import util.DBUtil;
import util.DateUtil;


public class ApplicationImpl extends MessageCracker implements Application {

	public ApplicationImpl() {

	}

	private static final Logger LOG = Logger.getLogger(ApplicationImpl.class.getName());
	private static volatile boolean loggedOn;
	private Connection conn = null;
	private static final Properties config = ConfigReader.getConfigFile();
	private static int LoginFailures = 0;

	@Override
	public void fromAdmin(quickfix.Message message, SessionID sessionID) {
		if (isMessageOfType(message, MsgType.LOGOUT)) {
			
			loggedOn = false;
			setLinkStatus("DOWN", false, config.getProperty("LogOut_Email", "N").equalsIgnoreCase("Y") ? true : false);
		}
	}


	@Override
	public void onCreate(SessionID arg0) {

	}

	@Override
	public void onLogon(SessionID arg0) {
		LOG.info("Logged in");
			setLinkStatus("UP", false, config.getProperty("Login_Email", "N").equalsIgnoreCase("Y") ? true : false);
		LoginFailures = 0;
		loggedOn = true;
	}

	@Override
	public void onLogout(SessionID arg0) {
		LOG.info("Logged out");
		if (loggedOn) {
			setLinkStatus("DOWN", false, config.getProperty("LogOut_Email", "N").equalsIgnoreCase("Y") ? true : false);
		}
		if (LoginFailures == Integer.parseInt(config.getProperty("LoginFailures", "999999999")))
			setLinkStatus("DOWN", true, config.getProperty("LogOut_Email", "N").equalsIgnoreCase("Y") ? true : false);
		loggedOn = false;
	}

	@Override
	public void toAdmin(Message message, SessionID arg1) {
		// TODO Auto-generated method stub
		if(isMessageOfType(message,MsgType.LOGON)) {
			LoginFailures++;

            try {
                if (conn == null || !conn.isValid(0)) {
                    conn = DBUtil.getDBConnection();
                    LOG.info("New Connection initalized by " + Thread.currentThread().getName());
                }
                try {
                    String password = "";
                    if (!config.getProperty("Password", "")
                               .trim()
                               .isEmpty()) {
                        password = config.getProperty("Password").trim();
                        
                    } else {
                    	
                    }
                    if (!password.isEmpty()) {
                        message.setField(new Password(password)); //Required
                    } else {
                        throw new Exception(" Password is missing ");
                    }
                } catch (Exception e) {
                    LOG.severe("Error in get_Price_Provider_Login_Credentials : " + e.getMessage());
                }
                try {
                    String userName = "";
                    if (!config.getProperty("Username", "")
                               .trim()
                               .isEmpty()) {
                        userName = config.getProperty("Username").trim();
                    } else {

                    }
                    if (!userName.isEmpty()) {
                        message.setField(new Username(userName)); //Required
                    } else {
                        throw new Exception(" Username is missing ");
                    }
                } catch (Exception e) {
                    LOG.severe("Error in get_Price_Provider_Login_Credentials : " + e.getMessage());
                }
            } catch (Exception e) {
                LOG.severe("Error in " + Thread.currentThread().getName() + " thread DB connection " + e.getMessage());
            }
		}
	}

	@Override
	public void toApp(quickfix.Message message, SessionID sessionID) {

	}
	public static boolean isLoggedOn() {
		return loggedOn;
	}

	@Override
	protected void onMessage(Message message, SessionID sessionID)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		super.onMessage(message, sessionID);
	}
	//to parse different types of messages
	@Override
	public void fromApp(quickfix.Message message, SessionID sessionID) throws UnsupportedMessageType, FieldNotFound,
	IncorrectTagValue {
		try {
			if (conn == null || conn.isClosed()) {
				conn = DBUtil.getDBConnection();
				LOG.info("New Connection initalized by " + Thread.currentThread().getName());
			}
			
			if (isMessageOfType(message, MsgType.QUOTE))
				parseQuote(message);
			else if (isMessageOfType(message, MsgType.QUOTE_CANCEL)) 
				parseQuoteCancel(message);
			else if (isMessageOfType(message, MsgType.EXECUTION_REPORT)) 
				parseExecReport(message);
			else if (isMessageOfType(message, MsgType.DONT_KNOW_TRADE)) 
				parseDntKnwTradeMsg(message);
			else if (isMessageOfType(message, MsgType.MASS_QUOTE_ACKNOWLEDGEMENT)) 
				parseQuoteAck(message);
			else if (isMessageOfType(message, MsgType.QUOTE_REQUEST_REJECT)) 
				parseQuoteRequestReject(message);
			
		} catch (Exception e) {
			LOG.severe("Error in " + Thread.currentThread().getName() + " thread DB connection " + e.getMessage());
		}
	}
	private boolean isMessageOfType(quickfix.Message message, String type) {
		try {
			return type.equals(message.getHeader().getField(new MsgType()).getValue());
		} catch (FieldNotFound e) {
			LOG.severe(e.getMessage());
			return false;
		}
	}

	private void parseQuote(Message message) {

		StructQuote quote = new StructQuote();
		try {
			quote.setQuoteReqID(message.getField(new QuoteReqID()).getValue());	//Set Quote Request ID

			quote.setQuoteID(message.getField(new QuoteID()).getValue());		//Set the Quote ID
			quote.setSymbol(message.getField(new Symbol()).getValue());
			quote.setCFICode(message.getField(new CFICode()).getValue());		//Set the CFICode,FFCPNO = Forward Outright ,RCSXXX = Spot 
			
			try {
				quote.setSecurityID(message.getField(new SecurityID()).getValue());
			}catch(FieldNotFound fnf)
			{
				quote.setSecurityID("");
			}
			try {
				quote.setSecurityIDSource(message.getField(new SecurityIDSource()).getValue());
			}catch(FieldNotFound fnf)
			{
				quote.setSecurityIDSource("");
			}
			try {
				quote.setBaseCcyDeliveryLocationType(message.getString(9102));
			}catch(FieldNotFound fnf)
			{
				quote.setBaseCcyDeliveryLocationType("");
			}
			try {
				quote.setCounterCcyDeliveryLocationType(message.getString(9103));
			}catch(FieldNotFound fnf)
			{
				quote.setCounterCcyDeliveryLocationType("");
			}
			
			try {
				quote.setMaturityDate(DateUtil.convertDateFormat(message.getField(new MaturityDate()).getValue(),
						DateUtil.DB_DATE_FORMAT));
			}catch(FieldNotFound fnf)
			{
				quote.setMaturityDate("");
			}
			try {
				quote.setFixingReference(message.getString(7075));
			}catch(FieldNotFound fnf)
			{
				quote.setFixingReference("");
			}
			
			//Set the Valid Until Time in proper format
			quote.setValidUntilTime(DateUtil.convertDateFormat(message.getField(new ValidUntilTime()).getValue().toString(),
					"EEE MMM dd HH:mm:ss z yyyy",
					DateUtil.DB_DATE_TIME_FORMAT));
			
			try {
				quote.setBidPx(message.getField(new BidPx()).getValue());		//Tag 132
			} catch (FieldNotFound fnf) {
				quote.setBidPx(0);
			}
			try {
				quote.setOfferPx(message.getField(new OfferPx()).getValue());	//Tag 133
			} catch (FieldNotFound fnf) {
				quote.setOfferPx(0);
			}
			quote.setBidSize(message.getField(new BidSize()).getValue());		//Tag 134
			quote.setOfferSize(message.getField(new OfferSize()).getValue());	//Tag 135

			try {
				quote.setBidSpotRate(message.getField(new BidSpotRate()).getValue());	//tag 188
			} catch (FieldNotFound fnf) {
				quote.setBidSpotRate(0);
			}
			try {
				quote.setOfferSpotRate(message.getField(new OfferSpotRate()).getValue());	//tag 190
			} catch (FieldNotFound fnf) {

				quote.setOfferSpotRate(0);
			}
			try {
				quote.setBidForwardPoints(message.getField(new BidForwardPoints()).getValue());	//tag 189
			}catch (FieldNotFound fnf) {
				quote.setBidForwardPoints(0);

			}
			try {
				quote.setBidForwardPoints2(message.getField(new BidForwardPoints2()).getValue());	//Used in Swap Far leg
			}catch (FieldNotFound fnf) {
				quote.setBidForwardPoints2(0);

			}
			try {
				quote.setOfferForwardPoints(message.getField(new OfferForwardPoints()).getValue());	//tag 191
			}catch (FieldNotFound fnf) {
				quote.setOfferForwardPoints(0);

			}
			try {
				quote.setOfferForwardPoints2(message.getField(new OfferForwardPoints2()).getValue()); //Used in Swap Far leg
			}catch (FieldNotFound fnf) {
				quote.setOfferForwardPoints2(0);

			}
			try {
				quote.setFutSettDate(DateUtil.convertDateFormat(message.getField(new FutSettDate()).getValue(),	
						DateUtil.DB_DATE_FORMAT));			//Tag 64,Value Date of a Forward
			}catch(FieldNotFound fnf)
			{
				quote.setFutSettDate("");
			}
			quote.setCurrency(message.getField(new Currency()).getValue());
			try {
				quote.setExpireTime(DateUtil.convertDateFormat(message.getField(new ValidUntilTime()).getValue().toString(),
						"EEE MMM dd HH:mm:ss z yyyy",
						DateUtil.DB_DATE_TIME_FORMAT));
			}catch(FieldNotFound fnf)
			{
				quote.setExpireTime("");
			}
			try {
				quote.setBidSwapPoints(message.getField(new BidSwapPoints()).getValue());
			}catch(FieldNotFound fnf)
			{
				quote.setBidSwapPoints(0);
			}
			try{
				quote.setOfferSwapPoints(message.getField(new OfferSwapPoints()).getValue());
			}catch(FieldNotFound fnf)
			{
				quote.setOfferSwapPoints(0);
			}
			LOG.info("Quote Received for ID:"+quote.getQuoteReqID());
			try {
				DB_Functions_FIX.update_FX_RFQ_Details(quote, conn);		
				LOG.info("Quote Updated Successfully");
			} catch (Exception e) {
				LOG.severe("Unable To Save Quote for ID : " + quote.getQuoteReqID() + " : " + e.getMessage());
			}
		}catch(Exception e) {

			LOG.severe("Error in parseQuote for ID : " + quote.getQuoteReqID() + " : " + e.getMessage());
		}

	}
	private void parseQuoteRequestReject(Message message) {
		// TODO Auto-generated method stub
		StructQuoteRequestReject quoteRequestRej=new StructQuoteRequestReject();
		
		try {
			quoteRequestRej.setQuoteReqID(message.getString(131));
			try {
				quoteRequestRej.setQuoteRequestRejectReason(message.getString(658));
			}catch(FieldNotFound fnf)
			{
				quoteRequestRej.setQuoteRequestRejectReason("");
			}
			
			quoteRequestRej.setNoRelatedSym(message.getInt(146));

			try {
			quoteRequestRej.setSymbol(message.getField(new Symbol()).getValue());				
			}catch(FieldNotFound fnf)
			{
				quoteRequestRej.setSymbol("");
			}
		

			try {
				quoteRequestRej.setText(message.getField(new Text()).getValue());
			}catch(FieldNotFound fnf)
			{
				quoteRequestRej.setText(message.getField(new Text()).getValue());
			}
			LOG.info("QuoteRequest Rejected received for ID :"+quoteRequestRej.getQuoteReqID());
			LOG.info("Reason:"+quoteRequestRej.getText());
			try {
				DB_Functions_FIX.update_FX_RFQ_Details(quoteRequestRej, conn);
			} catch (Exception e) {
				LOG.severe("Unable To Save parseQuoteRequestReject  for ID : " + quoteRequestRej.getQuoteReqID() );
			}
		}catch(Exception e)
		{
			LOG.severe("Error in parseQuoteRequestReject  for ID : " + quoteRequestRej.getQuoteReqID() );

		}

	}

		private void parseQuoteAck(quickfix.Message ackMsg) {
	        StructMassQuoteAck quoteAck = new StructMassQuoteAck();
	        try {
	            try {
	                quoteAck.setText(ackMsg.getField(new Text()).getValue());
	            } catch (FieldNotFound fnf) {
	                quoteAck.setText("");
	            }
	            try {
	                quoteAck.setQuoteAckStatus(ackMsg.getString(297));
	            } catch (FieldNotFound fnf) {
	                quoteAck.setQuoteAckStatus("");
	            }
	            try {
	                quoteAck.setTradingSessionId(ackMsg.getString(336));
	            } catch (FieldNotFound fnf) {
	                quoteAck.setTradingSessionId("");
	            }
	            try {
	                quoteAck.setQuoteRejectReason(ackMsg.getString(300));
	            } catch (FieldNotFound fnf) {
	                quoteAck.setQuoteRejectReason("");
	            }
	            quoteAck.setQuoteReqID(ackMsg.getString(131));
	
	            try {
	                DB_Functions_FIX.update_FX_RFQ_Details(quoteAck, conn);
	            } catch (Exception e) {
	                LOG.severe("Unable To Save QuoteAck  for RFQ ID : " + quoteAck.getQuoteReqID() + " : " +
	                           e.getMessage());
	            }
	        } catch (Exception e) {
	            LOG.severe("Error in parseQuoteAck  for RFQ ID : " + quoteAck.getQuoteReqID() + " : " + e.getMessage());
	        }
	    }


	private void parseDntKnwTradeMsg(Message message) {
		// TODO Auto-generated method stub
		StructDontKnowTrade dntKnowTrade=new StructDontKnowTrade();
		try {
			dntKnowTrade.setOrderID(message.getField(new OrderID()).getValue());
			dntKnowTrade.setExecID(message.getField(new OrderID()).getValue());
			
			dntKnowTrade.setDKReason(message.getField(new DKReason()).getValue());
			dntKnowTrade.setSymbol(message.getField(new Symbol()).getValue());
			dntKnowTrade.setOrderQty(message.getField(new OrderQty()).getValue());
			dntKnowTrade.setText(message.getField(new Text()).getValue());
			dntKnowTrade.setSide(message.getField(new Side()).getValue());
			
			
		}catch (Exception e) {
			e.printStackTrace();
			LOG.severe("Error in parseDntKnwTradeMsg for Order ID : " + dntKnowTrade.getOrderID() + " : " + e.getMessage());
		}
	}

	
	
	private void parseExecReport(Message execRptMsg) {


		StructExecutionReport execRpt = new StructExecutionReport();
		try {
			execRpt.setOrderID(execRptMsg.getField(new OrderID()).getValue());		//Set the Order Id in the Execution Report Structure
			execRpt.setClOrdID(execRptMsg.getField(new ClOrdID()).getValue());		//Set the Client Order ID
			execRpt.setOrdStatus(execRptMsg.getField(new OrdStatus()).getValue());
			execRpt.setExecID(execRptMsg.getField(new ExecID()).getValue());
			execRpt.setExecType(execRptMsg.getField(new ExecType()).getValue());
			execRpt.setOrdStatus(execRptMsg.getField(new OrdStatus()).getValue());


			if((execRptMsg.getField(new ExecType()).getValue())==ExecType.TRADE)		
			{
				execRpt.setLastPx(execRptMsg.getField(new LastPx()).getValue());
				execRpt.setLastSpotRate(execRptMsg.getField(new LastSpotRate()).getValue());
				try {
					execRpt.setLastForwardPoints(execRptMsg.getField(new LastForwardPoints()).getValue());
				}catch(FieldNotFound fnf)
				{
					execRpt.setLastForwardPoints(0);
				}
			}

			try {
				execRpt.setSymbol(execRptMsg.getField(new Symbol()).getValue());	//Set the Currency Pair,e.g:EUR/USD
			} catch (FieldNotFound fnf) {
				execRpt.setSymbol("");
			}

			execRpt.setCFICode(execRptMsg.getField(new CFICode()).getValue());		//Set the CFICode,FFCPNO = Forward Outright ,RCSXXX = Spot 
			execRpt.setSide(execRptMsg.getField(new Side()).getValue());			
			execRpt.setOrderQty(execRptMsg.getField(new OrderQty()).getValue());
			
			try {
				execRpt.setOrdType(execRptMsg.getField(new OrdType()).getValue());
			} catch (FieldNotFound fnf) {
				LOG.info(fnf.getMessage());
			}
			try {
				execRpt.setPrice(execRptMsg.getField(new Price()).getValue());
			} catch (FieldNotFound fnf) {
				LOG.info(fnf.getMessage());
			}
			try {
				execRpt.setCurrency(execRptMsg.getField(new Currency()).getValue());
			} catch (FieldNotFound fnf) {
				LOG.info(fnf.getMessage());
			}

			try {
				execRpt.setTimeInForce(execRptMsg.getField(new TimeInForce()).getValue());
			} catch (FieldNotFound fnf) {
				LOG.info(fnf.getMessage());
			}
			try {
				execRpt.setLastQty(execRptMsg.getField(new LastQty()).getValue());
			}catch(FieldNotFound fnf)
			{
				execRpt.setLastQty(0);
			}
			
			execRpt.setCumQty(execRptMsg.getField(new CumQty()).getValue());

			//If the OrdStatus is Canceled or Rejected then set LeavesQty=0 otherwise set LeavesQty = OrdQty - CumQty
			if((execRptMsg.getField(new OrdStatus()).getValue())==OrdStatus.CANCELED || (execRptMsg.getField(new OrdStatus()).getValue())!=OrdStatus.REJECTED)
				execRpt.setLeavesQty(0);
			else
				execRpt.setLeavesQty(execRpt.getOrderQty()-execRpt.getCumQty());
			
			try {
				execRpt.setAvgPx(execRptMsg.getField(new AvgPx()).getValue());
			}catch(FieldNotFound fnf) {
				execRpt.setAvgPx(0);
			}
			try {
				execRpt.setPrice(execRptMsg.getField(new Price()).getValue());
			}catch(FieldNotFound fnf) {
				execRpt.setPrice(0);
			}
			
			try {
				execRpt.setText(execRptMsg.getField(new Text()).getValue());
			} catch (FieldNotFound fnf) {
				execRpt.setText("");
			}
			LOG.info("Execution Report received for Order ID:"+execRpt.getClOrdID());
			try {
				DB_Functions_FIX.update_FX_RFQ_Details(execRpt, conn);		//Update the details in Database
			} catch (Exception e) {
				e.printStackTrace();
				LOG.severe("Unable To Save Execution Report for Order ID : " + execRpt.getClOrdID() + " : " +
						e.getMessage());
			}


		} catch (Exception e) {
			e.printStackTrace();
			LOG.severe("Error in parseExecReport for Order ID : " + execRpt.getClOrdID() + " : " + e.getMessage());
		}

	}

	private void parseQuoteCancel(Message message) {
		// TODO Auto-generated method stub

	}
	
	private void setLinkStatus(String status, boolean isMult_Login_Failure, boolean isEmailEnabled) {
		try {
			if (conn == null || conn.isClosed()) {
				conn = DBUtil.getDBConnection();
				LOG.info("New Connection initalized by " + Thread.currentThread().getName());
			}
			try {
				DB_Functions_FIX.update_Link_Update(FXClient.getPriceProvider_ID(), status, isMult_Login_Failure,
						isEmailEnabled, conn);
			} catch (SQLException e) {
				LOG.severe("Error While updating Link Status :- " + e.getMessage());
			}
		} catch (Exception e) {
			LOG.severe("Unable to intialize new Connection in setLinkStatus : " + e.getMessage());

		}
	}

}
