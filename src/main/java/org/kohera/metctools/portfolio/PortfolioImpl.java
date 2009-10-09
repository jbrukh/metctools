package org.kohera.metctools.portfolio;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.marketcetera.trade.MSymbol;

public class PortfolioImpl implements Portfolio {

	private Map<MSymbol,Trade> trades;
	
	private FillPolicy fillPolicy;
	private OrderTimeoutPolicy orderTimeoutPolicy;
	private Long orderTimeout;
	
	public PortfolioImpl() {
		trades = new LinkedHashMap<MSymbol,Trade>();
	}
	
	@Override
	public void addTrade(Trade trade) {
		if ( orderTimeoutPolicy!=null ) {
			trade.setOrderTimeoutPolicy(orderTimeoutPolicy);
		}
		
		if ( orderTimeout != null ) {
			trade.setOrderTimeout( orderTimeout.longValue());
		}
		
		if ( fillPolicy != null ) {
			trade.setFillPolicy(fillPolicy);
		}
		
		trades.put(trade.getSymbol(),trade);
	}

	@Override
	public void forEach(Action<Trade> action) {
		for ( Trade t : trades.values() ) {
			action.performAction(t);
		}
	}

	@Override
	public Trade getTrade(MSymbol symbol) {
		return trades.get(symbol);
	}

	@Override
	public Collection<Trade> getTrades() {
		return trades.values();
	}

	@Override
	public boolean hasTrade(MSymbol symbol) {
		return trades.containsKey(symbol);
	}

	@Override
	public void removeTrade(Trade trade) {
		trades.remove(trade.getSymbol());
	}

	@Override
	public void removeTrade(MSymbol symbol) {
		trades.remove(symbol);
	}
	
	@Override
	public void setFillPolicy(final FillPolicy policy) {
		fillPolicy = policy;
		forEach( new Action<Trade>() {
			@Override
			public void performAction(Trade trade) {
				trade.setFillPolicy(policy);
			}
		});
	}
	
	@Override
	public void clearFillPolicy() {
		fillPolicy = null;
	}

	@Override
	public void setOrderTimeoutPolicy(final OrderTimeoutPolicy policy) {
		orderTimeoutPolicy = policy;
		forEach( new Action<Trade>() {
			@Override
			public void performAction(Trade trade) {
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
		forEach( new Action<Trade>() {
			@Override
			public void performAction(Trade trade) {
				trade.setOrderTimeout(timeout);
			}
		});
	}

	@Override
	public BigDecimal getTotalPosition() {
		BigDecimal sum = BigDecimal.ZERO;
		for ( Trade t : trades.values() ) {
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
