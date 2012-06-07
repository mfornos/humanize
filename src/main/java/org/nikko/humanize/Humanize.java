package org.nikko.humanize;

import static java.lang.String.format;
import static org.nikko.humanize.util.Constants.EMPTY_STRING;
import static org.nikko.humanize.util.Constants.ND_FACTOR;
import static org.nikko.humanize.util.Constants.ORDINAL_FMT;
import static org.nikko.humanize.util.Constants.SPACE_STRING;
import static org.nikko.humanize.util.Constants.SPLIT_CAMEL_REGEX;
import static org.nikko.humanize.util.Constants.THOUSAND;
import static org.nikko.humanize.util.Constants.bigDecExponents;
import static org.nikko.humanize.util.Constants.binPrefixes;
import static org.nikko.humanize.util.Constants.metricPrefixes;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.spi.context.Context;
import org.nikko.humanize.spi.context.ContextFactory;
import org.nikko.humanize.util.RelativeDate;

/**
 * <p>
 * Facility for adding a "human touch" to data. It is thread-safe and supports
 * per-thread internationalization. Additionally provides a concise facade for
 * access to the standard internationalization Java APIs.
 * </p>
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
	 * <p>
	 * Converts a given number to a string preceded by the corresponding binary
	 * International System of Units (SI) prefix.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>"2 bytes"</td>
	 * </tr>
	 * <tr>
	 * <td>1536</td>
	 * <td>"1.5 kB"</td>
	 * </tr>
	 * <tr>
	 * <td>5242880</td>
	 * <td>"5.00 MB"</td>
	 * </tr>
	 * <tr>
	 * <td>1325899906842624L</td>
	 * <td>"1.18 PB"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Number to be converted
	 * @return The number preceded by the corresponding binary SI prefix
	 */
	public static String binaryPrefix(Number value) {

		return prefix(value, 1024, binPrefixes);

	}

	/**
	 * <p>
	 * Same as {@link #binaryPrefix(Number) binaryPrefix} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return The number preceded by the corresponding binary SI prefix
	 */
	public static String binaryPrefix(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return binaryPrefix(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #camelize(String, boolean) camelize} with capitalize to
	 * false.
	 * </p>
	 * 
	 * @param text
	 *            String to be camelized
	 * @return Camelized string
	 */
	public static String camelize(String text) {

		return camelize(text, false);

	}

	/**
	 * <p>
	 * Makes a phrase camel case. Spaces and underscores will be removed.
	 * </p>
	 * 
	 * @param capitalizeFirstChar
	 *            true makes the first letter uppercase
	 * @param text
	 *            String to be camelized
	 * @return Camelized string
	 */
	public static String camelize(String text, boolean capitalizeFirstChar) {

		StringBuilder sb = new StringBuilder();
		String[] tokens = text.split("[\\s_]+");

		if (tokens.length < 2)
			return capitalizeFirstChar ? capitalize(text) : text.toLowerCase();

		for (String token : tokens)
			sb.append(capitalize(token));

		return capitalizeFirstChar ? sb.toString() : sb.substring(0, 1).toLowerCase() + sb.substring(1);

	}

	/**
	 * <p>
	 * Makes the first letter uppercase and the rest lowercase.
	 * </p>
	 * 
	 * @param word
	 *            String to be capitalized
	 * @return capitalized string
	 */
	public static String capitalize(String word) {

		if (word.length() == 0)
			return word;
		return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();

	}

	/**
	 * <p>
	 * Converts a camel case string into a human-readable name.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>"MyClass"</td>
	 * <td>"My Class"</td>
	 * </tr>
	 * <tr>
	 * <td>"GL11Version"</td>
	 * <td>"GL 11 Version"</td>
	 * </tr>
	 * <tr>
	 * <td>"AString"</td>
	 * <td>"A String"</td>
	 * </tr>
	 * <tr>
	 * <td>"SimpleXMLParser"</td>
	 * <td>"Simple XML Parser"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param words
	 *            String to be converted
	 * @return words converted to human-readable name
	 */
	public static String decamelize(String words) {

		return SPLIT_CAMEL_REGEX.matcher(words).replaceAll(SPACE_STRING);

	}

	/**
	 * <p>
	 * Smartly formats the given number as a monetary amount.
	 * </p>
	 * 
	 * <p>
	 * For en_GB:
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>34</td>
	 * <td>"£34"</td>
	 * </tr>
	 * <tr>
	 * <td>1000</td>
	 * <td>"£1,000"</td>
	 * </tr>
	 * <tr>
	 * <td>12.5</td>
	 * <td>"£12.50"</td>
	 * </tr>
	 * </table>
	 * </p>
	 * 
	 * @param value
	 *            Number to be formatted
	 * @return String representing the monetary amount
	 */
	public static String formatCurrency(Number value) {

		DecimalFormat decf = context.get().getCurrencyFormat();
		return stripZeros(decf, decf.format(value));

	}

	/**
	 * <p>
	 * Same as {@link #formatCurrency(Number) formatCurrency} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be formatted
	 * @param locale
	 *            Target locale
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
	 * <p>
	 * Same as {@link #formatDate(int, Date) formatDate} with DateFormat.SHORT
	 * style.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDate(Date value) {

		return formatDate(DateFormat.SHORT, value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDate(Date) formatDate} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @param locale
	 *            Target locale
	 * @return String representation of the date
	 */
	public static String formatDate(final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatDate(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Formats the given date with the specified style.
	 * </p>
	 * 
	 * @param style
	 *            DateFormat style
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDate(int style, Date value) {

		return context.get().formatDate(style, value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDate(int, Date) formatDate} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param style
	 *            DateFormat style
	 * @param value
	 *            Date to be formatted
	 * @param locale
	 *            Target locale
	 * @return String representation of the date
	 */
	public static String formatDate(final int style, final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatDate(style, value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Formats the given date/time with SHORT style.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDateTime(Date value) {

		return context.get().formatDateTime(value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDateTime(Date) formatDateTime} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @param locale
	 *            Target locale
	 * @return String representation of the date
	 */
	public static String formatDateTime(final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatDateTime(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Formats the given date/time with the specified styles.
	 * </p>
	 * 
	 * @param dateStyle
	 *            DateFormat date style
	 * @param timeStyle
	 *            DateFormat time style
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDateTime(int dateStyle, int timeStyle, Date value) {

		return context.get().formatDateTime(dateStyle, timeStyle, value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDateTime(int, int, Date) formatDateTime} for the
	 * specified locale.
	 * </p>
	 * 
	 * @param dateStyle
	 * @param timeStyle
	 * @param value
	 *            Date to be formatted
	 * @param locale
	 *            Target locale
	 * @return String representation of the date
	 */
	public static String formatDateTime(final int dateStyle, final int timeStyle, final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatDateTime(dateStyle, timeStyle, value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Formats the given number to the standard decimal format for the default
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be formatted
	 * @return Standard localized format representation
	 */
	public static String formatDecimal(Number value) {

		return context.get().formatDecimal(value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDecimal(Number) formatDecimal} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be formatted
	 * @param locale
	 *            Target locale
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
	 * <p>
	 * Formats the given ratio as a percentage.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>0.5</td>
	 * <td>"50%"</td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>"100%"</td>
	 * </tr>
	 * <tr>
	 * <td>0.564</td>
	 * <td>"56%"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Ratio to be converted
	 * @return String representing the percentage
	 */
	public static String formatPercent(Number value) {

		return context.get().getPercentFormat().format(value);

	}

	/**
	 * <p>
	 * Same as {@link #formatPercent(Number) formatPercent} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Ratio to be converted
	 * @param locale
	 *            Target locale
	 * @return String representing the percentage
	 */
	public static String formatPercent(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatPercent(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Creates a RelativeDate instance. It is useful to compute multiple
	 * relative dates with the same instance.
	 * </p>
	 * 
	 * @return RelativeDate instance
	 */
	public static RelativeDate getRelativeDateInstance() {

		return context.get().getRelativeDate();

	}

	/**
	 * <p>
	 * Same as {@link #getRelativeDateInstance() getRelativeDateInstance} for
	 * the specified locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @return RelativeDate instance
	 */
	public static RelativeDate getRelativeDateInstance(final Locale locale) {

		return withinLocale(new Callable<RelativeDate>() {
			public RelativeDate call() throws Exception {

				return context.get().getRelativeDate();

			}
		}, locale);

	}

	/**
	 * <p>
	 * Converts a given number to a string preceded by the corresponding decimal
	 * multiplicative prefix.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>200</td>
	 * <td>"200"</td>
	 * </tr>
	 * <tr>
	 * <td>1000</td>
	 * <td>"1k"</td>
	 * </tr>
	 * <tr>
	 * <td>3500000</td>
	 * <td>"3.5M"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Number to be converted
	 * @return The number preceded by the corresponding SI prefix
	 */
	public static String metricPrefix(Number value) {

		return prefix(value, 1000, metricPrefixes);

	}

	/**
	 * <p>
	 * Same as {@link #metricPrefix(Number) metricPrefix} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return The number preceded by the corresponding SI prefix
	 */
	public static String metricPrefix(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return metricPrefix(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #naturalDay(int, Date) naturalDay} with DateFormat.SHORT
	 * style.
	 * </p>
	 * 
	 * @param value
	 *            Date to be converted
	 * @return String with "today", "tomorrow" or "yesterday" compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	public static String naturalDay(Date value) {

		return naturalDay(DateFormat.SHORT, value);

	}

	/**
	 * <p>
	 * Same as {@link #naturalDay(Date) naturalDay} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Date to be converted
	 * @param locale
	 *            Target locale
	 * @return String with "today", "tomorrow" or "yesterday" compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	public static String naturalDay(final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return naturalDay(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * For dates that are the current day or within one day, return "today",
	 * "tomorrow" or "yesterday", as appropriate. Otherwise, returns a string
	 * formatted according to a locale sensitive DateFormat.
	 * </p>
	 * 
	 * @param style
	 *            DateFormat style
	 * @param value
	 *            Date to be converted
	 * @return String with "today", "tomorrow" or "yesterday" compared to
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
	 * <p>
	 * Same as {@link #naturalTime(Date, Date) naturalTime} with current date as
	 * reference.
	 * </p>
	 * 
	 * @param duration
	 *            Date to be used as duration from current date
	 * @return String representing the relative date
	 */
	public static String naturalTime(Date duration) {

		return context.get().formatRelativeDate(duration);

	}

	/**
	 * <p>
	 * Computes both past and future relative dates.
	 * </p>
	 * 
	 * <p>
	 * E.g. "one day ago", "one day from now", "10 years ago", "3 minutes from
	 * now", "right now" and so on.
	 * </p>
	 * 
	 * @param reference
	 *            Date to be used as reference
	 * @param duration
	 *            Date to be used as duration from reference
	 * @return String representing the relative date
	 */
	public static String naturalTime(Date reference, Date duration) {

		return context.get().formatRelativeDate(reference, duration);

	}

	/**
	 * <p>
	 * Same as {@link #naturalTime(Date, Date) naturalTime} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param reference
	 *            Date to be used as reference
	 * @param duration
	 *            Date to be used as duration from reference
	 * @param locale
	 *            Target locale
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
	 * @param duration
	 *            Date to be used as duration from current date
	 * @param locale
	 *            Target locale
	 * @return String representing the relative date
	 */
	public static String naturalTime(final Date duration, final Locale locale) {

		return naturalTime(new Date(), duration, locale);

	}

	/**
	 * <p>
	 * Converts a number to its ordinal as a string.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>"1st"</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>"2nd"</td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td>"3rd"</td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td>"4th"</td>
	 * </tr>
	 * <tr>
	 * <td>1002</td>
	 * <td>"1002nd"</td>
	 * </tr>
	 * <tr>
	 * <td>2012</td>
	 * <td>"2012th"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Number to be converted
	 * @return String representing the number as ordinal
	 */
	public static String ordinalize(Number value) {

		int v = value.intValue();
		int vc = v % 100;

		if (vc > 10 && vc < 14)
			return format(ORDINAL_FMT, v, context.get().ordinalSuffix(0));

		return format(ORDINAL_FMT, v, context.get().ordinalSuffix(v % 10));

	}

	/**
	 * <p>
	 * Same as {@link #ordinalize(Number) ordinalize} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return String representing the number as ordinal
	 */
	public static String ordinalize(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() {

				return ordinalize(value);

			}
		}, locale);
	}

	/**
	 * <p>
	 * Constructs a message with pluralization logic from the given template.
	 * </p>
	 * 
	 * Example:
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	Message msg = pluralize(&quot;There {0} on {1}.::are no files::is one file::are {2} files&quot;);
	 * 
	 * 	msg.render(0, &quot;disk&quot;); // == &quot;There are no files on disk.&quot;
	 * 	msg.render(1, &quot;disk&quot;); // == &quot;There is one file on disk.&quot;
	 * 	msg.render(1000, &quot;disk&quot;); // == &quot;There are 1,000 files on disk.&quot;
	 * }
	 * </pre>
	 * 
	 * @param template
	 *            String of tokens delimited by '::'
	 * 
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static Message pluralize(String template) {

		String[] tokens = template.split("\\s*\\:{2}\\s*");

		if (tokens.length < 3)
			throw new IllegalArgumentException(format("Template '%s' must declare at least 3 tokens", template));

		return pluralize(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));

	}

	/**
	 * <p>
	 * Same as {@link #pluralize(String) pluralize} for the specified locale.
	 * </p>
	 * 
	 * @param template
	 *            String of tokens delimited by '::'
	 * @param locale
	 *            Target locale
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static Message pluralize(final String template, Locale locale) {

		return withinLocale(new Callable<Message>() {
			public Message call() throws Exception {

				return pluralize(template);

			};
		}, locale);

	}

	/**
	 * <p>
	 * Constructs a message with pluralization logic by the means of
	 * ChoiceFormat.
	 * </p>
	 * 
	 * @param pattern
	 *            Base pattern
	 * @param choices
	 *            Values that match the pattern
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static Message pluralize(String pattern, String... choices) {

		double[] indexes = new double[choices.length];
		for (int i = 0; i < choices.length; i++)
			indexes[i] = i;

		ChoiceFormat choiceForm = new ChoiceFormat(indexes, choices);
		Message format = context.get().getMessageFormat();
		format.applyPattern(pattern);
		format.setFormat(0, choiceForm);

		return format;

	}

	/**
	 * <p>
	 * Converts a big number to a friendly text representation. Accepts values
	 * ranging from thousands to googols. Uses BigDecimal.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
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
	 * <p>
	 * Same as {@link #spellBigNumber(Number) spellBigNumber} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
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
	 * <p>
	 * For decimal digits [0-9], returns the number spelled out. Otherwise,
	 * returns the number as string.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>"one"</td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td>"two"</td>
	 * </tr>
	 * <tr>
	 * <td>10</td>
	 * <td>"10"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Decimal digit
	 * @return String representing the number spelled out
	 */
	public static String spellDigit(Number value) {

		int v = value.intValue();
		if (v < 0 || v > 9)
			return value.toString();

		return context.get().digitStrings(v);

	}

	/**
	 * <p>
	 * Same as {@link #spellDigit(Number) spellDigit} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Decimal digit
	 * @param locale
	 *            Target locale
	 * @return String representing the number spelled out
	 */
	public static String spellDigit(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() {

				return spellDigit(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Capitalize all the words, and replace some characters in the string to
	 * create a nice looking title.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>"the_jackie_gleason show"</td>
	 * <td>"The Jackie Gleason Show"</td>
	 * </tr>
	 * <tr>
	 * <td>"first annual report (CD) 2001"</td>
	 * <td>"First Annual Report (CD) 2001"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param text
	 *            Text to be converted
	 * 
	 * @return Nice looking title
	 */
	public static String titleize(String text) {

		StringBuilder sb = new StringBuilder(text.length());
		boolean capitalize = true; // To get the first character right
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (Character.isWhitespace(ch)) {
				sb.append(' ');
				capitalize = true;
			} else if (ch == '_') {
				sb.append(' ');
				capitalize = true;
			} else if (capitalize) {
				sb.append(Character.toUpperCase(ch));
				capitalize = false;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();

	}

	/**
	 * <p>
	 * Converts the given number to words.
	 * </p>
	 * 
	 * <table border="0" cellpadding="3" cellspacing="0" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>2840</td>
	 * <td>"two thousand eight hundred and forty"</td>
	 * </tr>
	 * <tr>
	 * <td>1412605</td>
	 * <td>"one million four hundred and twelve thousand six hundred and five"</td>
	 * </tr>
	 * <tr>
	 * <td>23380000000L</td>
	 * <td>"twenty-three billion three hundred and eighty million"</td>
	 * </tr>
	 * <tr>
	 * <td>90489348043803948043 BigInt</td>
	 * <td>
	 * "ninety quintillion four hundred and eighty-nine quadrillion three
	 * hundred and forty-eight trillion and forty-three billion eight hundred
	 * and three million nine hundred and forty-eight thousand and forty-three"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param value
	 *            Number to be converted
	 * @return the number converted to words
	 */
	public static String toText(Number value) {

		return context.get().toText(value);
	}

	/**
	 * <p>
	 * Same as {@link #toText(Number) toText} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return the number converted to words
	 */
	public static String toText(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return toText(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Makes a phrase underscored instead of spaced.
	 * </p>
	 * 
	 * @param text
	 *            Phrase to underscore
	 * @return converted String
	 */
	public static String underscore(String text) {

		return text.replaceAll("\\s+", "_");

	}

	/**
	 * <p>
	 * Truncate a string to the closest word boundary after a number of
	 * characters.
	 * </p>
	 * 
	 * @param value
	 *            Text to be truncated
	 * @param len
	 *            Number of characters
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
	 * <p>
	 * Checks if the given integer contains any digit greater than 1.
	 * </p>
	 * 
	 * @param n
	 *            Number to be evaluated
	 * @return true if the number contains a digit greater than 1, false
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

	private static String prefix(final Number value, final int min, final Map<Long, String> prefixes) {

		DecimalFormat df = context.get().getDecimalFormat();

		long v = value.longValue();

		if (v < 0)
			return value.toString();

		for (Long num : prefixes.keySet())
			if (num <= v) {
				df.applyPattern(prefixes.get(num));
				return stripZeros(df, df.format((v >= min) ? v / (float) num : v));
			}

		return stripZeros(df, df.format(value.toString()));
	}

	private static String stripZeros(DecimalFormat decf, String fmtd) {

		char decsep = decf.getDecimalFormatSymbols().getDecimalSeparator();
		return fmtd.replaceAll("\\" + decsep + "00", EMPTY_STRING);

	}

	/**
	 * <p>
	 * Wraps the given operation on a context with the specified locale.
	 * </p>
	 * 
	 * @param operation
	 *            Operation to be performed
	 * @param locale
	 *            Target locale
	 * @return Result of the operation
	 */
	private static <T> T withinLocale(Callable<T> operation, Locale locale) {

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

	private Humanize() {

	}

}
