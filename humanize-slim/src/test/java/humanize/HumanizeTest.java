package humanize;

import static humanize.Humanize.binaryPrefix;
import static humanize.Humanize.camelize;
import static humanize.Humanize.decamelize;
import static humanize.Humanize.formatCurrency;
import static humanize.Humanize.formatDate;
import static humanize.Humanize.formatDateTime;
import static humanize.Humanize.formatDecimal;
import static humanize.Humanize.formatPercent;
import static humanize.Humanize.mask;
import static humanize.Humanize.metricPrefix;
import static humanize.Humanize.pluralize;
import static humanize.Humanize.replaceSupplementary;
import static humanize.Humanize.spellBigNumber;
import static humanize.Humanize.spellDigit;
import static humanize.Humanize.titleize;
import static humanize.Humanize.underscore;
import static humanize.Humanize.unmask;
import static humanize.Humanize.wordWrap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import humanize.spi.MessageFormat;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class HumanizeTest {

	private static final Locale ES = new Locale("es", "ES");

	private Random rand;

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void maskFormatTest() throws ParseException {

		int r = rand.nextInt(9);
		assertEquals(mask("_-__-_____/_", "A5881850" + r), "A-58-81850/" + r);
		assertEquals(mask("_# __ _____#-_", "A/5881850 " + r), "A 58 81850-" + r);
		assertEquals(mask("__ ____ _", r + "01100A"), r + "0 1100 A");
		assertEquals(unmask("__ ____ _", r + "0 1100 A"), r + "01100A");

	}

	
	@Test(threadPoolSize = 10, invocationCount = 10)
	public void formatCurrencyTest() {

		int df = rand.nextInt(9);
		assertEquals(formatCurrency(34), "£34");
		assertEquals(formatCurrency(1000 + df), "£1,00" + df);
		assertEquals(formatCurrency(10000.55 + df), "£10,00" + df + ".55");

		assertEquals(formatCurrency(100, ES), "100 €");
		assertEquals(formatCurrency(1000.55, ES), "1.000,55 €");

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void formatDateTest() {

		int day = rand.nextInt(20) + 1;
		Date date = newTestDate(day, 11, 2015);

		assertEquals(formatDate(DateFormat.MEDIUM, date), String.format("%02d-Dec-2015", day));
		assertEquals(formatDate(date), String.format("%02d/12/15", day));

		assertEquals(formatDate(DateFormat.MEDIUM, date, ES), String.format("%02d-dic-2015", day));
		assertEquals(formatDate(date, ES), String.format("%d/12/15", day));

		assertEquals(formatDate(date, "dd/MM/yy"), String.format("%02d/12/15", day));
		assertEquals(formatDate(date, "dd/MM/yy", ES), String.format("%02d/12/15", day));

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void formatDateTimeTest() {

		int day = rand.nextInt(20) + 1;
		Date date = newTestDate(day, 11, 2015, 22, 10, 0);

		assertEquals(formatDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM, date),
		        String.format("%02d-Dec-2015 22:10:00", day));
		assertEquals(formatDateTime(date), String.format("%02d/12/15 22:10", day));

		assertEquals(formatDateTime(DateFormat.MEDIUM, DateFormat.MEDIUM, date, ES),
		        String.format("%02d-dic-2015 22:10:00", day));
		assertEquals(formatDateTime(date, ES), String.format("%d/12/15 22:10", day));

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void formatDecimalTest() {

		int df = rand.nextInt(9);
		assertEquals(formatDecimal(1000 + df), "1,00" + df);
		assertEquals(formatDecimal(10000.55 + df), "10,00" + df + ".55");
		assertEquals(formatDecimal(1000 + df, ES), "1.00" + df);

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void pluralizeTest() {

		int df = rand.nextInt(9);

		String pattern = "There {0} on {1}.";
		String none = "are no files";
		String one = "is one file";
		String many = "are {2} files";

		MessageFormat f = pluralize(pattern, none, one, many);

		assertEquals(f.render(1000 + df, "disk", 1000 + df), "There are 1,00" + df + " files on disk.");
		assertEquals(f.render(0, "disk"), "There are no files on disk.");
		assertEquals(f.render(-1, "disk"), "There are no files on disk.");
		assertEquals(f.render(1, "disk"), "There is one file on disk.");
		assertEquals(f.render(1, "disk", 1000, "bla bla"), "There is one file on disk.");

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
	public void pluralizeWithSimpleTemplate() {

		int df = rand.nextInt(9);

		MessageFormat f = pluralize("There {0} on {1}. :: are no files::is one file::  are {2} files");

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

	@Test
	public void replaceSupplementaryTest() {

		assertEquals(
		        replaceSupplementary("The first three letters of the Gothic alphabet are: \uD800\uDF30 \uD800\uDF31 \uD800\uDF32 and not"),
		        "The first three letters of the Gothic alphabet are: \\uD800\\uDF30 \\uD800\\uDF31 \\uD800\\uDF32 and not");

		assertEquals(replaceSupplementary("A normal string"), "A normal string");

		// Emoji face
		assertEquals(replaceSupplementary(new StringBuilder().appendCodePoint(0x1F60A).toString()),
		        "\\uD83D\\uDE0A");

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@Test(threadPoolSize = 10, invocationCount = 10)
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


	@Test(threadPoolSize = 10, invocationCount = 10)
	public void titleizeTest() {

		assertEquals(titleize("the_jackie_gleason show"), "The Jackie Gleason Show");
		assertEquals(titleize("first annual report (CD) 2001"), "First Annual Report (CD) 2001");

	}

	@Test
	public void undescoreTest() {

		assertEquals(underscore("a bunch of  macarios"), "a_bunch_of_macarios");
		assertEquals(underscore(" "), "_");
		assertEquals(underscore(""), "");
		assertEquals(underscore("one_two__three"), "one_two__three");

	}

	@Test(threadPoolSize = 10, invocationCount = 10)
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

	@BeforeClass
	protected void setUp() {

		Locale.setDefault(Locale.UK);
		rand = new Random();

	}

	private Date newTestDate(int day, int month, int year) {

		return newTestDate(day, month, year, 0, 0, 0);

	}

	private Date newTestDate(int day, int month, int year, int h, int m, int s) {

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		cal.set(Calendar.SECOND, s);
		return cal.getTime();

	}

}
