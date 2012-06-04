package org.nikko.humanize;

import static java.lang.String.format;
import static org.nikko.humanize.util.Constants.EMPTY_STRING;
import static org.nikko.humanize.util.Constants.ND_FACTOR;
import static org.nikko.humanize.util.Constants.ORDINAL_FMT;
import static org.nikko.humanize.util.Constants.THOUSAND;
import static org.nikko.humanize.util.Constants.bigDecExponents;
import static org.nikko.humanize.util.Constants.binPrexies;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.spi.context.Context;
import org.nikko.humanize.spi.context.ContextFactory;

/**
 * Facility for adding a "human touch" to data. It is thread-safe and supports
 * internationalization.
 * 
 * @author mfornos
 * 
 */
public final class Humanize {

	private static final ContextFactory contextFactory = loadContextFactory();

	private static final ThreadLocal<Context> context = new ThreadLocal<Context>() {
		protected Context initialValue() {
			return contextFactory.createContext();
		};
	};

	/**
	 * Converts a given number to a string preceded by the corresponding binary
	 * International System of Units (SI) prefix.
	 * 
	 * <p>
	 * 
	 * E.g. 2 becomes '2 bytes', 1536 becomes '1.5 kB', 5242880 becomes '5.00
	 * MB', 1325899906842624 becomes '1.18 PB'
	 * 
	 * @param Number
	 *            The number to convert
	 * @return The number preceded by the corresponding binary SI prefix
	 */
	public static String binaryPrefix(Number value) {
		long v = value.longValue();

		if (v < 0)
			return value.toString();

		for (Long num : binPrexies.keySet())
			if (num <= v)
				return format(binPrexies.get(num), (v >= 1024) ? v / (float) num : v);

		return value.toString(); // unreachable
	}

	/**
	 * Smartly formats the given number as a monetary amount.
	 * 
	 * <p>
	 * 
	 * E.g. for en_GB 34 becomes '£34', 1000 becomes '£1,000', 12.5 becomes
	 * '£12.50'
	 * 
	 * @param Number
	 *            The number to format
	 * @return String representing the monetary amount
	 */
	public static String formatCurrency(Number value) {
		DecimalFormat decf = (DecimalFormat) context.get().getCurrencyFormat();
		char decsep = decf.getDecimalFormatSymbols().getDecimalSeparator();
		String fmtd = decf.format(value);
		return fmtd.replaceAll("\\" + decsep + "00", EMPTY_STRING);
	}

	/**
	 * Same as {@link #formatCurrency(Number) formatCurrency} for the specified
	 * locale.
	 * 
	 * @param Number
	 *            The number to format
	 * @param Locale
	 *            The locale
	 * @return String representing the monetary amount
	 */
	public static String formatCurrency(final Number value, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return formatCurrency(value);
			}
		}, locale);
	}

	/**
	 * Same as {@link #formatDate(int, Date) formatDate} with DateFormat.SHORT
	 * style.
	 * 
	 * @param Date
	 *            The date
	 * @return String representation of the date
	 */
	public static String formatDate(Date value) {
		return formatDate(DateFormat.SHORT, value);
	}

	/**
	 * Formats the given date with the specified DateFormat style.
	 * 
	 * @param int style DateFormat style
	 * @param Date
	 *            The date
	 * @return String representation of the date
	 */
	public static String formatDate(int style, Date value) {
		return context.get().formatDate(style, value);
	}

	/**
	 * Formats the given number to the standard decimal format for the default
	 * locale.
	 * 
	 * @param Number
	 *            The number to format
	 * @return Standard localized format representation
	 */
	public static String formatDecimal(Number value) {
		return context.get().formatDecimal(value);
	}

	/**
	 * Same as {@link #formatDecimal(Number) formatDecimal} for the specified
	 * locale.
	 * 
	 * @param Number
	 *            The number to format
	 * @param Locale
	 *            The locale
	 * @return Standard localized format representation
	 */
	public static String formatDecimal(final Number value, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return formatDecimal(value);
			}
		}, locale);
	}

	/**
	 * Same as {@link #naturalDay(int, Date) naturalDay} with DateFormat.SHORT
	 * style.
	 * 
	 * @param Date
	 *            The date
	 * @return String with 'today', 'tomorrow' or 'yesterday' compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	public static String naturalDay(Date value) {
		return naturalDay(DateFormat.SHORT, value);
	}

	/**
	 * For dates that are the current day or within one day, return 'today',
	 * 'tomorrow' or 'yesterday', as appropriate. Otherwise, returns a string
	 * formatted according to a locale sensitive DateFormat.
	 * 
	 * @param int The style of the Date
	 * @param Date
	 *            The date (GMT)
	 * @return String with 'today', 'tomorrow' or 'yesterday' compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	public static String naturalDay(int style, Date value) {
		Date today = new Date();
		long delta = value.getTime() - today.getTime();
		long days = delta / ND_FACTOR;

		if (days == 0)
			return context.get().getMessage("today");
		else if (days == 1)
			return context.get().getMessage("tomorrow");
		else if (days == -1)
			return context.get().getMessage("yesterday");

		return formatDate(style, value);
	}

	/**
	 * Same as {@link #naturalTime(Date, Date) naturalTime} with current date as
	 * reference.
	 * 
	 * @param Date
	 *            The duration
	 * @return String representing the relative date
	 */
	public static String naturalTime(Date duration) {
		return context.get().formatRelativeDate(duration);
	}

	/**
	 * Computes both past and future relative dates.
	 * 
	 * <p>
	 * 
	 * E.g. 'one day ago', 'one day from now', '10 years ago', '3 minutes from
	 * now', 'right now' and so on.
	 * 
	 * @param Date
	 *            The reference
	 * @param Date
	 *            The duration
	 * @return String representing the relative date
	 */
	public static String naturalTime(Date reference, Date duration) {
		return context.get().formatRelativeDate(reference, duration);
	}

	/**
	 * Same as {@link #naturalTime(Date, Date) naturalTime} for the specified
	 * locale.
	 * 
	 * @param Date
	 *            The reference
	 * @param Date
	 *            The duration
	 * @param Locale
	 *            The locale
	 * @return String representing the relative date
	 */
	public static String naturalTime(final Date reference, final Date duration, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return naturalTime(reference, duration);
			}
		}, locale);
	}

	/**
	 * Same as {@link #naturalTime(Date) naturalTime} for the specified locale.
	 * 
	 * @param Date
	 *            The duration
	 * @param Locale
	 *            The locale
	 * @return String representing the relative date
	 */
	public static String naturalTime(final Date duration, final Locale locale) {
		return naturalTime(new Date(), duration, locale);
	}

	/**
	 * Converts a number to its ordinal as a string.
	 * 
	 * <p>
	 * 
	 * E.g. 1 becomes '1st', 2 becomes '2nd', 3 becomes '3rd', etc.
	 * 
	 * @param Number
	 *            The number to convert
	 * @return String representing the number as ordinal
	 */
	public static String ordinal(Number value) {
		int v = value.intValue();
		int vc = v % 100;

		if (vc > 10 && vc < 14)
			return format(ORDINAL_FMT, v, context.get().ordinalSuffix(0));

		return format(ORDINAL_FMT, v, context.get().ordinalSuffix(v % 10));
	}

	/**
	 * Same as {@link #ordinal(Number) ordinal} for the specified locale.
	 * 
	 * @param Number
	 *            The number to convert
	 * @param Locale
	 *            The locale
	 * @return String representing the number as ordinal
	 */
	public static String ordinal(final Number value, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return ordinal(value);
			}
		}, locale);
	}

	/**
	 * Constructs a message with pluralization logic by the means of
	 * ChoiceFormat.
	 * 
	 * @param String
	 *            The base pattern
	 * @param []String The possible choices as strings
	 * @return Message prepared to generate pluralized strings
	 */
	public static Message pluralize(String pattern, String... choices) {
		double[] indexes = new double[choices.length];
		for (int i = 0; i < choices.length; i++)
			indexes[i] = i;

		ChoiceFormat choiceForm = new ChoiceFormat(indexes, choices);
		Message format = context.get().getFormat();
		format.applyPattern(pattern);
		format.setFormat(0, choiceForm);

		return format;
	}

	/**
	 * Converts a big number to a friendly text representation. Accepts values
	 * ranging from thousands to googols. Uses BigDecimal.
	 * 
	 * @param Number
	 *            The number to convert
	 * @return Friendly text representation of the given value
	 */
	public static String spellBigNumber(Number value) {
		BigDecimal v = new BigDecimal(value.toString());

		if (THOUSAND.compareTo(v.abs()) > 0)
			return value.toString();

		boolean isPlural = needPlural(v.unscaledValue().intValue());

		for (BigDecimal bigNum : bigDecExponents.keySet())
			if (bigNum.multiply(THOUSAND).compareTo(v.abs()) > 0)
				return context.get().formatMessage(
				        (isPlural) ? bigDecExponents.get(bigNum) + ".pl" : bigDecExponents.get(bigNum),
				        v.divide(bigNum));

		return value.toString();
	}

	/**
	 * Same as {@link #spellBigNumber(Number) spellBigNumber} for the specified
	 * locale.
	 * 
	 * @param Number
	 *            The number to convert
	 * @param Locale
	 *            The locale
	 * @return Friendly text representation of the given value
	 */
	public static String spellBigNumber(final Number value, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return spellBigNumber(value);
			}
		}, locale);
	}

	/**
	 * For decimal digits, returns the number spelled out. Otherwise, returns
	 * the number as string.
	 * 
	 * <p>
	 * 
	 * E.g. 1 becomes 'one', 2 becomes 'two', 10 becomes '10'
	 * 
	 * @param Number
	 *            Decimal digit
	 * @return The number spelled out
	 */
	public static String spellDigit(Number value) {
		int v = value.intValue();
		if (v < 0 || v > 9)
			return value.toString();

		return context.get().digitStrings(v);
	}

	/**
	 * Same as {@link #spellDigit(Number) spellDigit} for the specified locale.
	 * 
	 * @param Number
	 *            Decimal digit
	 * @param Locale
	 *            The locale
	 * @return The number spelled out
	 */
	public static String spellDigit(final Number value, final Locale locale) {
		return withinLocale(new Callable<String>() {
			public String call() {
				return spellDigit(value);
			}
		}, locale);
	}

	/**
	 * Truncate a string to the closest word boundary after a number of
	 * characters.
	 * 
	 * @param String
	 *            The text
	 * @param int Number of characters
	 * @return String truncated to the closes word boundary
	 */
	public static String wordWrap(String value, int len) {
		if (len < 0 || value.length() <= len)
			return value;

		BreakIterator bi = BreakIterator.getWordInstance(context.get().getLocale());
		bi.setText(value);
		return value.substring(0, bi.following(len));
	}

	// ( private methods )------------------------------------------------------

	private static ContextFactory loadContextFactory() {
		ServiceLoader<ContextFactory> ldr = ServiceLoader.load(ContextFactory.class);
		for (ContextFactory factory : ldr) {
			return factory;
		}
		throw new RuntimeException("No ContextFactory was found");
	}

	/**
	 * Checks if the given integer contains any digit greater than 1.
	 * 
	 * @param int The number
	 * @return true if the integer contains a digit greater than 1, false
	 *         otherwise
	 */
	private static boolean needPlural(int n) {
		int tmp = 0;
		n = Math.abs(n);

		while (n > 0) {
			tmp = n % 10;
			if (tmp > 1)
				return true;
			n /= 10;
		}

		return false;
	}

	/**
	 * Wraps the given operation on a context with the specified locale.
	 * 
	 * @param Callable
	 *            The operation
	 * @param Locale
	 *            The locale
	 * @return String with the results of the operation
	 */
	private static String withinLocale(Callable<String> operation, Locale locale) {
		Context ctx = context.get();
		Locale oldLocale = ctx.getLocale();
		try {
			ctx.setLocale(locale);
			return operation.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			ctx.setLocale(oldLocale);
			context.set(ctx);
		}
	}
	
}
