package humanize.text;

import humanize.Humanize;
import humanize.spi.MessageFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestExtendedMessageFormat
{

    private Locale defaultLocale;

    @Test
    public void basicFormats()
    {

        ExtendedMessageFormat extformat = new ExtendedMessageFormat("hello {0, number} xxx {1, date, ddmmyy}",
                Locale.ENGLISH);
        Object[] params = new Object[] { 100000, new Date(0) };
        String out = extformat.format(params);
        Assert.assertEquals(out, "hello 100,000 xxx  010070");

        out = ExtendedMessageFormat.format("hello {0, number} '{hello}' xxx {1, date,'a' ddmmyy}", params);
        Assert.assertEquals(out, "hello 100,000 {hello} xxx a 010070");

        extformat = new ExtendedMessageFormat("hello {0, number} xxx {1, date, ddmmyy}");
        out = extformat.format(params);
        Assert.assertEquals(out, "hello 100,000 xxx  010070");

        extformat = new ExtendedMessageFormat("hello {0, number} 'aa' {0, date,'dd'dd} {1, date, ddmmyy}",
                (Map<String, FormatFactory>) null);
        out = extformat.format(params);
        Assert.assertEquals(out, "hello 100,000 aa dd01  010070");

    }

    @Test
    public void bypass()
    {

        MessageFormat extformat = new MessageFormat("ok-ay");
        Assert.assertEquals(extformat.render(), "ok-ay");
    }

    @Test
    public void customFormats()
    {

        HashMap<String, FormatFactory> registry = new HashMap<String, FormatFactory>();
        registry.put("mask", MaskFormat.factory());

        MessageFormat extformat = new MessageFormat(
                "hello {0, number}{1, mask, __ ____}xxx {2, date, ddmmyy} abc {3, mask, ___ ___}", registry);
        String out = extformat.render(100000, 313378, new Date(0), 313378);

        Assert.assertEquals(out, "hello 100,00031 3378xxx  010070 abc 313 378");

    }

    @Test
    public void customFormatsAutoLoading()
    {

        MessageFormat extformat = new MessageFormat(
                "hello {0, number}{1, mask, __ ____}xxx {2, date, ddmmyy} abc {3, mask, ___ ___}");
        String out = extformat.render(100000, 313378, new Date(0), 313378);

        Assert.assertEquals(out, "hello 100,00031 3378xxx  010070 abc 313 378");

        Assert.assertEquals(Humanize.format("{0, mask, __ __}", 1100), "11 00");

    }

    @Test
    public void humanizeFormats()
    {

        MessageFormat extformat = new MessageFormat("{0, humanize, binaryPrefix}");
        Assert.assertEquals(extformat.render(10000), "9.8 KB");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void unknownFormat()
    {

        MessageFormat extformat = new MessageFormat("ok {0, unknown} test");
        extformat.render(1);

    }

    @BeforeClass
    protected void setUp()
    {

        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

    }

    @AfterClass
    protected void tearDown()
    {

        Locale.setDefault(defaultLocale);

    }

}
