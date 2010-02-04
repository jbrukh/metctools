package org.kohera.metctools.portfolio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.marketcetera.event.AskEvent;
import org.marketcetera.event.BidEvent;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderCancelReject;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderStatus;

public class Trade implements Serializable {

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
	private BigDecimal 		averagePrice;		// average price of last fill
	private BigDecimal		entryPrice;			// average price of the opening order (reset when trade is zeroed) 
	
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
	private final static Logger logger = 
		Logger.getLogger(Trade.class);
	
	
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
	
	/**
	 * Returns the parent Portfolio.
	 * 
	 */
	public final Portfolio getParentPortfolio() {
		return parentPortfolio;
	}

	/**
	 * Sets the parent Portfolio.  If this portfolio has account information,
	 * then the information is copied to this Trade.
	 * 
	 * If parentPortfolio is null, then this Trade's account information is
	 * deleted and the parent portfolio is unset.
	 * 
	 * @param parentPortfolio
	 */
	public final void setParentPortfolio(Portfolio parentPortfolio) {
		this.parentPortfolio = parentPortfolio;
		setAccountInfo();
	}

	/**
	 * Gets the this Trade's order timeout time.
	 * 
	 * @return
	 */
	public final long getOrderTimeout() {
		return orderTimeout;
	}

	/**
	 * Sets the order timeout time.
	 * 
	 * @param orderTimeout
	 */
	public final void setOrderTimeout(long orderTimeout) {
		this.orderTimeout = orderTimeout;
	}

	/**
	 * Gets the FillPolicy.
	 * 
	 * @return
	 */
	public final FillPolicy getFillPolicy() {
		return fillPolicy;
	}

	/**
	 * Sets the FillPolicy.
	 * 
	 * @param fillPolicy
	 */
	public final void setFillPolicy(FillPolicy fillPolicy) {
		this.fillPolicy = fillPolicy;
	}

	/**
	 * Gets the OrderTimeoutPolicy for this Trade.
	 * 
	 * @return
	 */
	public final OrderTimeoutPolicy getOrderTimeoutPolicy() {
		return orderTimeoutPolicy;
	}

	/**
	 * Sets the OrderTimeoutPolicy for this Trade.
	 * 
	 * @param orderTimeoutPolicy
	 */
	public final void setOrderTimeoutPolicy(OrderTimeoutPolicy orderTimeoutPolicy) {
		this.orderTimeoutPolicy = orderTimeoutPolicy;
	}

	/**
	 * Gets the OrderRejectPolicy.
	 * 
	 * @return
	 */
	public final RejectPolicy getRejectPolicy() {
		return rejectPolicy;
	}

	/**
	 * Sets the OrderRejectPolicy.
	 * 
	 * @param rejectPolicy
	 */
	public final void setRejectPolicy(RejectPolicy rejectPolicy) {
		this.rejectPolicy = rejectPolicy;
	}

	/**
	 * Gets the OrderInterface for this Trade.  This is an alias
	 * to order().
	 * 
	 * @see order()
	 * @return
	 */
	public final OrderInterface getOrderInterface() {
		return orderProcessor;
	}

	/**
	 * Gets the symbol for this Trade.
	 * 
	 * @return
	 */
	public final String getSymbol() {
		return symbol;
	}

	/**
	 * Gets the transacted quantity of this Trade.
	 * 
	 * Explanation: The transacted quantity is updated once a trade
	 * fully fills (or is cancelled).  While a trade is filling, the quantity
	 * differential as a result of partial fills is not reflected in this
	 * quantity.
	 * 
	 * @see getNetQty();
	 * @return
	 */
	public final BigDecimal getQty() {
		return quantity;
	}

	/**
	 * Returns the side of this trade.  Side is NONE when the trade has no
	 * position.
	 * 
	 * @return
	 */
	public final Side getSide() {
		return side;
	}

	/**
	 * If the trade is filling, returns the number of shares left to be
	 * filled.
	 * 
	 * @return
	 */
	public final BigDecimal getLeavesQty() {
		return leavesQty;
	}

	/**
	 * If the trade is filling, returns the number of shares cumulatively
	 * filled.  Note that this is different then the result of getQty(), which
	 * returns the transacted quantity;  the cumulative quantity is real
	 * position, but it is not accounted for by getQty() until the trade is
	 * concluded.
	 * 
	 * @see getQty(), getNetQty()
	 * @return
	 */
	public final BigDecimal getCumulativeQty() {
		return cumulativeQty;
	}

	/**
	 * Returns the orderStatus of the most recent execution report.
	 * 
	 * @return
	 */
	public final OrderStatus getOrderStatus() {
		return orderStatus;
	}

	/**
	 * If an order is pending, returns the side of the pending order.
	 * 
	 * @return
	 */
	public final Side getPendingSide() {
		return pendingSide;
	}

	/**
	 * Returns the OrderID of the pending order, if it exists.
	 * 
	 * @return
	 */
	public final OrderID getPendingOrderId() {
		return orderProcessor.getPendingOrderId();
	}

	/**
	 * Returns the OrderID of the cancel order, if it exists.
	 * 
	 * @return
	 */
	public final OrderID getCancelOrderId() {
		return orderProcessor.getCancelOrderId();
	}

	/**
	 * Returns the last average fill price as given by the last
	 * received execution report.
	 * 
	 * @return
	 */
	public final BigDecimal getAveragePrice() {
		return averagePrice;
	}

	/**
	 * Returns the last received TradeEvent.
	 * 
	 * @return
	 */
	public final TradeEvent getLastTradeEvent() {
		return lastTradeEvent;
	}
	
	/**
	 * Returns the broker id associated with this Trade.
	 * 
	 * @return
	 */
	public final BrokerID getBrokerId() {
		return brokerId;
	}

	/**
	 * Returns the account associated with this Trade.
	 * 
	 * @return
	 */
	public final String getAccount() {
		return account;
	}
	
	/**
	 * Returns the entry price for this Trade.
	 * 
	 * @return
	 */
	public final BigDecimal getEntryPrice() {
		return entryPrice;
	}
	
	public final Logger getLogger() {
		return logger;
	}
	
	/**
	 * Return the order interface.
	 * 
	 * @return
	 */
	public final OrderInterface order() {
		return orderProcessor;
	}

	/**
	 * Get the parent strategy.
	 * 
	 * @return
	 */
	public PortfolioStrategy getParentStrategy() {
		if ( parentPortfolio != null ) {
			return parentPortfolio.getParentStrategy();
		} return null;
	}
	
	
	
	
	// ACCOUNTING INFORMATION //
	
	/**
	 * Returns true if and only if there is an order pending fill.
	 * 
	 */
	public final boolean isPending() {
		return orderProcessor.isPending();
	}
	
	/**
	 * Returns true if and only if there is an order pending fill,
	 * and at least one execution report has been received.
	 * 
	 * @return
	 */
	public final boolean isFilling() {
		return (isPending() && getCumulativeQty().intValue()!=0);
	}
	
	/**
	 * Returns true if and only if the current instantaneous position
	 * in this security is non-zero.
	 * 
	 * 
	 * @return
	 */
	public final boolean isOpen() {
		return (getNetQty().intValue()!=0);
	}
	
	/**
	 * Returns the quantity multiplied by the side of the trade.  If the
	 * trade is currently being filled, this is not taken into account.
	 * 
	 * @see getNetQuantity()
	 * 
	 * @return
	 */
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
	public final BigDecimal getLastPrice() {
		if (lastTradeEvent==null) return BigDecimal.ZERO;
		return lastTradeEvent.getPrice();
	}

	/**
	 * Returns the last ask event.
	 * 
	 * @return
	 */
	public final AskEvent getLastAskEvent() {
		return lastAskEvent;
	}
	
	/**
	 * Returns the last ask event price.
	 * 
	 * @return
	 */
	public final BigDecimal getLastAskPrice() {
		if ( lastAskEvent==null) {
			return BigDecimal.ZERO;
		} return lastAskEvent.getPrice();
	}
	
	/**
	 * Returns the last bid event.
	 * 
	 * @return
	 */
	public final BidEvent getLastBidEvent() {
		return lastBidEvent;
	}
	
	/**
	 * Returns the last bid event price.
	 * 
	 * @return
	 */
	public final BigDecimal getLastBidPrice() {
		return lastBidEvent.getPrice();
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
	 * TODO -- change that!
	 * 
	 * @return
	 */
	public final BigDecimal getNetQty() {
		/* 1 = position and fills are the same side; 
		 * -1 = position and fills are different side;
		 * recall that all quantities are unsigned */
		BigDecimal polarity = side.polarize(pendingSide);
		return quantity.add( polarity.multiply(cumulativeQty));
	}
	
	/**
	 * Returns the instantaneous signed position of this trade.
	 * 
	 * @see getNetQty()
	 * 
	 * @return
	 */
	public final BigDecimal getSignedNetQty() {
		return side.polarize(getNetQty());
	}
	
	public final BigDecimal getSignedCumulativeQty() {
		return side.polarize(getCumulativeQty());
	}
	
	public final BigDecimal getSignedLeavesQty() {
		return side.polarize(getLeavesQty());
	}
	
	/**
	 * Returns the profit-loss for this trade.
	 * 
	 * The profit-loss is based on the entry price given by
	 * entryPrice() and the last data event that has occurred.
	 * This, of course, implies that the trade should be in a
	 * portfolio and data should be turned on for this method
	 * to return a correct result.
	 * 
	 * @return
	 */
	public final BigDecimal getProfitLoss() {
		BigDecimal last = getLastPrice();
		if ( last == null || entryPrice == null || entryPrice.intValue()==0 ) {
			return BigDecimal.ZERO;
		}
		BigDecimal change = last.setScale(8,BigDecimal.ROUND_HALF_UP)
						.divide(entryPrice,BigDecimal.ROUND_HALF_UP)
						.subtract(BigDecimal.valueOf(1))
						.multiply(BigDecimal.valueOf(100))
						.setScale(4, BigDecimal.ROUND_HALF_UP );
	
		return side.polarize(change);
	}
	
	@Override
	public String toString() {
		return String.format("{%s:[%.2f]:%s%d%s@%.4f}",
				getSymbol(),
				getLastPrice().floatValue(),
				(getSide()==Side.BUY?"+":(getSide()==Side.SELL?"-":"")),
				getQty().intValue(),
				/* shows pending values if order is pending */
				isFilling()? "(" + getCumulativeQty() + "cq/"+ getLeavesQty() + "lq)" : "",
				getEntryPrice().floatValue());
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
	
	protected void onExecutionReport( ExecutionReport report) { }
	

	/**
	 * Processes cancel rejections.
	 * 
	 * Because cancel reject messages come in through a different path
	 * than execution reports and do not contain symbol information, they
	 * are routed through the parent PortfolioStrategy's TradeRouter.
	 * 
	 * The TradeRouter will try to search for a Trade in the Portfolio that
	 * contains a pendingOrderId that matches this reject message.  If no
	 * such Trade exists, execution never reaches this method.
	 *
	 * @param reject
	 */
	public final void acceptCancelReject( OrderCancelReject reject ) {
		logger.info(">>> " + this + ": The cancel order " + reject.getOrderID() +
				" to cancel " + reject.getOriginalOrderID() + " has been REJECTED.");
		orderProcessor.cancelFailure();
	}
	
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
			orderStatus = report.getOrderStatus();
			break;
		default:
			logger.error(
					">>> Execution report status is " +
					orderStatus + ", which is not implemented.");
			break;
		}
		
		/* finally let the subclasses do something */
		onExecutionReport(report);
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
		logger.info(">>> " + this + ": Partial fill on " + report.getOrderID() + ".");
		logger.trace(">>> " + report);
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
		
		if ( entryPrice.intValue()==0 ) {
			entryPrice = averagePrice;
		}
	
		/* unblock the out thread */
		orderProcessor.orderSuccess();
		
		/* clean up */
		OrderID orderID = report.getOrderID();
		clearPendingFields();
		
		/* reset trade if zeroed */
		if ( getNetQty().intValue()==0) {
			clearAccountingFields();
		}
		
		/* order has been filled -- execute fill policy*/
		fillPolicy.onFill(parentPortfolio.getParentStrategy(), 
				            orderID, this);		
	}
	
	/**
	 * 
	 * Internal method for committing the pending quantities to the
	 * working quantity fields.
	 * 
	 * Explanation: Incoming position is accounted for by the cumulativeQty
	 * and leavesQty fields.  Once the order is filled, leavesQty becomes 0
	 * and the cumulativeQty is incorporated into the quantity field.
	 * 
	 * @param fillReport
	 */
	private void updateQuantity( ExecutionReport fillReport ) {
		/* add the cumulativeQty to the quantity */
		quantity = getNetQty(); 
		
		/* check if we have switched sides */
		if ( quantity.compareTo(BigDecimal.ZERO)<0) {
			side = side.opposite();
			BigDecimal inv = BigDecimal.valueOf(-1L,0);
			quantity = quantity.multiply(inv);
			logger.info(">>>\t" + this + ": Position has switched sides!");
		}
	}
	
	/**
	 * 
	 * Internal method for processing cancels.
	 * 
	 * @param report
	 */
	private final void processCanceled( ExecutionReport report ) {
		
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
	
	/**
	 * 
	 * Internal method for processing rejection messages.
	 * 
	 * @param report
	 */
	private final void processRejected( ExecutionReport report ) {
		orderProcessor.orderFailure();
		rejectPolicy.onReject(parentPortfolio.getParentStrategy(),
				report.getOrderID(),this,report);
	}
	
	
	
	
	// PRIVATE METHODS //
	
	/**
	 * Initialization.
	 */
	private final void init() {
		/* clear accounting fields */
		clearAccountingFields();
	
		/* set the default policies */
		fillPolicy = FillPolicies.ON_FILL_WARN;
		orderTimeoutPolicy = OrderTimeoutPolicies.ON_TIMEOUT_WARN;
		rejectPolicy = RejectPolicies.ON_REJECT_WARN;
		orderTimeout = DEFAULT_ORDER_TIMEOUT;
		
		initOrderProcessor();
		setAccountInfo();
	}
	
	private final void initOrderProcessor() {
		orderProcessor = new OrderProcessor(this);
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
	private final void scrapeReport(ExecutionReport report) {
		orderStatus 	= report.getOrderStatus();
		cumulativeQty 	= report.getCumulativeQuantity();
		leavesQty 		= report.getLeavesQuantity();
		pendingSide 	= Side.fromMetcSide(report.getSide());
		averagePrice 	= report.getAveragePrice();
	}
	
	/**
	 * Utility method for clearing the accounting fields that
	 * keep track of the incoming fills.
	 * 
	 */
	private final void clearPendingFields() {
		leavesQty = cumulativeQty = BigDecimal.ZERO;
		pendingSide = Side.NONE;		
	}
	
	private final void clearAccountingFields() {
		quantity = leavesQty = cumulativeQty = BigDecimal.ZERO;
		entryPrice = averagePrice = BigDecimal.ZERO;
		lastTradeEvent = null;
		lastBidEvent = null;
		lastAskEvent = null;
		side = pendingSide = Side.NONE;
	}
	
	// SERIALIZATION //
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) 
	 	throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		initOrderProcessor(); 
	 }
}
