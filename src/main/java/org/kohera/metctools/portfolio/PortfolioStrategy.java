package org.kohera.metctools.portfolio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.kohera.metctools.delegate.AskDelegate;
import org.kohera.metctools.delegate.BidDelegate;
import org.kohera.metctools.delegate.ExecutionReportDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.core.position.PositionKey;
import org.marketcetera.event.AskEvent;
import org.marketcetera.event.BidEvent;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.trade.ExecutionReport;


/**
 * 
 * A more complex wrapper that has Portfolio functionality.
 * 
 * Design:
 * 
 *   The user can use this class just like a DelegatorStrategy; he may
 *   also create a number of Trade objects and place them in the Portfolio
 *   by calling getPortfolio().addTrade().
 *   
 *   Once a trade is in the Portfolio, market data for that symbol will be
 *   turned on if startMarketData() is called (market data provider must be
 *   set using setMarketDataProvider() for this to work.)  ExecutionReports and
 *   TradeEvents are routed automatically to the Trade object and its state
 *   is automatically updated.  A Trade object will also obey order timeouts,
 *   order timeout policies, and fill polices when they are set for the
 *   entire portfolio.
 * 
 * @author Jake Brukhman
 *
 */
public abstract class PortfolioStrategy extends DelegatorStrategy {

	/* logging */
	private final static Logger logger = Logger.getLogger(PortfolioStrategy.class);
	
	/**
	 * Internal class that routes execution reports to the appropriate
	 * trades in the portfolio.
	 * 
	 * @author Administrator
	 *
	 */
	class TradeRouter implements ExecutionReportDelegate, TradeDelegate, 
		BidDelegate, AskDelegate {
		
		@Override
		public void onExecutionReport(DelegatorStrategy sender,
				ExecutionReport report) {
			String symbol = report.getSymbol().toString();
			if ( portfolio.hasTrade(symbol) ) {
				portfolio.getTrade(symbol)
					.acceptExecutionReport(PortfolioStrategy.this, report);
			} else {
				// TODO: clean up
				logger.warn(">>> Received external execution report. (Ignoring.)");
				logger.trace(">>> Report: " + report );
			}
		}

		@Override
		public void onTrade(DelegatorStrategy sender, TradeEvent tradeEvent) {
			String symbol = tradeEvent.getSymbol().toString();
			if ( portfolio.hasTrade(symbol) ) {
				portfolio.getTrade(symbol)
					.acceptTradeEvent(tradeEvent);
			} else {
				// TODO: clean up
				logger.warn(">>> Received external trade event. (Ignoring.)");
				logger.trace(">>> ...for symbol " + symbol + ".");
			}
		}

		@Override
		public void onBid(DelegatorStrategy sender, BidEvent bidEvent) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onAsk(DelegatorStrategy sender, AskEvent askEvent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/* fields */
	private Portfolio portfolio;
	private Integer dataRequestId;
	private String dataProvider;
		
	/**
	 * Create a new instance of a PortfolioStrategy.
	 * 
	 * @param dataProvider
	 * @throws ClientInitException
	 */
	public PortfolioStrategy() throws ClientInitException {
		super();
		
		portfolio = new PortfolioImpl(this);
		dataRequestId = null;
		
		/* route execution reports and trades (ticks) to the portfolio */
		addDelegate( new TradeRouter() );
	}

	/**
	 * Get the data provider.
	 * 
	 * @return
	 */
	public String getDataProvider() {
		return dataProvider;
	}
	
	/**
	 * Set the data provider.
	 * 
	 * @param dataProvider
	 */
	public void setDataProvider(String dataProvider) {
		this.dataProvider = dataProvider;
	}
	
	/**
	 * Get the portfolio.
	 * 
	 * @return
	 */
	public Portfolio getPortfolio() {
		return portfolio;
	}
	
	public void startMarketData() {		
		if ( !dataProviderIsSet() ) {
			throw new RuntimeException(Messages.MSG_DATA_PROVIDER_NOT_SET);
		}
		
		Collection<String> s = portfolio.getSymbols();
		String[] symbols = (String[])s.toArray(new String[s.size()]);
		
		if ( symbols.length < 1 ) {
			logger.warn(">>> Skipping market data (no symbols in portfolio).");
			return;
		}
		
		logger.info(">>> Starting market data...");
		MarketDataRequest request = MarketDataRequest
										.newRequest()
										.withSymbols(symbols)
										.fromProvider(dataProvider)
										.withContent("LATEST_TICK");
		dataRequestId = requestMarketData(request);
		logger.info(">>> Market data id: " + dataRequestId );
	}

	public void stopMarketData() {
		if (dataRequestId!=null && dataRequestId>0) {
			cancelDataRequest(dataRequestId);
		} dataRequestId = null;
	}

	private boolean dataProviderIsSet() {
		return dataProvider!=null;
	}
	
	/**
	 * Serializes the current portfolio in the specified file.
	 * 
	 * @param file
	 */
	public void serializePortfolio( String file ) {
		
		/* check that portfolio exists */
		if ( portfolio==null ) {
			logger.error(">>> Cannot serialize portfolio because it is undefined.");
			return;
		}
		
		/* output objects */
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);
			out.writeObject(portfolio);
		} catch (IOException e ) {
			e.printStackTrace();
			logger.error(">>> Could not serialize portfolio. (" + e.getMessage() + ")");
			return;
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		logger.info(">>> Serialized portfolio to " + file);
		logger.debug(portfolio.toString());
	}

	/**
	 * Deserializes the portfolio contained in the specified file,
	 * and installs this portfolio as this Strategy's portfolio.
	 * 
	 * One may not replace a non-empty portfolio with a serialized
	 * one, and therefore must liquidate it before a new portfolio
	 * is dynamically loaded.
	 * 
	 * @param file
	 */
	public void deserializePortfolio( String file ) {
		
		/* make sure the portfolio is empty or has no open positions */
		if ( portfolio.size() != 0 || 
				portfolio.getTotalPosition() != BigDecimal.ZERO ) {
			logger.error(">>> Cannot load portfolio from file because " +
					"the active portfolio is non-empty.");
			return;
		}
		
		/* input objects */
		FileInputStream fin = null;
		ObjectInputStream in = null;
		
		try {
			fin = new FileInputStream(file);
			in = new ObjectInputStream(fin);
			
			/* get the object */
			portfolio = (PortfolioImpl)in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			logger.error(">>> Serialization error. (" + e.getMessage() + ").");
			return;
		} catch (IOException e ) {
			logger.error(">>> Could not serialize portfolio. (" + e.getMessage() + ")");
			return;
		} finally {
			try {
				in.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		/* 
		 * rewire the portfolio 
		 */
		
		/* set the parent strategy */
		portfolio.setParentStrategy(this);
		/* reset the OrderProcessors for each trade
		 * (i.e. all memory of pending trades and timeouts
		 * is reset)
		 */
		portfolio.forEach( new Action<Trade>() {
			@Override
			public void performAction(Trade trade) {
				trade.setParentPortfolio(portfolio);
			}
		});
		
		logger.info(">>> Deserialized portfolio from " + file + ".");
		logger.debug(portfolio.toString());
	}

	/**
	 * This is a convenience method that syncs the portfolio to the
	 * actual ORS positions on a best-efforts basis.
	 * 
	 * Because ORS does not provide entry price or profit-loss
	 * information, it is impossible to reconstruct a Trade exclusively
	 * from ORS data.  Therefore, this method does not attempt to do so.
	 * 
	 * Instead, this method performs two operations:
	 * 
	 * 1. If a position exists in both the portfolio and the ORS,
	 * it simply updates the position size (and nothing else) in the
	 * portfolio.
	 * 
	 * 2. On the other hand, if the portfolio contains a position that
	 * does not exist in the ORS, this position is removed from the
	 * portfolio.
	 * 
	 * NOTE: It is better to sync positions prior to using the method
	 * startMarketData().  Trades that are removed from the portfolio
	 * as a result of the call to syncORSPositions() will still receive
	 * market data until the data feed is restarted.
	 */
	public void syncORSPositions() {
		Map<PositionKey,BigDecimal> positions =
			getPositions(new Date());
		
		/* maybe not connected...? */
		if ( positions == null ) {
			logger.error(">>> Could not sync ORS positions.");
			return;
		}
	
		/* now, let's sync */
		Set<String> symbols = new HashSet<String>();
		for ( PositionKey k : positions.keySet() ) {
			final String symbol = k.getSymbol();
			final String account = k.getAccount();

			if ( account.equals(portfolio.getAccount()) ) {
				symbols.add(symbol);
				if ( portfolio.hasTrade(symbol) ) {
					Trade trade = portfolio.getTrade(symbol);
					int pos = positions.get(k).intValue();
					BigDecimal qty = BigDecimal.valueOf(Math.abs(pos),0);
					Side side = Side.fromInt((int)Math.signum(pos));
				
					trade.overrideQuantity(qty);
					trade.overrideSide(side);
					
					logger.debug(">>> Syncing " + symbol + " to position " + pos + "." );	
				}
			}
		}
		
		portfolio.getSymbols().retainAll(symbols);
		
	}
	
}
