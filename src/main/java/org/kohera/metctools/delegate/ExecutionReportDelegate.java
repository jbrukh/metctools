package org.kohera.metctools.delegate;

import org.kohera.metctools.DelegatorStrategy;
import org.marketcetera.trade.ExecutionReport;

/**
 * Execution report delegate interface.
 * 
 * @author Jake Brukhman
 *
 */
public interface ExecutionReportDelegate extends EventDelegate {
	
	/**
	 * Override this method to handle ExecutionReports.
	 * 
	 * @param sender
	 * @param report
	 */
	public void onExecutionReport( DelegatorStrategy sender, ExecutionReport report );

}
