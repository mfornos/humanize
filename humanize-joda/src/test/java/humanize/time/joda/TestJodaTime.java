package humanize.time.joda;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestJodaTime {

	@Test
	public void periodFormat() {

		PeriodFormatter fmt = PeriodFormat.getDefault();
		Assert.assertEquals(new Period(0, 100000000).toString(fmt), "1 day, 3 hours, 46 minutes and 40 seconds");

		fmt = PeriodFormat.wordBased(new Locale("es"));
		Assert.assertEquals(new Period(0, 100000000).toString(fmt), "1 dia, 3 horas, 46 minutos y 40 segundos");

		fmt = ISOPeriodFormat.standard();
		Assert.assertEquals(new Period(0, 100000000).toString(fmt), "P1DT3H46M40S");

		fmt = ISOPeriodFormat.alternate();
		Assert.assertEquals(new Period(0, 100000000).toString(fmt), "P00000001T034640");

		fmt = ISOPeriodFormat.alternateWithWeeks();
		Assert.assertEquals(new Period(0, 100000000).toString(fmt), "P0000W0001T034640");

	}

	@Test
	public void dateFormat() {

		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		DateTime zero = new DateTime(0).millisOfDay().setCopy(0).secondOfDay().setCopy(0);
		Assert.assertEquals(zero.toString(fmt), "1970-01-01T00:00:00.000+01:00");

		fmt = ISODateTimeFormat.basicDate();
		Assert.assertEquals(zero.toString(fmt), "19700101");

		fmt = ISODateTimeFormat.basicOrdinalDate();
		Assert.assertEquals(zero.toString(fmt), "1970001");

		fmt = DateTimeFormat.fullDate().withLocale(Locale.ENGLISH);
		Assert.assertEquals(zero.toString(fmt), "Thursday, January 1, 1970");

	}

}
