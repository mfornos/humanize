package org.nikko.humanize.spi.context;

import static org.nikko.humanize.util.Constants.SPACE_STRING;
import static org.nikko.humanize.util.Constants.EMPTY_STRING;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.spi.cache.CacheProvider;
import org.nikko.humanize.util.RelativeDate;
import org.nikko.humanize.util.UTF8Control;

/**
 * Default implementation of {@link Context}.
 * 
 * @author mfornos
 * 
 */
// TODO impl. dateTime with styles...
public class DefaultContext implements Context {

	private static final String BUNDLE_LOCATION = "i18n.Humanize";

	private static final String ORDINAL_SUFFIXES = "ordinal.suffixes";

	private static final String CURRENCY = "currency";

	private static final String DECIMAL = "decimal";

	private static final String DIGITS = "digits";

	private final static CacheProvider cache = loadCacheProvider();

	private static CacheProvider loadCacheProvider() {

		ServiceLoader<CacheProvider> ldr = ServiceLoader.load(CacheProvider.class);
		for (CacheProvider provider : ldr) {
			return provider;
		}
		throw new RuntimeException("No CacheProvider was found");

	}

	private Locale locale;

	public DefaultContext() {

		this(Locale.getDefault());

	}

	public DefaultContext(Locale locale) {

		this.locale = locale;

	}

	@Override
	public String digitStrings(int index) {

		return resolveStringArray(DIGITS, index);

	}

	@Override
	public String formatDate(int style, Date value) {

		return getDateFormat(style).format(value);

	}

	@Override
	public String formatDateTime(Date date) {

		return getDateTimeFormat().format(date);

	}

	@Override
	public String formatDecimal(Number value) {

		return getNumberFormat().format(value);

	}

	@Override
	public String formatMessage(String key, Object... args) {

		return new Message(getBundle().getString(key), locale).render(args);

	}

	@Override
	public String formatRelativeDate(Date duration) {

		return getRelativeDate().format(duration);

	}

	@Override
	public String formatRelativeDate(Date reference, Date duration) {

		return getRelativeDate().format(reference, duration);

	}

	@Override
	public ResourceBundle getBundle() {

		if (!cache.containsBundle(locale))
			cache.putBundle(locale, ResourceBundle.getBundle(BUNDLE_LOCATION, locale, new UTF8Control()));

		return cache.getBundle(locale);

	}

	@Override
	public NumberFormat getCurrencyFormat() {

		if (!cache.containsNumberFormat(CURRENCY, locale))
			cache.putNumberFormat(CURRENCY, locale, NumberFormat.getCurrencyInstance(locale));

		return cache.getNumberFormat(CURRENCY, locale);

	}

	@Override
	public DateFormat getDateFormat(int style) {

		return DateFormat.getDateInstance(style, locale);

	}

	@Override
	public DateFormat getDateTimeFormat() {

		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);

	}

	@Override
	public Message getFormat() {

		return new Message(EMPTY_STRING, locale);

	}

	@Override
	public Locale getLocale() {

		return locale;

	}

	@Override
	public String getMessage(String key) {

		return getBundle().getString(key);

	}

	@Override
	public NumberFormat getNumberFormat() {

		if (!cache.containsNumberFormat(DECIMAL, locale))
			cache.putNumberFormat(DECIMAL, locale, NumberFormat.getInstance(locale));

		return cache.getNumberFormat(DECIMAL, locale);

	}

	@Override
	public RelativeDate getRelativeDate() {

		return RelativeDate.getInstance(this);

	}

	@Override
	public String ordinalSuffix(int index) {

		return resolveStringArray(ORDINAL_SUFFIXES, index);

	}

	@Override
	public void setLocale(Locale locale) {

		this.locale = locale;

	}

	private String resolveStringArray(String cacheName, int index) {

		if (!cache.containsStrings(cacheName, locale))
			cache.putStrings(cacheName, locale, getBundle().getString(cacheName).split(SPACE_STRING));

		return cache.getStrings(cacheName, locale)[index];

	}

}
