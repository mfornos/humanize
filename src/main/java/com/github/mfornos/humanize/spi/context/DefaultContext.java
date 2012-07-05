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

	private static final String DIGITS = "digits";

	private static final String PERCENT = "percent";

	private static final String RULE_BASED = "rule.based";

	private final static CacheProvider cache = loadCacheProvider();

	private static final String CURRENCY_PL = null;

	private static CacheProvider loadCacheProvider() {

		ServiceLoader<CacheProvider> ldr = ServiceLoader.load(CacheProvider.class);
		for (CacheProvider provider : ldr) {
			return provider;
		}
		throw new RuntimeException("No CacheProvider was found");

	}

	private Locale locale;

	private ULocale ulocale;

	private final MessageFormat messageFormat;

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

		if (!cache.containsBundle(locale))
			cache.putBundle(locale, ResourceBundle.getBundle(BUNDLE_LOCATION, locale, new UTF8Control()));

		return cache.getBundle(locale);

	}

	@Override
	public DecimalFormat getCurrencyFormat() {

		if (!cache.containsFormat(CURRENCY, locale))
			cache.putFormat(CURRENCY, locale, NumberFormat.getCurrencyInstance(locale));

		return (DecimalFormat) cache.getFormat(CURRENCY, locale);

	}

	@Override
	public DateFormat getDateFormat(int style) {

		return DateFormat.getDateInstance(style, locale);

	}

	@Override
	public DateFormat getDateTimeFormat() {

		return getDateTimeFormat(DateFormat.SHORT, DateFormat.SHORT);

	}

	@Override
	public DateFormat getDateTimeFormat(int dateStyle, int timeStyle) {

		return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);

	}

	@Override
	public DecimalFormat getDecimalFormat() {

		return (DecimalFormat) DecimalFormat.getInstance(locale);

	}

	@Override
	public DurationFormat getDurationFormat() {

		if (!cache.containsFormat(DURATION_FORMAT, locale))
			cache.putFormat(DURATION_FORMAT, locale, DurationFormat.getInstance(ulocale));

		return cache.getFormat(DURATION_FORMAT, locale);

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

		if (!cache.containsFormat(DECIMAL, locale))
			cache.putFormat(DECIMAL, locale, NumberFormat.getInstance(locale));

		return cache.getFormat(DECIMAL, locale);

	}

	@Override
	public DecimalFormat getPercentFormat() {

		if (!cache.containsFormat(PERCENT, locale))
			cache.putFormat(PERCENT, locale, NumberFormat.getPercentInstance(locale));

		return (DecimalFormat) cache.getFormat(PERCENT, locale);

	}

	@Override
	public DecimalFormat getPluralCurrencyFormat() {

		if (!cache.containsFormat(CURRENCY_PL, locale))
			cache.putFormat(CURRENCY_PL, locale, NumberFormat.getInstance(locale, NumberFormat.PLURALCURRENCYSTYLE));

		return (DecimalFormat) cache.getFormat(CURRENCY_PL, locale);

	}

	@Override
	public NumberFormat getRuleBasedNumberFormat(int type) {

		String ruleBasedName = RULE_BASED + type;
		if (!cache.containsFormat(ruleBasedName, locale))
			cache.putFormat(ruleBasedName, locale, new RuleBasedNumberFormat(locale, type));

		return cache.getFormat(ruleBasedName, locale);

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

		if (!cache.containsStrings(cacheName, locale))
			cache.putStrings(cacheName, locale, getBundle().getString(cacheName).split(SPACE_STRING));

		return cache.getStrings(cacheName, locale)[index];

	}

}
