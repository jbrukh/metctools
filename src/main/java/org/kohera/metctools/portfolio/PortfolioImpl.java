package org.kohera.metctools.portfolio;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.marketcetera.trade.BrokerID;


final class PortfolioImpl implements Portfolio {

	/**
	 *  For serialization. 
	 */
	private static final long serialVersionUID = -7913728982753589441L;
	
	/* trades */
	transient private PortfolioStrategy	parentStrategy;
	private Map<String,Trade>	trades;
	private BrokerID			brokerId;
	private String				account;
	
	/* default policies */
	private FillPolicy 			fillPolicy;
	private OrderTimeoutPolicy 	orderTimeoutPolicy;
	private RejectPolicy 		rejectPolicy;
	private Long 				orderTimeout;
	

	/* logging */
	private final static Logger logger = 
		Logger.getLogger(PortfolioImpl.class);
	
	/**
	 * Create a new PortfolioImpl instance.
	 * 
	 */
	public PortfolioImpl(PortfolioStrategy parent) {
		trades = new LinkedHashMap<String,Trade>();
		parentStrategy = parent;
	}
	
	@Override
	public void setAccountInfo(BrokerID brokerId, String account) {
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
	public boolean isAccountInfoSet() {
		return (brokerId!=null && account!=null);
	}
	
	@Override
	public PortfolioStrategy getParentStrategy() {
		return parentStrategy;
	}
	
	@Override
	public void addTrade(Trade trade) {
		
		if ( trade == null ) return;
		String symbol = trade.getSymbol();
		
		/* if the trade exists, but is not open, you can
		 * replace it.  Otherwise, there is an error.
		 */
		if ( trades.containsKey(symbol) ) {
			if ( trades.get(symbol).isOpen() ) {
				logger.error(">>> Trade for symbol " + 
						symbol + " already exists and is open.");
				return;
			}
			logger.warn(">>> Removing current zero-position trade for " + 
					symbol + " and replacing...");
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
		
		if ( rejectPolicy != null ) {
			trade.setRejectPolicy(rejectPolicy);
		}
		
		trade.setParentPortfolio(this);
		trades.put(symbol,trade);
		
		/* logging */
		logger.trace(">>> Added trade to portfolio: " + trade);
	}

	@Override
	public void forEach(Action action) {
		for ( Trade trade : trades.values() ) {
			action.performAction(trade);
		}
	}

	@Override
	public Trade getTrade(String symbol) {
		return createTrade(symbol);
	}
	
	@Override
	public Collection<Trade> getTrades() {
		return trades.values();
	}

	@Override
	public boolean hasTrade(String symbol) {
		return trades.containsKey(symbol);
	}

	@Override
	public void removeTrade(Trade trade) {
		
		/* make sure that the position is zeroed */
		if ( trade.getNetQty().compareTo(BigDecimal.ZERO) != 0 ) {
			throw new RuntimeException(">>> Cannot remove a trade that has a non-zero position.  First, liquidate this trade.");
		}
		forcefullyRemoveTrade(trade);
	}
	
	@Override
	public void forcefullyRemoveTrade(Trade trade) {
		trade.unsetParentPortfolio();
		trades.remove(trade.getSymbol());
		/* logging */
		logger.trace(">>> Removed, if it existed, from portfolio the trade: " + trade);
	}

	@Override
	public void removeTrade(String symbol) {
		Trade trade = trades.get(symbol);
		if ( trade!=null ) {
			removeTrade(trade);
		} else {
			logger.error(">>> No trade exists for symbol " + symbol + " (not removed).");
		}
	}
	
	@Override
	public void setFillPolicy(final FillPolicy policy) {
		fillPolicy = policy;
		forEach( new Action() {
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
	public void setRejectPolicy(final RejectPolicy policy) {
		rejectPolicy = policy;
		forEach( new Action() {
			@Override
			public void performAction(Trade trade) {
				trade.setRejectPolicy(policy);
			}
		});
	}
	
	@Override
	public void clearRejectPolicy() {
		rejectPolicy = null;
	}
	
	@Override
	public void setOrderTimeoutPolicy(final OrderTimeoutPolicy policy) {
		orderTimeoutPolicy = policy;
		forEach( new Action() {
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
		forEach( new Action() {
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
			sum = sum.add(t.getSignedQty());
		}
		return sum;
	}

	@Override
	public Collection<String> getSymbols() {
		return trades.keySet();
	}

	@Override
	public int size() {
		return trades.size();
	}

	@Override
	public Trade createTrade(String symbol) {
		if (trades.containsKey(symbol) ) return trades.get(symbol);
		
		Trade trade =
			new Trade(symbol,this);
		addTrade(trade);
		return trade;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("\n--- START PORTFOLIO ---\n");
		for ( Trade t : trades.values() ) {
			str.append(t.toString());
			str.append('\n');
		}
		str.append("--- END PORTFOLIO -----\n");
		return str.toString();
	}

	@Override
	public void setParentStrategy(PortfolioStrategy parent) {
		parentStrategy = parent;
	}

	@Override
	public void wipe() {
		trades.clear();
	}
}
