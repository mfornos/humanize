package humanize.icu.spi;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageFormatTest {

	@Test
	public void MessageLocale() {

		MessageFormat msg = new MessageFormat("hello {0} {1}", Locale.CANADA);
		Assert.assertNotNull(msg);
		Assert.assertEquals(msg.render("one", "two"), "hello one two");

		StringBuffer buffer = new StringBuffer();
		msg.render(buffer, "one", "two");
		Assert.assertEquals(buffer.toString(), "hello one two");

		Assert.assertEquals(new MessageFormat("hi {0}").render("ho"), "hi ho");
	}
}
