package humanize.spi.context;

import static humanize.util.Constants.EMPTY;
import static humanize.util.Constants.SPACE;
import humanize.spi.MessageFormat;
import humanize.spi.cache.CacheProvider;
import humanize.text.MaskFormat;
import humanize.time.RelativeDate;
import humanize.util.UTF8Control;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/**
 * Default implementation of {@link Context}.
 * 
 * @author mfornos
 * 
 */
public class DefaultContext implements Context, StandardContext {

	private static final String BUNDLE_LOCATION = "i18n.Humanize";

	private static final String ORDINAL_SUFFIXES = "ordinal.suffixes";

	private static final String CURRENCY = "currency";

	private static final String DECIMAL = "decimal";

	private static final String NUMBER = "number";

	private static final String DIGITS = "digits";

	private static final String PERCENT = "percent";

	private static final String DATE_FORMAT = "date";

	private static final String DATE_TIME_FORMAT = "date.time";

	private static final String SIMPLE_DATE = "simple.date";

	private static final String MASK = "mask";

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

	public DefaultContext() {

		this(Locale.getDefault());

	}

	public DefaultContext(Locale locale) {

		this.messageFormat = new MessageFormat(EMPTY, locale);

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
	public String formatRelativeDate(Date duration) {

		return getRelativeDate().format(duration);
	}

	@Override
	public String formatRelativeDate(Date reference, Date duration) {

		return getRelativeDate().format(reference, duration);
	}

	@Override
	public ResourceBundle getBundle() {

		return sharedCache.getBundle(locale, new Callable<ResourceBundle>() {
			@Override
			public ResourceBundle call() throws Exception {

				return ResourceBundle.getBundle(BUNDLE_LOCATION, locale, new UTF8Control());

			}
		});

	}

	@Override
	public DecimalFormat getCurrencyFormat() {

		return sharedCache.getFormat(CURRENCY, locale, new Callable<DecimalFormat>() {
			@Override
			public DecimalFormat call() throws Exception {

				return (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
			}
		});

	}

	@Override
	public DateFormat getDateFormat(final int style) {

		String name = DATE_FORMAT + style;

		return localCache.getFormat(name, locale, new Callable<DateFormat>() {
			@Override
			public DateFormat call() throws Exception {

				return DateFormat.getDateInstance(style, locale);
			}
		});

	}

	@Override
	public DateFormat getDateFormat(final String pattern) {

		return localCache.getFormat(SIMPLE_DATE, locale, new Callable<DateFormat>() {
			@Override
			public DateFormat call() throws Exception {

				return new SimpleDateFormat(pattern, locale);

			}
		});

	}

	@Override
	public DateFormat getDateTimeFormat() {

		return getDateTimeFormat(DateFormat.SHORT, DateFormat.SHORT);

	}

	@Override
	public DateFormat getDateTimeFormat(final int dateStyle, final int timeStyle) {

		String name = DATE_TIME_FORMAT + dateStyle + timeStyle;

		return localCache.getFormat(name, locale, new Callable<DateFormat>() {
			@Override
			public DateFormat call() throws Exception {

				return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
			}
		});

	}

	@Override
	public DecimalFormat getDecimalFormat() {

		return localCache.getFormat(DECIMAL, locale, new Callable<DecimalFormat>() {
			@Override
			public DecimalFormat call() throws Exception {

				return (DecimalFormat) DecimalFormat.getInstance(locale);
			}
		});

	}

	@Override
	public Locale getLocale() {

		return locale;

	}

	@Override
	public MaskFormat getMaskFormat() {

		return localCache.getFormat(MASK, Locale.ROOT, new Callable<MaskFormat>() {
			@Override
			public MaskFormat call() throws Exception {

				return new MaskFormat("");
			}
		});

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

		return sharedCache.getFormat(NUMBER, locale, new Callable<NumberFormat>() {
			@Override
			public NumberFormat call() throws Exception {

				return NumberFormat.getInstance(locale);
			}
		});

	}

	@Override
	public DecimalFormat getPercentFormat() {

		return sharedCache.getFormat(PERCENT, locale, new Callable<DecimalFormat>() {
			@Override
			public DecimalFormat call() throws Exception {

				return (DecimalFormat) NumberFormat.getPercentInstance(locale);
			}
		});

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

	private String resolveStringArray(final String cacheName, final int index) {

		return sharedCache.getStrings(cacheName, locale, new Callable<String[]>() {
			@Override
			public String[] call() throws Exception {

				return getBundle().getString(cacheName).split(SPACE);

			}
		})[index];

	}

}
