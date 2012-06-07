package org.nikko.humanize.spi.number;

import org.nikko.humanize.spi.ForLocale;

/**
 * <p>
 * English UK {@link NumberText} implementation.
 * </p>
 * 
 * @date 09/02/2012
 * @author na.shi.wu.you (raistlic@gmail.com)
 * 
 */
@ForLocale("en_GB")
public class NumberTextGB extends BaseNumberText {

	static enum Connect {

		Minus("minus"), //
		Point(" point "), //
		Hundred("hundred"), //
		And("and"), //
		AfterMinus(" "), //
		AfterNumber(" "), //
		AfterPower(" "), //
		AfterHundred(" "), //
		AfterAnd(" "), //
		AfterTen("-");

		final String display;

		Connect(String display) {

			this.display = display;

		}
	}

	static enum Digit {

		Zero("zero", "ten", ""), //
		One("one", "eleven", "ten"), //
		Two("two", "twelve", "twenty"), //
		Three("three", "thirteen", "thirty"), //
		Four("four", "fourteen", "forty"), //
		Five("five", "fifteen", "fifty"), //
		Six("six", "sixteen", "sixty"), //
		Seven("seven", "seventeen", "seventy"), //
		Eight("eight", "eighteen", "eighty"), //
		Nine("nine", "nineteen", "ninety"), ; //

		final String display, plusTen, multiTen;

		Digit(String display, String plusTen, String multiTen) {

			this.display = display;
			this.plusTen = plusTen;
			this.multiTen = multiTen;

		}
	}

	static enum Power {

		Thousand("thousand"), // 10 ^ 3
		Million("million"), // 10 ^ 6
		Billion("billion"), // 10 ^ 9
		Trillion("trillion"), // 10 ^ 12
		Quadrillion("quadrillion"), // 10 ^ 15
		Quintillion("quintillion"), // 10 ^ 18 (enough for Long.MAX_VALUE)
		Sextillion("sextillion"), // 10 ^ 21
		Septillion("septillion"), // 10 ^ 24
		Octillion("octillion"), // 10 ^ 27
		Nonillion("nonillion"), // 10 ^ 30
		Decillion("decillion"), // 10 ^ 33
		Undecillion("undecillion"), // 10 ^ 36
		Duodecillion("duodecillion"), // 10 ^ 39
		Tredecillion("tredecillion"), // 10 ^ 42
		Quattuordecillion("quattuordecillion"), // 10 ^ 45
		Quindecillion("quindecillion"), // 10 ^ 48
		Sexdecillion("sexdecillion"), // 10 ^ 51
		Septendecillion("septendecillion"), // 10 ^ 54
		Octodecillion("octodecillion"), // 10 ^ 57
		Novemdecillion("novemdecillion"), // 10 ^ 60
		Vigintillion("vigintillion"), // 10 ^ 63
		;

		final String display;

		Power(String display) {

			this.display = display;

		}
	}

	public static NumberText getInstance() {

		return new NumberTextGB();

	}

	@Override
	protected void appendDecimalPoint(StringBuilder builder) {

		builder.append(Connect.Point.display);

	}

	@Override
	protected void appendMinus(StringBuilder builder) {

		builder.append(getConnectDisplay(Connect.Minus)).append(getConnectDisplay(Connect.AfterMinus));

	}

	@Override
	protected void extend(StringBuilder builder, String number, int suffix, int power, int len, int hundreds, int tens,
	        int inds) {

		if (hundreds <= 0 && tens <= 0 && inds <= 0 && suffix >= 0) {
			if (number.length() == 1)
				builder.append(Digit.Zero.display);
			return;
		}

		if (len > 3)
			builder.append(getConnectDisplay(Connect.AfterPower));

		if (hundreds == 0 && (len > 3 && (tens > 0 || inds > 0))) {
			builder.append(getConnectDisplay(Connect.And)).append(getConnectDisplay(Connect.AfterAnd));
		} else if (hundreds > 0) {
			builder.append(Digit.values()[hundreds].display).append(getConnectDisplay(Connect.AfterNumber))
			        .append(getConnectDisplay(Connect.Hundred));
			if (tens > 0 || inds > 0)
				builder.append(getConnectDisplay(Connect.AfterHundred)).append(getConnectDisplay(Connect.And))
				        .append(getConnectDisplay(Connect.AfterAnd));
		}

		if (tens > 1) {
			builder.append(Digit.values()[tens].multiTen);
			if (inds > 0)
				builder.append(getConnectDisplay(Connect.AfterTen));
		}

		if (tens == 1)
			builder.append(Digit.values()[inds].plusTen);
		else if (inds > 0 || number.length() == 1)
			builder.append(Digit.values()[inds].display);

		if (power > 0)
			builder.append(getConnectDisplay(Connect.AfterNumber)).append(Power.values()[power - 1].display);

	}

	protected String getConnectDisplay(Connect connect) {

		return connect.display;

	}

	@Override
	protected int maxPower() {

		return Power.values().length;

	}

}
