package org.kohera.metctools;

import java.util.HashMap;
import java.util.Map;

import org.marketcetera.trade.MSymbol;

public class Test {
	
	public static void main( String[] args ) {
		
		Map<MSymbol,String> map = new HashMap<MSymbol, String>();
		
		MSymbol one = new MSymbol("AAPL");
		MSymbol two = new MSymbol("AAPL");
		
		map.put(one, "one");
		boolean flag = map.containsKey(two);
		
		System.out.println(one==two);
		System.out.println(one.equals(two));
		
		
	}

}
