package org.kohera.metctools;

import org.kohera.metctools.delegate.ExecutionReportDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.kohera.metctools.portfolio.Portfolio;
import org.kohera.metctools.portfolio.PortfolioImpl;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.marketdata.MarketDataRequest;
import org.marketcetera.trade.ExecutionReport;
import org.marketcetera.trade.MSymbol;

public abstract class PortfolioStrategy extends AdvancedStrategy {

	/**
	 * Internal class that routes execution reports to the appropriate
	 * trades in the portfolio.
	 * 
	 * @author Administrator
	 *
	 */
	class TradeRouter implements ExecutionReportDelegate, TradeDelegate {
		@Override
		public void onExecutionReport(AdvancedStrategy sender,
				ExecutionReport report) {
			MSymbol symbol = report.getSymbol();
			if ( portfolio.hasTrade(symbol) ) {
				portfolio.getTrade(symbol)
					.acceptExecutionReport(PortfolioStrategy.this, report);
			} else {
				// TODO: clean up
				warn("Received external execution report. (Ignoring.) -- " + report );
			}
		}

		@Override
		public void onTrade(AdvancedStrategy sender, TradeEvent tradeEvent) {
			MSymbol symbol = tradeEvent.getSymbol();
			if ( portfolio.hasTrade(symbol) ) {
				portfolio.getTrade(symbol)
					.acceptTrade(tradeEvent);
			} else {
				// TODO: clean up
				warn("Received external trade event. (Ignoring.)");
			}
		}
		
	}
	
	/* fields */
	private Portfolio portfolio;
	private int dataRequestId;
	private String dataProvider;
		
	/**
	 * Create a new instance of a PortfolioStrategy.
	 * 
	 * @param dataProvider
	 * @throws ClientInitException
	 */
	public PortfolioStrategy() throws ClientInitException {
		super();
		
		portfolio = new PortfolioImpl();
		this.dataProvider = returnDataProvider();
		
		/* route execution reports and trades to the portfolio */
		addDelegate( new TradeRouter() );
	}

	/**
	 * Must override this method to return the data provider for
	 * the strategy.
	 * 
	 * @return
	 */
	public abstract String returnDataProvider();
	
	/**
	 * Get the data provider.
	 * 
	 * @return
	 */
	public String getDataProvider() {
		return dataProvider;
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
		
		String[] symbols = portfolio.getSymbols();
		
		if ( symbols.length < 1 ) {
			return;
		}
		
		getRelay().warn("Starting market data...");
		MarketDataRequest request = MarketDataRequest
										.newRequest()
										.withSymbols(symbols)
										.fromProvider(dataProvider)
										.withContent("LATEST_TICK");
		dataRequestId = requestMarketData(request);
		getRelay().warn("Market data id: " + dataRequestId );
	}

	public void stopMarketData() {
		cancelDataRequest(dataRequestId);
	}

}
