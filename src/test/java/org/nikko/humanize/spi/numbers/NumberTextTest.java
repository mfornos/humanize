package org.nikko.humanize.spi.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.nikko.humanize.spi.number.NumberText;
import org.nikko.humanize.spi.number.NumberTextCN;
import org.nikko.humanize.spi.number.NumberTextES;
import org.nikko.humanize.spi.number.NumberTextGB;
import org.nikko.humanize.spi.number.NumberTextTW;
import org.nikko.humanize.spi.number.NumberTextUS;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NumberTextTest {

	@Test
	public void enGB() {

		NumberText nt = new NumberTextGB();
		Assert.assertEquals(nt.toText(0), "zero");
		Assert.assertEquals(nt.toText(123), "one hundred and twenty-three");
		Assert.assertEquals(nt.toText(2840), "two thousand eight hundred and forty");
		Assert.assertEquals(nt.toText(1803), "one thousand eight hundred and three");
		Assert.assertEquals(nt.toText(1412605), "one million four hundred and twelve thousand six hundred and five");
		Assert.assertEquals(nt.toText(2760300), "two million seven hundred and sixty thousand three hundred");
		Assert.assertEquals(nt.toText(999000), "nine hundred and ninety-nine thousand");
		Assert.assertEquals(nt.toText(23380000000L), "twenty-three billion three hundred and eighty million");
		Assert.assertEquals(
		        nt.toText(new BigInteger("90489348043803948043")),
		        "ninety quintillion four hundred and eighty-nine quadrillion three hundred and forty-eight trillion and forty-three"
		                + " billion eight hundred and three million nine hundred and forty-eight thousand and forty-three");

	}

	@Test
	public void enUS() {

		NumberText nt = new NumberTextUS();
		Assert.assertEquals(nt.toText(23380000000L), "twenty-three billion three hundred eighty million");
		Assert.assertEquals(nt.toText(123), "one hundred twenty-three");
		Assert.assertEquals(nt.toText(2840), "two thousand eight hundred forty");
		Assert.assertEquals(nt.toText(1803), "one thousand eight hundred three");
		Assert.assertEquals(nt.toText(1412605), "one million four hundred twelve thousand six hundred five");
		Assert.assertEquals(nt.toText(2760300), "two million seven hundred sixty thousand three hundred");
		Assert.assertEquals(nt.toText(999000), "nine hundred ninety-nine thousand");

	}

	@Test
	public void esES() {

		NumberText nt = new NumberTextES();
		Assert.assertEquals(nt.toText(0), "cero");
		Assert.assertEquals(nt.toText(27), "veintisiete");
		Assert.assertEquals(nt.toText(22), "veintidós");
		Assert.assertEquals(nt.toText(23), "veintitrés");
		Assert.assertEquals(nt.toText(26), "veintiséis");
		Assert.assertEquals(nt.toText(33), "treinta y tres");
		Assert.assertEquals(nt.toText(41), "cuarenta y uno");
		Assert.assertEquals(nt.toText(100), "cien");
		Assert.assertEquals(nt.toText(127), "ciento veintisiete");
		Assert.assertEquals(nt.toText(256), "doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(1000), "mil");
		Assert.assertEquals(nt.toText(3256), "tres mil doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(1256), "mil doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(10000), "diez mil");
		Assert.assertEquals(nt.toText(100000), "cien mil");
		Assert.assertEquals(nt.toText(101256), "ciento un mil doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(201256), "doscientos un mil doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(251256), "doscientos cincuenta y un mil doscientos cincuenta y seis");
		Assert.assertEquals(nt.toText(455567), "cuatrocientos cincuenta y cinco mil quinientos sesenta y siete");
		Assert.assertEquals(nt.toText(1000000), "un millón");
		Assert.assertEquals(nt.toText(2000000), "dos millones");
		Assert.assertEquals(nt.toText(1200000), "un millón doscientos mil");
		Assert.assertEquals(nt.toText(2427000), "dos millones cuatrocientos veintisiete mil");
		Assert.assertEquals(nt.toText(100002001), "cien millones dos mil uno");

		Assert.assertEquals(nt.toText(BigInteger.TEN.pow(25)), "diez cuatrillones");

		Assert.assertEquals(nt.toText(-1), "menos uno");

		Assert.assertEquals(nt.toText(-1.2), "menos uno coma dos");
		Assert.assertEquals(nt.toText(100.50), "cien coma cinco");
		Assert.assertEquals(nt.toText(101.51), "ciento uno coma cincuenta y uno");
		Assert.assertEquals(nt.toText(3300000.788), "tres millones trescientos mil coma setecientos ochenta y ocho");

		Assert.assertEquals(
		        nt.toText(new BigDecimal("2355555333355555555555555.23")),
		        "dos cuatrillones trescientos cincuenta y cinco cuatrillardos quinientos cincuenta y cinco trillones"
		                + " trescientos treinta y tres trillardos trescientos cincuenta y cinco billones quinientos cincuenta y"
		                + " cinco billardos quinientos cincuenta y cinco millones quinientos cincuenta y cinco mil quinientos"
		                + " cincuenta y cinco coma veintitrés");

	}

	@Test
	public void zhCN() {

		NumberText nt = new NumberTextCN();
		Assert.assertEquals(nt.toText(0), "零");
		Assert.assertEquals(nt.toText(127), "一百二十七");
		Assert.assertEquals(nt.toText(256), "二百五十六");
		Assert.assertEquals(nt.toText(1000), "一千");
		Assert.assertEquals(nt.toText(1000000), "一百万");
		Assert.assertEquals(nt.toText(2000000), "二百万");
		Assert.assertEquals(nt.toText(1200000), "一百二十万");
		Assert.assertEquals(nt.toText(2427000), "二百四十二万七千");
		Assert.assertEquals(nt.toText(1), "一");
		Assert.assertEquals(nt.toText(-1), "负一");

	}

	@Test
	public void zhTw() {

		NumberText nt = new NumberTextTW();
		Assert.assertEquals(nt.toText(0), "零");
		Assert.assertEquals(nt.toText(127), "壹佰贰拾柒");
		Assert.assertEquals(nt.toText(256), "贰佰伍拾陆");
		Assert.assertEquals(nt.toText(1000), "壹仟");
		Assert.assertEquals(nt.toText(1000000), "壹佰萬");
		Assert.assertEquals(nt.toText(2000000), "贰佰萬");
		Assert.assertEquals(nt.toText(1200000), "壹佰贰拾萬");
		Assert.assertEquals(nt.toText(2427000), "贰佰肆拾贰萬柒仟");
		Assert.assertEquals(nt.toText(1), "壹");
		Assert.assertEquals(nt.toText(-1), "負壹");

	}

}
