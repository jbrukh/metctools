package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.marketcetera.event.AskEvent;
import org.marketcetera.event.BidEvent;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderStatus;

public class Trade {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8466519547231754210L;
	private static final long DEFAULT_ORDER_TIMEOUT = 60*1000;
	
	/* internal fields  */
	transient private 
		Portfolio 			parentPortfolio;	// parent portfolio to whom this trade belongs
	transient private
		OrderProcessor 		orderProcessor;		// order processing object

	/* accounting */
	private final String 	symbol;				// underlying symbol
	private BigDecimal 		quantity;			// unsigned number of shares
	private Side 			side;				// side of the position
	
	private BigDecimal 		leavesQty;			// leavesQuantity of the pending order
	private BigDecimal 		cumulativeQty;		// number of shares pending fill
	private OrderStatus		orderStatus;		// order status of last (pertinent) execution report
	private Side 			pendingSide;		// side of the incoming fills
	private BigDecimal 		averagePrice;		// average entry price of last fill
	
	private TradeEvent 		lastTradeEvent;		// last trade of the underlying symbol
	private BidEvent 		lastBidEvent;		// last bid of the underlying symbol
	private AskEvent 		lastAskEvent;		// last ask of the underlying symbol
	
	private BrokerID		brokerId;
	private String			account;
	
	/* policies */
	private long 			orderTimeout;		// default timeout in milliseconds
	private FillPolicy 		fillPolicy;			// default fill policy (what to do on a fill?)
	private OrderTimeoutPolicy orderTimeoutPolicy; // default order timeout policy (what to do if order times out?)
	private RejectPolicy	rejectPolicy;
	
	/* logging */
	private final static Logger logger = Logger.getLogger(Trade.class);
	
	
	// CONSTRUCTORS //
	
	/**
	 * Create a new Trade object instance.
	 * 
	 * @param parent
	 * @param symbol
	 * @param brokerId
	 * @param account
	 */
	public Trade( String symbol, Portfolio parent ) {
		this.symbol = symbol;
		this.parentPortfolio = parent;
		init();
	}
	
	public Trade( String symbol ) {
		this(symbol,null);
	}
	
	

	
	// FIELD GETTERS AND SETTERS //
	
	public Portfolio getParentPortfolio() {
		return parentPortfolio;
	}

	public void setParentPortfolio(Portfolio parentPortfolio) {
		this.parentPortfolio = parentPortfolio;
		setAccountInfo();
	}

	public long getOrderTimeout() {
		return orderTimeout;
	}

	public void setOrderTimeout(long orderTimeout) {
		this.orderTimeout = orderTimeout;
	}

	public FillPolicy getFillPolicy() {
		return fillPolicy;
	}

	public void setFillPolicy(FillPolicy fillPolicy) {
		this.fillPolicy = fillPolicy;
	}

	public OrderTimeoutPolicy getOrderTimeoutPolicy() {
		return orderTimeoutPolicy;
	}

	public void setOrderTimeoutPolicy(OrderTimeoutPolicy orderTimeoutPolicy) {
		this.orderTimeoutPolicy = orderTimeoutPolicy;
	}

	public RejectPolicy getRejectPolicy() {
		return rejectPolicy;
	}

	public void setRejectPolicy(RejectPolicy rejectPolicy) {
		this.rejectPolicy = rejectPolicy;
	}

	public OrderProcessor getOrderProcessor() {
		return orderProcessor;
	}

	public String getSymbol() {
		return symbol;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public Side getSide() {
		return side;
	}

	public BigDecimal getLeavesQty() {
		return leavesQty;
	}

	public BigDecimal getCumulativeQty() {
		return cumulativeQty;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public Side getPendingSide() {
		return pendingSide;
	}

	public OrderID getPendingOrderId() {
		return orderProcessor.getPendingOrderId();
	}

	public OrderID getCancelOrderId() {
		return orderProcessor.getCancelOrderId();
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public TradeEvent getLastTradeEvent() {
		return lastTradeEvent;
	}
	
	public BrokerID getBrokerId() {
		return brokerId;
	}

	public String getAccount() {
		return account;
	}
	
	public OrderProcessor order() {
		return orderProcessor;
	}

	/**
	 * Get the parent strategy.
	 * 
	 * @return
	 */
	public final PortfolioStrategy getParentStrategy() {
		if ( parentPortfolio != null ) {
			return parentPortfolio.getParentStrategy();
		} return null;
	}
	
	
	
	
	// ACCOUNTING INFORMATION //
	
	public final boolean isPending() {
		return orderProcessor.isPending();
	}
	
	public final boolean isFilling() {
		return (isPending() && getCumulativeQty().intValue()!=0);
	}
	
	public final boolean isOpen() {
		return (getNetQuantity().intValue()!=0);
	}
	
	public final BigDecimal getSignedQty() {
		return side.polarize(quantity);
	}
	
	/**
	 * Returns the last trading price of the underlying symbol.
	 * 
	 * In order for this method to return an accurate value,
	 * data flows for this symbol must have been turned on,
	 * and this Trade must reside in a PortfolioStrategy's
	 * Portfolio.
	 * 
	 * @return
	 */
	public BigDecimal getLastPrice() {
		if (lastTradeEvent==null) return BigDecimal.ZERO;
		return lastTradeEvent.getPrice();
	}

	/**
	 * Returns the instantaneous (usually unsigned) position of this trade.
	 * 
	 * If there is no pending order currently being filled, this method is
	 * equivalent to getQuantity().
	 * 
	 * If there is a pending order currently being filled, this method returns
	 * the instantaneous position of the trade, taking into account the fills
	 * up to this point in time.
	 * 
	 * If the fills up to this point in time have caused the position to change
	 * sides, this is denoted with a negative sign in the result.
	 * 
	 * @return
	 */
	public final BigDecimal getNetQuantity() {
		/* 1 = position and fills are the same side; 
		 * -1 = position and fills are different side;
		 * recall that all quantities are unsigned */
		BigDecimal polarity = side.polarize(pendingSide);
		return quantity.add( polarity.multiply(cumulativeQty));
	}
	
	public String toString() {
		return String.format("{%s:[%.2f]:%s%d%s@%.4f}",
				getSymbol(),
				getLastPrice().floatValue(),
				(getSide()==Side.BUY?"+":(getSide()==Side.SELL?"-":"")),
				getQuantity().intValue(),
				/* shows pending values if order is pending */
				isFilling()? "(" + getCumulativeQty() + "cq/"+ getLeavesQty() + "lq)" : "",
				getAveragePrice().floatValue());
	}
	
	
	// OVERRIDES //
	
	public final void overrideSide( Side side ) {
		this.side = side;
	}
	
	public final void overrideQuantity( BigDecimal quantity ) {
		this.quantity = quantity;
	}
	
	public final void unsetParentPortfolio() {
		setParentPortfolio(null);
	}
	
	
	// EVENT HANDLING //
	
	/**
	 * Interface for getting the latest TradeEvent.
	 * 
	 * @param tradeEvent
	 */
	public final void acceptTradeEvent(TradeEvent tradeEvent) {
		lastTradeEvent = tradeEvent;
		
		/* for subclass processing of efficiently-routed TradeEvents */
		onTradeEvent(tradeEvent);
	}
	
	public final void acceptBidEvent(BidEvent bidEvent) {
		lastBidEvent = bidEvent;
		/* for subclass processing of efficiently-routed TradeEvents */
		onBidEvent(bidEvent);
	}
	
	public final void acceptAskEvent(AskEvent askEvent) {
		lastAskEvent = askEvent;
		/* for subclass processing of efficiently-routed TradeEvents */
		onAskEvent(askEvent);
	}
	
	/**
	 * Provided so that subclasses can react to
	 * efficiently-routed TradeEvents sent to this object via the 
	 * PortfolioStrategy's TradeRouter.
	 * 
	 * This method does not conflict with the TradeDelegate interface
	 * and that interface can still be implemented by subclasses for
	 * the purpose of catching global TradeEvents.
	 * 
	 * @param tradeEvent
	 */
	protected void onTradeEvent( TradeEvent event ) { }
	protected void onBidEvent( BidEvent event ) { }
	protected void onAskEvent( AskEvent event ) { }
	

	/**
	 * Adjusts the trade information based on an incoming execution report.
	 * 
	 * @param sender
	 * @param report
	 */
	public final void acceptExecutionReport(DelegatorStrategy sender,
			ExecutionReport report) {
		
		/* check the correct symbol and account */
		if ( !report.getSymbol().toString().equals(symbol) || 
				!report.getAccount().toString().equals(account) ) {
			logger.debug( Messages.MSG_EXTERNAL_REPORT(this));
			logger.debug(">>> " + symbol + "/" + report.getAccount());
			return;
		}
		
		/* 
		 * HANDLING OF EXTERNAL EXECUTION REPORTS
		 * 
		 * At this point, a report may have come from an order
		 * placed within the framework (the pendingOrderId field
		 * matches) or from some other external order placement
		 * procedure through the ORS;
		 * 
		 * The policy for handling external reports will be implemented
		 * in checkExternalReport().
		 * 
		 * IGNORING EXTERNAL REPORTS -- The advantages are that the
		 * strategy cannot be confounded by various irregularities
		 * that will inevitably occur if the user starts messing around
		 * manually.  The disadvantages are that ignoring reports
		 * may create inconsistencies: the user may set the real account
		 * position to zero, while the strategy still views the position
		 * as nonzero; as a result, the strategy may reopen positions
		 * due to automatic orders placed based on strategy logic.
		 * 
		 * INCORPORATING EXTERNAL REPORTS -- This would keep the positions
		 * tracked by the framework in sync with "real" positions as
		 * defined by all received reports.  However, this can also be
		 * problematic if strategy logic places orders based on position
		 * size, since the sizes would be artificially modified.  Furthermore,
		 * proper simultaneously handling reports from multiple asynchronous
		 * sources would require a queue which is currently not implemented.
		 * 
		 */
		if ( !processExternalReport(report)  ) {
			/* ignore the report if the check fails */
			logger.warn(">>>\t" + this + 
					": External execution report for " + 
					symbol + 
					" (Ignoring.) -- " +
					report);
			return;
		}
		
		orderStatus = report.getOrderStatus();
		
		switch(orderStatus) {
		case New:
			/* scrape the report, and log */
			processNew(report);
			break;
		case PartiallyFilled:
			/* scrape the report, set the side from report, and log */
			processPartialFill(report);
			break;
		case Filled:
			/* various actions depend on a fill */
			processFill(report);
			break;
		case Canceled:
			/* clear the pending fields info,
			 * and make sure to scrape.
			 */
			processCanceled(report);
			break;
		case Rejected:
			/* just calls the rejection policy */
			processRejected(report);
			break;
		case PendingNew:
		case PendingCancel:
			break;
		default:
			logger.error(
					">>>\tExecution report status is " +
					orderStatus + ", which is not implemented.");
			break;
		}
		
	}
	
	/**
	 * This method implements the policy for handling execution reports
	 * that come in externally through the Metc API and not via the MetcTools
	 * framework (i.e. through the Trade OrderProcessor).
	 * 
	 * Currently, this ignores external reports.
	 * 
	 * @return
	 */
	private final boolean processExternalReport(ExecutionReport report) {
		return true;
	}
	
	/**
	 * Internal method for processing New reports.
	 * 
	 * @param report
	 */
	private final void processNew( ExecutionReport report ) {
		/* get the info */
		scrapeReport(report);
		
		/* logging */
		logger.trace(">>>\t" + report);
	}
	
	/**
	 * Internal method for processing partial fills.
	 * 
	 * @param report
	 */
	private void processPartialFill( ExecutionReport report ) {
		scrapeReport(report);
		
		/* set the side from the first execution report */
		if ( side==Side.NONE ) {
			side = Side.fromMetcSide(report.getSide());
		}
		
		/* logging */
		logger.info(">>>\t" + this + ": Partial fill on " + report.getOrderID() + ".");
		logger.trace(">>>\t" + report);
	}
	
	/**
	 * Internal method for processing fills.
	 * 
	 * @param report
	 */
	private void processFill( ExecutionReport report ) {

		/* this might need to be set here 
		 * if there are no partials */
		if ( side==Side.NONE ) {
			side = Side.fromMetcSide(report.getSide());
		}

		scrapeReport(report);
		updateQuantity(report);
	
		/* unblock the out thread */
		orderProcessor.orderSuccess();
		
		/* clean up */
		OrderID orderID = report.getOrderID();
		clearPendingFields();  // watch out, this clears order cancels too
		
		/* reset trade if zeroed */
		if ( getNetQuantity().intValue()==0) {
			side = Side.NONE;
		}
		
		/* order has been filled -- execute fill policy*/
		fillPolicy.onFill(parentPortfolio.getParentStrategy(), 
				            orderID, this);		
	}
	
	private void updateQuantity( ExecutionReport fillReport ) {
		/* add the cumulativeQty to the quantity */
		quantity = getNetQuantity(); 
		
		/* check if we have switched sides */
		if ( quantity.compareTo(BigDecimal.ZERO)<0) {
			side = side.opposite();
			BigDecimal inv = BigDecimal.valueOf(-1L,0);
			quantity = quantity.multiply(inv);
			logger.info(">>>\t" + this + ": Position has switched sides!");
		}
	}
	
	/**
	 * Internal method for processing cancels.
	 * @param report
	 */
	private void processCanceled( ExecutionReport report ) {
		
		/* get the report info */
		scrapeReport(report);
			
		/* logging */
		logger.info(">>> " + this + ": Order " + report.getOriginalOrderID() + " has been canceled." );
		
		/* update the quantity */
		updateQuantity(report);
		
		/* timeout */
		orderProcessor.cancelSuccess();
	
		/* clean up */
		clearPendingFields();
	}
	
	private void processRejected( ExecutionReport report ) {
		rejectPolicy.onReject(parentPortfolio.getParentStrategy(),
				report.getOrderID(),this,report);
	}
	
	
	
	
	
	
	
	
	// PRIVATE METHODS //
	
	/**
	 * Initialization.
	 */
	private final void init() {
		quantity = leavesQty = cumulativeQty = BigDecimal.ZERO;
		averagePrice = BigDecimal.ZERO;
		lastTradeEvent = null;
		side = pendingSide = Side.NONE;
		
		fillPolicy = FillPolicies.ON_FILL_WARN;
		orderTimeoutPolicy = OrderTimeoutPolicies.ON_TIMEOUT_WARN;
		rejectPolicy = RejectPolicies.ON_REJECT_WARN;
		orderTimeout = DEFAULT_ORDER_TIMEOUT;
		
		orderProcessor = new OrderProcessor(this);
		setAccountInfo();
	}
	
	/**
	 * Obtains the account information from the parent portfolio.
	 * 
	 */
	private final void setAccountInfo() {
		if ( parentPortfolio!=null && parentPortfolio.isAccountInfoSet() ) {
			brokerId = parentPortfolio.getBrokerID();
			account = parentPortfolio.getAccount();
			orderProcessor.setAccountInfo(brokerId, account);
		} else {
			brokerId = null;
			account = null;
		}
	}
	
	/**
	 * Utility method that scrapes the relevant information
	 * from an incoming execution report.
	 * 
	 * @param report
	 */
	private void scrapeReport(ExecutionReport report) {
		orderStatus 	= report.getOrderStatus();
		cumulativeQty 	= report.getCumulativeQuantity();
		leavesQty 		= report.getLeavesQuantity();
		pendingSide 	= Side.fromMetcSide(report.getSide());
		averagePrice 	= report.getAveragePrice();
	}
		
	private void clearPendingFields() {
		leavesQty = cumulativeQty = BigDecimal.ZERO;
		pendingSide = Side.NONE;		
	}
	
	
}
