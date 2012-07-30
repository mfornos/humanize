package humanize;

import static humanize.util.Constants.EMPTY;
import static humanize.util.Constants.SPACE;
import static humanize.util.Constants.SPLIT_CAMEL;
import static humanize.util.Constants.THOUSAND;
import static humanize.util.Constants.bigDecExponents;
import static humanize.util.Constants.binPrefixes;
import static humanize.util.Constants.metricPrefixes;
import humanize.spi.MessageFormat;
import humanize.spi.context.ContextFactory;
import humanize.spi.context.DefaultContext;
import humanize.spi.context.DefaultContextFactory;
import humanize.text.MaskFormat;
import humanize.text.Replacer;
import humanize.text.TextUtils;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

/**
 * <p>
 * Facility for adding a "human touch" to data. It is thread-safe and supports
 * per-thread internationalization. Additionally provides a concise facade for
 * access to the Standard i18n Java APIs.
 * </p>
 * 
 * @author mfornos
 * 
 */
public final class Humanize {

	private static final ContextFactory contextFactory = loadContextFactory();

	private static final ThreadLocal<DefaultContext> context = new ThreadLocal<DefaultContext>() {
		protected DefaultContext initialValue() {

			return (DefaultContext) contextFactory.createContext();

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
	public static String binaryPrefix(final Number value) {

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
	public static String camelize(final String text) {

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
	public static String camelize(final String text, final boolean capitalizeFirstChar) {

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
	public static String capitalize(final String word) {

		if (word.length() == 0)
			return word;
		return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();

	}

	/**
	 * <p>
	 * Returns a SimpleDateFormat instance for the current thread.
	 * </p>
	 * <h4>Date and Time Patterns</h4>
	 * <p>
	 * Date and time formats are specified by <em>date and time pattern</em>
	 * strings. Within date and time pattern strings, unquoted letters from
	 * <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
	 * <code>'z'</code> are interpreted as pattern letters representing the
	 * components of a date or time string. Text can be quoted using single
	 * quotes (<code>'</code>) to avoid interpretation. <code>"''"</code>
	 * represents a single quote. All other characters are not interpreted;
	 * they're simply copied into the output string during formatting or matched
	 * against the input string during parsing.
	 * <p>
	 * The following pattern letters are defined (all other characters from
	 * <code>'A'</code> to <code>'Z'</code> and from <code>'a'</code> to
	 * <code>'z'</code> are reserved): <blockquote>
	 * <table border=0 cellspacing=3 cellpadding=0 summary="Chart shows pattern letters, date/time component, presentation, and examples.">
	 * <tr bgcolor="#ccccff">
	 * <th align=left>Letter
	 * <th align=left>Date or Time Component
	 * <th align=left>Presentation
	 * <th align=left>Examples
	 * <tr>
	 * <td><code>G</code>
	 * <td>Era designator
	 * <td><a href="#text">Text</a>
	 * <td><code>AD</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>y</code>
	 * <td>Year
	 * <td><a href="#year">Year</a>
	 * <td><code>1996</code>; <code>96</code>
	 * <tr>
	 * <td><code>M</code>
	 * <td>Month in year
	 * <td><a href="#month">Month</a>
	 * <td><code>July</code>; <code>Jul</code>; <code>07</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>w</code>
	 * <td>Week in year
	 * <td><a href="#number">Number</a>
	 * <td><code>27</code>
	 * <tr>
	 * <td><code>W</code>
	 * <td>Week in month
	 * <td><a href="#number">Number</a>
	 * <td><code>2</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>D</code>
	 * <td>Day in year
	 * <td><a href="#number">Number</a>
	 * <td><code>189</code>
	 * <tr>
	 * <td><code>d</code>
	 * <td>Day in month
	 * <td><a href="#number">Number</a>
	 * <td><code>10</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>F</code>
	 * <td>Day of week in month
	 * <td><a href="#number">Number</a>
	 * <td><code>2</code>
	 * <tr>
	 * <td><code>E</code>
	 * <td>Day in week
	 * <td><a href="#text">Text</a>
	 * <td><code>Tuesday</code>; <code>Tue</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>a</code>
	 * <td>Am/pm marker
	 * <td><a href="#text">Text</a>
	 * <td><code>PM</code>
	 * <tr>
	 * <td><code>H</code>
	 * <td>Hour in day (0-23)
	 * <td><a href="#number">Number</a>
	 * <td><code>0</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>k</code>
	 * <td>Hour in day (1-24)
	 * <td><a href="#number">Number</a>
	 * <td><code>24</code>
	 * <tr>
	 * <td><code>K</code>
	 * <td>Hour in am/pm (0-11)
	 * <td><a href="#number">Number</a>
	 * <td><code>0</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>h</code>
	 * <td>Hour in am/pm (1-12)
	 * <td><a href="#number">Number</a>
	 * <td><code>12</code>
	 * <tr>
	 * <td><code>m</code>
	 * <td>Minute in hour
	 * <td><a href="#number">Number</a>
	 * <td><code>30</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>s</code>
	 * <td>Second in minute
	 * <td><a href="#number">Number</a>
	 * <td><code>55</code>
	 * <tr>
	 * <td><code>S</code>
	 * <td>Millisecond
	 * <td><a href="#number">Number</a>
	 * <td><code>978</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>z</code>
	 * <td>Time zone
	 * <td><a href="#timezone">General time zone</a>
	 * <td><code>Pacific Standard Time</code>; <code>PST</code>;
	 * <code>GMT-08:00</code>
	 * <tr>
	 * <td><code>Z</code>
	 * <td>Time zone
	 * <td><a href="#rfc822timezone">RFC 822 time zone</a>
	 * <td><code>-0800</code>
	 * </table>
	 * </blockquote> Pattern letters are usually repeated, as their number
	 * determines the exact presentation:
	 * <ul>
	 * <li><strong><a name="text">Text:</a></strong> For formatting, if the
	 * number of pattern letters is 4 or more, the full form is used; otherwise
	 * a short or abbreviated form is used if available. For parsing, both forms
	 * are accepted, independent of the number of pattern letters.
	 * <li><strong><a name="number">Number:</a></strong> For formatting, the
	 * number of pattern letters is the minimum number of digits, and shorter
	 * numbers are zero-padded to this amount. For parsing, the number of
	 * pattern letters is ignored unless it's needed to separate two adjacent
	 * fields.
	 * <li><strong><a name="year">Year:</a></strong> If the formatter's
	 * {@link #getCalendar() Calendar} is the Gregorian calendar, the following
	 * rules are applied.<br>
	 * <ul>
	 * <li>For formatting, if the number of pattern letters is 2, the year is
	 * truncated to 2 digits; otherwise it is interpreted as a <a
	 * href="#number">number</a>.
	 * <li>For parsing, if the number of pattern letters is more than 2, the
	 * year is interpreted literally, regardless of the number of digits. So
	 * using the pattern "MM/dd/yyyy", "01/11/12" parses to Jan 11, 12 A.D.
	 * <li>For parsing with the abbreviated year pattern ("y" or "yy"),
	 * <code>SimpleDateFormat</code> must interpret the abbreviated year
	 * relative to some century. It does this by adjusting dates to be within 80
	 * years before and 20 years after the time the
	 * <code>SimpleDateFormat</code> instance is created. For example, using a
	 * pattern of "MM/dd/yy" and a <code>SimpleDateFormat</code> instance
	 * created on Jan 1, 1997, the string "01/11/12" would be interpreted as Jan
	 * 11, 2012 while the string "05/04/64" would be interpreted as May 4, 1964.
	 * During parsing, only strings consisting of exactly two digits, as defined
	 * by {@link Character#isDigit(char)}, will be parsed into the default
	 * century. Any other numeric string, such as a one digit string, a three or
	 * more digit string, or a two digit string that isn't all digits (for
	 * example, "-1"), is interpreted literally. So "01/02/3" or "01/02/003" are
	 * parsed, using the same pattern, as Jan 2, 3 AD. Likewise, "01/02/-3" is
	 * parsed as Jan 2, 4 BC.
	 * </ul>
	 * Otherwise, calendar system specific forms are applied. For both
	 * formatting and parsing, if the number of pattern letters is 4 or more, a
	 * calendar specific {@linkplain Calendar#LONG long form} is used.
	 * Otherwise, a calendar specific {@linkplain Calendar#SHORT short or
	 * abbreviated form} is used.
	 * <li><strong><a name="month">Month:</a></strong> If the number of pattern
	 * letters is 3 or more, the month is interpreted as <a
	 * href="#text">text</a>; otherwise, it is interpreted as a <a
	 * href="#number">number</a>.
	 * <li><strong><a name="timezone">General time zone:</a></strong> Time zones
	 * are interpreted as <a href="#text">text</a> if they have names. For time
	 * zones representing a GMT offset value, the following syntax is used:
	 * 
	 * <pre>
	 *     <a name="GMTOffsetTimeZone"><i>GMTOffsetTimeZone:</i></a>
	 *             <code>GMT</code> <i>Sign</i> <i>Hours</i> <code>:</code> <i>Minutes</i>
	 *     <i>Sign:</i> one of
	 *             <code>+ -</code>
	 *     <i>Hours:</i>
	 *             <i>Digit</i>
	 *             <i>Digit</i> <i>Digit</i>
	 *     <i>Minutes:</i>
	 *             <i>Digit</i> <i>Digit</i>
	 *     <i>Digit:</i> one of
	 *             <code>0 1 2 3 4 5 6 7 8 9</code>
	 * </pre>
	 * 
	 * <i>Hours</i> must be between 0 and 23, and <i>Minutes</i> must be between
	 * 00 and 59. The format is locale independent and digits must be taken from
	 * the Basic Latin block of the Unicode standard.
	 * <p>
	 * For parsing, <a href="#rfc822timezone">RFC 822 time zones</a> are also
	 * accepted.
	 * <li><strong><a name="rfc822timezone">RFC 822 time zone:</a></strong> For
	 * formatting, the RFC 822 4-digit time zone format is used:
	 * 
	 * <pre>
	 *     <i>RFC822TimeZone:</i>
	 *             <i>Sign</i> <i>TwoDigitHours</i> <i>Minutes</i>
	 *     <i>TwoDigitHours:</i>
	 *             <i>Digit Digit</i>
	 * </pre>
	 * 
	 * <i>TwoDigitHours</i> must be between 00 and 23. Other definitions are as
	 * for <a href="#timezone">general time zones</a>.
	 * <p>
	 * For parsing, <a href="#timezone">general time zones</a> are also
	 * accepted.
	 * </ul>
	 * <code>SimpleDateFormat</code> also supports <em>localized date and time
	 * pattern</em> strings. In these strings, the pattern letters described
	 * above may be replaced with other, locale dependent, pattern letters.
	 * <code>SimpleDateFormat</code> does not deal with the localization of text
	 * other than the pattern letters; that's up to the client of the class.
	 * <p>
	 * 
	 * <h4>Examples</h4>
	 * 
	 * The following examples show how date and time patterns are interpreted in
	 * the U.S. locale. The given date and time are 2001-07-04 12:08:56 local
	 * time in the U.S. Pacific Time time zone. <blockquote>
	 * <table border=0 cellspacing=3 cellpadding=0 summary="Examples of date and time patterns interpreted in the U.S. locale">
	 * <tr bgcolor="#ccccff">
	 * <th align=left>Date and Time Pattern
	 * <th align=left>Result
	 * <tr>
	 * <td><code>"yyyy.MM.dd G 'at' HH:mm:ss z"</code>
	 * <td><code>2001.07.04 AD at 12:08:56 PDT</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>"EEE, MMM d, ''yy"</code>
	 * <td><code>Wed, Jul 4, '01</code>
	 * <tr>
	 * <td><code>"h:mm a"</code>
	 * <td><code>12:08 PM</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>"hh 'o''clock' a, zzzz"</code>
	 * <td><code>12 o'clock PM, Pacific Daylight Time</code>
	 * <tr>
	 * <td><code>"K:mm a, z"</code>
	 * <td><code>0:08 PM, PDT</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>"yyyyy.MMMMM.dd GGG hh:mm aaa"</code>
	 * <td><code>02001.July.04 AD 12:08 PM</code>
	 * <tr>
	 * <td><code>"EEE, d MMM yyyy HH:mm:ss Z"</code>
	 * <td><code>Wed, 4 Jul 2001 12:08:56 -0700</code>
	 * <tr bgcolor="#eeeeff">
	 * <td><code>"yyMMddHHmmssZ"</code>
	 * <td><code>010704120856-0700</code>
	 * <tr>
	 * <td><code>"yyyy-MM-dd'T'HH:mm:ss.SSSZ"</code>
	 * <td><code>2001-07-04T12:08:56.235-0700</code>
	 * </table>
	 * </blockquote>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.SimpleDateFormat SimpleDateFormat}
	 * @return a DateFormat instance for the current thread
	 */
	public static DateFormat dateFormatInstance(final String pattern) {

		return context.get().getDateFormat(pattern);

	}

	/**
	 * <p>
	 * Same as {@link #dateFormatInstance(String) dateFormatInstance} for the
	 * specified locale.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.SimpleDateFormat SimpleDateFormat}
	 * @param locale
	 *            Target locale
	 * @return a DateFormat instance for the current thread
	 */
	public static DateFormat dateFormatInstance(final String pattern, final Locale locale) {

		return withinLocale(new Callable<DateFormat>() {
			public DateFormat call() throws Exception {

				return dateFormatInstance(pattern);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #decamelize(String String) decamelize} defaulting to SPACE
	 * for replacement
	 * </p>
	 * 
	 * @param words
	 *            String to be converted
	 * @return words converted to human-readable name
	 */
	public static String decamelize(final String words) {

		return SPLIT_CAMEL.matcher(words).replaceAll(SPACE);

	}

	/**
	 * <p>
	 * Converts a camel case string into a human-readable name.
	 * </p>
	 * 
	 * <p>
	 * Example assuming SPACE as replacement:
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
	 * @param replacement
	 *            String to be interpolated
	 * @return words converted to human-readable name
	 */
	public static String decamelize(final String words, final String replacement) {

		return SPLIT_CAMEL.matcher(words).replaceAll(replacement);

	}

	/**
	 * <p>
	 * Returns a DecimalFormat instance for the current thread.
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link com.ibm.icu.text.DecimalFormat DecimalFormat}
	 * @return a DecimalFormat instance for the current thread
	 */
	public static DecimalFormat decimalFormatInstance(final String pattern) {

		DecimalFormat decFmt = context.get().getDecimalFormat();
		decFmt.applyPattern(pattern);
		return decFmt;

	}

	/**
	 * <p>
	 * Same as {@link #decimalFormatInstance(String) decimalFormatInstance} for
	 * the specified locale.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link com.ibm.icu.text.DecimalFormat DecimalFormat}
	 * @param locale
	 *            Target locale
	 * @return a DecimalFormat instance for the current thread
	 */
	public static DecimalFormat decimalFormatInstance(final String pattern, final Locale locale) {

		return withinLocale(new Callable<DecimalFormat>() {
			public DecimalFormat call() throws Exception {

				return decimalFormatInstance(pattern);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Gets the ICU based DecimalFormat instance for the current thread with the
	 * given pattern and uses it to format the given arguments.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link com.ibm.icu.text.MessageFormat MessageFormat}
	 * @param args
	 *            Arguments
	 * @return The formatted String
	 */
	public static String format(final String pattern, final Object... args) {

		return messageFormatInstance(pattern).render(args);

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
	public static String formatCurrency(final Number value) {

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
	 * Same as {@link #formatDate(int, Date) formatDate} with SHORT style.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDate(final Date value) {

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
	 * Formats a date according to the given pattern.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @param pattern
	 *            The pattern. See {@link dateFormatInstance(String)}
	 * @return a formatted date/time string
	 */
	public static String formatDate(final Date value, final String pattern) {

		return new SimpleDateFormat(pattern, context.get().getLocale()).format(value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDate(Date, String) formatDate} for the specified
	 * locale.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @param pattern
	 *            The pattern. See {@link dateFormatInstance(String)}
	 * @param locale
	 *            Target locale
	 * @return a formatted date/time string
	 */
	public static String formatDate(final Date value, final String pattern, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return formatDate(value, pattern);

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
	public static String formatDate(final int style, final Date value) {

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
	public static String formatDateTime(final Date value) {

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
	 *            Date style
	 * @param timeStyle
	 *            Time style
	 * @param value
	 *            Date to be formatted
	 * @return String representation of the date
	 */
	public static String formatDateTime(final int dateStyle, final int timeStyle, final Date value) {

		return context.get().formatDateTime(dateStyle, timeStyle, value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDateTime(int, int, Date) formatDateTime} for the
	 * specified locale.
	 * </p>
	 * 
	 * @param dateStyle
	 *            Date style
	 * @param timeStyle
	 *            Time style
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
	public static String formatDecimal(final Number value) {

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
	public static String formatPercent(final Number value) {

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
	 * Formats the given text with the mask specified.
	 * </p>
	 * 
	 * @param mask
	 *            The pattern mask. See {@link MaskFormat} for details.
	 * @param value
	 *            The text to be masked
	 * @return The formatted text
	 */
	public static String mask(final String mask, final String value) {

		return maskFormatInstance(mask).format(value);

	}

	/**
	 * <p>
	 * Returns a {@link MaskFormat} instance for the current thread.
	 * </p>
	 * 
	 * @param mask
	 *            The pattern mask
	 * @return a {@link MaskFormat} instance
	 */
	public static MaskFormat maskFormatInstance(final String mask) {

		MaskFormat maskFmt = context.get().getMaskFormat();
		maskFmt.setMask(mask);
		return maskFmt;

	}

	/**
	 * <p>
	 * Returns an ICU based MessageFormat instance for the current thread. This
	 * formatter supports a rich pattern model. For plural rules see <a
	 * href="http://unicode
	 * .org/repos/cldr-tmp/trunk/diff/supplemental/language_plural_rules
	 * .html">CLDR Language Plural Rules</a>
	 * </p>
	 * 
	 * <h5>Patterns and Their Interpretation</h5>
	 * 
	 * <code>MessageFormat</code> uses patterns of the following form:
	 * <blockquote>
	 * 
	 * <pre>
	 * <i>MessageFormatPattern:</i>
	 *         <i>String</i>
	 *         <i>MessageFormatPattern</i> <i>FormatElement</i> <i>String</i>
	 * 
	 * <i>FormatElement:</i>
	 *         { <i>ArgumentIndexOrName</i> }
	 *         { <i>ArgumentIndexOrName</i> , <i>FormatType</i> }
	 *         { <i>ArgumentIndexOrName</i> , <i>FormatType</i> , <i>FormatStyle</i> }
	 * 
	 * <i>ArgumentIndexOrName: one of </i>
	 *         ['0'-'9']+
	 *         [:ID_START:][:ID_CONTINUE:]*
	 * 
	 * <i>FormatType: one of </i>
	 *         number date time choice
	 * 
	 * <i>FormatStyle:</i>
	 *         short
	 *         medium
	 *         long
	 *         full
	 *         integer
	 *         currency
	 *         percent
	 *         <i>SubformatPattern</i>
	 * 
	 * <i>String:</i>
	 *         <i>StringPart<sub>opt</sub></i>
	 *         <i>String</i> <i>StringPart</i>
	 * 
	 * <i>StringPart:</i>
	 *         ''
	 *         ' <i>QuotedString</i> '
	 *         <i>UnquotedString</i>
	 * 
	 * <i>SubformatPattern:</i>
	 *         <i>SubformatPatternPart<sub>opt</sub></i>
	 *         <i>SubformatPattern</i> <i>SubformatPatternPart</i>
	 * 
	 * <i>SubFormatPatternPart:</i>
	 *         ' <i>QuotedPattern</i> '
	 *         <i>UnquotedPattern</i>
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * <p>
	 * Within a <i>String</i>, <code>"''"</code> represents a single quote. A
	 * <i>QuotedString</i> can contain arbitrary characters except single
	 * quotes; the surrounding single quotes are removed. An
	 * <i>UnquotedString</i> can contain arbitrary characters except single
	 * quotes and left curly brackets. Thus, a string that should result in the
	 * formatted message "'{0}'" can be written as <code>"'''{'0}''"</code> or
	 * <code>"'''{0}'''"</code>.
	 * <p>
	 * Within a <i>SubformatPattern</i>, different rules apply. A
	 * <i>QuotedPattern</i> can contain arbitrary characters except single
	 * quotes; but the surrounding single quotes are <strong>not</strong>
	 * removed, so they may be interpreted by the subformat. For example,
	 * <code>"{1,number,$'#',##}"</code> will produce a number format with the
	 * pound-sign quoted, with a result such as: "$#31,45". An
	 * <i>UnquotedPattern</i> can contain arbitrary characters except single
	 * quotes, but curly braces within it must be balanced. For example,
	 * <code>"ab {0} de"</code> and <code>"ab '}' de"</code> are valid subformat
	 * patterns, but <code>"ab {0'}' de"</code> and <code>"ab } de"</code> are
	 * not.
	 * <p>
	 * <dl>
	 * <dt><b>Warning:</b>
	 * <dd>The rules for using quotes within message format patterns
	 * unfortunately have shown to be somewhat confusing. In particular, it
	 * isn't always obvious to localizers whether single quotes need to be
	 * doubled or not. Make sure to inform localizers about the rules, and tell
	 * them (for example, by using comments in resource bundle source files)
	 * which strings will be processed by MessageFormat. Note that localizers
	 * may need to use single quotes in translated strings where the original
	 * version doesn't have them. <br>
	 * Note also that the simplest way to avoid the problem is to use the real
	 * apostrophe (single quote) character \u2019 (') for human-readable text,
	 * and to use the ASCII apostrophe (\u0027 ' ) only in program syntax, like
	 * quoting in MessageFormat. See the annotations for U+0027 Apostrophe in
	 * The Unicode Standard.
	 * </p>
	 * </dl>
	 * <p>
	 * The <i>ArgumentIndex</i> value is a non-negative integer written using
	 * the digits '0' through '9', and represents an index into the
	 * <code>arguments</code> array passed to the <code>format</code> methods or
	 * the result array returned by the <code>parse</code> methods.
	 * <p>
	 * The <i>FormatType</i> and <i>FormatStyle</i> values are used to create a
	 * <code>Format</code> instance for the format element. The following table
	 * shows how the values map to Format instances. Combinations not shown in
	 * the table are illegal. A <i>SubformatPattern</i> must be a valid pattern
	 * string for the Format subclass used.
	 * <p>
	 * <table border=1>
	 * <tr>
	 * <th>Format Type
	 * <th>Format Style
	 * <th>Subformat Created
	 * <tr>
	 * <td colspan=2><i>(none)</i>
	 * <td><code>null</code>
	 * <tr>
	 * <td rowspan=5><code>number</code>
	 * <td><i>(none)</i>
	 * <td><code>NumberFormat.getInstance(getLocale())</code>
	 * <tr>
	 * <td><code>integer</code>
	 * <td><code>NumberFormat.getIntegerInstance(getLocale())</code>
	 * <tr>
	 * <td><code>currency</code>
	 * <td><code>NumberFormat.getCurrencyInstance(getLocale())</code>
	 * <tr>
	 * <td><code>percent</code>
	 * <td><code>NumberFormat.getPercentInstance(getLocale())</code>
	 * <tr>
	 * <td><i>SubformatPattern</i>
	 * <td>
	 * <code>new DecimalFormat(subformatPattern, new DecimalFormatSymbols(getLocale()))</code>
	 * <tr>
	 * <td rowspan=6><code>date</code>
	 * <td><i>(none)</i>
	 * <td>
	 * <code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
	 * <tr>
	 * <td><code>short</code>
	 * <td>
	 * <code>DateFormat.getDateInstance(DateFormat.SHORT, getLocale())</code>
	 * <tr>
	 * <td><code>medium</code>
	 * <td>
	 * <code>DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())</code>
	 * <tr>
	 * <td><code>long</code>
	 * <td><code>DateFormat.getDateInstance(DateFormat.LONG, getLocale())</code>
	 * <tr>
	 * <td><code>full</code>
	 * <td><code>DateFormat.getDateInstance(DateFormat.FULL, getLocale())</code>
	 * <tr>
	 * <td><i>SubformatPattern</i>
	 * <td><code>new SimpleDateFormat(subformatPattern, getLocale())
	 * <tr>
	 * <td rowspan=6><code>time</code>
	 * <td><i>(none)</i>
	 * <td>
	 * <code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
	 * <tr>
	 * <td><code>short</code>
	 * <td>
	 * <code>DateFormat.getTimeInstance(DateFormat.SHORT, getLocale())</code>
	 * <tr>
	 * <td><code>medium</code>
	 * <td>
	 * <code>DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())</code>
	 * <tr>
	 * <td><code>long</code>
	 * <td><code>DateFormat.getTimeInstance(DateFormat.LONG, getLocale())</code>
	 * <tr>
	 * <td><code>full</code>
	 * <td><code>DateFormat.getTimeInstance(DateFormat.FULL, getLocale())</code>
	 * <tr>
	 * <td><i>SubformatPattern</i>
	 * <td><code>new SimpleDateFormat(subformatPattern, getLocale())
	 * <tr>
	 * <td><code>choice</code>
	 * <td><i>SubformatPattern</i>
	 * <td><code>new ChoiceFormat(subformatPattern)</code>
	 * <tr>
	 * <td><code>spellout</code>
	 * <td><i>Ruleset name (optional)</i>
	 * <td>
	 * <code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.SPELLOUT)<br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
	 * <tr>
	 * <td><code>ordinal</code>
	 * <td><i>Ruleset name (optional)</i>
	 * <td>
	 * <code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.ORDINAL)<br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
	 * <tr>
	 * <td><code>duration</code>
	 * <td><i>Ruleset name (optional)</i>
	 * <td>
	 * <code>new RuleBasedNumberFormat(getLocale(), RuleBasedNumberFormat.DURATION)<br/>&nbsp;&nbsp;&nbsp;&nbsp;.setDefaultRuleset(ruleset);</code>
	 * <tr>
	 * <td><code>plural</code>
	 * <td><i>SubformatPattern</i>
	 * <td><code>new PluralFormat(subformatPattern)</code>
	 * </table>
	 * 
	 * <h5>Examples:</h5>
	 * 
	 * <pre>
	 * MessageFormat msg = messageFormatInstance("There {0, plural, one{is one file}other{are {0} files}} on {1}.")
	 * 
	 * msg.render(1000, "disk"); // == "There are 1,000 files on disk."
	 * </pre>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link com.ibm.icu.text.MessageFormat MessageFormat}
	 * @return a MessageFormat instance for the current thread
	 */
	public static MessageFormat messageFormatInstance(final String pattern) {

		MessageFormat msg = context.get().getMessageFormat();
		msg.applyPattern(pattern);
		return msg;

	}

	/**
	 * <p>
	 * Same as {@link #messageFormatInstance(String) messageFormatInstance} for
	 * the specified locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link com.ibm.icu.text.MessageFormat MessageFormat}
	 * @return a MessageFormat instance for the current thread
	 */
	public static MessageFormat messageFormatInstance(final String pattern, final Locale locale) {

		return withinLocale(new Callable<MessageFormat>() {
			public MessageFormat call() throws Exception {

				return messageFormatInstance(pattern);

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
	public static String metricPrefix(final Number value) {

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
	 * Constructs a message with pluralization logic from the given template.
	 * </p>
	 * 
	 * <h5>Examples:</h5>
	 * 
	 * <pre>
	 * MessageFormat msg = pluralize(&quot;There {0} on {1}.::are no files::is one file::are {2} files&quot;);
	 * 
	 * msg.render(0, &quot;disk&quot;); // == &quot;There are no files on disk.&quot;
	 * msg.render(1, &quot;disk&quot;); // == &quot;There is one file on disk.&quot;
	 * msg.render(1000, &quot;disk&quot;); // == &quot;There are 1,000 files on disk.&quot;
	 * </pre>
	 * 
	 * @param template
	 *            String of tokens delimited by '::'
	 * 
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static MessageFormat pluralize(final String template) {

		String[] tokens = template.split("\\s*\\:{2}\\s*");

		if (tokens.length < 3)
			throw new IllegalArgumentException(String.format("Template '%s' must declare at least 3 tokens", template));

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
	public static MessageFormat pluralize(final String template, final Locale locale) {

		return withinLocale(new Callable<MessageFormat>() {
			public MessageFormat call() throws Exception {

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
	public static MessageFormat pluralize(final String pattern, final String... choices) {

		double[] indexes = new double[choices.length];
		for (int i = 0; i < choices.length; i++)
			indexes[i] = i;

		ChoiceFormat choiceForm = new ChoiceFormat(indexes, choices);
		MessageFormat format = (MessageFormat) messageFormatInstance(pattern).clone();
		format.setFormat(0, choiceForm);

		return format;

	}

	/**
	 * <p>
	 * Replaces characters outside the Basic Multilingual Plane with their name.
	 * </p>
	 * 
	 * @param value
	 *            The text to be matched
	 * @return text with characters outside BMP replaced by their unicode
	 *         numbers or the given text unaltered
	 */
	public static String replaceSupplementary(final String value) {

		return TextUtils.replaceSupplementary(value, new Replacer() {
			public String replace(String in) {

				StringBuilder uc = new StringBuilder();
				for (char c : in.toCharArray()) {
					uc.append("\\\\u");
					uc.append(Integer.toHexString(c).toUpperCase());
				}

				return uc.toString();

			}
		});

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
	public static String spellBigNumber(final Number value) {

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
	public static String spellDigit(final Number value) {

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
	public static String titleize(final String text) {

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
	 * Makes a phrase underscored instead of spaced.
	 * </p>
	 * 
	 * @param text
	 *            Phrase to underscore
	 * @return converted String
	 */
	public static String underscore(final String text) {

		return text.replaceAll("\\s+", "_");

	}

	/**
	 * <p>
	 * Parses the given text with the mask specified.
	 * </p>
	 * 
	 * @param mask
	 *            The pattern mask. See {@link MaskFormat} for details.
	 * @param value
	 *            The text to be parsed
	 * @return The parsed text
	 * @throws ParseException
	 */
	public static String unmask(final String mask, final String value) throws ParseException {

		return maskFormatInstance(mask).parse(value);

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
	public static String wordWrap(final String value, final int len) {

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
			if (DefaultContextFactory.class.isAssignableFrom(factory.getClass()))
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
	private static boolean needPlural(final int n) {

		int tmp = 0;
		int an = Math.abs(n);

		while (an > 0) {
			tmp = an % 10;
			if (tmp > 1)
				return true;
			an /= 10;
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

	private static String stripZeros(final DecimalFormat decf, final String fmtd) {

		char decsep = decf.getDecimalFormatSymbols().getDecimalSeparator();
		return fmtd.replaceAll("\\" + decsep + "00", EMPTY);

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
	private static <T> T withinLocale(final Callable<T> operation, final Locale locale) {

		DefaultContext ctx = context.get();
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
