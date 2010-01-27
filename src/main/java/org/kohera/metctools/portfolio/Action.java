package org.kohera.metctools.portfolio;

/**
 * A generic action interface.
 * 
 * @author Jake Brukhman
 *
 * @param <T>
 */
public interface Action {
	
	public void performAction( Trade trade );

}
