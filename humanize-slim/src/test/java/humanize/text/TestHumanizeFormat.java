package humanize.text;

import humanize.Humanize;

import java.text.Format;
import java.text.ParseException;
import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestHumanizeFormat {

	@Test
	public void basic() {

		Assert.assertEquals(Humanize.ordinal(10, Locale.ENGLISH), "10th");

		FormatFactory factory = HumanizeFormat.factory();

		Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
		Assert.assertEquals(f.format(10), "10th");

		f = factory.getFormat("humanize", "binaryPrefix", Locale.UK);
		Assert.assertEquals(f.format(0), "0 bytes");

		f = factory.getFormat("humanize", "camelize", Locale.UK);
		Assert.assertEquals(f.format("hello world"), "helloWorld");

		f = factory.getFormat("humanize", "metricPrefix", Locale.UK);
		Assert.assertEquals(f.format(10000000), "10M");

		Assert.assertNull(factory.getFormat("humanize", "none", Locale.UK));

	}

	@Test
	public void invalidCall() {

		FormatFactory factory = HumanizeFormat.factory();

		Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
		Assert.assertEquals(f.format(new Object[] { "juidui" }), "[invalid call: 'argument type mismatch']");

	}

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void parse() throws ParseException {

		FormatFactory factory = HumanizeFormat.factory();

		Format f = factory.getFormat("humanize", "ordinal", Locale.UK);
		f.parseObject("any");

	}

}
