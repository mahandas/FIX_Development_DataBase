package db_functions.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


public class TradeDetailsXML {

	@XmlRootElement(name = "quoteDetails")
	public static class QuoteXML {
		@XmlElement(name = "QuoteReqID")
		private String QuoteReqID = "";

		@XmlElement(name = "QuoteID")
		private String QuoteID = "";

		@XmlElement(name = "Symbol")
		private String Symbol = "";

		@XmlElement(name = "OrderQty")
		private String OrderQty = "";

		@XmlElement(name = "BidPx")
		private String BidPx = "";

		@XmlElement(name = "OfferPx")
		private String OfferPx = "";

		@XmlElement(name = "TradingSessionId")
		private String TradingSessionId = "";

		@XmlElement(name = "BidFarAllInRate")
		private String BidFarAllInRate = "";

		@XmlElement(name = "OfferFarAllInRate")
		private String OfferFarAllInRate = "";

		@XmlElement(name = "ValidUntilTime")
		private String ValidUntilTime = "";

		@XmlElement(name = "BidSpotRate")
		private String BidSpotRate = "";

		@XmlElement(name = "SpotRate")
		private String SpotRate = "";

		@XmlElement(name = "OfferSpotRate")
		private String OfferSpotRate = "";

		@XmlElement(name = "BidForwardPoints")
		private String BidForwardPoints = "";

		@XmlElement(name = "OfferForwardPoints")
		private String OfferForwardPoints = "";

		@XmlElement(name = "BidFarForwardPoints")
		private String BidFarForwardPoints = "";

		@XmlElement(name = "OfferFarForwardPoints")
		private String OfferFarForwardPoints = "";

		@XmlElement(name = "BidSwapPoints")
		private String BidSwapPoints = "";

		@XmlElement(name = "OfferswapPoints")
		private String OfferswapPoints = "";

		@XmlElement(name = "futSettDate")
		private String futSettDate = "";

		@XmlElement(name = "forwardPoints")
		private String forwardPoints = "";

		public void setQuoteID(String QuoteID) {
			this.QuoteID = QuoteID;
		}

		public void setSymbol(String Symbol) {
			this.Symbol = Symbol;
		}

		public void setOrderQty(String OrderQty) {
			this.OrderQty = OrderQty;
		}

		public void setBidPx(String BidPx) {
			this.BidPx = BidPx;
		}

		public void setOfferPx(String OfferPx) {
			this.OfferPx = OfferPx;
		}

		public void setSpotRate(String SpotRate) {
			this.SpotRate = SpotRate;
		}

		public void setBidSpotRate(String BidSpotRate) {
			this.BidSpotRate = BidSpotRate;
		}

		public void setOfferSpotRate(String OfferSpotRate) {
			this.OfferSpotRate = OfferSpotRate;
		}

		public void setBidForwardPoints(String BidForwardPoints) {
			this.BidForwardPoints = BidForwardPoints;
		}

		public void setOfferForwardPoints(String OfferForwardPoints) {
			this.OfferForwardPoints = OfferForwardPoints;
		}

		public void setValidUntilTime(String ValidUntilTime) {
			this.ValidUntilTime = ValidUntilTime;
		}

		public void setQuoteReqID(String quoteReqID) {
			this.QuoteReqID = quoteReqID;
		}

		public void setTradingSessionId(String tradingSessionId) {
			this.TradingSessionId = tradingSessionId;
		}

		public void setBidFarAllInRate(String bidFarAllInRate) {
			this.BidFarAllInRate = bidFarAllInRate;
		}

		public void setOfferFarAllInRate(String offerFarAllInRate) {
			this.OfferFarAllInRate = offerFarAllInRate;
		}

		public void setBidFarForwardPoints(String bidFarForwardPoints) {
			this.BidFarForwardPoints = bidFarForwardPoints;
		}

		public void setOfferFarForwardPoints(String offerFarForwardPoints) {
			this.OfferFarForwardPoints = offerFarForwardPoints;
		}

		public void setBidSwapPoints(String bidSwapPoints) {
			this.BidSwapPoints = bidSwapPoints;
		}

		public void setOfferswapPoints(String offerswapPoints) {
			this.OfferswapPoints = offerswapPoints;
		}

		public void setFutSettlDate(String futSettDate) {
			this.futSettDate=futSettDate;
		}

		public void setForwardPoints(String forwardPoints) {
			this.forwardPoints=forwardPoints;
		}

	}

	@XmlRootElement(name = "quoteDetails")
	public static class QuoteCancelXML {
		@XmlElement(name = "Quote_Cancel_Type")
		private String Quote_Cancel_Type = "";

		@XmlElement(name = "Quote_Cancel_Level")
		private String Quote_Cancel_Level = "";

		@XmlElement(name = "QuoteReqID")
		private String QuoteReqID = "";

		@XmlElement(name = "Symbol")
		private String Symbol = "";

		public void setQuote_Cancel_Type(String quote_Cancel_Type) {
			this.Quote_Cancel_Type = quote_Cancel_Type;
		}

		public void setQuote_Cancel_Level(String quote_Cancel_Level) {
			this.Quote_Cancel_Level = quote_Cancel_Level;
		}

		public void setQuoteReqID(String quoteReqID) {
			this.QuoteReqID = quoteReqID;
		}

		public void setSymbol(String symbol) {
			this.Symbol = symbol;
		}

	}

	@XmlRootElement(name = "quoteDetails")
	public static class QuoteAckXML {
		@XmlElement(name = "Quote_Ack_Status")
		private String Quote_Ack_Status = "";

		@XmlElement(name = "Quote_Rejection_Reason")
		private String Quote_Rejection_Reason = "";

		@XmlElement(name = "Quote_Request_Rejection_Reason")
		private String Quote_Request_Rejection_Reason = "";


		public void setQuote_Ack_Status(String Quote_Ack_Status) {
			this.Quote_Ack_Status = Quote_Ack_Status;
		}

		public void setQuote_Rejection_Reason(String Quote_Rejection_Reason) {
			this.Quote_Rejection_Reason = Quote_Rejection_Reason;
		}

		public void setQuote_Request_Rejection_Reason(String Quote_Request_Rejection_Reason) {
			this.Quote_Request_Rejection_Reason = Quote_Request_Rejection_Reason;
		}
	}

	@XmlRootElement(name = "OrderDetails")
	public static class ExecReportXML {
		@XmlElement(name = "OrderID")
		private String OrderID = "";

		@XmlElement(name = "ClOrdID")
		private String ClOrdID = "";

		@XmlElement(name = "ExecID")
		private String ExecID = "";

		@XmlElement(name = "ExecTransType")
		private String ExecTransType = "";

		@XmlElement(name = "ExecType")
		private String ExecType = "";

		@XmlElement(name = "OrdStatus")
		private String OrdStatus = "";

		@XmlElement(name = "LeavesQty")
		private String LeavesQty = "";

		@XmlElement(name = "CumQty")
		private String CumQty = "";

		@XmlElement(name = "AvgPx")
		private String AvgPx = "";

		@XmlElement(name = "Text")
		private String Text = "";

		@XmlElement(name = "Notional")
		private String Notional = "";

		@XmlElement(name = "DealPair")
		private String DealPair = "";

		@XmlElement(name = "TradeDate")
		private String TradeDate = "";

		@XmlElement(name = "ValueDate")
		private String ValueDate = "";

		@XmlElement(name = "FixingDate")
		private String FixingDate = "";

		@XmlElement(name = "MaturityDate")
		private String MaturityDate = "";

		@XmlElement(name = "Price")
		private String Price="";

		public void setTradeDate(String TradeDate) {
			this.TradeDate = TradeDate;
		}

		public void setValueDate(String ValueDate) {
			this.ValueDate = ValueDate;
		}

		public void setFixingDate(String FixingDate) {
			this.FixingDate = FixingDate;
		}

		public void setMaturityDate(String Maturitydate) {
			this.MaturityDate = Maturitydate;
		}

		public void setOrderID(String OrderID) {
			this.OrderID = OrderID;
		}

		public void setClOrdID(String ClOrdID) {
			this.ClOrdID = ClOrdID;
		}

		public void setExecID(String ExecID) {
			this.ExecID = ExecID;
		}

		public void setExecTransType(String ExecTransType) {
			this.ExecTransType = ExecTransType;
		}

		public void setExecType(String ExecType) {
			this.ExecType = ExecType;
		}

		public void setOrdStatus(String OrdStatus) {
			this.OrdStatus = OrdStatus;
		}

		public void setLeavesQty(String LeavesQty) {
			this.LeavesQty = LeavesQty;
		}

		public void setCumQty(String CumQty) {
			this.CumQty = CumQty;
		}

		public void setAvgPx(String AvgPx) {
			this.AvgPx = AvgPx;
		}

		public void setText(String Text) {
			this.Text = Text;
		}

		public void setNotional(String Notional) {
			this.Notional = Notional;
		}

		public void setDealPair(String DealPair) {
			this.DealPair = DealPair;
		}

		public void setPrice(String Price) {
			this.Price=Price;			
		}

	}

	@XmlRootElement(name = "OrderDetails")
	public static class DontKnowTradeXMl {
		@XmlElement(name = "QuoteReqID")
		private String QuoteReqID = "";

		@XmlElement(name = "OrderID")
		private String OrderID = "";

		@XmlElement(name = "ExecID")
		private String ExecID = "";

		@XmlElement(name = "DKReason")
		private String DKReason = "";


		public void setQuoteReqID(String QuoteReqID) {
			this.QuoteReqID = QuoteReqID;
		}

		public void setOrderID(String OrderID) {
			this.OrderID = OrderID;
		}

		public void setExecID(String ExecID) {
			this.ExecID = ExecID;
		}

		public void setDKReason(String DKReason) {
			this.DKReason = DKReason;
		}
	}

	@XmlRootElement(name = "quoteDetails")
	public static class QuoteRequestRejectXML {
		@XmlElement(name = "Quote_Rejection_Reason")
		private String Quote_Rejection_Reason = "";

		@XmlElement(name = "Quote_Request_Rejection_Reason")
		private String Quote_Request_Rejection_Reason = "";

		@XmlElement(name = "Quote_Ack_Status")
		private String Quote_Ack_Status = "";

		public void setQuote_Rejection_Reason(String Symbol) {
			this.Quote_Rejection_Reason = Symbol;
		}

		public void setQuote_Request_Rejection_Reason(String Text) {
			this.Quote_Request_Rejection_Reason = Text;
		}

		public void setQuote_Ack_Status(String Quote_Ack_Status) {
			this.Quote_Ack_Status = Quote_Ack_Status;
		}

	}

}
