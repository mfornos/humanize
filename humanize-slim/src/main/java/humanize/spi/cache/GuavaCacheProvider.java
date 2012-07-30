package humanize.spi.cache;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link CacheProvider} implementation that uses Guava caches as in memory
 * storage.
 * 
 * @author mfornos
 * 
 */
public class GuavaCacheProvider implements CacheProvider {

	private final Cache<Locale, ResourceBundle> bundles = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.HOURS).<Locale, ResourceBundle> build();

	private final LoadingCache<String, Cache<Locale, Object>> formats = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.HOURS).build(new CacheLoader<String, Cache<Locale, Object>>() {

		        @Override
		        public Cache<Locale, Object> load(String cache) throws Exception {

			        return CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).<Locale, Object> build();

		        }

	        });

	private final LoadingCache<String, Cache<Locale, String[]>> stringCaches = CacheBuilder.newBuilder()
	        .expireAfterAccess(1, TimeUnit.HOURS).build(new CacheLoader<String, Cache<Locale, String[]>>() {

		        @Override
		        public Cache<Locale, String[]> load(String cache) throws Exception {

			        return CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).<Locale, String[]> build();

		        }

	        });

	@Override
	public boolean containsBundle(Locale locale) {

		return bundles.getIfPresent(locale) != null;

	}

	@Override
	public boolean containsFormat(String cache, Locale locale) {

		return getFormatsCache(cache).getIfPresent(locale) != null;

	}

	@Override
	public boolean containsStrings(String cache, Locale locale) {

		return getStringCache(cache).getIfPresent(locale) != null;

	}

	@Override
	public ResourceBundle getBundle(Locale locale, Callable<ResourceBundle> getCall) {

		try {
			return bundles.get(locale, getCall);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFormat(String cache, Locale locale, Callable<T> getCall) {

		try {
			return (T) getFormatsCache(cache).get(locale, getCall);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public String[] getStrings(String cache, Locale locale, Callable<String[]> getCall) {

		try {
			return getStringCache(cache).get(locale, getCall);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResourceBundle putBundle(Locale locale, ResourceBundle bundle) {

		bundles.put(locale, bundle);
		return bundle;

	}

	@Override
	public <T> T putFormat(String cache, Locale locale, T format) {

		Cache<Locale, T> numberFormatCache = getFormatsCache(cache);
		numberFormatCache.put(locale, format);
		return format;

	}

	@Override
	public String[] putStrings(String cache, Locale locale, String[] value) {

		Cache<Locale, String[]> stringCache = getStringCache(cache);
		stringCache.put(locale, value);
		return value;

	}

	@SuppressWarnings("unchecked")
	private <T> Cache<Locale, T> getFormatsCache(String cache) {

		try {
			return (Cache<Locale, T>) formats.get(cache);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

	private Cache<Locale, String[]> getStringCache(String cache) {

		try {
			return stringCaches.get(cache);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

	}

}
