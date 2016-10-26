/*

    _   _ _   _ __  __  ___  _   _ ___ __________ 
   | | | | | | |  \/  |  _  | \ | |_ _|__  / ____|
   | |_| | | | | |\/| | |_| |  \| || |  / /|  _|  
   |  _  | |_| | |  | |  _  | |\  || | / /_| |___ 
   |_| |_|\___/|_|  |_|_| |_|_| \_|___/____|_____|

   Copyright 2013-2015 mfornos

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package humanize;

import static humanize.util.Constants.COMB_DIACRITICAL;
import static humanize.util.Constants.EMPTY;
import static humanize.util.Constants.HYPEN_SPACE;
import static humanize.util.Constants.ND_FACTOR;
import static humanize.util.Constants.ONLY_SLUG_CHARS;
import static humanize.util.Constants.ORDINAL_FMT;
import static humanize.util.Constants.PUNCTUATION;
import static humanize.util.Constants.SPACE;
import static humanize.util.Constants.SPLIT_CAMEL;
import static humanize.util.Constants.THOUSAND;
import static humanize.util.Constants.bigDecExponents;
import static humanize.util.Constants.binPrefixes;
import static humanize.util.Constants.commaJoiner;
import static humanize.util.Constants.metricPrefixes;
import static humanize.util.Constants.nanoTimePrefixes;
import static humanize.util.Constants.titleIgnoredWords;
import static humanize.util.Constants.titleWordSperator;
import humanize.spi.Expose;
import humanize.spi.MessageFormat;
import humanize.spi.context.ContextFactory;
import humanize.spi.context.DefaultContext;
import humanize.spi.context.DefaultContextFactory;
import humanize.text.MaskFormat;
import humanize.text.util.InterpolationHelper;
import humanize.text.util.Replacer;
import humanize.time.Pace;
import humanize.time.Pace.Accuracy;
import humanize.time.PrettyTimeFormat;
import humanize.time.TimeMillis;
import humanize.util.Constants.TimeStyle;
import humanize.util.Parameters.PaceParameters;
import humanize.util.Parameters.PluralizeParams;
import humanize.util.Parameters.SlugifyParams;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.BreakIterator;
import java.text.ChoiceFormat;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import javax.xml.bind.DatatypeConverter;

import me.xuender.unidecode.Unidecode;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ObjectArrays;

/**
 * <p>
 * Facility for adding a "human touch" to data. It is thread-safe and supports
 * per-thread internationalization. Additionally provides a concise facade for
 * access to Standard i18n Java APIs.
 * </p>
 * 
 */
public final class Humanize
{

    private static final ContextFactory contextFactory = loadContextFactory();

    private static final ThreadLocal<DefaultContext> context = new ThreadLocal<DefaultContext>()
    {
        protected DefaultContext initialValue()
        {
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
     * <td>"1.5 KB"</td>
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
    public static String binaryPrefix(final Number value)
    {
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
    public static String binaryPrefix(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String camelize(final String text)
    {
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
    public static String camelize(final String text, final boolean capitalizeFirstChar)
    {
        StringBuilder sb = new StringBuilder();
        String[] tokens = text.split("[\\.\\s_-]+");

        if (tokens.length < 2)
            return capitalizeFirstChar ? capitalize(text) : text;

        for (String token : tokens)
            sb.append(capitalize(token));

        return capitalizeFirstChar ? sb.toString() : sb.substring(0, 1).toLowerCase(currentLocale())
                + sb.substring(1);
    }

    /**
     * <p>
     * Same as {@link #camelize(String, boolean)} for the specified locale.
     * </p>
     * 
     * @param capitalizeFirstChar
     *            true makes the first letter uppercase
     * @param text
     *            String to be camelized
     * @return Camelized string
     */
    public static String camelize(final String text, final boolean capitalizeFirstChar, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return camelize(text, capitalizeFirstChar);
            }
        }, locale);
    }

    /**
     * <p>
     * Same as {@link #camelize(String)} for the specified locale.
     * </p>
     * 
     * @param text
     *            String to be camelized
     * @param locale
     *            Target locale
     * @return Camelized string
     */
    @Expose
    public static String camelize(final String text, final Locale locale)
    {
        return camelize(text, false, locale);
    }

    /**
     * <p>
     * Capitalizes the given text smartly.
     * </p>
     * 
     * @param text
     *            String to be capitalized
     * @return capitalized string
     */
    public static String capitalize(final String text)
    {
        String tmp = text.trim();
        int len = tmp.length();

        if (len == 0)
            return text;

        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++)
        {
            if (Character.isLetter(tmp.charAt(i)))
            {
                Locale locale = currentLocale();
                int lc = i + 1;

                if (i > 0)
                {
                    sb.append(tmp.substring(0, i));
                }

                sb.append(tmp.substring(i, lc).toUpperCase(locale));
                sb.append(tmp.substring(lc).toLowerCase(locale));

                break;
            }
        }

        return sb.length() == 0 ? tmp : sb.toString();
    }

    /**
     * <p>
     * Same as {@link #capitalize(String)} for the specified locale.
     * </p>
     * 
     * @param text
     *            String to be capitalized
     * @param locale
     *            Target locale
     * @return capitalized string
     */
    @Expose
    public static String capitalize(final String text, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return capitalize(text);
            }
        }, locale);
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
    public static DateFormat dateFormat(final String pattern)
    {
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
    public static DateFormat dateFormat(final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<DateFormat>()
        {
            public DateFormat call() throws Exception
            {
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
    public static String decamelize(final String words)
    {
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
    public static String decamelize(final String words, final String replacement)
    {
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
    public static DecimalFormat decimalFormat(final String pattern)
    {
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
    public static DecimalFormat decimalFormat(final String pattern, final Locale locale)
    {

        return withinLocale(new Callable<DecimalFormat>()
        {
            public DecimalFormat call() throws Exception
            {

                return decimalFormat(pattern);

            }
        }, locale);

    }

    /**
     * <p>
     * Formats a number of seconds as hours, minutes and seconds. Defaults to
     * STANDARD time style.
     * </p>
     * 
     * @param seconds
     *            Number of seconds
     * @return a String with the formatted time
     */
    public static String duration(final Number seconds)
    {
        return duration(seconds, TimeStyle.STANDARD);
    }

    /**
     * <p>
     * Same as {@link #duration(Number)} for the specified locale.
     * </p>
     * 
     * @param seconds
     *            Number of seconds
     * @param locale
     *            Target locale
     * @return a String with the formatted time
     */
    public static String duration(final Number seconds, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return duration(seconds);
            }
        }, locale);
    }

    /**
     * <p>
     * Formats a number of seconds as hours, minutes and seconds.
     * </p>
     * 
     * @param seconds
     *            Number of seconds
     * @param style
     *            Time style
     * @return a String with the formatted time according to the given style
     */
    public static String duration(final Number seconds, final TimeStyle style)
    {
        int s = seconds.intValue();
        boolean neg = s < 0;
        s = Math.abs(s);
        return style.format(context.get(), neg, s / 3600, (s / 60) % 60, s % 60);
    }

    /**
     * <p>
     * Same as {@link #duration(Number, TimeStyle)} for the specified locale.
     * </p>
     * 
     * @param seconds
     *            Number of seconds
     * @param style
     *            Time style
     * @param locale
     *            Target locale
     * @return a String with the formatted time
     */
    public static String duration(final Number seconds, final TimeStyle style, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return duration(seconds, style);
            }
        }, locale);
    }

    /**
     * <p>
     * Pads or truncates a string to a specified length.
     * </p>
     * 
     * @param text
     *            String to be fixed
     * @param charsNum
     *            The fixed length in number of chars
     * @param paddingChar
     *            The padding character
     * @return A fixed length string
     */
    public static String fixLength(final String text, final int charsNum, final char paddingChar)
    {
        return fixLength(text, charsNum, paddingChar, false);
    }

    /**
     * <p>
     * Pads or truncates a string to a specified length.
     * </p>
     * 
     * @param text
     *            String to be fixed
     * @param charsNum
     *            The fixed length in number of chars
     * @param paddingChar
     *            The padding character
     * @param left
     *            true for left padding
     * @return A fixed length string
     */
    public static String fixLength(final String text, final int charsNum, final char paddingChar, final boolean left)
    {
        Preconditions.checkArgument(charsNum > 0, "The number of characters must be greater than zero.");

        String str = text == null ? "" : text;
        String fmt = String.format("%%%ss", left ? charsNum : -charsNum);

        return String.format(fmt, str).substring(0, charsNum).replace(' ', paddingChar);
    }

    /**
     * <p>
     * Same as {@link #format(String, Object...)} for the specified locale.
     * </p>
     * 
     * @param locale
     *            The target locale
     * @param pattern
     *            Format pattern that follows the conventions of
     *            {@link humanize.text.ExtendedMessageFormat MessageFormat}
     * @param args
     *            Arguments
     * @return The formatted String
     */
    public static String format(final Locale locale, final String pattern, final Object... args)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return format(pattern, args);
            }
        }, locale);
    }

    /**
     * <p>
     * Gets an ExtendedMessageFormat instance for the current thread with the
     * given pattern and uses it to format a message with the specified
     * arguments.
     * </p>
     * 
     * @param pattern
     *            Format pattern that follows the conventions of
     *            {@link humanize.text.ExtendedMessageFormat MessageFormat}
     * @param args
     *            Arguments
     * @return The formatted String
     */
    public static String format(final String pattern, final Object... args)
    {

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
    public static String formatCurrency(final Number value)
    {
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
    public static String formatCurrency(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
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
    public static String formatDate(final Date value)
    {
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
    public static String formatDate(final Date value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String formatDate(final Date value, final String pattern)
    {
        return new SimpleDateFormat(pattern, currentLocale()).format(value);
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
    public static String formatDate(final Date value, final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String formatDate(final int style, final Date value)
    {
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
    public static String formatDate(final int style, final Date value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String formatDateTime(final Date value)
    {
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
    public static String formatDateTime(final Date value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String formatDateTime(final int dateStyle, final int timeStyle, final Date value)
    {
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
    public static String formatDateTime(final int dateStyle, final int timeStyle, final Date value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String formatDecimal(final Number value)
    {
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
    public static String formatDecimal(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
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
    public static String formatPercent(final Number value)
    {
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
    public static String formatPercent(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return formatPercent(value);
            }
        }, locale);
    }

    /**
     * <p>
     * Same as {@link #lossyEquals(String, String)} for the specified locale.
     * </p>
     * 
     * @param locale
     *            The target locale
     * @param source
     *            The source string to be compared
     * @param target
     *            The target string to be compared
     * @return true if the two strings are equals according to primary
     *         differences only, false otherwise
     */
    public static boolean lossyEquals(final Locale locale, final String source, final String target)
    {
        return withinLocale(new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return lossyEquals(source, target);
            }

        }, locale);
    }

    /**
     * <p>
     * Locale-sensitive string comparison for primary differences.
     * </p>
     * 
     * <pre>
     * <code>
     * loosyEquals("Aáà-aa", "aaaaa") // == true 
     * loosyEquals("abc", "cba") // == false
     * </code>
     * </pre>
     * 
     * @param source
     *            The source string to be compared
     * @param target
     *            The target string to be compared
     * @return true if the two strings are equals according to primary
     *         differences only, false otherwise
     */
    public static boolean lossyEquals(final String source, final String target)
    {
        Collator c = Collator.getInstance(currentLocale());
        c.setStrength(Collator.PRIMARY);
        return c.equals(source, target);
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
    public static String mask(final String mask, final String value)
    {
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
    public static MaskFormat maskFormat(final String mask)
    {
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
    public static MessageFormat messageFormat(final String pattern)
    {
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
    public static MessageFormat messageFormat(final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<MessageFormat>()
        {
            public MessageFormat call() throws Exception
            {
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
    public static String metricPrefix(final Number value)
    {
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
    public static String metricPrefix(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
    public static String nanoTime(final Number value)
    {
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
    public static String nanoTime(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return prefix(value, 1000, nanoTimePrefixes);
            }
        }, locale);
    }

    /**
     * Same as {@link #naturalDay(int, Date)} with DateFormat.SHORT style.
     * 
     * @param then
     *            The date
     * @return String with 'today', 'tomorrow' or 'yesterday' compared to
     *         current day. Otherwise, returns a string formatted according to a
     *         locale sensitive DateFormat.
     */
    public static String naturalDay(Date then)
    {
        return naturalDay(DateFormat.SHORT, then);
    }

    /**
     * Same as {@link #naturalDay(Date)} with the given locale.
     * 
     * @param then
     *            The date
     * @param locale
     *            Target locale
     * @return String with 'today', 'tomorrow' or 'yesterday' compared to
     *         current day. Otherwise, returns a string formatted according to a
     *         locale sensitive DateFormat.
     */
    @Expose
    public static String naturalDay(Date then, Locale locale)
    {
        return naturalDay(DateFormat.SHORT, then, locale);
    }

    /**
     * For dates that are the current day or within one day, return 'today',
     * 'tomorrow' or 'yesterday', as appropriate. Otherwise, returns a string
     * formatted according to a locale sensitive DateFormat.
     * 
     * @param style
     *            The style of the Date
     * @param then
     *            The date (GMT)
     * @return String with 'today', 'tomorrow' or 'yesterday' compared to
     *         current day. Otherwise, returns a string formatted according to a
     *         locale sensitive DateFormat.
     */
    public static String naturalDay(int style, Date then)
    {
        Date today = new Date();
        long delta = then.getTime() - today.getTime();
        long days = delta / ND_FACTOR;

        if (days == 0)
            return context.get().getMessage("today");
        else if (days == 1)
            return context.get().getMessage("tomorrow");
        else if (days == -1)
            return context.get().getMessage("yesterday");

        return formatDate(style, then);
    }

    /**
     * Same as {@link #naturalDay(int, Date)} with the given locale.
     * 
     * @param style
     *            The style of the Date
     * @param then
     *            The date (GMT)
     * @param locale
     *            Target locale
     * @return String with 'today', 'tomorrow' or 'yesterday' compared to
     *         current day. Otherwise, returns a string formatted according to a
     *         locale sensitive DateFormat.
     */
    public static String naturalDay(final int style, final Date then, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return naturalDay(style, then);
            }
        }, locale);
    }

    /**
     * Same as {@link #naturalTime(Date, Date)} with current date as reference.
     * 
     * @param duration
     *            The duration
     * @return String representing the relative date
     */
    public static String naturalTime(Date duration)
    {
        return naturalTime(new Date(), duration);
    }

    /**
     * Computes both past and future relative dates.
     * 
     * <p>
     * 
     * E.g. 'one day ago', 'one day from now', '10 years ago', '3 minutes from
     * now', 'right now' and so on.
     * 
     * @param reference
     *            The reference
     * @param duration
     *            The duration
     * @return String representing the relative date
     */
    public static String naturalTime(Date reference, Date duration)
    {
        return context.get().formatRelativeDate(reference, duration);
    }

    /**
     * Same as {@link #naturalTime(Date, Date)} for the specified locale.
     * 
     * @param reference
     *            The reference
     * @param duration
     *            The duration
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date reference, final Date duration, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return naturalTime(reference, duration);
            }
        }, locale);
    }

    /**
     * Computes both past and future relative dates with arbitrary precision.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precision to retain in milliseconds
     * @return String representing the relative date
     */
    public static String naturalTime(Date reference, Date duration, long precision)
    {
        return context.get().formatRelativeDate(reference, duration, precision);
    }

    /**
     * Same as {@link #naturalTime(Date, Date, long)} for the specified locale.
     * 
     * @param reference
     *            The reference
     * @param duration
     *            The duration
     * @param precision
     *            The precesion to retain in milliseconds
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date reference, final Date duration, final long precision,
            final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return naturalTime(reference, duration, precision);
            }
        }, locale);
    }

    /**
     * Same as {@link #naturalTime(Date, Date, long)}.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precision to retain in milliseconds
     * @return String representing the relative date
     */
    public static String naturalTime(Date reference, Date duration, TimeMillis precision)
    {
        return naturalTime(reference, duration, precision.millis());
    }

    /**
     * Same as {@link #naturalTime(Date, Date, long, Locale)}.
     * 
     * @param reference
     *            The reference
     * @param duration
     *            The duration
     * @param precision
     *            The precesion to retain in milliseconds
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date reference, final Date duration, final TimeMillis precision,
            final Locale locale)
    {
        return naturalTime(reference, duration, precision.millis(), locale);
    }

    /**
     * Same as {@link #naturalTime(Date)} for the specified locale.
     * 
     * @param duration
     *            The duration
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    @Expose
    public static String naturalTime(final Date duration, final Locale locale)
    {
        return naturalTime(new Date(), duration, locale);
    }

    /**
     * Same as {@link #naturalTime(Date, Date, long)} with current date as
     * reference.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precision to retain in milliseconds
     * @return String representing the relative date
     */
    public static String naturalTime(Date duration, long precision)
    {
        return naturalTime(new Date(), duration, precision);
    }

    /**
     * Same as {@link #naturalTime(Date, long)} for the specified locale.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precesion to retain in milliseconds
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date duration, final long precision, final Locale locale)
    {
        return naturalTime(new Date(), duration, precision, locale);
    }

    /**
     * Same as {@link #naturalTime(Date, long)}.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precision to retain in milliseconds
     * @return String representing the relative date
     */
    public static String naturalTime(Date duration, TimeMillis precision)
    {
        return naturalTime(duration, precision.millis());
    }

    /**
     * Same as {@link #naturalTime(Date, long, Locale)}.
     * 
     * @param duration
     *            The duration
     * @param precision
     *            The precesion to retain in milliseconds
     * @param locale
     *            The locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date duration, final TimeMillis precision, final Locale locale)
    {
        return naturalTime(duration, precision.millis(), locale);
    }

    /**
     * Converts a number to its ordinal as a string.
     * 
     * <p>
     * 
     * E.g. 1 becomes '1st', 2 becomes '2nd', 3 becomes '3rd', etc.
     * 
     * @param value
     *            The number to convert
     * @return String representing the number as ordinal
     */
    public static String ordinal(Number value)
    {
        int v = value.intValue();
        int vc = v % 100;

        if (vc > 10 && vc < 14)
            return String.format(ORDINAL_FMT, v, context.get().ordinalSuffix(0));

        return String.format(ORDINAL_FMT, v, context.get().ordinalSuffix(v % 10));
    }

    /**
     * Same as {@link #ordinal(Number)} for the specified locale.
     * 
     * @param value
     *            The number to convert
     * @param locale
     *            The locale
     * @return String representing the number as ordinal
     */
    @Expose
    public static String ordinal(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return ordinal(value);
            }
        }, locale);
    }

    /**
     * <p>
     * Converts a list of items to a human readable string.
     * </p>
     * 
     * @param items
     *            The items collection
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(Collection<?> items)
    {
        return oxford(items.toArray());
    }

    /**
     * <p>
     * Converts a list of items to a human readable string with an optional
     * limit.
     * </p>
     * 
     * @param items
     *            The items collection
     * @param limit
     *            The number of items to print. -1 for unbounded.
     * @param limitStr
     *            The string to be appended after extra items. Null or "" for
     *            default.
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(Collection<?> items, int limit, String limitStr)
    {
        return oxford(items.toArray(), limit, limitStr);
    }

    /**
     * Same as {@link #oxford(Object[])} for the specified locale.
     * 
     * @param items
     *            The items collection
     * @param locale
     *            Target locale
     * @return human readable representation of a bounded list of items
     */
    @Expose
    public static String oxford(Collection<?> items, Locale locale)
    {
        return oxford(items.toArray(), locale);
    }

    /**
     * <p>
     * Converts a list of items to a human readable string.
     * </p>
     * 
     * @param items
     *            The items array
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(Object[] items)
    {
        return oxford(items, -1, null);
    }

    /**
     * <p>
     * Converts a list of items to a human readable string with an optional
     * limit.
     * </p>
     * 
     * @param items
     *            The items array
     * @param limit
     *            The number of items to print. -1 for unbounded.
     * @param limitStr
     *            The string to be appended after extra items. Null or "" for
     *            default.
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(final Object[] items, final int limit, final String limitStr)
    {
        if (items == null || items.length == 0)
        {
            return "";
        }

        int itemsNum = items.length;

        if (itemsNum == 1)
        {
            return items[0].toString();
        }

        ResourceBundle bundle = context.get().getBundle();

        if (itemsNum == 2)
        {
            return format(bundle.getString("oxford.pair"), items[0], items[1]);
        }

        int limitIndex;
        String append;
        int extra = itemsNum - limit;

        if (limit > 0 && extra > 1)
        {
            limitIndex = limit;
            String pattern = Strings.isNullOrEmpty(limitStr) ? bundle.getString("oxford.extra") : limitStr;
            append = format(pattern, extra);
        } else
        {
            limitIndex = itemsNum - 1;
            append = items[limitIndex].toString();
        }

        String pattern = bundle.getString("oxford");
        return format(pattern, commaJoiner.join(Arrays.copyOf(items, limitIndex)), append);
    }

    /**
     * Same as {@link #oxford(Object[], int, String)} for the specified locale.
     * 
     * @param items
     *            The items array
     * @param limit
     *            The number of items to print. -1 for unbounded.
     * @param limitStr
     *            The string to be appended after extra items. Null or "" for
     *            default.
     * @param locale
     *            Target locale
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(final Object[] items, final int limit, final String limitStr, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return oxford(items, limit, limitStr);
            }
        }, locale);
    }

    /**
     * Same as {@link #oxford(Object[])} for the specified locale.
     * 
     * @param items
     *            The items array
     * @param locale
     *            Target locale
     * @return human readable representation of a bounded list of items
     */
    public static String oxford(Object[] items, Locale locale)
    {
        return oxford(items, -1, null, locale);
    }

    /**
     * Matches a pace (value and interval) with a logical time frame. Very
     * useful for slow paces.
     * 
     * <p>
     * Examples:
     * </p>
     * 
     * <pre>
     * <code>
     *   // 3 occurrences within a 3000ms interval
     *   pace(3, 3000); // => ~1/sec.
     *   
     *   // 200 occurrences within a 70000ms interval
     *   pace(200, 70000); // => ~3/sec.
     *   
     *   // 10 occurrences within a 70000ms interval
     *   pace(10, 70000); // => ~9/min.
     *   
     *   // 14 occurrences within a 31557600000ms interval (a year)
     *   pace(14, 31557600000L); // => ~1/month
     *   
     *   // 25 occurrences within a 31557600000ms interval
     *   pace(25, 31557600000L); // => ~2/month
     *   
     *   // 9 occurrences within a 31557600000ms interval
     *   pace(9, 31557600000L); // => >1/month (less than one per month)
     * </code>
     * </pre>
     * 
     * @param value
     *            The number of occurrences within the specified interval
     * @param interval
     *            The interval in milliseconds
     * @return a Pace instance with data for a given value and interval
     */
    public static Pace pace(final Number value, final long interval)
    {
        double dval = Math.round(value.doubleValue());

        if (dval == 0 || interval == 0)
            return Pace.EMPTY;

        Pace args = null;
        double rate = Math.abs(dval / interval);
        TimeMillis[] intvls = TimeMillis.values();

        for (TimeMillis p : intvls)
        {
            double relativePace = rate * p.millis();
            if (relativePace >= 1)
            {
                args = new Pace(Math.round(relativePace), Accuracy.APROX, p);
                break;
            }
        }

        if (args == null)
        {
            args = new Pace(1, Accuracy.LESS_THAN, intvls[intvls.length - 1]);
        }

        return args;
    }

    /**
     * Same as {@link #pace(Number, long)}.
     * 
     * @param value
     *            The number of occurrences within the specified interval
     * @param interval
     *            Time millis interval
     * @return a Pace instance with data for a given value and interval
     */
    public static Pace pace(final Number value, final TimeMillis interval)
    {
        return pace(value, interval.millis());
    }

    /**
     * Same as {@link #paceFormat(Number, long)} for a target locale.
     * 
     * @param locale
     *            The target locale
     * @param value
     *            The number of occurrences within the specified interval
     * @param interval
     *            The interval in milliseconds
     * @return an human readable textual representation of the pace
     */
    public static String paceFormat(final Locale locale, final Number value, final long interval)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return paceFormat(value, interval);
            }
        }, locale);
    }

    /**
     * Same as {@link #paceFormat(Number, long, String, String, String)} for a
     * target locale.
     * 
     * @param locale
     *            The target locale
     * @param value
     *            The number of occurrences within the specified interval
     * @param params
     *            The pace format parameterezation
     * @return an human readable textual representation of the pace
     */
    public static String paceFormat(final Locale locale, final Number value, final PaceParameters params)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return paceFormat(value, params);
            }
        }, locale);
    }

    /**
     * Matches a pace (value and interval) with a logical time frame. Very
     * useful for slow paces. e.g. heartbeats, ingested calories, hyperglycemic
     * crises.
     * 
     * @param value
     *            The number of occurrences within the specified interval
     * @param interval
     *            The interval in milliseconds
     * @return an human readable textual representation of the pace
     */
    public static String paceFormat(final Number value, final long interval)
    {
        ResourceBundle b = context.get().getBundle();

        PaceParameters params = PaceParameters.begin(b.getString("pace.one"))
                .none(b.getString("pace.none"))
                .many(b.getString("pace.many"))
                .interval(interval);

        return paceFormat(value, params);
    }

    /**
     * Matches a pace (value and interval) with a logical time frame. Very
     * useful for slow paces.
     * 
     * @param value
     *            The number of occurrences within the specified interval
     * @param params
     *            The pace format parameterization
     * @return an human readable textual representation of the pace
     */
    public static String paceFormat(final Number value, final PaceParameters params)
    {
        params.checkArguments();

        Pace args = pace(value, params.interval);

        ResourceBundle bundle = context.get().getBundle();

        String accuracy = bundle.getString(args.getAccuracy());
        String timeUnit = bundle.getString(args.getTimeUnit());

        params.exts(accuracy, timeUnit);

        return capitalize(pluralize(args.getValue(), params.plural));
    }

    /**
     * Same as {@link #paceFormat(Number, long)}
     * 
     * @param value
     *            The number of occurrences within the specified interval
     * @param interval
     *            The interval in milliseconds
     * @return an human readable textual representation of the pace
     */
    public static String paceFormat(final Number value, final TimeMillis interval)
    {
        return paceFormat(value, interval.millis());
    }

    /**
     * Converts the string argument into an array of bytes.
     * 
     * @param base64str
     *            The Base64 encoded string
     * @return an array of bytes with the decoded content
     */
    public static byte[] parseBase64(String base64str)
    {
        return DatatypeConverter.parseBase64Binary(base64str);
    }

    /**
     * Converts the string argumento into a Date value.
     * 
     * @param dateStr
     *            The ISO8601 date string
     * @return the converted Date
     */
    public static Date parseISODate(String dateStr)
    {
        return DatatypeConverter.parseDate(dateStr).getTime();
    }

    /**
     * Converts the string argumento into a Date value.
     * 
     * @param dateStr
     *            The ISO8601 date string
     * @return the converted Date
     */
    public static Date parseISODateTime(String dateStr)
    {
        return DatatypeConverter.parseDateTime(dateStr).getTime();
    }

    /**
     * Converts the string argumento into a Date value.
     * 
     * @param timeStr
     *            The ISO8601 time string
     * @return the converted Date
     */
    public static Date parseISOTime(String timeStr)
    {
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
     * for (String ds : dates)
     * {
     *     Date date = Humanize.parseSmartDate(ds, &quot;dd/MM/yy&quot;, &quot;yyyy/MM/dd&quot;, &quot;dd/MM/yyyy&quot;);
     *     Assert.assertEquals(date, target);
     * }
     * </pre>
     * 
     * @param dateStr
     *            The date string
     * @param fmts
     *            An array of formats
     * @return the converted Date
     */
    public static Date parseSmartDate(String dateStr, String... fmts)
    {
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
    public static Date parseSmartDateWithSeparator(final String dateStr, final String separator, final String... fmts)
    {
        String tmp = dateStr.replaceAll(separator, "/");

        for (String fmt : fmts)
        {
            try
            {
                DateFormat df = dateFormat(fmt); // cached
                df.setLenient(false);
                return df.parse(tmp);
            } catch (ParseException ignored)
            {
                //
            }
        }

        throw new IllegalArgumentException("Unable to parse date '" + dateStr + "'");
    }

    /**
     * <p>
     * Same as {@link #pluralize(Number, PluralizeParams)} for the target
     * locale.
     * </p>
     * 
     * @param locale
     *            Target locale
     * @param number
     *            The number that triggers the plural state
     * @return formatted text according the right plural state
     */
    public static String pluralize(final Locale locale, final Number number, final PluralizeParams params)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return pluralize(number, params);
            }
        }, locale);
    }

    /**
     * <p>
     * Applies the proper format for a given plural state.
     * </p>
     * Example:
     * 
     * <pre>
     * PluralizeParams p = PluralizeParams
     *         .begin(&quot;There is one file on {1}.&quot;)
     *         .many(&quot;There are {0} files on {1}.&quot;)
     *         .none(&quot;There are no files on {1}.&quot;)
     *         .exts(&quot;disk&quot;);
     * 
     * pluralize(1, p);
     * // == There is one file on disk.
     * pluralize(2, p);
     * // == There are 2 files on disk.
     * pluralize(0, p);
     * // == There are no files on disk.
     * 
     * // ---
     * 
     * PluralizeParams p = PluralizeParams
     *         .begin(&quot;one&quot;)
     *         .many(&quot;{0}&quot;)
     *         .none(&quot;none&quot;);
     * 
     * pluralize(1, p);
     * // = &quot;one&quot;
     * pluralize(2, p);
     * // = &quot;2&quot;
     * </pre>
     * 
     * @param number
     *            The number that triggers the plural state
     * @return formatted text according the right plural state
     */
    public static String pluralize(final Number number, final PluralizeParams p)
    {
        Preconditions.checkNotNull(p.many, "Please, specify a format for many elements");
        Preconditions.checkNotNull(p.one, "Please, specify a format for a single element");

        String none = p.none == null ? p.many : p.none;
        MessageFormat format = pluralizeFormat("{0}", none, p.one, p.many);
        Object[] fp = p.exts == null ? new Object[] { number } : ObjectArrays.concat(number, p.exts);
        return format.render(fp);
    }

    /**
     * Signature for the main use case of
     * {@link #pluralize(Number, PluralizeParams)}.
     * 
     * @param one
     *            Format for a single element
     * @param many
     *            Format for many elements
     * @param none
     *            Format for no element
     * @param number
     *            The number that triggers the plural state
     * @param Optional
     *            extension objects to be passed to the formatter
     * @return formatted text according the right plural state
     */
    public static String pluralize(final String one, final String many,
            final String none, final Number number, final Object... exts)
    {
        PluralizeParams p = PluralizeParams
                .begin(one)
                .many(many)
                .none(none)
                .exts(exts);

        return pluralize(number, p);
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
    public static MessageFormat pluralizeFormat(final String template)
    {
        String[] tokens = template.split("\\s*\\:{2}\\s*");

        if (tokens.length < 4)
        {
            if (tokens.length == 2)
            {
                tokens = new String[] { "{0}", tokens[1], tokens[0], tokens[1] };
            } else if (tokens.length == 3)
            {
                tokens = new String[] { "{0}", tokens[0], tokens[1], tokens[2] };
            } else
            {
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
    public static MessageFormat pluralizeFormat(final String template, final Locale locale)
    {
        return withinLocale(new Callable<MessageFormat>()
        {
            public MessageFormat call() throws Exception
            {
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
    public static MessageFormat pluralizeFormat(final String pattern, final String... choices)
    {
        double[] indexes = new double[choices.length];

        for (int i = 0; i < choices.length; i++)
        {
            indexes[i] = i;
        }

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
    public static PrettyTimeFormat prettyTimeFormat()
    {
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
    public static PrettyTimeFormat prettyTimeFormat(final Locale locale)
    {
        return withinLocale(new Callable<PrettyTimeFormat>()
        {
            public PrettyTimeFormat call() throws Exception
            {
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
    public static String replaceSupplementary(final String value)
    {
        return InterpolationHelper.replaceSupplementary(value, new Replacer()
        {
            public String replace(String in)
            {
                StringBuilder uc = new StringBuilder();

                for (char c : in.toCharArray())
                {
                    uc.append("\\\\u");
                    uc.append(Integer.toHexString(c).toUpperCase());
                }

                return uc.toString();
            }
        });
    }

    /**
     * <p>
     * Rounds a number to significant figures.
     * </p>
     * 
     * @param num
     *            The number to be rounded
     * @param precision
     *            The number of significant digits
     * @return The number rounded to significant figures
     */
    public static Number roundToSignificantFigures(Number num, int precision)
    {
        return new BigDecimal(num.doubleValue())
                .round(new MathContext(precision, RoundingMode.HALF_EVEN));
    }

    /**
     * <p>
     * Sort of poor man's transliteration, i.e. normalizes and strips
     * diacritical marks.
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
     *            The text to be simplified.
     * @return simplified text.
     */
    @Expose
    public static String simplify(final String text)
    {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        return COMB_DIACRITICAL.matcher(normalized).replaceAll("");
    }

    /**
     * @param text
     *            The text to be slugified
     * @return a slugified representation of text specified
     */
    @Expose
    public static String slugify(final String text)
    {
        return slugify(text, SlugifyParams.begin());
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
     * <td>"j-etudie-le-francais"</td>
     * </tr>
     * <tr>
     * <td>"Lo siento, no hablo español"</td>
     * <td>"lo-siento-no-hablo-espanol"</td>
     * </tr>
     * <tr>
     * <td>"\nsome@mail.com\n"</td>
     * <td>"some-mail-com"</td>
     * </tr>
     * </table>
     * 
     * @param text
     *            The text to be slugified
     * @param params
     *            The slugify parameterization object
     * @return a slugified representation of text specified
     */
    public static String slugify(final String text, final SlugifyParams params)
    {
        String result = unidecode(text);
        result = PUNCTUATION.matcher(result).replaceAll("-");
        result = ONLY_SLUG_CHARS.matcher(result).replaceAll("");
        result = CharMatcher.WHITESPACE.trimFrom(result);
        result = HYPEN_SPACE.matcher(result).replaceAll(params.separator);
        result = CharMatcher.INVISIBLE.removeFrom(result);
        return params.isToLowerCase ? result.toLowerCase() : result;
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
    public static String spellBigNumber(final Number value)
    {
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
    public static String spellBigNumber(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
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
    public static String spellDigit(final Number value)
    {
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
    public static String spellDigit(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return spellDigit(value);
            }
        }, locale);
    }

    /**
     * Interprets numbers as occurrences.
     * 
     * @param num
     *            The number of occurrences
     * @return textual representation of the number as occurrences
     */
    public static String times(final Number num)
    {
        java.text.MessageFormat f = new java.text.MessageFormat(
                context.get().getBundle().getString("times.choice"),
                currentLocale()
                );
        return f.format(new Object[] { Math.abs(num.intValue()) });
    }

    /**
     * Same as {@link #times(Number)} for the specified locale.
     * 
     * @param num
     *            The number of occurrences
     * @param locale
     *            Target locale
     * @return textual representation of the number as occurrences
     */
    @Expose
    public static String times(final Number num, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return times(num);
            }
        }, locale);
    }

    /**
     * <p>
     * Converts the given text to title case smartly.
     * </p>
     * 
     * Known limitations:
     * <ul>
     * <li>Phrasal verb detection</li>
     * </ul>
     * 
     * Applies Quick Guide to Capitalization in English at SAP.
     * 
     * <h3>Capitalize</h3>
     * <ul>
     * <li>Nouns</li>
     * <li>Verbs (including <i>is</i> and other forms of <i>be</i>)</li>
     * <li>Participles</li>
     * <li>Adverbs (including <i>than</i> and <i>when</i>)</li>
     * <li>Adjectives (including <i>this</i>, <i>that</i>, and <i>each</i>)</li>
     * <li>Pronouns (including <i>its</i>)</li>
     * <li>Subordinating conjunctions (<i>if</i>, <i>because</i>, <i>as</i>,
     * <i>that</i>)</li>
     * <li>Prepositions and conjunctions with five or more letters
     * (<i>between</i>, <i>without</i>, <i>during, about</i>,<i> because,
     * through</i>)</li>
     * <li>First and last words, no matter what part of speech they are</li>
     * <li>Prepositions that are part of a verb phrase (<i>Logging On</i>,
     * <i>Setting Up</i>)</li>
     * <li>Both elements of hyphenated words (<i>How-To,</i>
     * <i>Country-Specific</i>)</li>
     * <li>Words and phrases in parentheses as you would capitalize them if they
     * did not appear in parentheses</li>
     * <li>Any words, phrases, fragments, or sentences after a colon or
     * semicolon</li>
     * </ul>
     * <h3>Do Not Capitalize</h3>
     * <ul>
     * <li>Coordinating conjunctions (<i>and</i>, <i>but</i>, <i>or</i>,
     * <i>nor</i>, <i>for</i>)</li>
     * <li>Prepositions of four or fewer letters (<i>with</i>, <i>to</i>,
     * <i>for</i>, <i>at</i>, and so on)</li>
     * <li>Articles (<i>a</i>, <i>an</i>, <i>the, some</i>) unless the article
     * is the first or last word in the title</li>
     * <li>The word<i> to</i> in an infinitive phrase</li>
     * <li>Case-specific product names, words, or phrases (<i>mySAP.com</i>,
     * <i>README </i>file, <i>e-Business</i>, and so on)</li>
     * </ul>
     * 
     * @param text
     *            Text to be converted
     * 
     * @return a nice styled title
     */
    @Expose
    public static String titleize(final String text)
    {
        return titleize(text, null);
    }

    /**
     * <p>
     * Converts the given text to title case smartly.
     * </p>
     * 
     * @param text
     *            Text to be converted
     * @param intCaps
     *            An internal capitalized word list
     * @return a nice styled title
     */
    public static String titleize(final String text, final String[] intCaps)
    {
        String str = text.toLowerCase(Locale.ENGLISH).replaceAll("[\\s_]+", SPACE).trim();
        return titleize(str, SPACE, intCaps);
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
    @Expose
    public static String underscore(final String text)
    {
        return text.replaceAll("\\s+", "_");
    }

    /**
     * Simple, just enough, unicode to ascii transliteration.
     * 
     * @param text
     *            The text to be decoded
     * @return unidecoded text
     */
    @Expose
    public static String unidecode(final String text)
    {
        return Unidecode.decode(text);
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
    public static String unmask(final String mask, final String value) throws ParseException
    {
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
    public static String wordWrap(final String value, final int len)
    {
        if (len < 0 || value.length() <= len)
            return value;

        BreakIterator bi = BreakIterator.getWordInstance(currentLocale());
        bi.setText(value);

        return value.substring(0, bi.following(len));
    }

    private static Locale currentLocale()
    {
        return context.get().getLocale();
    }

    private static ContextFactory loadContextFactory()
    {
        ServiceLoader<ContextFactory> ldr = ServiceLoader.load(ContextFactory.class);

        for (ContextFactory factory : ldr)
        {
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
    private static boolean needPlural(final int n)
    {
        int tmp = 0;
        int an = Math.abs(n);

        while (an > 0)
        {
            tmp = an % 10;

            if (tmp > 1)
            {
                return true;
            }

            an /= 10;
        }

        return false;
    }

    private static String prefix(final Number value, final int min, final Map<Long, String> prefixes)
    {
        DecimalFormat df = context.get().getDecimalFormat();

        long v = value.longValue();

        if (v < 0)
        {
            return value.toString();
        }

        for (Entry<Long, String> entry : prefixes.entrySet())
        {
            if (entry.getKey() <= v)
            {
                df.applyPattern(entry.getValue());
                return stripZeros(df, df.format((v >= min) ? v / (float) entry.getKey() : v));
            }
        }

        return stripZeros(df, df.format(value.toString()));
    }

    private static String resolveInternalCapsWord(String word, String[] internalCaps)
    {
        for (String ic : internalCaps)
        {
            if (word.matches(String.format("(?i)[\\(\\[-]*%s[\\)\\]-]*", ic)))
            {
                return word.replace(ic.toLowerCase(), ic);
            }
        }
        return capitalize(word);
    }

    private static String stripZeros(final DecimalFormat decf, final String fmtd)
    {
        char decsep = decf.getDecimalFormatSymbols().getDecimalSeparator();
        return fmtd.replaceAll("\\" + decsep + "00", EMPTY);
    }

    private static String titleize(String str, String separator, String[] intCaps)
    {
        StringBuilder sb = new StringBuilder(str.length());
        String[] parts = str.split(separator);
        Matcher m;

        for (int i = 0; i < parts.length; i++)
        {
            String word = parts[i];
            boolean notLastWord = i < parts.length - 1;

            if (i > 0 && notLastWord && titleIgnoredWords.contains(word))
            {
                sb.append(word);
            }
            else if ((m = titleWordSperator.matcher(word)).find())
            {
                sb.append(titleize(word, m.group(1), intCaps));

                // hmmm, okay... a bit upsetted cyclomatic complexity
                while (m.find())
                {
                    sb.append(titleize(word, m.group(1), intCaps));
                }
            }
            else
            {
                sb.append(intCaps == null ? capitalize(word) : resolveInternalCapsWord(word, intCaps));
            }

            if (notLastWord)
            {
                sb.append(separator);
            }
        }

        return sb.toString();
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
    private static <T> T withinLocale(final Callable<T> operation, final Locale locale)
    {
        DefaultContext ctx = context.get();
        Locale oldLocale = ctx.getLocale();

        try
        {
            ctx.setLocale(locale);
            return operation.call();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            ctx.setLocale(oldLocale);
            context.set(ctx);
        }
    }

    private Humanize()
    {
        //
    }

}
