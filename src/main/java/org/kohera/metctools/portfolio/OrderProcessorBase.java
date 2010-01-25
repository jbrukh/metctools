package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.util.OrderBuilder;
import org.marketcetera.trade.OrderCancel;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.BrokerID;

class OrderProcessorBase {

	private static final Object transactionLock = new Object();

	private BrokerID 	brokerId;
	private String 		account;
	private OrderID		pendingOrderId;
	private OrderID		cancelOrderId;

	protected final OrderBuilder orderBuilder;
	protected final Trade parentTrade;

	private Thread		outThr;

	/* logging */
	private final static Logger logger = 
		Logger.getLogger(OrderProcessorBase.class);

	/**
	 * Get a new instance.
	 * 
	 * In this case, no account information is set and must be
	 * provided using the setAccountInfo() method.
	 * 
	 */
	public OrderProcessorBase(Trade parent) {
		orderBuilder = new OrderBuilder();
		parentTrade = parent;
	}

	/**
	 * Get a new instance with account information set.
	 * 
	 * @param brokerId
	 * @param account
	 */
	public OrderProcessorBase(Trade parent, BrokerID brokerId, String account ) {
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

	/**
	 * Get the order builder.
	 * 
	 * @return
	 */
	public final OrderBuilder getOrderBuilder() {
		return orderBuilder;
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

	public final boolean isPending() {
		return pendingOrderId!=null;
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

		logger.trace("--- Starting the out thread...");

		/* We create a new thread that will send the order and
		 * wait for the result.
		 */
		outThr = new Thread() {
			@Override
			public void run() {
				synchronized(transactionLock) {
					/* make sure all fields are available */
					checkGoodToSend();

					/* get the parent */
					PortfolioStrategy parent = parentTrade.getParentStrategy();
					pendingOrderId = order.getOrderID();
					parent.getFramework().send(order);

					/* logging */
					logger.trace("--- Sent the order, waiting.");

					/* wait until this transaction is completed */
					try {
						transactionLock.wait(timeout);

						/* if it is still pending... */
						if ( isPending() && policy != null ) {
							policy.onOrderTimeout(
									parentTrade.getParentStrategy(), 
									pendingOrderId, 
									timeout, 
									parentTrade);
						}

					} catch (InterruptedException e) {
						/* this thread has been descheduled due to something */
						logger.trace("--- Transaction has been disrupted...");
					}
				}

				/* everything went ok */
				logger.trace("--- Transaction has completed.");
			}
		};
		outThr.start();

		if (block) {
			logger.trace("--- Blocking until out thread completes...");
			try {
				outThr.join();
			} catch (InterruptedException e) {
				//
			}
		}
	}

	protected final void cancelOrder(final boolean block) {
		if ( !isPending() ) {
			logger.warn(">>> " + parentTrade + ": There is no pending order to cancel.");
		}

		OrderCancel orderCancel = parentTrade.getParentStrategy().getFramework()
		.cancelOrder(pendingOrderId, true);
		cancelOrderId = orderCancel.getOrderID();
		
		logger.debug(">>> Sending cancel order " + cancelOrderId + " to cancel " + pendingOrderId );

		if (block) {
			synchronized(transactionLock) {
				try {
					transactionLock.wait();
				} catch (InterruptedException e) { }
			}
		}
	}

	/**
	 * Will unlock the outgoing thread, which will complete.
	 */
	public final void orderSuccess() {
		pendingOrderId = null;

		if ( cancelOrderId!=null) {
			logger.warn(">>> Failed to execute cancel order " + cancelOrderId );
			cancelOrderId = null;
		}

		synchronized(transactionLock) {
			transactionLock.notify();
		}
	}

	public final void cancelSuccess() {
		cancelOrderId = pendingOrderId = null;
		synchronized(transactionLock) {
			transactionLock.notify();
		}
	}

	public final void disrupt() {
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

}
