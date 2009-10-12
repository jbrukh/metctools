package org.kohera.metctools;

import org.kohera.metctools.delegate.StartDelegate;
import org.kohera.metctools.delegate.StopDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.kohera.metctools.portfolio.PortfolioStrategy;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;


public class PortfolioStrategyTest extends PortfolioStrategy
		implements StartDelegate, StopDelegate, TradeDelegate {

	public PortfolioStrategyTest() throws ClientInitException {
		super();
		addDelegate(this);
	}

	@Override
	public void onStart(DelegatorStrategy sender) {
		setDataProvider(getParameter("DATA"));
		info("Hello world.");
		
		getPortfolio().setAccountCredentials(new BrokerID("tos_qa"), "08132009");
		getPortfolio().createTrade("AAPL");
		getPortfolio().createTrade("MSFT");
		
		startMarketData();
	}

	@Override
	public void onTrade(DelegatorStrategy sender, TradeEvent tradeEvent) {
		info("trade -- " + tradeEvent);
	}

	@Override
	public void onStop(DelegatorStrategy sender) {
		stopMarketData();
		info("Bye!");
	}

}
