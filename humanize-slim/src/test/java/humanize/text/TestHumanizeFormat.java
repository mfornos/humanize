package humanize.text;

import humanize.Humanize;

import java.text.Format;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestHumanizeFormat
{

    @Test
    public void basic()
    {

        Assert.assertEquals(Humanize.ordinal(10, Locale.ENGLISH), "10th");

        FormatFactory factory = HumanizeFormatProvider.factory();

        Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
        Assert.assertEquals(f.format(10), "10th");

        f = factory.getFormat("humanize", "binaryPrefix", Locale.UK);
        Assert.assertEquals(f.format(0), "0 bytes");

        f = factory.getFormat("humanize", "binary.prefix", Locale.UK);
        Assert.assertEquals(f.format(0), "0 bytes");

        f = factory.getFormat("humanize", "binary-prefix", Locale.UK);
        Assert.assertEquals(f.format(0), "0 bytes");

        f = factory.getFormat("humanize", "binary_prefix", Locale.UK);
        Assert.assertEquals(f.format(0), "0 bytes");

        f = factory.getFormat("humanize", "binary prefix", Locale.UK);
        Assert.assertEquals(f.format(0), "0 bytes");

        f = factory.getFormat("humanize", "camelize", Locale.UK);
        Assert.assertEquals(f.format("hello world"), "helloWorld");

        f = factory.getFormat("humanize", "metricPrefix", Locale.UK);
        Assert.assertEquals(f.format(10000000), "10M");

        f = factory.getFormat("humanize", "oxford", Locale.UK);
        List<String> list = Arrays.asList(new String[] { "One", "Two" });
        Assert.assertEquals(f.format(list), "One and Two");

        Assert.assertNull(factory.getFormat("humanize", "none", Locale.UK));

    }

    @Test
    public void invalidCall()
    {

        FormatFactory factory = HumanizeFormatProvider.factory();

        Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
        Assert.assertEquals(f.format(new Object[] { "juidui" }), "[invalid call: 'argument type mismatch']");

    }

    @Test
    public void nestedFormatsTest()
    {
        List<String> list = Arrays.asList(new String[] { "One", "Two" });
        Assert.assertEquals(Humanize.format(Locale.UK, "prefix {0,number} {1,humanize,oxford} suffix", 10, list),
                "prefix 10 One and Two suffix");
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void parse() throws ParseException
    {

        FormatFactory factory = HumanizeFormatProvider.factory();

        Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
        f.parseObject("any");

    }

}
