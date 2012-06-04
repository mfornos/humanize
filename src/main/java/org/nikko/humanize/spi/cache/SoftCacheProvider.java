package org.nikko.humanize.spi.cache;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.nikko.humanize.util.SoftHashMap;

/**
 * {@link CacheProvider} implementation that uses {@link SoftHashMap} as in
 * memory storage.
 * 
 * @author mfornos
 * 
 */
public class SoftCacheProvider implements CacheProvider {

	private final static Map<Locale, ResourceBundle> bundles = new SoftHashMap<Locale, ResourceBundle>();

	private final static Map<String, Map<Locale, NumberFormat>> numberFormats = new SoftHashMap<String, Map<Locale, NumberFormat>>();

	private final static Map<String, Map<Locale, String[]>> stringCaches = new HashMap<String, Map<Locale, String[]>>();

	@Override
	public boolean containsBundle(Locale locale) {

		return bundles.containsKey(locale);
		
	}

	@Override
	public boolean containsNumberFormat(String cache, Locale locale) {

		return getNumberFormatCache(cache).containsKey(locale);
		
	}

	@Override
	public boolean containsStrings(String cache, Locale locale) {

		return getStringCache(cache).containsKey(locale);
		
	}

	@Override
	public ResourceBundle getBundle(Locale locale) {

		return bundles.get(locale);
		
	}

	@Override
	public NumberFormat getNumberFormat(String cache, Locale locale) {

		return getNumberFormatCache(cache).get(locale);
		
	}

	public String[] getStrings(String cache, Locale locale) {

		return getStringCache(cache).get(locale);
		
	}

	@Override
	public ResourceBundle putBundle(Locale locale, ResourceBundle bundle) {

		synchronized (bundles) {
			return bundles.put(locale, bundle);
		}
		
	}

	@Override
	public NumberFormat putNumberFormat(String cache, Locale locale, NumberFormat format) {

		Map<Locale, NumberFormat> numberFormatCache = getNumberFormatCache(cache);
		synchronized (numberFormatCache) {
			return numberFormatCache.put(locale, format);
		}
		
	}

	@Override
	public String[] putStrings(String cache, Locale locale, String[] value) {

		Map<Locale, String[]> stringCache = getStringCache(cache);
		synchronized (stringCache) {
			return stringCache.put(locale, value);
		}
		
	}

	private Map<Locale, NumberFormat> getNumberFormatCache(String cache) {

		synchronized (numberFormats) {
			if (!numberFormats.containsKey(cache))
				numberFormats.put(cache, new SoftHashMap<Locale, NumberFormat>());

			return numberFormats.get(cache);
		}
		
	}

	private Map<Locale, String[]> getStringCache(String cache) {

		synchronized (stringCaches) {
			if (!stringCaches.containsKey(cache))
				stringCaches.put(cache, new SoftHashMap<Locale, String[]>());

			return stringCaches.get(cache);
		}
		
	}

}
