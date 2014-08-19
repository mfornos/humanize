package humanize.util;

import humanize.spi.context.DefaultContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

/**
 * Humanize constants
 * 
 */
public final class Constants
{

    public enum TimeStyle
    {
        STANDARD
        {
            public String format(DefaultContext ctx, boolean neg, int h, int m, int s)
            {

                return String.format("%s%d:%02d:%02d", neg ? '-' : "", h, m, s);

            }
        },
        FRENCH_DECIMAL
        {
            public String format(DefaultContext ctx, boolean neg, int h, int m, int s)
            {

                String r;

                if (h == 0)
                {
                    r = (m == 0) ? String.format("%d%s", s, ctx.timeSuffix(2)) :
                            (s == 0) ? String.format("%d%s", m, ctx.timeSuffix(1)) :
                                    String.format("%d%s %d%s", m, ctx.timeSuffix(1), s, ctx.timeSuffix(2));
                } else
                {
                    r = (m == 0) ?
                            ((s == 0) ? String.format("%d%s", h, ctx.timeSuffix(0)) :
                                    String.format("%d%s %d%s", h, ctx.timeSuffix(0), s, ctx.timeSuffix(2))) :
                            (s == 0) ?
                                    String.format("%d%s %d%s", h, ctx.timeSuffix(0), m, ctx.timeSuffix(1)) :
                                    String.format("%d%s %d%s %d%s", h, ctx.timeSuffix(0), m, ctx.timeSuffix(1), s,
                                            ctx.timeSuffix(2));
                }

                return (neg ? '-' : "") + r;
            }
        };

        public abstract String format(DefaultContext defaultContext, boolean neg, int h, int m, int s);

    }

    public static final Pattern SPLIT_CAMEL = Pattern
            .compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");

    public static final Pattern ONLY_SLUG_CHARS = Pattern.compile("[^-\\w\\s]");

    public static final String DEFAULT_SLUG_SEPARATOR = "-";

    public static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}+");

    public static final Pattern COMB_DIACRITICAL = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public static final Pattern HYPEN_SPACE = Pattern.compile("[-\\s]+");

    // See http://en.wikipedia.org/wiki/UTF-16
    public static final Pattern NOT_IN_BMP = Pattern.compile("([^\u0000-\uD7FF\uE000-\uFFFF])");

    public static final String SPACE = " ";

    public static final String EMPTY = "";

    public static final String ORDINAL_FMT = "%d%s";

    public static final int ND_FACTOR = 1000 * 60 * 60 * 23;

    public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    public static final Map<BigDecimal, String> bigDecExponents = new LinkedHashMap<BigDecimal, String>();

    static
    {
        bigDecExponents.put(BigDecimal.TEN.pow(3), "thousand");
        bigDecExponents.put(BigDecimal.TEN.pow(6), "million");
        bigDecExponents.put(BigDecimal.TEN.pow(9), "billion");
        bigDecExponents.put(BigDecimal.TEN.pow(12), "trillion");
        bigDecExponents.put(BigDecimal.TEN.pow(15), "quadrillion");
        bigDecExponents.put(BigDecimal.TEN.pow(18), "quintillion");
        bigDecExponents.put(BigDecimal.TEN.pow(21), "sextillion");
        bigDecExponents.put(BigDecimal.TEN.pow(24), "septillion");
        bigDecExponents.put(BigDecimal.TEN.pow(27), "octillion");
        bigDecExponents.put(BigDecimal.TEN.pow(30), "nonillion");
        bigDecExponents.put(BigDecimal.TEN.pow(33), "decillion");
        bigDecExponents.put(BigDecimal.TEN.pow(36), "undecillion");
        bigDecExponents.put(BigDecimal.TEN.pow(39), "duodecillion");
        // bigDecExponents.put(BigDecimal.TEN.pow(100), "googol");
    }

    public static final Map<Long, String> binPrefixes = new LinkedHashMap<Long, String>();

    static
    {
        binPrefixes.put(1125899906842624L, "#.## PB");
        binPrefixes.put(1099511627776L, "#.## TB");
        binPrefixes.put(1073741824L, "#.## GB");
        binPrefixes.put(1048576L, "#.## MB");
        binPrefixes.put(1024L, "#.# KB");
        binPrefixes.put(0L, "# bytes");
    }

    public static final Map<Long, String> metricPrefixes = new LinkedHashMap<Long, String>();

    static
    {
        metricPrefixes.put(1000000000000000L, "#.##P");
        metricPrefixes.put(1000000000000L, "#.##T");
        metricPrefixes.put(1000000000L, "#.##G");
        metricPrefixes.put(1000000L, "#.##M");
        metricPrefixes.put(1000L, "#.#k");
        metricPrefixes.put(0L, "#.#");
    }

    public static final Map<Long, String> nanoTimePrefixes = new LinkedHashMap<Long, String>();

    static
    {
        nanoTimePrefixes.put(1000000000L, "#.##s");
        nanoTimePrefixes.put(1000000L, "#.###ms");
        nanoTimePrefixes.put(1000L, "#.####µs");
        nanoTimePrefixes.put(0L, "#.####ns");
    }

    public static final Joiner commaJoiner = Joiner.on(", ").skipNulls();

    public static final List<String> titleIgnoredWords = Arrays.asList(new String[] {
            "a", "an", "and", "but", "nor", "it", "the", "to", "with", "in", "on", "of",
            "up", "or", "at", "into", "onto", "by", "from", "then", "for", "via", "versus"
    });

    public static final Pattern titleWordSperator = Pattern.compile(".+(\\||-|/).+");

}
