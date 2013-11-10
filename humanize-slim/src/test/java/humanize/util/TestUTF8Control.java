package humanize.util;

import java.util.Locale;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestUTF8Control
{

	// TODO implement test
	@Test
	public void newBundle()
	{

		UTF8Control control = new UTF8Control();
		try
		{
			control.newBundle("", Locale.getDefault(), "", TestUTF8Control.class.getClassLoader(), true);
		} catch (Exception e)
		{
			Assert.fail(e.getMessage(), e);
		}

	}
}
