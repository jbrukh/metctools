package org.kohera.metctools;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.kohera.metctools.delegate.AskDelegate;
import org.kohera.metctools.delegate.BidDelegate;
import org.kohera.metctools.delegate.BrokerStatusDelegate;
import org.kohera.metctools.delegate.CallbackDelegate;
import org.kohera.metctools.delegate.Delegator;
import org.kohera.metctools.delegate.EventDelegate;
import org.kohera.metctools.delegate.ExecutionReportDelegate;
import org.kohera.metctools.delegate.OrderCancelRejectDelegate;
import org.kohera.metctools.delegate.OtherDelegate;
import org.kohera.metctools.delegate.ServerStatusDelegate;
import org.kohera.metctools.delegate.StartDelegate;
import org.kohera.metctools.delegate.StopDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.strategy.RunningStrategy;
import org.marketcetera.strategy.java.Strategy;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.OrderCancel;
import org.marketcetera.trade.OrderCancelReject;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.OrderReplace;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.client.BrokerStatusListener;
import org.marketcetera.client.Client;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.client.ClientManager;
import org.marketcetera.client.ConnectionException;
import org.marketcetera.client.ServerStatusListener;
import org.marketcetera.client.brokers.BrokerStatus;
import org.marketcetera.core.position.PositionKey;
import org.marketcetera.event.AskEvent;
import org.marketcetera.event.BidEvent;
import org.marketcetera.event.EventBase;
import org.marketcetera.event.TradeEvent;

import quickfix.Message;

/**
 * Basic org.marketcetera.strategy.java.Strategy wrapper that implements:
 * 
 *   - BrokerStatus and ServerStatus event handling
 *   - Delegation of events.
 * 
 * How to use:
 * 
 *  Subclass DelegatorStrategy and use addDelegate() to set your delegate
 *  objects.
 *   
 * @author Jake Brukhman
 * @since 1.0.0
 *
 */
public class DelegatorStrategy extends Strategy
	implements RunningStrategy {

	/* fields */
	private Client 		client;
	private Delegator 	delegator;
	private Framework 	framework;
	
	/* status listeners*/
	private final BrokerStatusListener BROKER_STATUS_LISTENER;
	private final ServerStatusListener SERVER_STATUS_LISTENER;
	
	
	/**
	 * Inner class that makes exposes action methods from the Strategy class
	 * to outside delegates.
	 *
	 * TODO: Implement full API.
	 */
	public class Framework {
	
		public String getParameter(String key) {
			return DelegatorStrategy.this.getParameter(key);
		}
		
		public String getProperty(String key) {
			return DelegatorStrategy.getProperty(key);
		}
		
		public ExecutionReport[] getExecutionReports(OrderID orderId) {
			return DelegatorStrategy.this.getExecutionReports(orderId);
		}
		
		public BrokerStatus[] getBrokers() {
			return DelegatorStrategy.this.getBrokers();
		}
		
		public BigDecimal getPositionAsOf(Date date, String symbol) {
			return DelegatorStrategy.this.getPositionAsOf(date, symbol);
		}
		
		public void setProperty(String key, String value) {
			DelegatorStrategy.setProperty(key,value);
		}
		
		public void requestCallbackAfter(long delay, Object data) {
			DelegatorStrategy.this.requestCallbackAfter(delay, data);
		}
		
		public void requestCallbackAt(Date date, Object data) {
			DelegatorStrategy.this.requestCallbackAt(date, data);
		}
		
		public int requestMarketData( MarketDataRequest request) {
			return DelegatorStrategy.this.requestMarketData(request);
		}
		
		public void cancelDataRequest( int id ) {
			DelegatorStrategy.this.cancelDataRequest(id);
		}
		
		public void cancelAllDataRequests() {
			DelegatorStrategy.this.cancelAllDataRequests();
		}
		
		public int requestCEPData(String[] statements, String source) {
			return DelegatorStrategy.this.requestCEPData(statements, source);
		}
		
		public int requestProcessedMarketData( MarketDataRequest request, String[] statements, String cepSource) {
			return DelegatorStrategy.this.requestProcessedMarketData(request, statements, cepSource);
		}
		
		public void suggestTrade(OrderSingle order, BigDecimal score, String id) {
			DelegatorStrategy.this.suggestTrade(order, score, id);
		}
		
		public boolean send(Object object) {
			return DelegatorStrategy.this.send(object);
		}
		
		public OrderReplace cancelReplace( OrderID orderId, OrderSingle order, boolean sendOrder) {
			return DelegatorStrategy.this.cancelReplace(orderId, order, sendOrder);
		}
		
		public OrderCancel cancelOrder( OrderID orderId, boolean sendOrder) {
			return DelegatorStrategy.this.cancelOrder(orderId, sendOrder);
		}
		
		public int cancelAllOrders() {
			return DelegatorStrategy.this.cancelAllOrders();
		}
		
		public void sendMessage(Message message, BrokerID brokerId) {
			DelegatorStrategy.this.sendMessage(message,brokerId);
		}
		
		public void sendEventToCEP(EventBase event, String source) {
			DelegatorStrategy.this.sendEventToCEP(event, source);
		}
		
		public void sendEvent( EventBase event ) {
			DelegatorStrategy.this.sendEvent(event);
		}
		
		public void notifyLow( String subject, String body ) {
			DelegatorStrategy.this.notifyLow(subject, body);
		}

		public void notifyHigh( String subject, String body ) {
			DelegatorStrategy.this.notifyHigh(subject, body);
		}
		
		public void notifyMedium( String subject, String body ) {
			DelegatorStrategy.this.notifyMedium(subject, body);
		}
		
		public void debug( String message ) {
			DelegatorStrategy.this.debug(message);
		}

		public void info( String message ) {
			DelegatorStrategy.this.info(message);
		}

		public void warn( String message ) {
			DelegatorStrategy.this.warn(message);
		}

		public void error( String message ) {
			DelegatorStrategy.this.error(message);
		}
	}

	/**
	 * Constructor.
	 * @throws ClientInitException 
	 * 
	 * @throws ClientInitException 
	 */
	public DelegatorStrategy() throws ClientInitException {
		/* call the superclass constructor */
		super();

		/* delegates */
		delegator = new Delegator(this);
		framework = new Framework();
		
		/* event listening init */
		client = ClientManager.getInstance();
		BROKER_STATUS_LISTENER = new BrokerStatusListener() {
			@Override
			public void receiveBrokerStatus(BrokerStatus status) {
				delegator.delegate(BrokerStatusDelegate.class,status);
			}
		};
		SERVER_STATUS_LISTENER = new ServerStatusListener() {
			@Override
			public void receiveServerStatus(boolean status) {
				delegator.delegate(ServerStatusDelegate.class,status);
			}
		};	
		client.addBrokerStatusListener( BROKER_STATUS_LISTENER );
		client.addServerStatusListener( SERVER_STATUS_LISTENER );		
	}

	/**
	 * Methods
	 */
	
	/**
	 * Add a delegate to this strategy.  The delegate must
	 * implement one or more extensions of the EventDelegate interface.
	 */
	public void addDelegate( EventDelegate delegate ) {
		delegator.addDelegate(delegate);
	}
	
	/**
	 * Remove a a delegate from this strategy.
	 * @param delegate
	 */
	public void removeDelegate(EventDelegate delegate) {
		delegator.removeDelegate(delegate);
	}
	
	/**
	 * Returns a positions map from the client.
	 * 
	 * @param date
	 * @return
	 */
	public Map<PositionKey,BigDecimal> getPositions(Date date) {
		try {
			return client.getPositionsAsOf(date);
		} catch (ConnectionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the BrokerStatus of a particular broker.
	 * 
	 * @param brokerId
	 * @return
	 */
	public BrokerStatus getBrokerStatus( BrokerID brokerId ) {
		for ( BrokerStatus status : getBrokers() ) {
			if ( brokerId.equals(status.getId()) ) {
				return status;
			}
		} return null;
	}
	
	/**
	 * Returns the relay, so that action methods of this Strategy object can be called.
	 * @return
	 */
	public Framework getFramework() {
		return framework;
	}
	
	/**
	 * Events.  All incoming events are delegated to the appropriate
	 * EventDelegates that have been set by addDelegate().
	 */
	
	public final void onAsk( AskEvent event ) {
		delegator.delegate(AskDelegate.class,event);
	}

	public final void onBid( BidEvent event ) {
		delegator.delegate(BidDelegate.class,event);
	}
	
	public final void onTrade( TradeEvent event ) {
		delegator.delegate(TradeDelegate.class,event);
	}
	
	public final void onExecutionReport( ExecutionReport message ) {
		delegator.delegate(ExecutionReportDelegate.class,message);
	}
	
	public final void onCancelReject( OrderCancelReject message ) {
		delegator.delegate(OrderCancelRejectDelegate.class,message);
	}

	public final void onOther( Object message ) {
		delegator.delegate(OtherDelegate.class, message);
	}
	
	public final void onCallback( Object message ) {
		delegator.delegate(CallbackDelegate.class, message);		
	}
	
	public final void onStart() {
		delegator.delegate(StartDelegate.class, null);
	}
	
	public final void onStop() {
		client.removeBrokerStatusListener(BROKER_STATUS_LISTENER);
		client.removeServerStatusListener(SERVER_STATUS_LISTENER);
		delegator.delegate(StopDelegate.class, null);
	}

}
