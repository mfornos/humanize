package humanize.time.joda;

import humanize.Humanize;
import humanize.spi.MessageFormat;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestHumanizeMessage
{

    private Locale defaultLocale;

    @Test(expectedExceptions = ClassCastException.class)
    public void boxing()
    {
        Humanize.format("hello {0, joda.period}!!", "badguy");
        Assert.fail();
    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void dateTime()
    {
        DateTime zero = new DateTime(0).millisOfDay().setCopy(0).secondOfDay().setCopy(0);

        Assert.assertEquals(Humanize.format("hello {0, joda.time}!!", zero), "hello 1/1/70!!");

        MessageFormat mf = Humanize.messageFormat("hello {0, joda.time, full.date}!!", Locale.FRENCH);
        Assert.assertEquals(mf.render(zero), "hello jeudi 1 janvier 1970!!");

        // Assert.assertEquals(Humanize.format("hello {0, joda.iso.time, basic.week.date.time.no}!!",
        // zero),
        // "hello 1970W014T000000+0100!!");

        Assert.assertEquals(Humanize.format("hello {0, joda.iso.time, basic.ordinal.date}!!", zero),
                "hello 1970001!!");
        Assert.assertEquals(Humanize.format("hello {0, joda.iso.time, weekyear.week.day}!!", zero),
                "hello 1970-W01-4!!");
        Assert.assertEquals(Humanize.format("hello {0, joda.iso.time, year.month.day}!!", zero),
                "hello 1970-01-01!!");

    }

    @Test(threadPoolSize = 10, invocationCount = 10)
    public void period()
    {

        Assert.assertEquals(Humanize.format("hello {0, joda.period}!!", new Period(0, 100000)),
                "hello 1 minute and 40 seconds!!");

        MessageFormat mf = Humanize.messageFormat("hello {0, joda.period}!!", Locale.FRENCH);
        Assert.assertEquals(mf.render(new Period(0, 100000)), "hello 1 minute et 40 secondes!!");

        mf = Humanize.messageFormat("hello {0, joda.period}!!", Locale.GERMAN);
        Assert.assertEquals(mf.render(new Period(0, 100000)), "hello 1 Minute und 40 Sekunden!!");

    }

    @BeforeClass
    void setUp()
    {
        this.defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass
    void tearDown()
    {
        Locale.setDefault(defaultLocale);
    }

}
