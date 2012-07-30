package humanize.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This is similar to the standard <code>WeakHashMap</code>, but it uses
 * <code>SoftReference</code>s for the map's values instead of
 * <code>WeakReference</code>s for the maps keys. Thus, an entry is removed from
 * the map when the JVM is low in memory and when the entry's value is softly
 * reachable.
 * 
 * @author Borislav Iordanov
 * 
 * @param <K>
 *            The type of the map key.
 * @param <V>
 *            The type of the map value.
 */
@SuppressWarnings("unchecked")
public class SoftHashMap<K, V> extends AbstractMap<K, V> {

	private static class F<K, V> implements Map.Entry<K, V> {

		private K k;
		private V v;

		public F(K k, V v) {

			this.k = k;
			this.v = v;

		}

		public K getKey() {

			return k;

		}

		public V getValue() {

			return v;

		}

		public V setValue(V v) {

			V old = v;
			this.v = v;
			return old;

		}

	}

	private static class SoftValue<Ka, Va> extends SoftReference<Va> {

		private final Ka key;

		private SoftValue(Ka aKey, Va aValue, ReferenceQueue<Va> q) {

			super(aValue, q);
			this.key = aKey;

		}

	}

	private final Map<K, SoftValue<K, V>> hash;

	private final ReferenceQueue<V> queue = new ReferenceQueue<V>();

	public SoftHashMap() {

		hash = new HashMap<K, SoftValue<K, V>>();

	}

	public SoftHashMap(int initialCapacity) {

		hash = new HashMap<K, SoftValue<K, V>>(initialCapacity);

	}

	public SoftHashMap(int initialCapacity, float loadFactor) {

		hash = new HashMap<K, SoftValue<K, V>>(initialCapacity, loadFactor);

	}

	public SoftHashMap(Map<? extends K, ? extends V> m) {

		this();
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			hash.put(e.getKey(), new SoftValue<K, V>(e.getKey(), e.getValue(), queue));
		}

	}

	public void clear() {

		processQueue();
		hash.clear();

	}

	public Set<Map.Entry<K, V>> entrySet() {

		final Set<Map.Entry<K, SoftValue<K, V>>> s = hash.entrySet();

		return new Set<Map.Entry<K, V>>() {

			public boolean add(Map.Entry<K, V> e) {

				throw new UnsupportedOperationException();

			}

			public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {

				throw new UnsupportedOperationException();

			}

			public void clear() {

				throw new UnsupportedOperationException();

			}

			public boolean contains(Object o) {

				throw new UnsupportedOperationException();

			}

			public boolean containsAll(Collection<?> c) {

				throw new UnsupportedOperationException();

			}

			public boolean isEmpty() {

				return s.isEmpty();

			}

			public Iterator<Map.Entry<K, V>> iterator() {

				synchronized (hash) {

					final Iterator<Map.Entry<K, SoftValue<K, V>>> i = s.iterator();

					return new Iterator<Map.Entry<K, V>>() {

						public boolean hasNext() {

							return i.hasNext();

						}

						public Map.Entry<K, V> next() {

							Map.Entry<K, SoftValue<K, V>> e = i.next();
							return new F<K, V>(e.getKey(), e.getValue().get());

						}

						public void remove() {

							i.remove();

						}
					};

				}
			}

			public boolean remove(Object o) {

				throw new UnsupportedOperationException();

			}

			public boolean removeAll(@SuppressWarnings("rawtypes") Collection c) {

				throw new UnsupportedOperationException();

			}

			public boolean retainAll(@SuppressWarnings("rawtypes") Collection c) {

				throw new UnsupportedOperationException();

			}

			public int size() {

				return s.size();

			}

			public Object[] toArray() {

				return toArray(null);

			}

			public Object[] toArray(Object[] a) {

				@SuppressWarnings("rawtypes")
				F[] result = null;
				if (a != null && a instanceof F[] && a.length >= size())
					result = (F[]) a;
				else
					result = new F[size()];
				Object[] A = s.toArray();
				for (int i = 0; i < A.length; i++) {
					Map.Entry<K, SoftValue<K, V>> e = (Map.Entry<K, SoftValue<K, V>>) A[i];
					result[i] = new F<K, V>(e.getKey(), e.getValue().get());
				}
				return result;

			}
		};
	}

	@Override
	public V get(Object key) {

		V result = null;
		SoftValue<K, V> soft_ref = hash.get(key);
		if (soft_ref != null) {
			result = soft_ref.get();
			if (result == null)
				synchronized (hash) {
					hash.remove(key);
				}
		}
		return result;

	}

	public V put(K key, V value) {

		synchronized (hash) {
			processQueue();
			hash.put(key, new SoftValue<K, V>(key, value, queue));
			return value;
		}

	}

	public V remove(Object key) {

		synchronized (hash) {
			processQueue();
			SoftValue<K, V> soft = hash.remove(key);
			return soft == null ? null : soft.get();
		}
	}

	public int size() {

		processQueue();
		return hash.size();

	}

	private void processQueue() {

		SoftValue<K, V> sv;
		while ((sv = (SoftValue<K, V>) queue.poll()) != null) {
			synchronized (hash) {
				hash.remove(sv.key);
			}
		}

	}

}
