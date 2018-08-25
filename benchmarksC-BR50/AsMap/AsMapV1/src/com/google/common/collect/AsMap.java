package com.google.common.collect;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class AsMap<K, V> extends AbstractMap<K, Collection<V>> {
	/** Usually the same as map, but smaller for the headMap(), tailMap(), or subMap() of a SortedAsMap. */
	final transient Map<K, Collection<V>> submap;
	private transient int totalSize;

	AsMap(final Map<K, Collection<V>> submap) {
		this.submap = submap;
		this.entrySet = null;
	}

	public AsMap() {

		submap = new HashMap();
		this.entrySet = null;
	}

	transient Set<Map.Entry<K, Collection<V>>> entrySet;
	@Override
	public Set<Map.Entry<K, Collection<V>>> entrySet() {
		final Set<Map.Entry<K, Collection<V>>> result = entrySet;
		// assert entrySet == result;
		// return (entrySet == null) ? entrySet = new AsMapEntries() : result;
		if (result == null) {
			entrySet = new AsMapEntries();
			return entrySet;
		}
		else {
			return result;
		}
	}

	// The following methods are included for performance.

	@Override
	public boolean containsKey(final Object key) {
		return submap.containsKey(key);
	}

	@Override
	public Collection<V> get(final Object key) {
		final Collection<V> collection = submap.get(key);
		if (collection == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final K k = (K) key;
		return wrapCollection(k, collection);
	}

	@Override
	public Set<K> keySet() {
		return this.keySet();
	}

	@Override
	public Collection<V> remove(final Object key) {
		final Collection<V> collection = submap.remove(key);
		if (collection == null) {
			return null;
		}

		final Collection<V> output = (Collection<V>) new HashMap();
		output.addAll(collection);
		totalSize -= collection.size();
		collection.clear();
		return output;
	}

	@Override
	public boolean equals(final Object object) {
		return this == object || submap.equals(object);
	}

	@Override
	public int hashCode() {
		return submap.hashCode();
	}

	@Override
	public String toString() {
		return submap.toString();
	}

	class AsMapEntries extends AbstractSet<Map.Entry<K, Collection<V>>> {
		@Override
		public Iterator<Map.Entry<K, Collection<V>>> iterator() {
			return new AsMapIterator();
		}

		@Override
		public int size() {
			return submap.size();
		}

		// The following methods are included for performance.

		@Override
		public boolean contains(final Object o) {
			return submap.entrySet().contains(o);
		}

		@Override
		public boolean remove(final Object o) {
			if (!contains(o)) {
				return false;
			}
			final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
			// removeValuesForKey(entry.getKey());
			return true;
		}
	}

	/** Iterator across all keys and value collections. */
	class AsMapIterator implements Iterator<Map.Entry<K, Collection<V>>> {
		final Iterator<Map.Entry<K, Collection<V>>> delegateIterator = submap.entrySet().iterator();
		Collection<V> collection;

		@Override
		public boolean hasNext() {
			return delegateIterator.hasNext();
		}

		@Override
		public Map.Entry<K, Collection<V>> next() {
			final Map.Entry<K, Collection<V>> entry = delegateIterator.next();
			final K key = entry.getKey();
			collection = entry.getValue();
			return Maps.immutableEntry(key, wrapCollection(key, collection));
		}

		@Override
		public void remove() {
			delegateIterator.remove();
			totalSize -= collection.size();
			collection.clear();
		}
	}

	private Collection<V> wrapCollection(final K key, final Collection<V> collection) {
		return collection;

	}
}
