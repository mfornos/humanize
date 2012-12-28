package humanize;

import static humanize.util.Constants.EMPTY;
import static humanize.util.Constants.HYPEN_SPACE;
import static humanize.util.Constants.ND_FACTOR;
import static humanize.util.Constants.ONLY_SLUG_CHARS;
import static humanize.util.Constants.ORDINAL_FMT;
import static humanize.util.Constants.SPACE;
import static humanize.util.Constants.SPLIT_CAMEL;
import static humanize.util.Constants.THOUSAND;
import static humanize.util.Constants.bigDecExponents;
import static humanize.util.Constants.binPrefixes;
import static humanize.util.Constants.metricPrefixes;
import static humanize.util.Constants.nanoTimePrefixes;
import humanize.spi.Expose;
import humanize.spi.MessageFormat;
import humanize.spi.context.ContextFactory;
import humanize.spi.context.DefaultContext;
import humanize.spi.context.DefaultContextFactory;
import humanize.text.MaskFormat;
import humanize.text.util.InterpolationHelper;
import humanize.text.util.Replacer;
import humanize.time.PrettyTimeFormat;

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

import javax.xml.bind.DatatypeConverter;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ObjectArrays;

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
	 * Same as {@link #binaryPrefix(Number)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return The number preceded by the corresponding binary SI prefix
	 */
	@Expose
	public static String binaryPrefix(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return binaryPrefix(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #camelize(String, boolean)} with capitalize to false.
	 * </p>
	 * 
	 * @param text
	 *            String to be camelized
	 * @return Camelized string
	 */
	@Expose
	public static String camelize(final String text) {

		return camelize(text, false);

	}

	/**
	 * <p>
	 * Makes a phrase camel case. Spaces, hyphens, underscores and dots will be
	 * removed.
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
		String[] tokens = text.split("[\\.\\s_-]+");

		if (tokens.length < 2)
			return capitalizeFirstChar ? capitalize(text) : text;

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
	@Expose
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
	public static DateFormat dateFormat(final String pattern) {

		return context.get().getDateFormat(pattern);

	}

	/**
	 * <p>
	 * Same as {@link #dateFormat(String)} for the specified locale.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.SimpleDateFormat SimpleDateFormat}
	 * @param locale
	 *            Target locale
	 * @return a DateFormat instance for the current thread
	 */
	public static DateFormat dateFormat(final String pattern, final Locale locale) {

		return withinLocale(new Callable<DateFormat>() {
			public DateFormat call() throws Exception {

				return dateFormat(pattern);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #decamelize(String String)} defaulting to SPACE for
	 * replacement
	 * </p>
	 * 
	 * @param words
	 *            String to be converted
	 * @return words converted to human-readable name
	 */
	@Expose
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
	 *            {@link java.text.DecimalFormat DecimalFormat}
	 * @return a DecimalFormat instance for the current thread
	 */
	public static DecimalFormat decimalFormat(final String pattern) {

		DecimalFormat decFmt = context.get().getDecimalFormat();
		decFmt.applyPattern(pattern);
		return decFmt;

	}

	/**
	 * <p>
	 * Same as {@link #decimalFormat(String)} for the specified locale.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.DecimalFormat DecimalFormat}
	 * @param locale
	 *            Target locale
	 * @return a DecimalFormat instance for the current thread
	 */
	public static DecimalFormat decimalFormat(final String pattern, final Locale locale) {

		return withinLocale(new Callable<DecimalFormat>() {
			public DecimalFormat call() throws Exception {

				return decimalFormat(pattern);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Gets a DecimalFormat instance for the current thread with the given
	 * pattern and uses it to format the given arguments.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.MessageFormat MessageFormat}
	 * @param args
	 *            Arguments
	 * @return The formatted String
	 */
	public static String format(final String pattern, final Object... args) {

		return messageFormat(pattern).render(args);

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
	 * Same as {@link #formatCurrency(Number)} for the specified locale.
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
	 * Same as {@link #formatDate(int, Date)} with SHORT style.
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
	 * Same as {@link #formatDate(Date)} for the specified locale.
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
	 *            The pattern.
	 * @return a formatted date/time string
	 * @see #dateFormat(String)
	 */
	public static String formatDate(final Date value, final String pattern) {

		return new SimpleDateFormat(pattern, context.get().getLocale()).format(value);

	}

	/**
	 * <p>
	 * Same as {@link #formatDate(Date, String)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Date to be formatted
	 * @param pattern
	 *            The pattern.
	 * @param locale
	 *            Target locale
	 * @return a formatted date/time string
	 * @see #dateFormat(String)
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
	 * Same as {@link #formatDate(int, Date)} for the specified locale.
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
	 * Same as {@link #formatDateTime(Date)} for the specified locale.
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
	 * Same as {@link #formatDateTime(int, int, Date)} for the specified locale.
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
	 * Same as {@link #formatDecimal(Number)} for the specified locale.
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
	 * Same as {@link #formatPercent(Number)} for the specified locale.
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
	 *            The pattern mask.
	 * @param value
	 *            The text to be masked
	 * @return The formatted text
	 * @see MaskFormat
	 */
	public static String mask(final String mask, final String value) {

		return maskFormat(mask).format(value);

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
	public static MaskFormat maskFormat(final String mask) {

		MaskFormat maskFmt = context.get().getMaskFormat();
		maskFmt.setMask(mask);
		return maskFmt;

	}

	/**
	 * <p>
	 * Returns a MessageFormat instance for the current thread.
	 * </p>
	 * 
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.MessageFormat MessageFormat}
	 * @return a MessageFormat instance for the current thread
	 */
	public static MessageFormat messageFormat(final String pattern) {

		MessageFormat msg = context.get().getMessageFormat();
		msg.applyPattern(pattern);
		return msg;

	}

	/**
	 * <p>
	 * Same as {@link #messageFormat(String)} for the specified locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @param pattern
	 *            Format pattern that follows the conventions of
	 *            {@link java.text.MessageFormat MessageFormat}
	 * @return a MessageFormat instance for the current thread
	 */
	public static MessageFormat messageFormat(final String pattern, final Locale locale) {

		return withinLocale(new Callable<MessageFormat>() {
			public MessageFormat call() throws Exception {

				return messageFormat(pattern);

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
	 * Same as {@link #metricPrefix(Number)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return The number preceded by the corresponding SI prefix
	 */
	@Expose
	public static String metricPrefix(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return metricPrefix(value);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Formats a number of nanoseconds as the proper ten power unit.
	 * </p>
	 * 
	 * @param value
	 *            Number of nanoseconds
	 * @return The transformed quantity preceded by the corresponding SI symbol
	 */
	public static String nanoTime(final Number value) {

		return prefix(value, 1000, nanoTimePrefixes);

	}

	/**
	 * <p>
	 * Same as {@link #nanoTime(Number)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number of nanoseconds
	 * @param locale
	 *            Target locale
	 * @return The number preceded by the corresponding SI symbol
	 */
	@Expose
	public static String nanoTime(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() {

				return prefix(value, 1000, nanoTimePrefixes);

			}
		}, locale);

	}

	/**
	 * Same as {@link #naturalDay(int, Date)} with DateFormat.SHORT style.
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
	 * Same as {@link #naturalDay(Date)} with the given locale.
	 * 
	 * @param Date
	 *            The date
	 * @param Locale
	 *            Target locale
	 * @return String with 'today', 'tomorrow' or 'yesterday' compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	@Expose
	public static String naturalDay(Date value, Locale locale) {

		return naturalDay(DateFormat.SHORT, value, locale);

	}

	/**
	 * For dates that are the current day or within one day, return 'today',
	 * 'tomorrow' or 'yesterday', as appropriate. Otherwise, returns a string
	 * formatted according to a locale sensitive DateFormat.
	 * 
	 * @param style
	 *            The style of the Date
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
	 * Same as {@link #naturalDay(int, Date)} with the given locale.
	 * 
	 * @param style
	 *            The style of the Date
	 * @param Date
	 *            The date (GMT)
	 * @param locale
	 *            Target locale
	 * @return String with 'today', 'tomorrow' or 'yesterday' compared to
	 *         current day. Otherwise, returns a string formatted according to a
	 *         locale sensitive DateFormat.
	 */
	public static String naturalDay(final int style, final Date value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() throws Exception {

				return naturalDay(style, value);

			}
		}, locale);

	}

	/**
	 * Same as {@link #naturalTime(Date, Date)} with current date as reference.
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
	 * Same as {@link #naturalTime(Date, Date)} for the specified locale.
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
	 * Same as {@link #naturalTime(Date)} for the specified locale.
	 * 
	 * @param Date
	 *            The duration
	 * @param Locale
	 *            The locale
	 * @return String representing the relative date
	 */
	@Expose
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
			return String.format(ORDINAL_FMT, v, context.get().ordinalSuffix(0));

		return String.format(ORDINAL_FMT, v, context.get().ordinalSuffix(v % 10));

	}

	/**
	 * Same as {@link #ordinal(Number)} for the specified locale.
	 * 
	 * @param Number
	 *            The number to convert
	 * @param Locale
	 *            The locale
	 * @return String representing the number as ordinal
	 */
	@Expose
	public static String ordinal(final Number value, final Locale locale) {

		return withinLocale(new Callable<String>() {
			public String call() {

				return ordinal(value);

			}
		}, locale);

	}

	/**
	 * Converts the string argument into an array of bytes.
	 * 
	 * @param base64str
	 *            The Base64 encoded string
	 * @return an array of bytes with the decoded content
	 */
	public static byte[] parseBase64(String base64str) {

		return DatatypeConverter.parseBase64Binary(base64str);

	}

	/**
	 * Converts the string argumento into a Date value.
	 * 
	 * @param dateStr
	 *            The ISO8601 date string
	 * @return the converted Date
	 */
	public static Date parseISODate(String dateStr) {

		return DatatypeConverter.parseDate(dateStr).getTime();

	}

	/**
	 * Converts the string argumento into a Date value.
	 * 
	 * @param dateStr
	 *            The ISO8601 date string
	 * @return the converted Date
	 */
	public static Date parseISODateTime(String dateStr) {

		return DatatypeConverter.parseDateTime(dateStr).getTime();

	}

	/**
	 * Converts the string argumento into a Date value.
	 * 
	 * @param timeStr
	 *            The ISO8601 time string
	 * @return the converted Date
	 */
	public static Date parseISOTime(String timeStr) {

		return DatatypeConverter.parseTime(timeStr).getTime();

	}

	/**
	 * <p>
	 * Same as {@link #parseSmartDateWithSeparator(String, String, String...)}
	 * but with "[\\D-_\\s]+" as the default separator.
	 * </p>
	 * Example:
	 * 
	 * <pre>
	 * Date target = newDate(2012, 1, 1, 0, 0, 0);
	 * 
	 * String dates[] = new String[] { &quot;1.2.12&quot;, &quot;01.02.2012&quot;, &quot;2012.02.01&quot;, &quot;01-02-12&quot;, &quot;1 2 2012&quot; };
	 * 
	 * for (String ds : dates) {
	 * 	Date date = Humanize.parseSmartDate(ds, &quot;dd/MM/yy&quot;, &quot;yyyy/MM/dd&quot;, &quot;dd/MM/yyyy&quot;);
	 * 	Assert.assertEquals(date, target);
	 * }
	 * </pre>
	 * 
	 * @param dateStr
	 *            The date string
	 * @param fmts
	 *            An array of formats
	 * @return the converted Date
	 */
	public static Date parseSmartDate(String dateStr, String... fmts) {

		return parseSmartDateWithSeparator(dateStr, "[\\D-_\\s]+", fmts);

	}

	/**
	 * <p>
	 * Tries to parse a date string applying an array of non lenient format
	 * patterns. The formats are automatically cached.
	 * </p>
	 * 
	 * @param dateStr
	 *            The date string
	 * @param separator
	 *            The separator regexp
	 * @param fmts
	 *            An array of formats
	 * @return the converted Date
	 * @see #parseSmartDate(String, String...)
	 */
	public static Date parseSmartDateWithSeparator(String dateStr, String separator, String... fmts) {

		dateStr = dateStr.replaceAll(separator, "/");

		for (String fmt : fmts) {
			try {
				DateFormat df = dateFormat(fmt); // cached
				df.setLenient(false);
				return df.parse(dateStr);
			} catch (ParseException ignored) {
			}
		}
		throw new IllegalArgumentException("Unable to parse date '" + dateStr + "'");
	}

	/**
	 * <p>
	 * Same as {@link #pluralize(String, String, Number, Object...)} for the
	 * target locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @param one
	 *            Format for single element
	 * @param many
	 *            Format for many elements
	 * @param n
	 *            The number that triggers the plural state
	 * @param exts
	 *            Extended parameters for the specified formats
	 * @return formatted text according the right plural state
	 */
	public static String pluralize(final Locale locale, final String one, final String many, final Number n,
	        final Object... exts) {

		return withinLocale(new Callable<String>() {
			@Override
			public String call() throws Exception {

				return pluralize(one, many, n, exts);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Same as {@link #pluralize(String, String, String, Number, Object...)} for
	 * the target locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @param one
	 *            Format for single element
	 * @param many
	 *            Format for many elements
	 * @param none
	 *            Format for no element
	 * @param n
	 *            The number that triggers the plural state
	 * @param exts
	 *            Extended parameters for the specified formats
	 * @return formatted text according the right plural state
	 */
	public static String pluralize(final Locale locale, final String one, final String many, final String none,
	        final Number n, final Object... exts) {

		return withinLocale(new Callable<String>() {
			@Override
			public String call() throws Exception {

				return pluralize(one, many, none, n, exts);

			}
		}, locale);

	}

	/**
	 * <p>
	 * Applies the proper format for a given plural state.
	 * </p>
	 * 
	 * Example:
	 * 
	 * <pre>
	 * pluralize(&quot;There is one file on {1}.&quot;, &quot;There are {0} files on {1}.&quot;, 1, &quot;disk&quot;);
	 * // == There is one file on disk.
	 * pluralize(&quot;There is one file on {1}.&quot;, &quot;There are {0} files on {1}.&quot;, 2, &quot;disk&quot;);
	 * // == There are 2 files on disk.
	 * </pre>
	 * 
	 * @param one
	 *            Format for single element
	 * @param many
	 *            Format for many elements
	 * @param n
	 *            The number that triggers the plural state
	 * @param exts
	 *            Extended parameters for the specified formats
	 * @return formatted text according the right plural state
	 */
	public static String pluralize(final String one, final String many, final Number n, final Object... exts) {

		MessageFormat format = pluralizeFormat("{0}", many, one, many);
		Object[] params = exts == null ? new Object[] { n } : ObjectArrays.concat(n, exts);
		return format.render(params);

	}

	/**
	 * <p>
	 * Applies the proper format for a given plural state.
	 * </p>
	 * Example:
	 * 
	 * <pre>
	 * pluralize(&quot;There is one file on {1}.&quot;, &quot;There are {0} files on {1}.&quot;, &quot;There are no files on {1}.&quot;, 1, &quot;disk&quot;);
	 * // == There is one file on disk.
	 * pluralize(&quot;There is one file on {1}.&quot;, &quot;There are {0} files on {1}.&quot;, &quot;There are no files on {1}.&quot;, 2, &quot;disk&quot;);
	 * // == There are 2 files on disk.
	 * pluralize(&quot;There is one file on {1}.&quot;, &quot;There are {0} files on {1}.&quot;, &quot;There are no files on {1}.&quot;, 0, &quot;disk&quot;);
	 * // == There are no files on disk.
	 * 
	 * pluralize(&quot;one&quot;, &quot;{0}&quot;, &quot;none&quot;, 1);
	 * // = &quot;one&quot;
	 * pluralize(&quot;one&quot;, &quot;{0}&quot;, &quot;none&quot;, 2);
	 * // = &quot;2&quot;
	 * </pre>
	 * 
	 * @param one
	 *            Format for single element
	 * @param many
	 *            Format for many elements
	 * @param none
	 *            Format for no element
	 * @param n
	 *            The number that triggers the plural state
	 * @param exts
	 *            Extended parameters for the specified formats
	 * @return formatted text according the right plural state
	 */
	public static String pluralize(final String one, final String many, final String none, final Number n,
	        final Object... exts) {

		MessageFormat format = pluralizeFormat("{0}", none, one, many);
		Object[] params = exts == null ? new Object[] { n } : ObjectArrays.concat(n, exts);
		return format.render(params);

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
	 * <pre>
	 * MessageFormat msg = pluralize(&quot;nothing::one thing::{0} things&quot;);
	 * 
	 * msg.render(-1); // == &quot;nothing&quot;
	 * msg.render(0); // == &quot;nothing&quot;
	 * msg.render(1); // == &quot;one thing&quot;
	 * msg.render(2); // == &quot;2 things&quot;
	 * </pre>
	 * 
	 * <pre>
	 * MessageFormat msg = pluralize(&quot;one thing::{0} things&quot;);
	 * 
	 * msg.render(-1); // == &quot;-1 things&quot;
	 * msg.render(0); // == &quot;0 things&quot;
	 * msg.render(1); // == &quot;one thing&quot;
	 * msg.render(2); // == &quot;2 things&quot;
	 * </pre>
	 * 
	 * @param template
	 *            String of tokens delimited by '::'
	 * 
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static MessageFormat pluralizeFormat(final String template) {

		String[] tokens = template.split("\\s*\\:{2}\\s*");

		if (tokens.length < 4) {
			if (tokens.length == 2) {
				tokens = new String[] { "{0}", tokens[1], tokens[0], tokens[1] };
			} else if (tokens.length == 3) {
				tokens = new String[] { "{0}", tokens[0], tokens[1], tokens[2] };
			} else {
				throw new IllegalArgumentException(String.format(
				        "Template '%s' must declare at least 2 tokens. V.gr. 'one thing::{0} things'", template));
			}
		}

		return pluralizeFormat(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));

	}

	/**
	 * <p>
	 * Same as {@link #pluralizeFormat(String)} for the specified locale.
	 * </p>
	 * 
	 * @param template
	 *            String of tokens delimited by '::'
	 * @param locale
	 *            Target locale
	 * @return Message instance prepared to generate pluralized strings
	 */
	public static MessageFormat pluralizeFormat(final String template, final Locale locale) {

		return withinLocale(new Callable<MessageFormat>() {
			public MessageFormat call() throws Exception {

				return pluralizeFormat(template);

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
	public static MessageFormat pluralizeFormat(final String pattern, final String... choices) {

		double[] indexes = new double[choices.length];
		for (int i = 0; i < choices.length; i++)
			indexes[i] = i;

		ChoiceFormat choiceForm = new ChoiceFormat(indexes, choices);
		MessageFormat format = (MessageFormat) messageFormat(pattern).clone();
		format.setFormat(0, choiceForm);

		return format;

	}

	/**
	 * <p>
	 * Returns a thread-safe {@link PrettyTimeFormat} instance.
	 * </p>
	 * 
	 * @return PrettyTimeFormat instance
	 * @see PrettyTimeFormat
	 */
	public static PrettyTimeFormat prettyTimeFormat() {

		return context.get().getPrettyTimeFormat();

	}

	/**
	 * <p>
	 * Same as {@link #prettyTimeFormat()} for the specified locale.
	 * </p>
	 * 
	 * @param locale
	 *            Target locale
	 * @return PrettyTimeFormat instance
	 * @see PrettyTimeFormat
	 */
	public static PrettyTimeFormat prettyTimeFormat(final Locale locale) {

		return withinLocale(new Callable<PrettyTimeFormat>() {
			public PrettyTimeFormat call() throws Exception {

				return context.get().getPrettyTimeFormat();

			}
		}, locale);

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
	@Expose
	public static String replaceSupplementary(final String value) {

		return InterpolationHelper.replaceSupplementary(value, new Replacer() {
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
	 * Transforms a text into a representation suitable to be used in an URL.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>"J'étudie le français"</td>
	 * <td>"jetudie-le-francais"</td>
	 * </tr>
	 * <tr>
	 * <td>"Lo siento, no hablo español."</td>
	 * <td>"lo-siento-no-hablo-espanol"</td>
	 * </tr>
	 * </table>
	 * 
	 * @param text
	 *            The text to be slugified
	 * @return Slugified String
	 */
	@Expose
	public static String slugify(final String text) {

		String result = transliterate(text);
		result = ONLY_SLUG_CHARS.matcher(result).replaceAll("");
		result = CharMatcher.WHITESPACE.trimFrom(result);
		result = HYPEN_SPACE.matcher(result).replaceAll("-");

		return result.toLowerCase();

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
	 * Same as {@link #spellBigNumber(Number)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Number to be converted
	 * @param locale
	 *            Target locale
	 * @return Friendly text representation of the given value
	 */
	@Expose
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
	 * Same as {@link #spellDigit(Number)} for the specified locale.
	 * </p>
	 * 
	 * @param value
	 *            Decimal digit
	 * @param locale
	 *            Target locale
	 * @return String representing the number spelled out
	 */
	@Expose
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
	@Expose
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
	 * Strips diacritic marks.
	 * </p>
	 * 
	 * <table border="0" cellspacing="0" cellpadding="3" width="100%">
	 * <tr>
	 * <th class="colFirst">Input</th>
	 * <th class="colLast">Output</th>
	 * </tr>
	 * <tr>
	 * <td>"J'étudie le français"</td>
	 * <td>"J'etudie le francais"</td>
	 * </tr>
	 * <tr>
	 * <td>"Lo siento, no hablo español."</td>
	 * <td>"Lo siento, no hablo espanol."</td>
	 * </tr>
	 * </table>
	 * 
	 * @param text
	 *            The text to be transliterated.
	 * @return String without diacritic marks.
	 */
	@Expose
	public static String transliterate(final String text) {

		String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
		return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

	}

	// ( private methods )------------------------------------------------------

	/**
	 * <p>
	 * Makes a phrase underscored instead of spaced.
	 * </p>
	 * 
	 * @param text
	 *            Phrase to underscore
	 * @return converted String
	 */
	@Expose
	public static String underscore(final String text) {

		return text.replaceAll("\\s+", "_");

	}

	/**
	 * <p>
	 * Parses the given text with the mask specified.
	 * </p>
	 * 
	 * @param mask
	 *            The pattern mask.
	 * @param value
	 *            The text to be parsed
	 * @return The parsed text
	 * @throws ParseException
	 * @see MaskFormat
	 */
	public static String unmask(final String mask, final String value) throws ParseException {

		return maskFormat(mask).parse(value);

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
