package org.nikko.humanize.spi.number;

import java.math.BigDecimal;

public abstract class BaseNumberText implements NumberText {

	@Override
	public String toText(Number value) {

		StringBuilder builder = new StringBuilder();

		if (isDecimal(value)) {
			BigDecimal bd = new BigDecimal(value.toString());
			int scale = bd.scale();
			if (scale > 0) {
				BigDecimal remainder = bd.remainder(BigDecimal.ONE).movePointRight(scale);

				buildText(builder, abs(builder, bd.toBigInteger()));
				appendDecimalPoint(builder);
				buildText(builder, remainder.unscaledValue().abs().toString());
				
				return builder.toString();
			}
		}

		buildText(builder, abs(builder, value));

		return builder.toString();

	}

	abstract protected void appendDecimalPoint(StringBuilder builder);

	abstract protected void appendMinus(StringBuilder builder);

	protected void buildText(StringBuilder builder, String number) {

		int power = 0;
		while (number.length() > (power + 1) * 3)
			power++;

		checkPower(power);

		while (power > 0) {
			extendToken(builder, number, power);
			power--;
		}
		extendToken(builder, number, 0);

	}

	abstract protected void extend(StringBuilder builder, String number, int suffix, int power, int len, int hundreds,
	        int tens, int inds);

	protected void extendToken(final StringBuilder builder, final String number, int power) {

		int suffix = power * 3;
		int len = number.length() - suffix;
		int hundreds = len > 2 ? toInt(number.charAt(len - 3)) : -1;
		int tens = len > 1 ? toInt(number.charAt(len - 2)) : -1;
		int inds = toInt(number.charAt(len - 1));

		extend(builder, number, suffix, power, len, hundreds, tens, inds);
	}

	abstract protected int maxPower();

	private String abs(StringBuilder builder, Number value) {

		String numString = value.toString();

		if (numString.charAt(0) == '-') {
			appendMinus(builder);
			numString = numString.substring(1);
		}
		return numString;

	}

	private void checkPower(int power) {

		if (power > maxPower())
			throw new IllegalArgumentException("Number is too big, needs a power of " + power + " !");

	}

	private boolean isDecimal(Number number) {

		return Math.round(number.doubleValue()) != number.doubleValue();

	}

	private int toInt(final char c) {

		return (int) (c - '0');

	}

}
