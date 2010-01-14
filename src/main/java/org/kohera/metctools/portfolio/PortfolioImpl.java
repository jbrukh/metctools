package org.kohera.metctools.portfolio;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.MSymbol;

import com.sun.net.ssl.internal.ssl.Debug;

final class PortfolioImpl implements Portfolio {

	/* trades */
	private Map<MSymbol,Trade>	trades;
	private PortfolioStrategy	parentStrategy;
	private BrokerID			brokerId;
	private String				account;
	
	/* default policies */
	private FillPolicy 			fillPolicy;
	private OrderTimeoutPolicy 	orderTimeoutPolicy;
	private Long 				orderTimeout;
	/*
	 * TODO -- add policies for Reject, CancelReject, etc.
	 */

	/* logging */
	private final static Logger logger = Logger.getLogger(PortfolioImpl.class);
	
	/**
	 * Create a new PortfolioImpl instance.
	 * 
	 */
	public PortfolioImpl(PortfolioStrategy parent) {
		trades = new LinkedHashMap<MSymbol,Trade>();
		parentStrategy = parent;
	}
	
	@Override
	public void setAccountCredentials(BrokerID brokerId, String account) {
		this.brokerId = brokerId;
		this.account = account;
	}
	
	@Override
	public BrokerID getBrokerID() {
		return brokerId;
	}
	
	@Override
	public String getAccount() {
		return account;
	}
	
	@Override
	public PortfolioStrategy getParentStrategy() {
		return parentStrategy;
	}
	
	@Override
	public void addTrade(Trade trade) {
		
		if ( trades.containsKey(trade.getSymbol())) {
			logger.error(">>> Trade for symbol " + 
					trade.getSymbol() + " already exists.");
			return;
		}
		
		/* set the policies for the trades from the portfolio,
		 * unless they are already customized
		 */
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
		
		/* logging */
		logger.trace(">>> Added trade to portfolio: " + trade);
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
		
		/* logging */
		logger.trace(">>> Removed, if it existed, from portfolio the trade: " + trade);
	}

	@Override
	public void removeTrade(MSymbol symbol) {
		trades.remove(symbol);

		/* logging */
		logger.trace(">>> Removed, if it existed, from portfolio the trade: " + symbol);
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

	@Override
	public int size() {
		return trades.size();
	}

	@Override
	public Trade createTrade(String symbol) {
		if (trades.containsKey(symbol) ) return trades.get(symbol);
		
		Trade trade =
			new Trade(new MSymbol(symbol),this);
		addTrade(trade);
		return trade;
	}



}
