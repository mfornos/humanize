package humanize.icu.spi.context;

import static humanize.util.Constants.EMPTY;
import humanize.icu.spi.MessageFormat;
import humanize.spi.cache.CacheProvider;
import humanize.spi.context.Context;
import humanize.text.MaskFormat;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;

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
public class DefaultICUContext implements Context, ICUContext
{

    private static final String DURATION_FORMAT = "icu.duration.format";

    private static final String CURRENCY = "icu.currency";

    private static final String DECIMAL = "icu.decimal";

    private static final String NUMBER = "icu.number";

    private static final String PERCENT = "icu.percent";

    private static final String RULE_BASED = "icu.rule.based";

    private static final String DATE_FORMAT = "icu.date";

    private static final String CURRENCY_PL = "icu.currency.pl";

    private static final String DATE_TIME_FORMAT = "icu.date.time";

    private final static CacheProvider sharedCache = loadCacheProvider();

    private static CacheProvider loadCacheProvider()
    {

        ServiceLoader<CacheProvider> ldr = ServiceLoader.load(CacheProvider.class);
        for (CacheProvider provider : ldr)
        {
            return provider;
        }
        throw new RuntimeException("No CacheProvider was found");

    }

    private final CacheProvider localCache = loadCacheProvider();

    private Locale locale;

    private ULocale ulocale;

    private MessageFormat messageFormat;

    public DefaultICUContext()
    {

        this(Locale.getDefault());

    }

    public DefaultICUContext(Locale locale)
    {

        setLocale(locale);
        this.messageFormat = new MessageFormat(EMPTY, locale);

    }

    @Override
    public String digitStrings(int index)
    {

        throw new UnsupportedOperationException("Use humanize-slim instead.");

    }

    @Override
    public String formatDate(int style, Date value)
    {

        return getDateFormat(style).format(value);

    }

    @Override
    public String formatDateTime(Date date)
    {

        return getDateTimeFormat().format(date);

    }

    @Override
    public String formatDateTime(int dateStyle, int timeStyle, Date date)
    {

        return getDateTimeFormat(dateStyle, timeStyle).format(date);
    }

    @Override
    public String formatDecimal(Number value)
    {

        return getNumberFormat().format(value);

    }

    @Override
    public String formatMessage(String key, Object... args)
    {

        throw new UnsupportedOperationException("Use humanize-slim instead.");

    }

    @Override
    public String getBestPattern(String skeleton)
    {

        DateTimePatternGenerator generator = DateTimePatternGenerator.getInstance(getULocale());
        return generator.getBestPattern(skeleton);

    }

    @Override
    public ResourceBundle getBundle()
    {

        throw new UnsupportedOperationException("Use humanize-slim instead.");

    }

    @Override
    public DecimalFormat getCurrencyFormat()
    {

        return sharedCache.getFormat(CURRENCY, locale, new Callable<DecimalFormat>()
        {
            @Override
            public DecimalFormat call() throws Exception
            {

                return (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
            }
        });

    }

    @Override
    public DateFormat getDateFormat(final int style)
    {

        String name = DATE_FORMAT + style;

        return localCache.getFormat(name, locale, new Callable<DateFormat>()
        {
            @Override
            public DateFormat call() throws Exception
            {

                return DateFormat.getDateInstance(style, locale);
            }
        });

    }

    @Override
    public DateFormat getDateTimeFormat()
    {

        return getDateTimeFormat(DateFormat.SHORT, DateFormat.SHORT);

    }

    @Override
    public DateFormat getDateTimeFormat(final int dateStyle, final int timeStyle)
    {

        String name = DATE_TIME_FORMAT + dateStyle + timeStyle;

        return localCache.getFormat(name, locale, new Callable<DateFormat>()
        {
            @Override
            public DateFormat call() throws Exception
            {

                return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
            }
        });

    }

    @Override
    public DecimalFormat getDecimalFormat()
    {

        return localCache.getFormat(DECIMAL, locale, new Callable<DecimalFormat>()
        {
            @Override
            public DecimalFormat call() throws Exception
            {

                return (DecimalFormat) DecimalFormat.getInstance(locale);
            }
        });

    }

    @Override
    public DurationFormat getDurationFormat()
    {

        return sharedCache.getFormat(DURATION_FORMAT, locale, new Callable<DurationFormat>()
        {
            @Override
            public DurationFormat call() throws Exception
            {

                return DurationFormat.getInstance(ulocale);
            }
        });

    }

    @Override
    public Locale getLocale()
    {

        return locale;

    }

    @Override
    public MaskFormat getMaskFormat()
    {

        throw new UnsupportedOperationException("Use humanize-slim instead.");

    }

    @Override
    public String getMessage(String key)
    {

        throw new UnsupportedOperationException("Use humanize-slim instead.");

    }

    @Override
    public MessageFormat getMessageFormat()
    {

        messageFormat.setLocale(locale);
        return messageFormat;

    }

    @Override
    public NumberFormat getNumberFormat()
    {

        return sharedCache.getFormat(NUMBER, locale, new Callable<NumberFormat>()
        {
            @Override
            public NumberFormat call() throws Exception
            {

                return NumberFormat.getInstance(locale);
            }
        });

    }

    @Override
    public DecimalFormat getPercentFormat()
    {

        return sharedCache.getFormat(PERCENT, locale, new Callable<DecimalFormat>()
        {
            @Override
            public DecimalFormat call() throws Exception
            {

                return (DecimalFormat) NumberFormat.getPercentInstance(locale);
            }
        });

    }

    @Override
    public DecimalFormat getPluralCurrencyFormat()
    {

        return sharedCache.getFormat(CURRENCY_PL, locale, new Callable<DecimalFormat>()
        {
            @Override
            public DecimalFormat call() throws Exception
            {

                return (DecimalFormat) NumberFormat.getInstance(locale, NumberFormat.PLURALCURRENCYSTYLE);
            }
        });

    }

    @Override
    public NumberFormat getRuleBasedNumberFormat(final int type)
    {

        String ruleBasedName = RULE_BASED + type;
        return sharedCache.getFormat(ruleBasedName, locale, new Callable<NumberFormat>()
        {
            @Override
            public NumberFormat call() throws Exception
            {

                return new RuleBasedNumberFormat(locale, type);
            }
        });
    }

    @Override
    public ULocale getULocale()
    {

        return ulocale;

    }

    @Override
    public void setLocale(Locale locale)
    {

        this.locale = locale;
        this.ulocale = ULocale.forLocale(locale);

    }

}
