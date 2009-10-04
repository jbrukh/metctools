package org.kohera.metctools.portfolio;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.marketcetera.trade.MSymbol;

public class PortfolioImpl implements Portfolio {

	private Map<MSymbol,ITrade> trades;
	private OrderTimeoutPolicy orderTimeoutPolicy;
	private Long orderTimeout;
	
	public PortfolioImpl() {
		trades = new LinkedHashMap<MSymbol,ITrade>();
	}
	
	@Override
	public void addTrade(ITrade trade) {
		if ( orderTimeoutPolicy!=null ) {
			trade.setOrderTimeoutPolicy(orderTimeoutPolicy);
		}
		
		if ( orderTimeout != null ) {
			trade.setOrderTimeout( orderTimeout.longValue());
		}
		
		trades.put(trade.getSymbol(),trade);
	}

	@Override
	public void forEach(Action<ITrade> action) {
		for ( ITrade t : trades.values() ) {
			action.performAction(t);
		}
	}

	@Override
	public ITrade getTrade(MSymbol symbol) {
		return trades.get(symbol);
	}

	@Override
	public Collection<ITrade> getTrades() {
		return trades.values();
	}

	@Override
	public boolean hasTrade(MSymbol symbol) {
		return trades.containsKey(symbol);
	}

	@Override
	public void removeTrade(ITrade trade) {
		trades.remove(trade.getSymbol());
	}

	@Override
	public void removeTrade(MSymbol symbol) {
		trades.remove(symbol);
	}

	@Override
	public void setOrderTimeoutPolicy(final OrderTimeoutPolicy policy) {
		orderTimeoutPolicy = policy;
		forEach( new Action<ITrade>() {
			@Override
			public void performAction(ITrade trade) {
				trade.setOrderTimeoutPolicy(policy);
			}
		});
	}
	
	@Override
	public void clearOrderTimeoutPolicy() {
		orderTimeoutPolicy = null;
	}
	
	@Override
	public void clearOrderTimeout() {
		orderTimeout = null;
	}

	@Override
	public void setOrderTimeout(final long timeout) {
		orderTimeout = Long.valueOf(timeout);
		forEach( new Action<ITrade>() {
			@Override
			public void performAction(ITrade trade) {
				trade.setOrderTimeout(timeout);
			}
		});
	}

	@Override
	public BigDecimal getTotalPosition() {
		BigDecimal sum = BigDecimal.ZERO;
		for ( ITrade t : trades.values() ) {
			sum = sum.add(t.getSignedQuantity());
		}
		return sum;
	}

	@Override
	public String[] getSymbols() {
		int size = trades.size();
		if ( size < 1 ) return null;
		
		String[] symbols = new String[trades.size()];
		int i = 0;
		for ( MSymbol symbol : trades.keySet() ) {
			symbols[i] = symbol.toString();
			i++;
		}
		return symbols;
	}

	



}
