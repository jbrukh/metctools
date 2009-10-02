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
import org.marketcetera.trade.OrderCancelReject;
import org.marketcetera.trade.OrderID;
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

public class AdvancedStrategy extends Strategy
	implements RunningStrategy {

	/**
	 * FIELDS
	 */
	private Client client;
	private Delegator delegator;
	private Relay relay;

	private final BrokerStatusListener BROKER_STATUS_LISTENER;
	private final ServerStatusListener SERVER_STATUS_LISTENER;
	/**/

	
	/**
	 * Inner class that makes exposes action methods from the Strategy class
	 * to outside delegates.
	 * 
	 * TODO: Implement a complete listing of actions.
	 */
	public class Relay {
	
		public void setProperty(String key, String value) {
			AdvancedStrategy.setProperty(key,value);
		}
		
		public void requestCallbackAfter(long delay, Object data) {
			AdvancedStrategy.this.requestCallbackAfter(delay, data);
		}
		
		public void requestCallbackAt(Date date, Object data) {
			AdvancedStrategy.this.requestCallbackAt(date, data);
		}
		
		public int requestMarketData( MarketDataRequest request) {
			return AdvancedStrategy.this.requestMarketData(request);
		}
		
		public void cancelDataRequest( int id ) {
			AdvancedStrategy.this.cancelDataRequest(id);
		}
		
		public void cancelAllDataRequests() {
			AdvancedStrategy.this.cancelAllDataRequests();
		}
		
		public int requestCEPData(String[] statements, String source) {
			return AdvancedStrategy.this.requestCEPData(statements, source);
		}
		
		public int requestProcessedMarketData( String request, String[] statements, String cepSource) {
			return AdvancedStrategy.this.requestProcessedMarketData(request, statements, cepSource);
		}
		
		public void suggestTrade(OrderSingle order, BigDecimal score, String id) {
			AdvancedStrategy.this.suggestTrade(order, score, id);
		}
		
		/** BEGIN -- FOR METC VERSION 1.5 **/
		
		public OrderID sendOrder(OrderSingle order) {
			return AdvancedStrategy.this.sendOrder(order);
		}
		
		public boolean cancelOrder( OrderID orderId ) {
			return AdvancedStrategy.this.cancelOrder(orderId);
		}
		
		public OrderID cancelReplace( OrderID orderId, OrderSingle order) {
			return AdvancedStrategy.this.cancelReplace(orderId, order);
		}
		/** END -- FOR METC VERSION 1.5 **/
		
		
		/** BEGIN -- FOR METC VERSION 1.6 **/
		/*public void send(Object object) {
			AdvancedStrategy.this.send(object);
		}
		
		public OrderReplace cancelReplace( OrderID orderId, OrderSingle order, boolean sendOrder) {
			return AdvancedStrategy.this.cancelReplace(orderId, order, sendOrder);
		}
		
		public OrderCancel cancelOrder( OrderID orderId, boolean sendOrder) {
			return AdvancedStrategy.this.cancelOrder(orderId, sendOrder);
		}*/
		/** END -- FOR METC VERSION 1.6 **/
		
		public int cancelAllOrders() {
			return AdvancedStrategy.this.cancelAllOrders();
		}
		
		public void sendMessage(Message message, BrokerID brokerId) {
			AdvancedStrategy.this.sendMessage(message,brokerId);
		}
		
		public void sendEventToCEP(EventBase event, String source) {
			AdvancedStrategy.this.sendEventToCEP(event, source);
		}
		
		public void sendEvent( EventBase event ) {
			AdvancedStrategy.this.sendEvent(event);
		}
		
		public void notifyLow( String subject, String body ) {
			AdvancedStrategy.this.notifyLow(subject, body);
		}

		public void notifyHigh( String subject, String body ) {
			AdvancedStrategy.this.notifyHigh(subject, body);
		}
		
		public void notifyMedium( String subject, String body ) {
			AdvancedStrategy.this.notifyMedium(subject, body);
		}
		
		public void debug( String message ) {
			AdvancedStrategy.this.debug(message);
		}

		public void info( String message ) {
			AdvancedStrategy.this.info(message);
		}

		public void warn( String message ) {
			AdvancedStrategy.this.warn(message);
		}

		public void error( String message ) {
			AdvancedStrategy.this.error(message);
		}
	}
	


	/**
	 * Constructor.
	 * @throws ClientInitException 
	 */
	public AdvancedStrategy() throws ClientInitException {
		/* call the superclass constructor */
		super();

		/* delegates */
		delegator = new Delegator(this);
		relay = new Relay();
		
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
	
	public void addDelegate( EventDelegate delegate ) {
		delegator.addDelegate(delegate);
	}
	
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
		} return null;
	}
	
	/**
	 * Returns the BrokerStatus of a particular broker.
	 * 
	 * @param brokerId
	 * @return
	 */
	public BrokerStatus getBrokerStatus( BrokerID brokerId ) {
		for ( BrokerStatus status : getBrokers() ) {
			if ( status.getId()==brokerId ) {
				return status;
			}
		} return null;
	}
	
	/**
	 * Returns the relay, so that action methods of this Strategy object can be called.
	 * @return
	 */
	public Relay getRelay() {
		return relay;
	}

	
	/**
	 * Events
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
