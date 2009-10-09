package org.kohera.metctools.delegate;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.kohera.metctools.util.Table;
import org.kohera.metctools.DelegatorStrategy;

/**
 * The delegator handles delegation of events to the appropriate objects.
 * 
 * Delegate objects must implement some subinterface of EventDelegate, one
 * of the interfaces mentioned in EVENTS_ARRAY.  Then, a delegate object is
 * added to the Delegator, after which the delegate() method will broadcast
 * objects to the appropriate destinations.
 * 
 * 
 * @author Jake Brukhman
 *
 */

public final class Delegator {
	
	/* fields */
	public static final Class<?>[] EVENTS_ARRAY = { 
		AskDelegate.class, 
		BidDelegate.class,
		TradeDelegate.class,
		ExecutionReportDelegate.class,
		BrokerStatusDelegate.class,
		ServerStatusDelegate.class,
		OrderCancelRejectDelegate.class,
		OtherDelegate.class,
		CallbackDelegate.class,
		StartDelegate.class,
		StopDelegate.class
	};
	public static final Collection<Class<?>> EVENTS_COLLECTION =
		Arrays.asList(EVENTS_ARRAY);
	
	/* members */
	private Table<Class<?>,EventDelegate> delegates;
	private DelegatorStrategy parent;
	
	/**
	 * Create a new Delegator which can relay requests back to a 
	 * particular AdvancedStrategy.
	 * 
	 * @param sender
	 */
	public Delegator(DelegatorStrategy sender) {
		delegates = new Table<Class<?>,EventDelegate>();
		this.parent = sender;
	}

	/**
	 * Add a delegate.
	 * @param delegate
	 */
	public void addDelegate( EventDelegate delegate ) {
		for ( Class<?> interf : getInterfaces(delegate) ) {
			delegates.add(interf, delegate);
		}
	}
	
	/**
	 * Remove a delegate.
	 * @param delegate
	 */
	public void removeDelegate(EventDelegate delegate) {
		for ( Class<?> interf : getInterfaces(delegate) ) {
			delegates.remove(interf, delegate);
		}
	}

	/**
	 * Delegate a message to all delegates of a certain interface type
	 * given by key.
	 * 
	 * @param key
	 * @param message
	 */
	public void delegate(Class<?> key, Object message) {
		if ( delegates.containsKey(key) ) {
			List<EventDelegate> list = delegates.get(key);
			for ( EventDelegate delegate : list ) {
				Method m = key.getMethods()[0];
				try {
					// TODO: check that onStart and onStop go through ok
					int length = m.getParameterTypes().length;
					if ( length == 2 ) {
						m.invoke(delegate,parent,message);
					} else if ( length == 1 ) {
						m.invoke(delegate,parent);
					} else throw new RuntimeException("Delegate had too many parameters for method " + m.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Utility method to extract only the correct implemented interfaces
	 * from a given EventDelegate object.
	 * 
	 * @param delegate
	 * @return
	 */
	private List<Class<?>> getInterfaces( EventDelegate delegate ) {	
		List<Class<?>> interfaces = 
			Arrays.asList(delegate.getClass().getInterfaces());
		interfaces.retainAll(EVENTS_COLLECTION);
		return interfaces;
	}
	
}
