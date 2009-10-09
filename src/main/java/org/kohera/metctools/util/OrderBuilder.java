package org.kohera.metctools.util;

import java.math.BigDecimal;

import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.Factory;
import org.marketcetera.trade.MSymbol;
import org.marketcetera.trade.OrderSingle;
import org.marketcetera.trade.OrderType;
import org.marketcetera.trade.Side;
import org.marketcetera.trade.TimeInForce;

/**
 * TODO: Make method listing more complete.
 * 
 * @author Administrator
 *
 */
public class OrderBuilder {
	
	private OrderSingle order;
	
	private BrokerID defaultBrokerId;
	private String defaultAccount;
	private TimeInForce defaultTimeInForce;
	
	public OrderBuilder() {
		newOrder();
	}
	
	public OrderBuilder(BrokerID brokerId, String account, TimeInForce tif ) {
		this.defaultBrokerId = brokerId;
		this.defaultAccount = account;
		this.defaultTimeInForce = tif;
		newOrder();
	}
	
	public OrderBuilder(BrokerID brokerId, String account) {
		this(brokerId,account,TimeInForce.Day);
	}
		
	
	public OrderBuilder(BrokerID brokerId) {
		this(brokerId,null,TimeInForce.Day);
	}
	
	/**
	 * Utility method for generating the base order with defaults.
	 */
	public OrderBuilder newOrder() {
		order = Factory.getInstance().createOrderSingle();
		
		if ( defaultAccount != null ) {
			withAccount(defaultAccount);
		}
		if ( defaultBrokerId != null ) {
			withBrokerID(defaultBrokerId);
		}
		if ( defaultTimeInForce != null ) {
			withTimeInForce(defaultTimeInForce);
		}
		
		return this;
	}
	
	/**
	 * Returns the order that has been built.
	 * 
	 * @return
	 */
	public OrderSingle getOrder() {
		OrderSingle done = order;
		newOrder();
		return done;
	}
	
	
	public OrderBuilder withAccount( String account ) {
		order.setAccount(account);
		return this;
	}

	public OrderBuilder withBrokerID( BrokerID brokerId ) {
		order.setBrokerID(brokerId);
		return this;
	}
	
	public OrderBuilder withOrderType( OrderType type ) {
		order.setOrderType(type);
		return this;
	}
	
	public OrderBuilder withQuantity( BigDecimal qty ) {
		order.setQuantity(qty);
		return this;
	}
	
	public OrderBuilder withQuantity( int qty ) {
		return this.withQuantity(BigDecimal.valueOf(qty, 0));
	}
	
	public OrderBuilder withSide( Side side ) {
		order.setSide(side);
		return this;
	}
	
	public OrderBuilder withSide( org.kohera.metctools.portfolio.Side side ) {
		return this.withSide( side.toMetcSide() );
	}

	public OrderBuilder withSymbol( MSymbol symbol ) {
		order.setSymbol(symbol);
		return this;
	}
	
	public OrderBuilder withSymbol( String symbol ) {
		return this.withSymbol( new MSymbol(symbol) );
	}
	
	public OrderBuilder withTimeInForce( TimeInForce tif ) {
		order.setTimeInForce(tif);
		return this;
	}
	
	public OrderBuilder makeMarket(BrokerID brokerId, String account, MSymbol symbol, BigDecimal qty, Side side ) {
		return makeMarket(symbol,qty,side).withBrokerID(brokerId).withAccount(account);
	}
	
	public OrderBuilder makeMarket(MSymbol symbol, BigDecimal qty, Side side) {
		return this
			.withSymbol(symbol)
			.withQuantity(qty)
			.withSide(side)
			.withOrderType(OrderType.Market);
	}

}
