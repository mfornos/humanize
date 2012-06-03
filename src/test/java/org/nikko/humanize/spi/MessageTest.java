package org.nikko.humanize.spi;

import java.util.Locale;

import org.nikko.humanize.spi.Message;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageTest {

	@Test
	public void MessageLocale() {
		Message msg = new Message("hello {0} {1}", Locale.CANADA);
		Assert.assertNotNull(msg);
		Assert.assertEquals(msg.render("one", "two"), "hello one two");
		
		StringBuffer buffer = new StringBuffer();
		msg.render(buffer, "one", "two");
		Assert.assertEquals(buffer.toString(), "hello one two");

		Assert.assertEquals(new Message("hi {0}").render("ho"), "hi ho");
	}
}
