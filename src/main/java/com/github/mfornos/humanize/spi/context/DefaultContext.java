package com.github.mfornos.humanize.spi.context;

import static com.github.mfornos.humanize.util.Constants.EMPTY_STRING;
import static com.github.mfornos.humanize.util.Constants.SPACE_STRING;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import com.github.mfornos.humanize.spi.MessageFormat;
import com.github.mfornos.humanize.spi.cache.CacheProvider;
import com.github.mfornos.humanize.util.UTF8Control;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DateTimePatternGenerator;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DurationFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;

/**
 * Default implementation of {@link Context}.
 * 
 * @author mfornos
 * 
 */
public class DefaultContext implements Context {

	private static final String DURATION_FORMAT = "duration.format";

	private static final String BUNDLE_LOCATION = "i18n.Humanize";

	private static final String CURRENCY = "currency";

	private static final String DECIMAL = "decimal";

	private static final String NUMBER = "number";

	private static final String DIGITS = "digits";

	private static final String PERCENT = "percent";

	private static final String RULE_BASED = "rule.based";

	private static final String DATE_FORMAT = "date";

	private static final String CURRENCY_PL = "currency.pl";

	private static final String DATE_TIME_FORMAT = "date.time";

	private final static CacheProvider sharedCache = loadCacheProvider();

	private static CacheProvider loadCacheProvider() {

		ServiceLoader<CacheProvider> ldr = ServiceLoader.load(CacheProvider.class);
		for (CacheProvider provider : ldr) {
			return provider;
		}
		throw new RuntimeException("No CacheProvider was found");

	}

	private final CacheProvider localCache = loadCacheProvider();

	private final MessageFormat messageFormat;

	private Locale locale;

	private ULocale ulocale;

	public DefaultContext() {

		this(Locale.getDefault());

	}

	public DefaultContext(Locale locale) {

		this.messageFormat = new MessageFormat(EMPTY_STRING, locale);

		setLocale(locale);

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
	public String formatDateTime(int dateStyle, int timeStyle, Date date) {

		return getDateTimeFormat(dateStyle, timeStyle).format(date);
	}

	@Override
	public String formatDecimal(Number value) {

		return getNumberFormat().format(value);

	}

	@Override
	public String formatMessage(String key, Object... args) {

		MessageFormat fmt = getMessageFormat();
		fmt.applyPattern(getBundle().getString(key));
		return fmt.render(args);

	}

	@Override
	public String getBestPattern(String skeleton) {

		DateTimePatternGenerator generator = DateTimePatternGenerator.getInstance(getULocale());
		return generator.getBestPattern(skeleton);

	}

	@Override
	public ResourceBundle getBundle() {

		synchronized (sharedCache) {
			if (!sharedCache.containsBundle(locale))
				sharedCache.putBundle(locale, ResourceBundle.getBundle(BUNDLE_LOCATION, locale, new UTF8Control()));
		}
		return sharedCache.getBundle(locale);

	}

	@Override
	public DecimalFormat getCurrencyFormat() {

		DecimalFormat rs = sharedCache.getFormat(CURRENCY, locale);
		return (DecimalFormat) ((rs == null) ? toSharedFormatsCache(CURRENCY, NumberFormat.getCurrencyInstance(locale))
		        : rs);

	}

	@Override
	public DateFormat getDateFormat(int style) {

		String name = DATE_FORMAT + style;
		if (!localCache.containsFormat(name, locale))
			localCache.putFormat(name, locale, DateFormat.getDateInstance(style, locale));

		return localCache.getFormat(name, locale);

	}

	@Override
	public DateFormat getDateTimeFormat() {

		return getDateTimeFormat(DateFormat.SHORT, DateFormat.SHORT);

	}

	@Override
	public DateFormat getDateTimeFormat(int dateStyle, int timeStyle) {

		String name = DATE_TIME_FORMAT + dateStyle + timeStyle;
		if (!localCache.containsFormat(name, locale))
			localCache.putFormat(name, locale, DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));

		return localCache.getFormat(name, locale);

	}

	@Override
	public DecimalFormat getDecimalFormat() {

		if (!localCache.containsFormat(DECIMAL, locale))
			localCache.putFormat(DECIMAL, locale, DecimalFormat.getInstance(locale));

		return localCache.getFormat(DECIMAL, locale);

	}

	@Override
	public DurationFormat getDurationFormat() {

		DurationFormat rs = sharedCache.getFormat(DURATION_FORMAT, locale);
		return (rs == null) ? toSharedFormatsCache(DURATION_FORMAT, DurationFormat.getInstance(ulocale)) : rs;

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
	public MessageFormat getMessageFormat() {

		messageFormat.setLocale(locale);
		return messageFormat;

	}

	@Override
	public NumberFormat getNumberFormat() {

		NumberFormat rs = sharedCache.getFormat(NUMBER, locale);
		return (rs == null) ? toSharedFormatsCache(NUMBER, NumberFormat.getInstance(locale)) : rs;

	}

	@Override
	public DecimalFormat getPercentFormat() {

		DecimalFormat rs = sharedCache.getFormat(PERCENT, locale);
		return (DecimalFormat) ((rs == null) ? toSharedFormatsCache(PERCENT, NumberFormat.getPercentInstance(locale))
		        : rs);

	}

	@Override
	public DecimalFormat getPluralCurrencyFormat() {

		DecimalFormat rs = sharedCache.getFormat(CURRENCY_PL, locale);
		return (DecimalFormat) ((rs == null) ? toSharedFormatsCache(CURRENCY_PL,
		        NumberFormat.getInstance(locale, NumberFormat.PLURALCURRENCYSTYLE)) : rs);

	}

	@Override
	public NumberFormat getRuleBasedNumberFormat(int type) {

		String ruleBasedName = RULE_BASED + type;
		NumberFormat rs = sharedCache.getFormat(ruleBasedName, locale);
		return (NumberFormat) ((rs == null) ? toSharedFormatsCache(ruleBasedName, new RuleBasedNumberFormat(locale,
		        type)) : rs);

	}

	@Override
	public ULocale getULocale() {

		return ulocale;

	}

	@Override
	public void setLocale(Locale locale) {

		this.locale = locale;
		this.ulocale = ULocale.forLocale(locale);

	}

	private String resolveStringArray(String cacheName, int index) {

		synchronized (sharedCache) {
			if (!sharedCache.containsStrings(cacheName, locale))
				sharedCache.putStrings(cacheName, locale, getBundle().getString(cacheName).split(SPACE_STRING));
		}
		return sharedCache.getStrings(cacheName, locale)[index];

	}

	private <T> T toSharedFormatsCache(String cache, T obj) {

		synchronized (sharedCache) {
			return sharedCache.putFormat(cache, locale, obj);
		}
	}

}
