package org.nikko.humanize.spi.number;

import org.nikko.humanize.spi.ForLocale;

/**
 * <p>
 * Spanish {@link NumberText} implementation.
 * </p>
 * 
 */
@ForLocale("es_ES")
public class NumberTextES extends BaseNumberText {

	private static enum Connect {

		Minus("menos"), //
		Point(" coma "), //
		AfterMinus(" "), //
		AfterNumber(" "), //
		AfterPower(" "), //
		AfterHundred(" "), //
		AfterTen(" y ");

		final String display;

		Connect(String display) {

			this.display = display;

		}
	}

	private static enum Digit {

		Zero("cero", "diez", "", ""), //
		One("uno", "once", "diez", "ciento"), //
		Two("dos", "doce", "veinte", "doscientos"), //
		Three("tres", "trece", "treinta", "trescientos"), //
		Four("cuatro", "catorce", "cuarenta", "cuatrocientos"), //
		Five("cinco", "quince", "cincuenta", "quinientos"), //
		Six("seis", "dieciseis", "sesenta", "seiscientos"), //
		Seven("siete", "diecisiete", "setenta", "setecientos"), //
		Eight("ocho", "diciocho", "ochenta", "ochocientos"), //
		Nine("nueve", "dicinueve", "noventa", "novecientos"), ; //

		final String display, plusTen, multiTen, multiHundred;

		Digit(String display, String plusTen, String multiTen, String multiHundred) {

			this.display = display;
			this.plusTen = plusTen;
			this.multiTen = multiTen;
			this.multiHundred = multiHundred;

		}
	}

	private static enum Power {

		Thousand("mil", "mil"), // 10 ^ 3
		Million("millón", "millones"), // 10 ^ 6
		Billion("billardo", "billardos"), // 10 ^ 9
		Trillion("billón", "billones"), // 10 ^ 12
		Quadrillion("trillardo", "trillardos"), // 10 ^ 15
		Quintillion("trillón", "trillones"), // 10 ^ 18 (Long.MAX_VALUE)
		Sextillion("cuatrillardo", "cuatrillardos"), // 10 ^ 21
		Septillion("cuatrillón", "cuatrillones"), // 10 ^ 24
		Octillion("quintillardo", "quintillardos"), // 10 ^ 27
		Nonillion("quintillón", "quintillones"), // 10 ^ 30
		Decillion("sextillardo", "sextillardos"), // 10 ^ 33
		Undecillion("sectillón", "sextillones"), // 10 ^ 36
		Duodecillion("duodecillion", "xxxx"), // 10 ^ 39
		Tredecillion("tredecillion", "xxxx"), // 10 ^ 42
		Quattuordecillion("quattuordecillion", "xxxx"), // 10 ^ 45
		Quindecillion("quindecillion", "xxxx"), // 10 ^ 48
		Sexdecillion("sexdecillion", "xxxx"), // 10 ^ 51
		Septendecillion("septendecillion", "xxxx"), // 10 ^ 54
		Octodecillion("octodecillion", "xxxx"), // 10 ^ 57
		Novemdecillion("novemdecillion", "xxxx"), // 10 ^ 60
		Vigintillion("vigintillion", "xxxx"), // 10 ^ 63
		;

		final String display, plural;

		Power(String display, String plural) {

			this.display = display;
			this.plural = plural;

		}
	}

	private static final String UNIT_VARIANT = "un";

	private static final String HUNDRED = "cien";

	private static String[] twenties = new String[] { "veinti", "dós", "trés", "séis" };

	@Override
	protected void appendMinus(StringBuilder builder) {

		builder.append(Connect.Minus.display).append(Connect.AfterMinus.display);

	}

	@Override
	protected void extend(StringBuilder builder, String number, int suffix, int power, int len, int hundreds, int tens,
	        int inds) {

		if (isZero(suffix, hundreds, tens, inds)) {
			if (number.length() == 1)
				builder.append(Digit.Zero.display);
			return;
		}

		if (lessThanMillion(power, suffix, hundreds, tens, inds)) {
			builder.append(Power.values()[power - 1].display);
			return;
		}

		if (len > 3)
			builder.append(Connect.AfterPower.display);

		appendHundreds(builder, len, hundreds, tens, inds);
		appendTens(builder, tens, inds);
		appendUnits(builder, number, suffix, tens, inds);
		appendPowerUnits(builder, power, hundreds, tens, inds);

	}

	@Override
	protected int maxPower() {

		return Power.values().length;

	}

	private void appendHundreds(final StringBuilder builder, int len, int hundreds, int tens, int inds) {

		if (hundreds == 1)
			builder.append(isHundred(tens, inds) ? HUNDRED : Digit.values()[hundreds].multiHundred);
		else if (hundreds > 0)
			builder.append(Digit.values()[hundreds].multiHundred);

		if (needAfterHundred(hundreds, tens, inds))
			builder.append(Connect.AfterHundred.display);

	}

	private void appendPowerUnits(final StringBuilder builder, int power, int hundreds, int tens, int inds) {

		if (power > 0)
			builder.append(Connect.AfterNumber.display).append(
			        isHundrerPlural(hundreds, tens, inds) ? Power.values()[power - 1].plural
			                : Power.values()[power - 1].display);

	}

	private void appendTens(final StringBuilder builder, int tens, int inds) {

		if (tens == 2) {
			builder.append(twenties[0]);
		} else if (tens > 1) {
			builder.append(Digit.values()[tens].multiTen);
			builder.append((inds > 0) ? Connect.AfterTen.display : Connect.AfterNumber.display);
		}

	}

	private void appendUnits(final StringBuilder builder, final String number, int suffix, int tens, int inds) {

		if (tens == 1)
			builder.append(Digit.values()[inds].plusTen);
		else if (is22or23(tens, inds))
			builder.append(twenties[inds - 1]);
		else if (is26(tens, inds))
			builder.append(twenties[3]);
		else if (hasUnit(number, inds))
			builder.append(isComposedOne(suffix, inds) ? UNIT_VARIANT : Digit.values()[inds].display);

	}

	private boolean hasUnit(final String number, int inds) {

		return inds > 0 || number.length() == 1;

	}

	private boolean is22or23(int tens, int inds) {

		return tens == 2 && (inds == 2 || inds == 3);

	}

	private boolean is26(int tens, int inds) {

		return tens == 2 && inds == 6;

	}

	private boolean isComposedOne(int suffix, int inds) {

		return inds == 1 && suffix > 0;

	}

	private boolean isHundred(int tens, int inds) {

		return tens == 0 && inds == 0;

	}

	private boolean isHundrerPlural(int hundreds, int tens, int inds) {

		return inds > 1 || tens >= 0 || hundreds >= 0;

	}

	private boolean isZero(int suffix, int hundreds, int tens, int inds) {

		return hundreds <= 0 && tens <= 0 && inds <= 0 && suffix >= 0;

	}

	private boolean lessThanMillion(int power, int suffix, int hundreds, int tens, int inds) {

		return power > 0 && inds == 1 && tens < 0 && hundreds < 0 && suffix < 6;

	}

	private boolean needAfterHundred(int hundreds, int tens, int inds) {

		return hundreds > 0 && (tens > 0 || inds > 0);

	}

	@Override
	protected void appendDecimalPoint(StringBuilder builder) {

		builder.append(Connect.Point.display);

	}

}
