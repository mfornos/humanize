package humanize.time;

import humanize.spi.FormatProvider;
import humanize.text.FormatFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeFormat;
import org.ocpsoft.prettytime.TimeUnit;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * {@link Format} implementation for {@link PrettyTime}.
 * 
 */
public class PrettyTimeFormat extends Format implements FormatProvider
{

    private static final long serialVersionUID = -1398312177396430967L;

    public static FormatFactory factory()
    {

        return new FormatFactory()
        {
            @Override
            public Format getFormat(String name, String args, Locale locale)
            {
                // TODO support unrounded in args?
                return new PrettyTimeFormat(locale);
            }
        };

    }

    private transient PrettyTime prettyTime;

    private final Locale locale;

    public PrettyTimeFormat()
    {
        this(Locale.getDefault());
    }

    public PrettyTimeFormat(Locale locale)
    {
        this.prettyTime = new PrettyTime(locale);
        this.locale = locale;
    }

    public Duration approximateDuration(Date then)
    {
        return prettyTime.approximateDuration(then);
    }

    public List<Duration> calculatePreciseDuration(Date then)
    {
        return prettyTime.calculatePreciseDuration(then);
    }

    public List<TimeUnit> clearUnits()
    {
        return prettyTime.clearUnits();
    }

    /**
     * Convenience format method.
     * 
     * @param ref
     *            The date of reference.
     * @param then
     *            The future date.
     * @return a relative format date as text representation
     */
    public String format(Date ref, Date then)
    {
        return prettyTime.format(DurationHelper.calculateDuration(ref, then, prettyTime.getUnits()));
    }

    /**
     * Convenience format method for precise durations.
     * 
     * @param ref
     *            The date of reference.
     * @param then
     *            The future date.
     * @param precision
     *            The precision to retain in milliseconds.
     * @return a relative format date as text representation or an empty string
     *         if no durations are retained
     */
    public String format(Date ref, Date then, long precision)
    {
        List<Duration> durations = DurationHelper.calculatePreciseDuration(ref, then, prettyTime.getUnits());
        List<Duration> retained = retainPrecision(durations, precision);
        return retained.isEmpty() ? "" : prettyTime.format(retained);
    }

    public String format(Duration duration)
    {
        return prettyTime.format(duration);
    }

    public String format(List<Duration> durations)
    {
        return prettyTime.format(durations);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
    {
        if (Duration.class.isAssignableFrom(obj.getClass()))
        {
            return toAppendTo.append(prettyTime.format((Duration) obj));
        }

        if (Date.class.isAssignableFrom(obj.getClass()))
        {
            return toAppendTo.append(prettyTime.format((Date) obj));
        }

        if (List.class.isAssignableFrom(obj.getClass()))
        {
            return toAppendTo.append(prettyTime.format((List<Duration>) obj));
        }

        if (Number.class.isAssignableFrom(obj.getClass()))
        {
            return toAppendTo.append(prettyTime.format(new Date(((Number) obj).longValue())));
        }

        throw new IllegalArgumentException(String.format("Class %s is not suitable for PrettyTimeFormat",
                obj.getClass()));
    }

    public String formatUnrounded(Date then)
    {
        return prettyTime.formatUnrounded(then);
    }

    public String formatUnrounded(Duration duration)
    {
        return prettyTime.formatUnrounded(duration);
    }

    @Override
    public FormatFactory getFactory()
    {
        return factory();
    }

    public TimeFormat getFormat(TimeUnit unit)
    {
        return prettyTime.getFormat(unit);
    }

    @Override
    public String getFormatName()
    {
        return "prettytime";
    }

    /**
     * Gets the underlying {@link PrettyTime} instance.
     * 
     * @return the underlying {@link PrettyTime} instance.
     */
    public PrettyTime getPrettyTime()
    {
        return prettyTime;
    }

    public List<TimeUnit> getUnits()
    {
        return prettyTime.getUnits();
    }

    @Override
    public Object parseObject(String source, ParsePosition pos)
    {
        throw new UnsupportedOperationException();
    }

    public PrettyTime registerUnit(TimeUnit unit, TimeFormat format)
    {
        return prettyTime.registerUnit(unit, format);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
    {
        ois.defaultReadObject();
        this.prettyTime = new PrettyTime(locale);
    }

    private List<Duration> retainPrecision(final List<Duration> durations, final long precision)
    {
        return ImmutableList.copyOf(Iterables.filter(durations, new Predicate<Duration>()
        {
            @Override
            public boolean apply(Duration it)
            {
                return it.getUnit().getMillisPerUnit() >= precision;
            }
        }));
    }

}
