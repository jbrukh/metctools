package org.kohera.metctools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Table<K,V> {

	private HashMap<K,List<V>> map;
	
	public Table() {
		map = new HashMap<K, List<V>>();
	}
	
	public boolean add(K key, V value) {
		if ( !map.containsKey(key) ) {
			map.put(key,new ArrayList<V>());
		}
		List<V> row = map.get(key);
		return row.add(value);
	}
	
	public List<V> get(K key) {
		return map.get(key);
	}
	
	public void remove(K key, V value) {
		List<V> list = map.get(key);
		list.remove(value);
	}
	
	public boolean containsKey( K key ) {
		return map.containsKey(key);
	}
	
}
