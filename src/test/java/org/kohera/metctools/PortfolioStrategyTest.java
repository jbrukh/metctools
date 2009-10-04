package org.kohera.metctools;

import java.math.BigDecimal;

import org.kohera.metctools.delegate.StartDelegate;
import org.kohera.metctools.delegate.StopDelegate;
import org.kohera.metctools.delegate.TradeDelegate;
import org.kohera.metctools.portfolio.ITrade;
import org.kohera.metctools.portfolio.OrderTimeoutPolicies;
import org.kohera.metctools.portfolio.Side;
import org.kohera.metctools.portfolio.Trade;
import org.marketcetera.client.ClientInitException;
import org.marketcetera.event.TradeEvent;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.trade.MSymbol;

public class PortfolioStrategyTest extends PortfolioStrategy 
	implements StartDelegate, StopDelegate, TradeDelegate {

	public PortfolioStrategyTest(String dataProvider)
			throws ClientInitException {
		super();
		
	}

	@Override
	public String returnDataProvider() {
		return getParameter("DATA");
	}

	@Override
	public void onStart(AdvancedStrategy sender) {
		getPortfolio().setOrderTimeout(60*1000);
		getPortfolio().setOrderTimeoutPolicy(
				OrderTimeoutPolicies.ON_TIMEOUT_CANCEL
				);
		
		ITrade AAPL =
			new Trade(
					this,
					new MSymbol("AAPL"),
					new BrokerID("tos"),
					"08132009" );
		getPortfolio().addTrade(AAPL);
		startMarketData();
		
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		AAPL.marketOrder(BigDecimal.valueOf(100,0), Side.BUY);
	}

	@Override
	public void onStop(AdvancedStrategy sender) {
		stopMarketData();
	}

	@Override
	public void onTrade(AdvancedStrategy sender, TradeEvent tradeEvent) {
		sender.getRelay().warn("trade -- " + tradeEvent);
	}

}
