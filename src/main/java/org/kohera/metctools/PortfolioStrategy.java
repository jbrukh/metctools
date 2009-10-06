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

public abstract class PortfolioStrategy extends DelegatorStrategy {

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
		public void onTrade(DelegatorStrategy sender, TradeEvent tradeEvent) {
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
		
		portfolio = new PortfolioImpl();
		dataRequestId = null;
		
		/* route execution reports and trades to the portfolio */
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
		if (dataRequestId!=null && dataRequestId>0) {
			cancelDataRequest(dataRequestId);
		} dataRequestId = null;
	}

	private boolean dataProviderIsSet() {
		return dataProvider!=null;
	}
}
