package humanize;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestHumanizeParse
{

    private Locale defaultLocale;

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void parseBase64()
    {

        byte[] decoded = Humanize.parseBase64("R3JlZW4gb3ZlciBibGFjay4=");
        String msg = new String(decoded);

        Assert.assertEquals(msg, "Green over black.");
    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void parseDate()
    {

        Date target = newDate(2011, 8, 14, 15, 22, 1);

        Date date = Humanize.parseISODateTime("2011-09-14T15:22:01Z");
        Assert.assertEquals(date, target);

        date = Humanize.parseISODate("2011-09-14T15:22:01Z");
        Assert.assertEquals(date, target);

        target = newTime(15, 22, 1);
        date = Humanize.parseISOTime("15:22:01Z");
        Assert.assertEquals(date, target);

    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void parseSmartDate()
    {

        Date target = newDate(2012, 1, 1, 0, 0, 0, TimeZone.getDefault());

        String dates[] = new String[] { "1.2.12", "01.02.2012", "2012.02.01", "01-02-12", "1 2 2012" };

        for (String ds : dates)
        {
            Date date = Humanize.parseSmartDate(ds, "dd/MM/yy", "yyyy/MM/dd", "dd/MM/yyyy");
            Assert.assertEquals(date, target);
        }

        for (String ds : dates)
        {
            Date date = Humanize.parseSmartDate(ds, "dd/MM/yy,yyyy/MM/dd,dd/MM/yyyy".split(","));
            Assert.assertEquals(date, target);
        }

        for (String ds : "1$2$12,01$02$2012,2012$02$01".split(","))
        {
            Date date = Humanize.parseSmartDateWithSeparator(ds, "\\$+", "dd/MM/yy,yyyy/MM/dd,dd/MM/yyyy".split(","));
            Assert.assertEquals(date, target);
        }

    }

    @AfterClass
    void after()
    {

        Locale.setDefault(defaultLocale);

    }

    @BeforeClass
    void before()
    {

        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

    }

    private Date newDate(int year, int month, int day, int hour, int minute, int second)
    {

        return newDate(year, month, day, hour, minute, second, TimeZone.getTimeZone("GMT+00:00"));

    }

    private Date newDate(int year, int month, int day, int hour, int minute, int second, TimeZone tz)
    {

        Calendar cal = Calendar.getInstance(tz);
        cal.set(year, month, day, hour, minute, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();

    }

    private Date newTime(int hour, int minute, int second)
    {

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00"));
        cal.setTimeInMillis(0);
        cal.set(Calendar.HOUR, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        return cal.getTime();

    }

}
