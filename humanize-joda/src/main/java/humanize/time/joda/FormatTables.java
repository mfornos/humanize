package humanize.time.joda;

import humanize.time.joda.JodaTimeFormatProvider.JodaDateTimeFormat;
import humanize.time.joda.JodaTimeFormatProvider.JodaPeriodFormat;

import java.text.Format;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;

/**
 * Table to find Joda time format variants by name.
 */
public final class FormatTables implements FormatNames
{

    private static FormatTables instance;

    /**
     * Retrieves a variants map for a given format name.
     * 
     * @param name
     *            The format name
     * @return a mutable map of variants
     */
    public static Map<String, Format> get(String name)
    {
        return instance().methods.get(name);
    }

    public static synchronized FormatTables instance()
    {
        if (instance == null) {
            instance = new FormatTables();
        }
        return instance;
    }

    private final Map<String, Map<String, Format>> methods = new HashMap<String, Map<String, Format>>();

    private FormatTables()
    {
        initialize();
    }

    private void initialize()
    {
        Map<String, Format> jtm = new HashMap<String, Format>();

        JodaDateTimeFormat shortDate = newDateTimeFormat("shortDate");
        jtm.put(DEFAULT, shortDate);
        jtm.put(JODA_SHORT_DATE, shortDate);
        jtm.put(JODA_SHORT_TIME, newDateTimeFormat("shortTime"));
        jtm.put(JODA_SHORT_DATE_TIME, newDateTimeFormat("shortDateTime"));
        jtm.put(JODA_MEDIUM_DATE, newDateTimeFormat("mediumDate"));
        jtm.put(JODA_MEDIUM_TIME, newDateTimeFormat("mediumTime"));
        jtm.put(JODA_MEDIUM_DATE_TIME, newDateTimeFormat("mediumDateTime"));
        jtm.put(JODA_LONG_DATE, newDateTimeFormat("longDate"));
        jtm.put(JODA_LONG_TIME, newDateTimeFormat("longTime"));
        jtm.put(JODA_LONG_DATE_TIME, newDateTimeFormat("longDateTime"));
        jtm.put(JODA_FULL_DATE, newDateTimeFormat("fullDate"));
        jtm.put(JODA_FULL_TIME, newDateTimeFormat("fullTime"));
        jtm.put(JODA_FULL_DATE_TIME, newDateTimeFormat("fullDateTime"));

        methods.put(FORMAT_JODA_TIME, jtm);

        Map<String, Format> itm = new HashMap<String, Format>();

        JodaDateTimeFormat basicDate = newISODateTimeFormat("date");
        itm.put(DEFAULT, basicDate);
        itm.put(JODA_ISO_TIME, newISODateTimeFormat("time"));
        itm.put(JODA_ISO_YEAR, newISODateTimeFormat("year"));
        itm.put(JODA_ISO_YEAR_MONTH, newISODateTimeFormat("yearMonth"));
        itm.put(JODA_ISO_YEAR_MONTH_DAY, newISODateTimeFormat("yearMonthDay"));
        itm.put(JODA_ISO_WEEKYEAR, newISODateTimeFormat(JODA_ISO_WEEKYEAR));
        itm.put(JODA_ISO_WEEKYEAR_WEEK, newISODateTimeFormat("weekyearWeek"));
        itm.put(JODA_ISO_WEEKYEAR_WEEK_DAY, newISODateTimeFormat("weekyearWeekDay"));
        itm.put(JODA_ISO_HOUR, newISODateTimeFormat("hour"));
        itm.put(JODA_ISO_HOUR_MINUTE, newISODateTimeFormat("hourMinute"));
        itm.put(JODA_ISO_HOUR_MINUTE_SECOND, newISODateTimeFormat("hourMinuteSecond"));
        itm.put(JODA_ISO_HOUR_MINUTE_SECOND_MILLIS, newISODateTimeFormat("hourMinuteSecondMillis"));
        itm.put(JODA_ISO_HOUR_MINUTE_SECOND_FRACTION, newISODateTimeFormat("hourMinuteSecondFraction"));
        itm.put(JODA_ISO_DATE_HOUR, newISODateTimeFormat("dateHour"));
        itm.put(JODA_ISO_DATE_HOUR_MINUTE, newISODateTimeFormat("dateHourMinute"));
        itm.put(JODA_ISO_DATE_HOUR_MINUTE_SECOND, newISODateTimeFormat("dateHourMinuteSecond"));
        itm.put(JODA_ISO_DATE_HOUR_MINUTE_SECOND_MILLIS, newISODateTimeFormat("dateHourMinuteSecondMillis"));
        itm.put(JODA_ISO_DATE_HOUR_MINUTE_SECOND_FRACTION, newISODateTimeFormat("dateHourMinuteSecondFraction"));
        itm.put(JODA_ISO_TIME_NO, newISODateTimeFormat("timeNoMillis"));
        itm.put(JODA_ISO_DATE_TIME, newISODateTimeFormat("dateTime"));
        itm.put(JODA_ISO_DATE_TIME_NO, newISODateTimeFormat("dateTimeNoMillis"));
        itm.put(JODA_ISO_BASIC_DATE, newISODateTimeFormat("basicDate"));
        itm.put(JODA_ISO_BASIC_TIME, newISODateTimeFormat("basicTimeNoMillis"));
        itm.put(JODA_ISO_BASIC_TIME_NO, newISODateTimeFormat("basicTimeNoMillis"));
        itm.put(JODA_ISO_BASIC_DATE_TIME, newISODateTimeFormat("basicDateTime"));
        itm.put(JODA_ISO_BASIC_DATE_TIME_NO, newISODateTimeFormat("basicDateTimeNoMillis"));
        itm.put(JODA_ISO_BASIC_ORDINAL_DATE, newISODateTimeFormat("basicOrdinalDate"));
        itm.put(JODA_ISO_BASIC_ORDINAL_DATE_TIME, newISODateTimeFormat("basicOrdinalDateTime"));
        itm.put(JODA_ISO_BASIC_ORDINAL_DATE_TIME_NO, newISODateTimeFormat("basicOrdinalDateTimeNoMillis"));
        itm.put(JODA_ISO_BASIC_WEEK_DATE, newISODateTimeFormat("basicWeekDate"));
        itm.put(JODA_ISO_BASIC_WEEK_DATE_TIME, newISODateTimeFormat("basicWeekDateTime"));
        itm.put(JODA_ISO_BASIC_WEEK_DATE_TIME_NO, newISODateTimeFormat("basicWeekDateTimeNoMillis"));
        itm.put(JODA_ISO_ORDINAL_DATE, newISODateTimeFormat("ordinalDate"));
        itm.put(JODA_ISO_ORDINAL_DATE_TIME, newISODateTimeFormat("ordinalDateTime"));
        itm.put(JODA_ISO_ORDINAL_DATE_TIME_NO, newISODateTimeFormat("ordinalDateTimeNoMillis"));

        methods.put(FORMAT_JODA_ISO_TIME, itm);

        Map<String, Format> ipm = new HashMap<String, Format>();

        ipm.put(DEFAULT, newISOPeriodFormat("standard"));
        ipm.put(ISO_PERIOD_ALTERNATE, newISOPeriodFormat(ISO_PERIOD_ALTERNATE));
        ipm.put(ISO_PERIOD_ALTERNATE_EXTENDED, newISOPeriodFormat("alternateExtended"));
        ipm.put(ISO_PERIOD_ALTERNATE_WITH_WEEKS, newISOPeriodFormat("alternateWithWeeks"));
        ipm.put(ISO_PERIOD_ALTERNATE_EXTENDED_WITH_WEEKS, newISOPeriodFormat("alternateExtendedWithWeeks"));

        methods.put(FORMAT_JODA_ISO_PERIOD, ipm);
    }

    private JodaDateTimeFormat newDateTimeFormat(Class<?> clazz, String name)
    {
        try
        {
            return new JodaDateTimeFormat(clazz.getMethod(name));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    private JodaDateTimeFormat newDateTimeFormat(String name)
    {
        return newDateTimeFormat(DateTimeFormat.class, name);
    }

    private JodaDateTimeFormat newISODateTimeFormat(String name)
    {
        return newDateTimeFormat(ISODateTimeFormat.class, name);
    }

    private JodaPeriodFormat newISOPeriodFormat(String name)
    {
        return newPeriodFormat(ISOPeriodFormat.class, name);
    }

    private JodaPeriodFormat newPeriodFormat(Class<?> clazz, String name, Class<?>... args)
    {
        try
        {
            return new JodaPeriodFormat(clazz.getMethod(name, args));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
