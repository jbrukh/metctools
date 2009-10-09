package org.kohera.metctools.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class that encapsulates a Map from keys K to lists of values V.
 * 
 * @author Jake Brukhman
 *
 * @param <K>
 * @param <V>
 */
public class Table<K,V> {

	/* fields */
	private HashMap<K,List<V>> map;
	
	/**
	 * Create a new Table instance.
	 */
	public Table() {
		map = new HashMap<K, List<V>>();
	}
	
	/**
	 * Add a key-value pair.
	 * 
	 * If no such key exists in the Table, then a new entry is
	 * created.
	 * 
	 * If the key already exists, then the value gets appended to the
	 * list of values associated with the key.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(K key, V value) {
		if ( !map.containsKey(key) ) {
			map.put(key,new ArrayList<V>());
		}
		List<V> row = map.get(key);
		return row.add(value);
	}
	
	/**
	 * Returns the list of values associated with the key (or null).
	 * 
	 * @param key
	 * @return
	 */
	public List<V> get(K key) {
		return map.get(key);
	}
	
	/**
	 * Removes a value from the list of values associated with they key,
	 * if such a key and value exist.
	 * 
	 * @param key
	 * @param value
	 */
	public void remove(K key, V value) {
		List<V> list = map.get(key);
		list.remove(value);
	}
	
	/**
	 * Returns true if and only if the Table contains the key.
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey( K key ) {
		return map.containsKey(key);
	}
	
}
