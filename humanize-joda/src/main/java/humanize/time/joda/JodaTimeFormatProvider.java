package humanize.time.joda;

import humanize.spi.FormatProvider;
import humanize.text.FormatFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;

/**
 * {@link FormatProvider} for Joda time.
 * 
 */
public class JodaTimeFormatProvider implements FormatProvider
{

    /**
     * Creates a factory for the specified format.
     * 
     * @return FormatFactory instance
     */
    public static FormatFactory factory()
    {

        return new FormatFactory()
        {
            @Override
            public Format getFormat(String name, String args, Locale locale)
            {
                Map<String, Format> mt = FormatTables.get(name);
                Format m = mt.get((args == null || args.length() < 1) ? FormatNames.DEFAULT : args);
                return ((ConfigurableFormat) m).withLocale(locale);
            }
        };

    }

    @Override
    public FormatFactory getFactory()
    {
        return factory();
    }

    @Override
    public String getFormatName()
    {
        return String.format("%s|%s|%s|%s", FormatNames.FORMAT_JODA_TIME, FormatNames.FORMAT_JODA_PERIOD,
                FormatNames.FORMAT_JODA_ISO_TIME, FormatNames.FORMAT_JODA_ISO_PERIOD);
    }

    public interface ConfigurableFormat
    {
        Format withLocale(Locale locale);
    }

    /**
     * Base class for Joda formats.
     * 
     * @param <T>
     *            Formatter type
     */
    public abstract static class JodaBaseFormat<T> extends Format implements ConfigurableFormat
    {

        private static final long serialVersionUID = 9053371900269483473L;

        protected final Method method;

        protected T format;

        public JodaBaseFormat(Method method)
        {

            this.method = method;

        }

        public T get()
        {
            if (format == null)
            {
                synchronized (method)
                {
                    try
                    {
                        format = invoke();
                    } catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }

            return format;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos)
        {
            try
            {
                int begin = pos.getIndex();
                pos.setIndex(source.length());
                String text = source.substring(begin);
                return (text == null || text.length() < 1) ? text : parse(text);
            } catch (ParseException e)
            {
                pos.setIndex(0);
                pos.setErrorIndex(e.getErrorOffset());
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        protected T invoke() throws IllegalAccessException, InvocationTargetException
        {
            return (T) method.invoke(null);
        }

        abstract protected Object parse(String text) throws ParseException;

    }

    /**
     * {@link Format} for Joda {@link DateTime}.
     * 
     */
    public static class JodaDateTimeFormat extends JodaBaseFormat<DateTimeFormatter>
    {

        private static final long serialVersionUID = -7080564879531103796L;

        public JodaDateTimeFormat(Method method)
        {
            super(method);
        }

        @Override
        public StringBuffer format(Object param, StringBuffer appendTo, FieldPosition pos)
        {
            return appendTo.append(((DateTime) param).toString(get()));
        }

        public Object parse(String source) throws ParseException
        {
            return format.parseDateTime(source);
        }

        public Format withLocale(Locale locale)
        {
            format = get().withLocale(locale);
            return this;
        }

    }

    /**
     * {@link Format} for Joda {@link Period}.
     * 
     */
    public static class JodaPeriodFormat extends JodaBaseFormat<PeriodFormatter>
    {

        private static final long serialVersionUID = 7075580918316147610L;

        private Locale locale;

        public JodaPeriodFormat(Method method)
        {
            super(method);
        }

        @Override
        public StringBuffer format(Object param, StringBuffer appendTo, FieldPosition pos)
        {
            return appendTo.append(((Period) param).toString(get()));
        }

        public Object parse(String source) throws ParseException
        {
            return format.parsePeriod(source);
        }

        public Format withLocale(Locale locale)
        {
            this.locale = locale;
            this.format = get();
            return this;
        }

        @Override
        protected PeriodFormatter invoke() throws IllegalAccessException, InvocationTargetException
        {
            return (PeriodFormatter) method.invoke(null, locale);
        }

    }

}
