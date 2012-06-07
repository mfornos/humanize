package org.nikko.humanize;

import static org.nikko.humanize.Humanize.*;
import static org.testng.Assert.*;

import java.math.BigInteger;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.util.RelativeDate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HumanizeTest {

	private static final Locale ES = new Locale("es", "ES");

	private Random rand;

	@BeforeClass
	public void setUp() {

		Locale.setDefault(Locale.UK);
		rand = new Random();

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void ordinalizeTest() {

		assertEquals(ordinalize(0), "0th");
		assertEquals(ordinalize(1), "1st");
		assertEquals(ordinalize(2), "2nd");
		assertEquals(ordinalize(3), "3rd");
		assertEquals(ordinalize(4), "4th");
		assertEquals(ordinalize(5), "5th");
		assertEquals(ordinalize(33), "33rd");
		assertEquals(ordinalize(11), "11th");
		assertEquals(ordinalize(12), "12th");
		assertEquals(ordinalize(13), "13th");
		assertEquals(ordinalize(10), "10th");
		assertEquals(ordinalize(22), "22nd");
		assertEquals(ordinalize(101), "101st");
		assertEquals(ordinalize(-10), "-10th");
		assertEquals(ordinalize(1.25), "1st");
		assertEquals(ordinalize(new Float(1.33)), "1st");
		assertEquals(ordinalize(new Long(10000000)), "10000000th");

		assertEquals(ordinalize(1, ES), "1º");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void spellDigitTest() {

		assertEquals(spellDigit(1), "one");
		assertEquals(spellDigit(3), "three");
		assertEquals(spellDigit(0), "zero");
		assertEquals(spellDigit(10), "10");
		assertEquals(spellDigit(-1), "-1");
		assertEquals(spellDigit(9), "nine");

		assertEquals(spellDigit(1, ES), "uno");
		assertEquals(spellDigit(9, ES), "nueve");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void decamelizeTest() {

		assertEquals(decamelize("lowercase"), "lowercase");
		assertEquals(decamelize("Class"), "Class");
		assertEquals(decamelize("MyClass"), "My Class");
		assertEquals(decamelize("HTML"), "HTML");
		assertEquals(decamelize("PDFLoader"), "PDF Loader");
		assertEquals(decamelize("AString"), "A String");
		assertEquals(decamelize("SimpleXMLParser"), "Simple XML Parser");
		assertEquals(decamelize("GL11Version"), "GL 11 Version");

	}

	@Test
	public void camelizeTest() {

		assertEquals(camelize("bla bla_bla "), "blaBlaBla");
		assertEquals(camelize("  blA_blA  Bla", true), "BlaBlaBla");
		assertEquals(camelize("bla_bla!"), "blaBla!");
		assertEquals(camelize("xxx"), "xxx");
		assertEquals(camelize("___"), "___");
		assertEquals(camelize(" "), " ");
		assertEquals(camelize(" _ _ _"), " _ _ _");
		assertEquals(camelize(""), "");
		assertEquals(camelize("xxx", true), "Xxx");

		try {
			camelize(null);
			fail("handles null?");
		} catch (NullPointerException ex) {

		}

	}

	@Test
	public void undescoreTest() {

		assertEquals(underscore("a bunch of  macarios"), "a_bunch_of_macarios");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void formatDecimalTest() {

		int df = rand.nextInt(9);
		assertEquals(formatDecimal(1000 + df), "1,00" + df);
		assertEquals(formatDecimal(10000.55 + df), "10,00" + df + ".55");
		assertEquals(formatDecimal(1000 + df, ES), "1.00" + df);

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void formatCurrencyTest() {

		int df = rand.nextInt(9);
		assertEquals(formatCurrency(34), "£34");
		assertEquals(formatCurrency(1000 + df), "£1,00" + df);
		assertEquals(formatCurrency(10000.55 + df), "£10,00" + df + ".55");

		assertEquals(formatCurrency(100, ES), "100 €");
		assertEquals(formatCurrency(1000.55, ES), "1.000,55 €");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void formatDateTest() {

		Calendar cal = Calendar.getInstance();
		int day = rand.nextInt(20) + 1;
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.YEAR, 2015);

		assertEquals(formatDate(DateFormat.MEDIUM, cal.getTime()), String.format("%02d-Dec-2015", day));
		assertEquals(formatDate(cal.getTime()), String.format("%02d/12/15", day));

		assertEquals(formatDate(DateFormat.MEDIUM, cal.getTime(), ES), String.format("%02d-dic-2015", day));
		assertEquals(formatDate(cal.getTime(), ES), String.format("%d/12/15", day));

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void formatDateTimeTest() {

		Calendar cal = Calendar.getInstance();
		int day = rand.nextInt(20) + 1;
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.MONTH, 11);
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.HOUR_OF_DAY, 22);
		cal.set(Calendar.MINUTE, 10);
		cal.set(Calendar.SECOND, 0);

		assertEquals(formatDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM, cal.getTime()),
		        String.format("%02d-Dec-2015 22:10:00", day));
		assertEquals(formatDateTime(cal.getTime()), String.format("%02d/12/15 22:10", day));

		assertEquals(formatDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM, cal.getTime(), ES),
		        String.format("%02d-dic-2015 22:10:00", day));
		assertEquals(formatDateTime(cal.getTime(), ES), String.format("%d/12/15 22:10", day));

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void spellBigNumberTest() {

		assertEquals(spellBigNumber(100), "100");
		assertEquals(spellBigNumber(2300), "2.3 thousand");
		assertEquals(spellBigNumber(1000000), "1 million");
		assertEquals(spellBigNumber(1300000), "1.3 million");
		assertEquals(spellBigNumber(1000000000), "1 billion");
		assertEquals(spellBigNumber(1550000001), "1.55 billion");
		assertEquals(spellBigNumber(-1550000001), "-1.55 billion");
		assertEquals(spellBigNumber(BigInteger.TEN.pow(33).multiply(BigInteger.valueOf(3))), "3 decillion");
		assertEquals(spellBigNumber(BigInteger.TEN.pow(100).multiply(BigInteger.valueOf(2))), "2 googol");
		BigInteger ultraBig = BigInteger.TEN.pow(1000);
		assertEquals(spellBigNumber(ultraBig), ultraBig.toString());

		// within locale
		assertEquals(spellBigNumber(100, ES), "100");
		assertEquals(spellBigNumber(2300, ES), "2,3 miles");
		assertEquals(spellBigNumber(1000000, ES), "1 millón");
		assertEquals(spellBigNumber(1300000, ES), "1,3 millones");
		assertEquals(spellBigNumber(1000000000, ES), "1 millardo");
		assertEquals(spellBigNumber(1550000001, ES), "1,55 millardos");
		assertEquals(spellBigNumber(-1550000001, ES), "-1,55 millardos");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void pluralizeTest() {

		int df = rand.nextInt(9);

		String pattern = "There {0} on {1}.";
		String none = "are no files";
		String one = "is one file";
		String many = "are {2} files";

		Message f = pluralize(pattern, none, one, many);

		assertEquals(f.render(1000 + df, "disk", 1000 + df), "There are 1,00" + df + " files on disk.");
		assertEquals(f.render(0, "disk"), "There are no files on disk.");
		assertEquals(f.render(-1, "disk"), "There are no files on disk.");
		assertEquals(f.render(1, "disk"), "There is one file on disk.");
		assertEquals(f.render(1, "disk", 1000, "bla bla"), "There is one file on disk.");

		// template

		df = rand.nextInt(9);

		f = pluralize("There {0} on {1}. :: are no files::is one file::  are {2} files");

		assertEquals(f.render(1000 + df, "disk", 1000 + df), "There are 1,00" + df + " files on disk.");
		assertEquals(f.render(0, "disk"), "There are no files on disk.");
		assertEquals(f.render(-1, "disk"), "There are no files on disk.");
		assertEquals(f.render(1, "disk"), "There is one file on disk.");
		assertEquals(f.render(1, "disk", 1000, "bla bla"), "There is one file on disk.");

		f = pluralize("{0}.::No hay ficheros::Hay un fichero::Hay {0,number} ficheros", ES);
		assertEquals(f.render(0), "No hay ficheros.");
		assertEquals(f.render(1), "Hay un fichero.");
		assertEquals(f.render(2000), "Hay 2.000 ficheros.");

		try {
			pluralize("---::---");
			fail("incorrect number of tokens");
		} catch (IllegalArgumentException ex) {

		}

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void binPrefixTest() {

		assertEquals(binaryPrefix(-1), "-1");
		assertEquals(binaryPrefix(0), "0 bytes");
		assertEquals(binaryPrefix(1), "1 bytes");
		assertEquals(binaryPrefix(101), "101 bytes");
		assertEquals(binaryPrefix(1025), "1 kB");
		assertEquals(binaryPrefix(1024), "1 kB");
		assertEquals(binaryPrefix(1536), "1.5 kB");
		assertEquals(binaryPrefix(1048576 * 5), "5 MB");
		assertEquals(binaryPrefix(1073741824L * 2), "2 GB");
		assertEquals(binaryPrefix(1099511627776L * 3), "3 TB");
		assertEquals(binaryPrefix(1325899906842624L), "1.18 PB");

		assertEquals(binaryPrefix(1325899906842624L, ES), "1,18 PB");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void metricPrefixTest() {

		assertEquals(metricPrefix(-1), "-1");
		assertEquals(metricPrefix(0), "0");
		assertEquals(metricPrefix(1), "1");
		assertEquals(metricPrefix(101), "101");
		assertEquals(metricPrefix(1000), "1k");
		assertEquals(metricPrefix(1000000), "1M");
		assertEquals(metricPrefix(3500000), "3.5M");

		assertEquals(metricPrefix(3500000, ES), "3,5M");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void formatPercentTest() {

		assertEquals(formatPercent(0), "0%");
		assertEquals(formatPercent(-1), "-100%");
		assertEquals(formatPercent(0.5), "50%");
		assertEquals(formatPercent(1.5), "150%");
		assertEquals(formatPercent(0.564), "56%");
		assertEquals(formatPercent(1000.564), "100,056%");

		assertEquals(formatPercent(0, ES), "0%");
		assertEquals(formatPercent(-1, ES), "-100%");
		assertEquals(formatPercent(0.5, ES), "50%");
		assertEquals(formatPercent(1.5, ES), "150%");
		assertEquals(formatPercent(0.564, ES), "56%");
		assertEquals(formatPercent(1000.564, ES), "100.056%");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void naturalDayTest() {

		Calendar cal = Calendar.getInstance();
		assertEquals(naturalDay(cal.getTime()), "today");
		cal.add(Calendar.DATE, 1);
		assertEquals(naturalDay(cal.getTime()), "tomorrow");
		cal.add(Calendar.DAY_OF_MONTH, -2);
		assertEquals(naturalDay(cal.getTime()), "yesterday");
		cal.add(Calendar.DAY_OF_WEEK, -1);
		assertEquals(naturalDay(cal.getTime()), formatDate(cal.getTime()));

		cal.setTime(new Date());
		assertEquals(naturalDay(cal.getTime(), ES), "hoy");
		cal.add(Calendar.DATE, 1);
		assertEquals(naturalDay(cal.getTime(), ES), "mañana");
		cal.add(Calendar.DATE, -2);
		assertEquals(naturalDay(cal.getTime(), ES), "ayer");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void naturalTimeTest() {

		assertEquals(naturalTime(new Date()), "right now");

		assertEquals(naturalTime(new Date(0), new Date(1000 * 60 * 12)), "12 minutes from now");
		assertEquals(naturalTime(new Date(0), new Date(1000 * 60 * 60 * 3)), "3 hours from now");
		assertEquals(naturalTime(new Date(0), new Date(1000 * 60 * 60 * 24 * 1)), "one day from now");
		assertEquals(naturalTime(new Date(0), new Date(1000 * 60 * 60 * 24 * 3)), "3 days from now");
		assertEquals(naturalTime(new Date(0), new Date(1000 * 60 * 60 * 24 * 7 * 3)), "3 weeks from now");
		assertEquals(naturalTime(new Date(0), new Date(2629743830L * 3L)), "3 months from now");
		assertEquals(naturalTime(new Date(0), new Date(2629743830L * 13L * 3L)), "3 years from now");
		assertEquals(naturalTime(new Date(0), new Date(315569259747L * 3L)), "30 years from now");
		assertEquals(naturalTime(new Date(0), new Date(3155792597470L * 3L)), "300 years from now");

		assertEquals(naturalTime(new Date(6000), new Date(0)), "moments ago");
		assertEquals(naturalTime(new Date(1000 * 60 * 12), new Date(0)), "12 minutes ago");
		assertEquals(naturalTime(new Date(1000 * 60 * 60 * 3), new Date(0)), "3 hours ago");
		assertEquals(naturalTime(new Date(1000 * 60 * 60 * 24 * 1), new Date(0)), "one day ago");
		assertEquals(naturalTime(new Date(1000 * 60 * 60 * 24 * 3), new Date(0)), "3 days ago");
		assertEquals(naturalTime(new Date(1000 * 60 * 60 * 24 * 7 * 3), new Date(0)), "3 weeks ago");
		assertEquals(naturalTime(new Date(2629743830L * 3L), new Date(0)), "3 months ago");
		assertEquals(naturalTime(new Date(2629743830L * 13L * 3L), new Date(0)), "3 years ago");
		assertEquals(naturalTime(new Date(315569259747L * 3L), new Date(0)), "30 years ago");
		assertEquals(naturalTime(new Date(3155792597470L * 3L), new Date(0)), "300 years ago");

		// within locale
		assertEquals(naturalTime(new Date(), ES), "justo ahora");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void relativeDateTest() {

		RelativeDate relativeDate = getRelativeDateInstance();
		assertEquals(relativeDate.format(new Date(0), new Date(1000 * 60 * 12)), "12 minutes from now");
		assertEquals(relativeDate.format(new Date(0), new Date(1000 * 60 * 60 * 3)), "3 hours from now");
		assertEquals(relativeDate.format(new Date(0), new Date(1000 * 60 * 60 * 24 * 1)), "one day from now");
		assertEquals(relativeDate.format(new Date(0), new Date(1000 * 60 * 60 * 24 * 3)), "3 days from now");
		assertEquals(relativeDate.format(new Date(0), new Date(1000 * 60 * 60 * 24 * 7 * 3)), "3 weeks from now");
		assertEquals(relativeDate.format(new Date(2629743830L * 3L), new Date(0)), "3 months ago");
		assertEquals(relativeDate.format(new Date(2629743830L * 13L * 3L), new Date(0)), "3 years ago");
		assertEquals(relativeDate.format(new Date(315569259747L * 3L), new Date(0)), "30 years ago");
		assertEquals(relativeDate.format(new Date(3155792597470L * 3L), new Date(0)), "300 years ago");

		// within locale
		relativeDate = getRelativeDateInstance(ES);
		assertEquals(relativeDate.format(new Date()), "justo ahora");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void toTextTest() {

		assertEquals(Humanize.toText(123), "one hundred and twenty-three");
		assertEquals(Humanize.toText(2840), "two thousand eight hundred and forty");
		assertEquals(Humanize.toText(1803), "one thousand eight hundred and three");
		assertEquals(Humanize.toText(1412605), "one million four hundred and twelve thousand six hundred and five");
		assertEquals(Humanize.toText(2760300), "two million seven hundred and sixty thousand three hundred");
		assertEquals(Humanize.toText(999000), "nine hundred and ninety-nine thousand");
		assertEquals(Humanize.toText(23380000000L), "twenty-three billion three hundred and eighty million");

		assertEquals(Humanize.toText(23, ES), "veintitrés");
		assertEquals(Humanize.toText(201256, ES), "doscientos un mil doscientos cincuenta y seis");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void titleizeTest() {

		assertEquals(titleize("the_jackie_gleason show"), "The Jackie Gleason Show");
		assertEquals(titleize("first annual report (CD) 2001"), "First Annual Report (CD) 2001");

	}

	@Test(threadPoolSize = 5, invocationCount = 10)
	public void wordWrapTest() {

		int df = rand.nextInt(9);

		String phrase = "Lorem ipsum dolorem si amet, lorem ipsum. Dolorem sic et nunc." + df;
		assertEquals(wordWrap(phrase, 2), "Lorem");
		assertEquals(wordWrap(phrase, 30), "Lorem ipsum dolorem si amet, lorem");
		assertEquals(wordWrap(phrase, phrase.length()), phrase);
		assertEquals(wordWrap(phrase, phrase.length() * 2), phrase);
		assertEquals(wordWrap(phrase, 0), "Lorem");
		assertEquals(wordWrap(phrase, -2), phrase);

	}

}
