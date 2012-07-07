package com.github.mfornos.humanize.spi.cache;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.github.mfornos.humanize.util.SoftHashMap;

/**
 * {@link CacheProvider} implementation that uses {@link SoftHashMap} as in
 * memory storage.
 * 
 * @author mfornos
 * 
 */
public class SoftCacheProvider implements CacheProvider {

	private final Map<Locale, ResourceBundle> bundles = new SoftHashMap<Locale, ResourceBundle>();

	private final Map<String, Map<Locale, Object>> formats = new SoftHashMap<String, Map<Locale, Object>>();

	private final Map<String, Map<Locale, String[]>> stringCaches = new HashMap<String, Map<Locale, String[]>>();

	@Override
	public boolean containsBundle(Locale locale) {

		return bundles.containsKey(locale);

	}

	@Override
	public boolean containsFormat(String cache, Locale locale) {

		return getFormatsCache(cache).containsKey(locale);

	}

	@Override
	public boolean containsStrings(String cache, Locale locale) {

		return getStringCache(cache).containsKey(locale);

	}

	@Override
	public ResourceBundle getBundle(Locale locale) {

		return bundles.get(locale);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getFormat(String cache, Locale locale) {

		return (T) getFormatsCache(cache).get(locale);

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
	public <T> T putFormat(String cache, Locale locale, T format) {

		Map<Locale, T> numberFormatCache = getFormatsCache(cache);
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

	@SuppressWarnings("unchecked")
	private <T> Map<Locale, T> getFormatsCache(String cache) {

		if (!formats.containsKey(cache))
			formats.put(cache, (Map<Locale, Object>) new SoftHashMap<Locale, T>());

		return (Map<Locale, T>) formats.get(cache);

	}

	private Map<Locale, String[]> getStringCache(String cache) {

		if (!stringCaches.containsKey(cache))
			stringCaches.put(cache, new SoftHashMap<Locale, String[]>());

		return stringCaches.get(cache);

	}

}
