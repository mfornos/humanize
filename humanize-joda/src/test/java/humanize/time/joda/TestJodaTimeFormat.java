package humanize.time.joda;

import static humanize.time.joda.FormatNames.FORMAT_JODA_ISO_TIME;
import static humanize.time.joda.FormatNames.FORMAT_JODA_TIME;
import static humanize.time.joda.FormatNames.JODA_FULL_DATE;
import static humanize.time.joda.FormatNames.JODA_ISO_BASIC_DATE;

import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

import org.joda.time.DateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestJodaTimeFormat
{

    @Test
    public void instance() throws ParseException
    {
        Format fmt = JodaTimeFormatProvider.factory().getFormat(FORMAT_JODA_TIME, JODA_FULL_DATE, Locale.ENGLISH);
        DateTime begin = new DateTime(0).hourOfDay().setCopy(0);
        Assert.assertEquals(fmt.format(begin), "Thursday, January 1, 1970");
        Assert.assertEquals(fmt.parseObject("Thursday, January 1, 1970"), begin);

        fmt = JodaTimeFormatProvider.factory().getFormat(FORMAT_JODA_ISO_TIME, JODA_ISO_BASIC_DATE, Locale.ENGLISH);
        Assert.assertEquals(fmt.format(begin), "19700101");
        Assert.assertEquals(fmt.parseObject("19700101"), begin);

        fmt = JodaTimeFormatProvider.factory().getFormat("joda.iso.time", null, Locale.ENGLISH);
        Assert.assertEquals(fmt.format(begin), "1970-01-01");
    }

}
