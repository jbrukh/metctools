package org.kohera.metctools.portfolio;

import org.kohera.metctools.util.OrderBuilder;
<<<<<<< HEAD:src/main/java/org/kohera/metctools/portfolio/OrderProcessorBase.java
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;

public class OrderProcessorBase {
	
	/* for locking transactions */
	private final Object transactionLock = new Object();
	private OrderID pendingOrderId;
	private PortfolioStrategy parent;
	
	/**
	 * Get a new instance.
	 */
	public OrderProcessorBase() {
		
	}

	public OrderID getPendingOrderId() {
		return pendingOrderId;
	}
	
	public void acceptExecutionReport( ExecutionReport report ) {
		
	}
	
	protected void sendOrder( OrderSingle order, long timeout, OrderTimeoutPolicy policy ) {
		
	}

=======
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;

class OrderProcessorBase {

	private static final Object transactionLock = new Object();
	
	private BrokerID 	brokerId;
	private String 		account;
	private OrderID		pendingOrderId;
	private OrderID		cancelOrderId;
	
	private final OrderBuilder orderBuilder;
	private final TradeBase parentTrade;
	
	private Thread		outThr;
	
	/**
	 * Get a new instance.
	 * 
	 * In this case, no account information is set and must be
	 * provided using the setAccountInfo() method.
	 * 
	 */
	public OrderProcessorBase(TradeBase parent) {
		orderBuilder = new OrderBuilder();
		parentTrade = parent;
	}
	
	/**
	 * Get a new instance with account information set.
	 * 
	 * @param brokerId
	 * @param account
	 */
	public OrderProcessorBase(TradeBase parent, BrokerID brokerId, String account ) {
		this(parent);
		setAccountInfo(brokerId, account);
	}
	
	
	/**
	 * Set the account information for this OrderProcessor.
	 * 
	 * @param brokerId
	 * @param account
	 */
	public final void setAccountInfo( BrokerID brokerId, String account ) {
		this.brokerId = brokerId;
		this.account = account;
		orderBuilder.setDefaultAccount(account);
		orderBuilder.setDefaultBrokerId(brokerId);
	}
	
	// ACCOUNTING METHODS //
	
	/**
	 * Returns the pending order id, or null.
	 * 
	 */
	public final OrderID getPendingOrderId() {
		return pendingOrderId;
	}
	
	/**
	 * Returns the cancel order id, or null.
	 * 
	 * @return
	 */
	public final OrderID getCancelOrderId() {
		return cancelOrderId;
	}
	
	// PRIVATE METHODS //
	
	/**
	 * Internal method for sending orders.
	 * 
	 * This method can only be accessed by a single thread at a time.  Once
	 * it is invoked, the invoking thread will block until
	 */
	synchronized protected final void sendOrder(final OrderSingle order, 
			final long timeout, final OrderTimeoutPolicy policy,
			final boolean block) {
	
	
		/* We create a new thread that will send the order and
		 * wait for the result.
		 */
		outThr = new Thread() {
			@Override
			public void run() {
				/* make sure all fields are available */
				checkGoodToSend();
				
				/* get the parent */
				PortfolioStrategy parent = parentTrade.getParentStrategy();
				pendingOrderId = order.getOrderID();
				parent.getFramework().send(pendingOrderId);
				
				/* wait until this transaction is completed */
				synchronized(transactionLock) {
					try {
						transactionLock.wait();
					} catch (InterruptedException e) {
						/* this thread has been descheduled due to something */
					}
				}
				
				/* everything went ok */
			}
		};
		outThr.start();
		
		if (block) {
			try {
				outThr.join();
			} catch (InterruptedException e) {
				//
			}
		}
	}
	
	protected final void cancelOrder() {
	}
	
	/**
	 * Will unlock the outgoing thread, which will complete.
	 */
	protected final void success() {
		synchronized(transactionLock) {
			transactionLock.notify();
		}
	}
	
	protected final void disrupt() {
		outThr.interrupt();
	}
	
	
	/**
	 * Checks that account info is in place and that there
	 * is a parent strategy available to send the order.
	 * 
	 */
	private void checkGoodToSend() {
		if ( brokerId == null || account == null ) {
			throw new RuntimeException(">>> " + parentTrade + 
					": Cannot send order (no account information).");
		}
		else if ( parentTrade.getParentStrategy() == null ) {
			throw new RuntimeException(">>> " + parentTrade + 
			": Cannot send order (no parent strategy available).");

		}
	}
	
>>>>>>> master:src/main/java/org/kohera/metctools/portfolio/OrderProcessorBase.java
}
