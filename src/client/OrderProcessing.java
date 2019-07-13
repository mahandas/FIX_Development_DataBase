package client;

import com.sun.rowset.CachedRowSetImpl;

import client.FIX_MessageStructure.StructNewOrderSingle;
import db_functions.DB_Functions_FIX;

import java.sql.Connection;

import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

import quickfix.Session;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.Currency;
import quickfix.field.HandlInst;
import quickfix.field.NoPartyIDs;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.PartyID;
import quickfix.field.PartyIDSource;
import quickfix.field.PartyRole;
import quickfix.field.Price;
import quickfix.field.QuoteID;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;

import util.DBUtil;

public class OrderProcessing implements Runnable {

	 public OrderProcessing() {
	    }

	    private Connection conn = null;
	    private static final Logger LOG = Logger.getLogger(OrderProcessing.class.getName());
	    private static final Properties config = ConfigReader.getConfigFile();


	@Override
	public void run() {
		 System.out.println(" Thread Started " + Thread.currentThread().getName());
	 
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
	                placeOrder();
	                try {
	                    Thread.sleep(pollInterval);
	                } catch (InterruptedException ie) {
	                    LOG.severe("Error in OrderProcessing Thread " + ie.getMessage());
	                }
	            } catch (Exception e) {
	                LOG.severe("Error in OrderProcessing thread DB connection " + e.getMessage());
	            }

	        }	
	     }
    public void placeOrder() {
        try {
            CachedRowSet crs = new CachedRowSetImpl();
            try {
                crs.populate(DB_Functions_FIX.get_NewOrderSingle_Details_FX(FXClient.getEntityID(),
                                                                                 FXClient.getPriceProvider_ID(),
                                                                                 conn));
            } catch (Exception e) {
                LOG.severe("Error in get_NewOrderSingle_Details_FX : " + e.getMessage());
                crs.close();
                return;
            }
            StructNewOrderSingle objNewOrder = new StructNewOrderSingle();
            SessionID sessionID;
            while (crs != null && crs.next()) {
                try {
                    
                    if (!ApplicationImpl.isLoggedOn()) {
                        throw new Exception("Unable get logon response from FX server..");
                    }
                    objNewOrder.setClOrdID(crs.getString("Order_ID"));
                    objNewOrder.setQuoteID(crs.getString("Quote_ID")); // Generated by FIX server

                    objNewOrder.setSymbol(crs.getString("Symbol"));
                    objNewOrder.setOrderQty(crs.getDouble("OrderQty"));
                    objNewOrder.setSide((crs.getString("Side")).charAt(0)=='1'?Side.BUY:Side.SELL);
                    objNewOrder.setPrice(crs.getDouble("Price"));
                    System.out.println("Booking Price:"+objNewOrder.getPrice());
                    objNewOrder.setCurrency(crs.getString("Currency"));
                    sessionID = UBSFXClient.getSessionID();
                    if (sessionID != null && Session.doesSessionExist(sessionID)) {
                        if (Session.sendToTarget(buildOrder(objNewOrder), sessionID) == true) {
                            LOG.info("Order request sent successfully For Order No:" + objNewOrder.getClOrdID() + "");
                        } else {
                            throw new Exception("Error while sending Order");
                        }
                    } else {
                        throw new Exception("Session does not exist ...");
                    }
                } catch (Exception e) {
                    LOG.severe("Error in Order No : " + objNewOrder.getClOrdID() + " : " + e.getMessage());
                } finally {
                    // Try to send a Order only once. Don't resend even if send successfully or not.
                    DB_Functions_FIX.update_Order_Requested_YN(objNewOrder.getClOrdID().substring(0,
                                                                                                  objNewOrder.getClOrdID().length() -
                                                                                                  1), "Y", conn);
                }
            }
            crs.close();
        } catch (Exception e) {
            LOG.severe("Error in placeOrder : " + e.getMessage());
        }
    }
    private NewOrderSingle buildOrder(StructNewOrderSingle objNewOrder) throws Exception {
        try {
            NewOrderSingle newOrder = new NewOrderSingle();
            newOrder.set(new ClOrdID(objNewOrder.getClOrdID()));
            

	  				
	  				NewOrderSingle.NoPartyIDs grpPartyID = new NewOrderSingle.NoPartyIDs();
	  				grpPartyID.setField(new PartyID(config.getProperty("PartyID")));	//Get the party Id from Properties file
	  				grpPartyID.setField(new PartyIDSource('D'));		//Accepted Value only D=Proprietary

	  				grpPartyID.setField(new PartyRole(13));	//Accepted Value only 13=Order Origination Firm
	  				newOrder.addGroup(grpPartyID);

              newOrder.setField(new NoPartyIDs(1));
            
            newOrder.setField(new HandlInst(HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE)); //1 = Automated execution order, private, no Broker intervention
            newOrder.setField(new Symbol(objNewOrder.getSymbol()));
            newOrder.set(new Side(objNewOrder.getSide()));
            newOrder.set(new TransactTime(new Date()));
            newOrder.set(new OrderQty(objNewOrder.getOrderQty()));
            newOrder.set(new OrdType(OrdType.PREVIOUSLY_QUOTED));
            newOrder.set(new Price(objNewOrder.getPrice()));
            newOrder.setField(new TimeInForce(TimeInForce.FILL_OR_KILL));

            newOrder.set(new QuoteID(objNewOrder.getQuoteID()));
            

            newOrder.set(new Currency(objNewOrder.getCurrency()));

            return newOrder;
        } catch (Exception e) {
            throw new Exception("Error in buildOrderAndSend() : " + e.getMessage());
        }
    }

}