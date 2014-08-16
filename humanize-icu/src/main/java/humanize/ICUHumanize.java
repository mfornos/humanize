/*
   
    _   _ _   _ __  __  ___  _   _ ___ __________ 
   | | | | | | |  \/  |  _  | \ | |_ _|__  / ____|
   | |_| | | | | |\/| | |_| |  \| || |  / /|  _|  
   |  _  | |_| | |  | |  _  | |\  || | / /_| |___ 
   |_| |_|\___/|_|  |_|_| |_|_| \_|___/____|_____|
   
 
   Copyright 2013 mfornos
   
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

import static humanize.util.Constants.EMPTY;
import humanize.icu.spi.MessageFormat;
import humanize.icu.spi.context.DefaultICUContext;
import humanize.icu.spi.context.ICUContextFactory;
import humanize.spi.context.ContextFactory;
import humanize.text.util.InterpolationHelper;
import humanize.text.util.Replacer;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

import com.google.common.collect.ObjectArrays;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.Transliterator;
import com.ibm.icu.text.CompactDecimalFormat.CompactStyle;

/**
 * <p>
 * Facility for adding a "human touch" to data. It is thread-safe and supports
 * per-thread internationalization. Additionally provides a concise facade for
 * access to the <a href="http://icu-project.org/">International Components for
 * Unicode</a> (ICU) Java APIs.
 * </p>
 * 
 */
public final class ICUHumanize
{

    private static final ContextFactory contextFactory = loadContextFactory();

    private static final ThreadLocal<DefaultICUContext> context = new ThreadLocal<DefaultICUContext>()
    {
        protected DefaultICUContext initialValue()
        {
            return (DefaultICUContext) contextFactory.createContext();
        };
    };

    /**
     * <p>
     * Same as {@link #compactDecimal(Number, CompactStyle) compactDecimal} but
     * defaults to SHORT compact style.
     * </p>
     * 
     * @param value
     *            The number to be abbreviated
     * @return a compact textual representation of the given value
     */
    public static String compactDecimal(final Number value)
    {
        NumberFormat fmt = context.get().getCompactDecimalFormat();
        return fmt.format(value);
    }

    /**
     * Produces abbreviated numbers. For example, '1.2B' instead of
     * '1,200,000,000'. The format will be appropriate for the given language,
     * such as '2,4 Millionen' for German.
     * 
     * @param value
     *            The number to be abbreviated
     * @param style
     *            The compaction style
     * @return a compact textual representation of the given value
     */
    public static String compactDecimal(final Number value, final CompactStyle style)
    {
        NumberFormat fmt = context.get().getCompactDecimalFormat(style);
        return fmt.format(value);
    }

    /**
     * <p>
     * Same as {@link #compactDecimal(Number, CompactStyle) compactDecimal} for
     * the specified locale.
     * </p>
     * 
     * @param value
     *            The number to be abbreviated
     * @param style
     *            The compaction style
     * @param locale
     *            The locale
     * @return a compact textual representation of the given value
     */
    public static String compactDecimal(final Number value, final CompactStyle style, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return compactDecimal(value, style);
            }
        }, locale);
    }

    /**
     * <p>
     * Same as {@link #compactDecimal(Number) compactDecimal} for the specified
     * locale.
     * </p>
     * 
     * @param value
     *            The number to be abbreviated
     * @param locale
     *            The locale
     * @return a compact textual representation of the given value
     */
    public static String compactDecimal(final Number value, final Locale locale)
    {
        return compactDecimal(value, CompactStyle.SHORT, locale);
    }

    /**
     * <p>
     * Returns an ICU based DateFormat instance for the current thread.
     * </p>
     * <p>
     * Date/Time format syntax:
     * </p>
     * <p>
     * The date/time format is specified by means of a string time pattern. In
     * this pattern, all ASCII letters are reserved as pattern letters, which
     * are defined as the following:
     * </p>
     * <style>pre { white-space: pre-wrap; white-space: -moz-pre-wrap;
     * white-space: -pre-wrap; white-space: -o-pre-wrap; word-wrap: break-word;
     * }</style>
     * 
     * <pre>
     *  Symbol   Meaning                 Presentation        Example
     *  ------   -------                 ------------        -------
     *  G        era designator          (Text)              AD
     *  y        year                    (Number)            1996
     *  Y        year (week of year)     (Number)            1997
     *  u        extended year           (Number)            4601
     *  U        cyclic year name        (Text,NumFallback)  ren-chen (29)
     *  Q        Quarter                 (Text &amp; Number)     Q2 &amp; 02
     *  M        month in year           (Text &amp; Number)     July &amp; 07
     *  d        day in month            (Number)            10
     *  h        hour in am/pm (1~12)    (Number)            12
     *  H        hour in day (0~23)      (Number)            0
     *  m        minute in hour          (Number)            30
     *  s        second in minute        (Number)            55
     *  S        fractional second       (Number)            978
     *  E        day of week             (Text)              Tuesday
     *  e        day of week (local 1~7) (Text &amp; Number)     Tues &amp; 2
     *  D        day in year             (Number)            189
     *  F        day of week in month    (Number)            2 (2nd Wed in July)
     *  w        week in year            (Number)            27
     *  W        week in month           (Number)            2
     *  a        am/pm marker            (Text)              PM
     *  k        hour in day (1~24)      (Number)            24
     *  K        hour in am/pm (0~11)    (Number)            0
     *  z        time zone               (Text)              PST
     *  zzzz     time zone               (Text)              Pacific Standard Time
     *  Z        time zone (RFC 822)     (Number)            -0800
     *  ZZZZ     time zone (RFC 822)     (Text &amp; Number)     GMT-08:00
     *  ZZZZZ    time zone (ISO 8601)    (Text &amp; Number)     -08:00 &amp; Z
     *  v        time zone (generic)     (Text)              PT
     *  vvvv     time zone (generic)     (Text)              Pacific Time
     *  V        time zone (abreviation) (Text)              PST
     *  VVVV     time zone (location)    (Text)              United States Time (Los Angeles)
     *  g        Julian day              (Number)            2451334
     *  A        milliseconds in day     (Number)            69540000
     *  q        stand alone quarter     (Text &amp; Number)     Q2 &amp; 02
     *  L        stand alone month       (Text &amp; Number)     July &amp; 07
     *  c        stand alone day of week (Text &amp; Number)     Tuesday &amp; 2
     *  &#39;        escape for text         (Delimiter)         &#39;Date=&#39;
     *  &#39;&#39;       single quote            (Literal)           &#39;o&#39;&#39;clock&#39;
     * </pre>
     * <p>
     * The count of pattern letters determine the format.
     * </p>
     * <p>
     * (Text): 4 or more, use full form, &lt;4, use short or abbreviated form if
     * it exists. (e.g., "EEEE" produces "Monday", "EEE" produces "Mon")
     * </p>
     * <p>
     * (Number): the minimum number of digits. Shorter numbers are zero-padded
     * to this amount (e.g. if "m" produces "6", "mm" produces "06"). Year is
     * handled specially; that is, if the count of 'y' is 2, the Year will be
     * truncated to 2 digits. (e.g., if "yyyy" produces "1997", "yy" produces
     * "97".) Unlike other fields, fractional seconds are padded on the right
     * with zero.
     * </p>
     * <p>
     * (Text &amp; Number): 3 or over, use text, otherwise use number. (e.g.,
     * "M" produces "1", "MM" produces "01", "MMM" produces "Jan", and "MMMM"
     * produces "January".)
     * </p>
     * <p>
     * (Text,NumFallback): Behaves like Text if there is supporting data, like
     * Number otherwise.
     * </p>
     * <p>
     * Any characters in the pattern that are not in the ranges of ['a'..'z']
     * and ['A'..'Z'] will be treated as quoted text. For instance, characters
     * like ':', '.', ' ', '#' and '@' will appear in the resulting time text
     * even they are not embraced within single quotes.
     * </p>
     * <p>
     * A pattern containing any invalid pattern letter will result in a failing
     * UErrorCode result during formatting or parsing.
     * </p>
     * <p>
     * Examples using the US locale:
     * </p>
     * 
     * <pre>
     *     Format Pattern                         Result
     *     --------------                         -------
     *     &quot;yyyy.MM.dd G &#39;at&#39; HH:mm:ss vvvv&quot; -&gt;&gt;  1996.07.10 AD at 15:08:56 Pacific Time
     *     &quot;EEE, MMM d, &#39;&#39;yy&quot;                -&gt;&gt;  Wed, July 10, &#39;96
     *     &quot;h:mm a&quot;                          -&gt;&gt;  12:08 PM
     *     &quot;hh &#39;o&#39;&#39;clock&#39; a, zzzz&quot;           -&gt;&gt;  12 o&#39;clock PM, Pacific Daylight Time
     *     &quot;K:mm a, vvv&quot;                     -&gt;&gt;  0:00 PM, PT
     *     &quot;yyyyy.MMMMM.dd GGG hh:mm aaa&quot;    -&gt;&gt;  1996.July.10 AD 12:08 PM
     * </pre>
     * 
     * @param pattern
     *            Format pattern that follows the conventions of
     *            {@link com.ibm.icu.text.DateFormat DateFormat}
     * @return a DateFormat instance for the current thread
     */
    public static DateFormat dateFormatInstance(final String pattern)
    {
        return DateFormat.getPatternInstance(pattern, context.get().getLocale());
    }

    /**
     * <p>
     * Same as {@link #dateFormatInstance(String) dateFormatInstance} for the
     * specified locale.
     * </p>
     * 
     * @param pattern
     *            Format pattern that follows the conventions of
     *            {@link com.ibm.icu.text.DateFormat DateFormat}
     * @param locale
     *            Target locale
     * @return a DateFormat instance for the current thread
     */
    public static DateFormat dateFormatInstance(final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<DateFormat>()
        {
            public DateFormat call() throws Exception
            {
                return dateFormatInstance(pattern);
            }
        }, locale);
    }

    /**
     * <p>
     * Returns an ICU based DecimalFormat instance for the current thread. It
     * has a variety of features designed to make it possible to parse and
     * format numbers in any locale, including support for Western, Arabic, or
     * Indic digits. It also supports different flavors of numbers, including
     * integers ("123"), fixed-point numbers ("123.4"), scientific notation
     * ("1.23E4"), percentages ("12%"), and currency amounts ("$123.00",
     * "USD123.00", "123.00 US dollars"). All of these flavors can be easily
     * localized.
     * </p>
     * 
     * <h5>Patterns</h5>
     * 
     * <p>
     * A <code>DecimalFormat</code> consists of a <em>pattern</em> and a set of
     * <em>symbols</em>. The pattern may be set directly using #applyPattern ,
     * or indirectly using other API methods which manipulate aspects of the
     * pattern, such as the minimum number of integer digits. The symbols are
     * stored in a DecimalFormatSymbols object. When using the NumberFormat
     * factory methods, the pattern and symbols are read from ICU's locale data.
     * 
     * <h5>Special Pattern Characters</h5>
     * 
     * <p>
     * Many characters in a pattern are taken literally; they are matched during
     * parsing and output unchanged during formatting. Special characters, on
     * the other hand, stand for other characters, strings, or classes of
     * characters. For example, the '#' character is replaced by a localized
     * digit. Often the replacement character is the same as the pattern
     * character; in the U.S. locale, the ',' grouping character is replaced by
     * ','. However, the replacement is still happening, and if the symbols are
     * modified, the grouping character changes. Some special characters affect
     * the behavior of the formatter by their presence; for example, if the
     * percent character is seen, then the value is multiplied by 100 before
     * being displayed.
     * 
     * <p>
     * To insert a special character in a pattern as a literal, that is, without
     * any special meaning, the character must be quoted. There are some
     * exceptions to this which are noted below.
     * 
     * <p>
     * The characters listed here are used in non-localized patterns. Localized
     * patterns use the corresponding characters taken from this formatter's
     * DecimalFormatSymbols object instead, and these characters lose their
     * special status. Two exceptions are the currency sign and quote, which are
     * not localized.
     * 
     * <table border="0" cellspacing="0" cellpadding="3" width="100%" summary="Chart showing symbol, location, localized, and meaning.">
     * <tr>
     * <th align=left>Symbol
     * <th align=left>Location
     * <th align=left>Localized?
     * <th align=left>Meaning
     * <tr valign=top>
     * <td><code>0</code>
     * <td>Number
     * <td>Yes
     * <td>Digit
     * <tr valign=top">
     * <td><code>1-9</code>
     * <td>Number
     * <td>Yes
     * <td>'1' through '9' indicate rounding.
     * 
     * <tr valign=top>
     * <td><code>@</code>
     * <td>Number
     * <td>No
     * <td>Significant digit
     * <tr valign=top>
     * <td><code>#</code>
     * <td>Number
     * <td>Yes
     * <td>Digit, zero shows as absent
     * <tr valign=top>
     * <td><code>.</code>
     * <td>Number
     * <td>Yes
     * <td>Decimal separator or monetary decimal separator
     * <tr valign=top>
     * <td><code>-</code>
     * <td>Number
     * <td>Yes
     * <td>Minus sign
     * <tr valign=top>
     * <td><code>,</code>
     * <td>Number
     * <td>Yes
     * <td>Grouping separator
     * <tr valign=top>
     * <td><code>E</code>
     * <td>Number
     * <td>Yes
     * <td>Separates mantissa and exponent in scientific notation.
     * <em>Need not be quoted in prefix or suffix.</em>
     * <tr valign=top>
     * <td><code>+</code>
     * <td>Exponent
     * <td>Yes
     * <td>Prefix positive exponents with localized plus sign.
     * <em>Need not be quoted in prefix or suffix.</em>
     * <tr valign=top>
     * <td><code>;</code>
     * <td>Subpattern boundary
     * <td>Yes
     * <td>Separates positive and negative subpatterns
     * <tr valign=top>
     * <td><code>%</code>
     * <td>Prefix or suffix
     * <td>Yes
     * <td>Multiply by 100 and show as percentage
     * <tr valign=top>
     * <td><code>&#92;u2030</code>
     * <td>Prefix or suffix
     * <td>Yes
     * <td>Multiply by 1000 and show as per mille
     * <tr valign=top>
     * <td><code>&#164;</code> (<code>&#92;u00A4</code>)
     * <td>Prefix or suffix
     * <td>No
     * <td>Currency sign, replaced by currency symbol. If doubled, replaced by
     * international currency symbol. If tripled, replaced by currency plural
     * names, for example, "US dollar" or "US dollars" for America. If present
     * in a pattern, the monetary decimal separator is used instead of the
     * decimal separator.
     * <tr valign=top>
     * <td><code>'</code>
     * <td>Prefix or suffix
     * <td>No
     * <td>Used to quote special characters in a prefix or suffix, for example,
     * <code>"'#'#"</code> formats 123 to <code>"#123"</code>. To create a
     * single quote itself, use two in a row: <code>"# o''clock"</code>.
     * <tr valign=top>
     * <td><code>*</code>
     * <td>Prefix or suffix boundary
     * <td>Yes
     * <td>Pad escape, precedes pad character
     * </table>
     * <p>
     * A <code>DecimalFormat</code> pattern contains a postive and negative
     * subpattern, for example, "#,##0.00;(#,##0.00)". Each subpattern has a
     * prefix, a numeric part, and a suffix. If there is no explicit negative
     * subpattern, the negative subpattern is the localized minus sign prefixed
     * to the positive subpattern. That is, "0.00" alone is equivalent to
     * "0.00;-0.00". If there is an explicit negative subpattern, it serves only
     * to specify the negative prefix and suffix; the number of digits, minimal
     * digits, and other characteristics are ignored in the negative subpattern.
     * That means that "#,##0.0#;(#)" has precisely the same result as
     * "#,##0.0#;(#,##0.0#)".
     * 
     * <p>
     * The prefixes, suffixes, and various symbols used for infinity, digits,
     * thousands separators, decimal separators, etc. may be set to arbitrary
     * values, and they will appear properly during formatting. However, care
     * must be taken that the symbols and strings do not conflict, or parsing
     * will be unreliable. For example, either the positive and negative
     * prefixes or the suffixes must be distinct for #parse to be able to
     * distinguish positive from negative values. Another example is that the
     * decimal separator and thousands separator should be distinct characters,
     * or parsing will be impossible.
     * 
     * <p>
     * The <em>grouping separator</em> is a character that separates clusters of
     * integer digits to make large numbers more legible. It commonly used for
     * thousands, but in some locales it separates ten-thousands. The
     * <em>grouping size</em> is the number of digits between the grouping
     * separators, such as 3 for "100,000,000" or 4 for "1 0000 0000". There are
     * actually two different grouping sizes: One used for the least significant
     * integer digits, the <em>primary grouping size</em>, and one used for all
     * others, the <em>secondary grouping size</em>. In most locales these are
     * the same, but sometimes they are different. For example, if the primary
     * grouping interval is 3, and the secondary is 2, then this corresponds to
     * the pattern "#,##,##0", and the number 123456789 is formatted as
     * "12,34,56,789". If a pattern contains multiple grouping separators, the
     * interval between the last one and the end of the integer defines the
     * primary grouping size, and the interval between the last two defines the
     * secondary grouping size. All others are ignored, so "#,##,###,####" ==
     * "###,###,####" == "##,#,###,####".
     * 
     * <p>
     * Illegal patterns, such as "#.#.#" or "#.###,###", will cause
     * <code>DecimalFormat</code> to throw an IllegalArgumentException with a
     * message that describes the problem.
     * 
     * <h5>Pattern BNF</h5>
     * 
     * <pre>
     * pattern    := subpattern (';' subpattern)?
     * subpattern := prefix? number exponent? suffix?
     * number     := (integer ('.' fraction)?) | sigDigits
     * prefix     := '&#92;u0000'..'&#92;uFFFD' - specialCharacters
     * suffix     := '&#92;u0000'..'&#92;uFFFD' - specialCharacters
     * integer    := '#'* '0'* '0'
     * fraction   := '0'* '#'*
     * sigDigits  := '#'* '@' '@'* '#'*
     * exponent   := 'E' '+'? '0'* '0'
     * padSpec    := '*' padChar
     * padChar    := '&#92;u0000'..'&#92;uFFFD' - quote
     * &#32;
     * Notation:
     *   X*       0 or more instances of X
     *   X?       0 or 1 instances of X
     *   X|Y      either X or Y
     *   C..D     any character from C up to D, inclusive
     *   S-T      characters in S, except those in T
     * </pre>
     * 
     * The first subpattern is for positive numbers. The second (optional)
     * subpattern is for negative numbers.
     * 
     * <p>
     * Not indicated in the BNF syntax above:
     * 
     * <ul>
     * <li>The grouping separator ',' can occur inside the integer and sigDigits
     * elements, between any two pattern characters of that element, as long as
     * the integer or sigDigits element is not followed by the exponent element.
     * 
     * <li>Two grouping intervals are recognized: That between the decimal point
     * and the first grouping symbol, and that between the first and second
     * grouping symbols. These intervals are identical in most locales, but in
     * some locales they differ. For example, the pattern &quot;#,##,###&quot;
     * formats the number 123456789 as &quot;12,34,56,789&quot;.</li>
     * 
     * <li>
     * The pad specifier <code>padSpec</code> may appear before the prefix,
     * after the prefix, before the suffix, after the suffix, or not at all.
     * 
     * <li>
     * In place of '0', the digits '1' through '9' may be used to indicate a
     * rounding increment.
     * </ul>
     * 
     * @param pattern
     *            Format pattern that follows the conventions of
     *            {@link com.ibm.icu.text.DecimalFormat DecimalFormat}
     * @return a DecimalFormat instance for the current thread
     */
    public static DecimalFormat decimalFormatInstance(final String pattern)
    {
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
    public static DecimalFormat decimalFormatInstance(final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<DecimalFormat>()
        {
            public DecimalFormat call() throws Exception
            {
                return decimalFormatInstance(pattern);
            }
        }, locale);
    }

    /**
     * <p>
     * Formats a number of seconds as hours, minutes and seconds.
     * </p>
     * 
     * @param value
     *            Number of seconds
     * @return Number of seconds as hours, minutes and seconds
     */
    public static String duration(final Number value)
    {
        // NOTE: does not support any other locale
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return context.get().getRuleBasedNumberFormat(RuleBasedNumberFormat.DURATION).format(value);
            }
        }, Locale.ENGLISH);
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
    public static String format(final String pattern, final Object... args)
    {
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
    public static String formatCurrency(final Number value)
    {
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
     * Same as {@link #formatDate(int, Date) formatDate} with SHORT style.
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
     * Same as {@link #formatDate(Date) formatDate} for the specified locale.
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
     *            The pattern. See {@link dateFormatInstance(String)}
     * @return a formatted date/time string
     */
    public static String formatDate(final Date value, final String pattern)
    {
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
     * Formats a monetary amount with currency plural names, for example,
     * "US dollar" or "US dollars" for America.
     * </p>
     * 
     * @param value
     *            Number to be formatted
     * @return String representing the monetary amount
     */
    public static String formatPluralCurrency(final Number value)
    {
        DecimalFormat decf = context.get().getPluralCurrencyFormat();
        return stripZeros(decf, decf.format(value));
    }

    /**
     * <p>
     * Same as {@link #formatPluralCurrency(Number) formatPluralCurrency} for
     * the specified locale.
     * </p>
     * 
     * @param value
     *            Number to be formatted
     * @param locale
     *            Target locale
     * @return String representing the monetary amount
     */
    public static String formatPluralCurrency(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return formatPluralCurrency(value);
            }
        }, locale);
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
    public static MessageFormat messageFormatInstance(final String pattern)
    {
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
    public static MessageFormat messageFormatInstance(final String pattern, final Locale locale)
    {
        return withinLocale(new Callable<MessageFormat>()
        {
            public MessageFormat call() throws Exception
            {
                return messageFormatInstance(pattern);
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
    public static String naturalDay(final Date value)
    {
        return naturalDay(DateFormat.RELATIVE_SHORT, value);
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
    public static String naturalDay(final Date value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
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
     *            DateFormat style. RELATIVE_SHORT, RELATIVE_MEDIUM or
     *            RELATIVE_LONG
     * @param value
     *            Date to be converted
     * @return String with "today", "tomorrow" or "yesterday" compared to
     *         current day. Otherwise, returns a string formatted according to a
     *         locale sensitive DateFormat.
     */
    public static String naturalDay(final int style, final Date value)
    {
        return formatDate(style, value).toLowerCase();
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
    public static String naturalTime(final Date duration)
    {
        return context.get().getDurationFormat().formatDurationFromNowTo(duration);
    }

    /**
     * <p>
     * Computes both past and future relative dates.
     * </p>
     * 
     * <p>
     * E.g. "1 day ago", "1 day from now", "10 years ago", "3 minutes from now"
     * and so on.
     * </p>
     * 
     * @param reference
     *            Date to be used as reference
     * @param duration
     *            Date to be used as duration from reference
     * @return String representing the relative date
     */
    public static String naturalTime(final Date reference, final Date duration)
    {
        long diff = duration.getTime() - reference.getTime();
        return context.get().getDurationFormat().formatDurationFrom(diff, reference.getTime());
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
     * Same as {@link #naturalTime(Date) naturalTime} for the specified locale.
     * 
     * @param duration
     *            Date to be used as duration from current date
     * @param locale
     *            Target locale
     * @return String representing the relative date
     */
    public static String naturalTime(final Date duration, final Locale locale)
    {
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
    public static String ordinalize(final Number value)
    {
        return context.get().getRuleBasedNumberFormat(RuleBasedNumberFormat.ORDINAL).format(value);
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
    public static String ordinalize(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call()
            {
                return ordinalize(value);
            }
        }, locale);
    }

    /**
     * <p>
     * Converts the given text to number.
     * </p>
     * 
     * @param text
     *            String containing a spelled out number.
     * @return Text converted to Number
     * @throws ParseException
     */
    public static Number parseNumber(final String text) throws ParseException
    {
        return context.get().getRuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT).parse(text);
    }

    /**
     * <p>
     * Same as {@link #parseNumber(String) parseNumber} for the specified
     * locale.
     * </p>
     * 
     * @param text
     *            String containing a spelled out number.
     * @param locale
     *            Target locale
     * @return Text converted to Number
     * @throws ParseException
     */
    public static Number parseNumber(final String text, final Locale locale) throws ParseException
    {
        return withinLocale(new Callable<Number>()
        {
            public Number call() throws Exception
            {
                return parseNumber(text);
            }
        }, locale);
    }

    /**
     * <p>
     * Same as {@link #pluralize(String, Number, Object...)} for the target
     * locale.
     * </p>
     * 
     * @param locale
     *            The target locale
     * @param pattern
     *            The formatting pattern with plural rules
     * @param value
     *            The number that will trigger plural category
     * @param args
     *            Optional arguments for the formatting pattern
     * @return a properly formatted message
     */
    public static String pluralize(final Locale locale, final String pattern, final Number value,
            final Object... args)
    {
        return withinLocale(new Callable<String>()
        {
            @Override
            public String call() throws Exception
            {
                return pluralize(pattern, value, args);
            }

        }, locale);
    }

    /**
     * <p>
     * Helper method to use ICU message patterns with Language Plural Rules
     * logic. For categories available by language, please see:
     * http://www.unicode.org/cldr/charts/latest/supplemental/
     * language_plural_rules.html
     * </p>
     * 
     * <p>
     * Examples:
     * </p>
     * 
     * <pre>
     * <code>
     * pluralize("Hi {0, plural, one{man}other{men}}!", 1) // == "Hi man!"
     * pluralize("Hi {0, plural, one{man}other{men}}!", 25) // == "Hi men!"
     * </code>
     * </pre>
     * 
     * @param pattern
     *            The formatting pattern with plural rules
     * @param value
     *            The number that will trigger plural category
     * @param args
     *            Optional arguments for the formatting pattern
     * @return a properly formatted message
     */
    public static String pluralize(final String pattern, final Number value, final Object... args)
    {
        Object[] params = ObjectArrays.concat(value, args);
        return messageFormatInstance(pattern).render(params);
    }

    /**
     * <p>
     * Replaces characters outside the Basic Multilingual Plane with their name.
     * </p>
     * 
     * @param value
     *            The text to be matched
     * @return text with characters outside BMP replaced by their name or the
     *         given text unaltered
     */
    public static String replaceSupplementary(final String value)
    {
        return InterpolationHelper.replaceSupplementary(value, new Replacer()
        {
            public String replace(String in)
            {
                return UCharacter.getName(in, ", ");
            }
        });
    }

    /**
     * <p>
     * Guesses the best locale-dependent pattern to format the date/time fields
     * that the skeleton specifies.
     * </p>
     * 
     * @param value
     *            The date to be formatted
     * @param skeleton
     *            A pattern containing only the variable fields. For example,
     *            "MMMdd" and "mmhh" are skeletons.
     * @return A string with a text representation of the date
     */
    public static String smartDateFormat(final Date value, final String skeleton)
    {
        return formatDate(value, context.get().getBestPattern(skeleton));
    }

    /**
     * <p>
     * Same as {@link #smartDateFormat(Date) smartDateFormat} for the specified
     * locale.
     * </p>
     * 
     * @param value
     *            The date to be formatted
     * @param skeleton
     *            A pattern containing only the variable fields. For example,
     *            "MMMdd" and "mmhh" are skeletons.
     * @param locale
     *            Target locale
     * @return A string with a text representation of the date
     */
    public static String smartDateFormat(final Date value, final String skeleton, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return smartDateFormat(value, skeleton);
            }
        }, locale);
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
    public static String spellNumber(final Number value)
    {
        return context.get().getRuleBasedNumberFormat(RuleBasedNumberFormat.SPELLOUT).format(value);
    }

    /**
     * <p>
     * Same as {@link #spellNumber(Number) spellNumber} for the specified
     * locale.
     * </p>
     * 
     * @param value
     *            Number to be converted
     * @param locale
     *            Target locale
     * @return the number converted to words
     */
    public static String spellNumber(final Number value, final Locale locale)
    {
        return withinLocale(new Callable<String>()
        {
            public String call() throws Exception
            {
                return spellNumber(value);
            }
        }, locale);
    }

    /**
     * Converts the characters of the given text to latin.
     * 
     * @param text
     *            The text to be transformed
     * @return transliterated text
     */
    public static String transliterate(final String text)
    {
        return transliterate(text, "Latin");
    }

    /**
     * Converts the characters of the given text to the specified script.
     * 
     * <p>
     * The simplest identifier is a 'basic ID'.
     * </p>
     * 
     * <pre>
     * basicID := (&lt;source&gt; "-")? &lt;target&gt; ("/" &lt;variant&gt;)?
     * </pre>
     * 
     * <p>
     * A basic ID typically names a source and target. In "Katakana-Latin",
     * "Katakana" is the source and "Latin" is the target. The source specifier
     * describes the characters or strings that the transform will modify. The
     * target specifier describes the result of the modification. If the source
     * is not given, then the source is "Any", the set of all characters. Source
     * and Target specifiers can be Script IDs (long like "Latin" or short like
     * "Latn"), Unicode language Identifiers (like fr, en_US, or zh_Hant), or
     * special tags (like Any or Hex). For example:
     * </p>
     * 
     * <pre>
     * Katakana-Latin
     * Null
     * Hex-Any/Perl
     * Latin-el
     * Greek-en_US/UNGEGN
     * </pre>
     * 
     * <p>
     * Some basic IDs contain a further specifier following a forward slash.
     * This is the variant, and it further specifies the transform when several
     * versions of a single transformation are possible. For example, ICU
     * provides several transforms that convert from Unicode characters to
     * escaped representations. These include standard Unicode syntax "U+4E01",
     * Perl syntax "\x{4E01}", XML syntax "&#x4E01;", and others. The transforms
     * for these operations are named "Any-Hex/Unicode", "Any-Hex/Perl", and
     * "Any-Hex/XML", respectively. If no variant is specified, then the default
     * variant is selected. In the example of "Any-Hex", this is the Java
     * variant (for historical reasons), so "Any-Hex" is equivalent to
     * "Any-Hex/Java".
     * </p>
     * 
     * @param text
     *            The text to be transformed
     * @param id
     *            The transliterator identifier
     * @return transliterated text
     */
    public static String transliterate(final String text, final String id)
    {
        Transliterator transliterator = Transliterator.getInstance(id);
        return transliterator.transform(text);
    }

    // ( private methods )------------------------------------------------------

    private static ContextFactory loadContextFactory()
    {
        ServiceLoader<ContextFactory> ldr = ServiceLoader.load(ContextFactory.class);
        for (ContextFactory factory : ldr)
        {
            if (ICUContextFactory.class.isAssignableFrom(factory.getClass()))
                return factory;
        }

        throw new RuntimeException("No ContextFactory was found");
    }

    private static String stripZeros(final DecimalFormat decf, final String fmtd)
    {
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
    private static <T> T withinLocale(final Callable<T> operation, final Locale locale)
    {
        DefaultICUContext ctx = context.get();
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

    private ICUHumanize()
    {
        //
    }

}
