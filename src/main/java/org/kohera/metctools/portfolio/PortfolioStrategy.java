package org.kohera.metctools.portfolio;

import org.apache.log4j.Logger;
import org.kohera.metctools.DelegatorStrategy;
import org.kohera.metctools.Messages;
import org.kohera.metctools.delegate.ExecutionReportDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.MSymbol;

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
	class TradeRouter implements ExecutionReportDelegate, TradeDelegate {
		@Override
		public void onExecutionReport(DelegatorStrategy sender,
				ExecutionReport report) {
			String symbol = report.getSymbol().toString();
			logger.trace(">>> Received report for symbol '"+symbol+"':");
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
			//logger.trace(">>> Received trade for symbol '"+symbol+"'");
			if ( portfolio.hasTrade(symbol) ) {
				portfolio.getTrade(symbol)
					.acceptTrade(tradeEvent);
			} else {
				// TODO: clean up
				logger.warn(">>> Received external trade event. (Ignoring.)");
				logger.trace(">>> ...for symbol " + symbol + ".");
			}
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
			throw new RuntimeException(Messages.DATA_PROVIDER_NOT_SET);
		}
		
		String[] symbols = portfolio.getSymbols();
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

}
