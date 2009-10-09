package org.kohera.metctools.portfolio;

import java.math.BigDecimal;

/**
 * Describes the side of a trade.  Buys have a value of 1, and sells have a value of -1.
 * @author Jake Brukhman
 *
 */
public enum Side {

	BUY (1),
	SELL (-1),
	NONE(0);

	private int side;
	
	private Side( int side ) {
		this.side = side;
	}

	/**
	 * Returns the integer value of the side.
	 * @return
	 */
	public int value() {
		return side;
	}
	
	public static Side fromMetcSide( org.marketcetera.trade.Side side ) {
		if (side==org.marketcetera.trade.Side.Buy) {
			return BUY;
		} else if (side==org.marketcetera.trade.Side.Sell ||
				side==org.marketcetera.trade.Side.SellShort) {
			return SELL;
		}
		return NONE;
	}
	
	public org.marketcetera.trade.Side toMetcSide() {
		if ( this==BUY ) return org.marketcetera.trade.Side.Buy;
		else if ( this==SELL ) return org.marketcetera.trade.Side.Sell;
		else return null;
	}
	
	public BigDecimal toBigDecimal() {
		return BigDecimal.valueOf(this.value());
	}
	
	/**
	 * Returns the opposite side of this side.
	 * @return
	 */
	public Side opposite() {
		return ((this==BUY)? SELL: (this==SELL? BUY : NONE));
	}
}
