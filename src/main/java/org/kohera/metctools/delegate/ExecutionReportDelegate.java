package org.kohera.metctools.delegate;

import org.kohera.metctools.AdvancedStrategy;
import org.marketcetera.trade.ExecutionReport;

/**
 * Execution report delegate interface.
 * 
 * @author Administrator
 *
 */
public interface ExecutionReportDelegate extends EventDelegate {
	
	public void onExecutionReport( AdvancedStrategy sender, ExecutionReport report );

}
