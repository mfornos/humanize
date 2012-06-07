package org.nikko.humanize.spi.number;

import org.nikko.humanize.spi.ForLocale;

/**
 * <p>
 * Chinese {@link NumberText} implementation.
 * </p>
 * 
 * @date 09/02/2012
 * @author na.shi.wu.you (raistlic@gmail.com)
 */
@ForLocale("zh_CN")
public class NumberTextCN implements NumberText {

	static enum Connect {
		Di("第", "第"), Fu("负", "負"), Ling("零", "零"), Shi("十", "拾"), Bai("百", "佰"), Qian("千", "仟"), ;

		final String display, displayTraditional;

		Connect(String display, String displayTraditional) {

			this.display = display;
			this.displayTraditional = displayTraditional;
		}
	}

	static enum Digit {

		Ling("零", "零"), // just to occupy this position
		Yi("一", "壹"), //
		Er("二", "贰"), //
		San("三", "叁"), //
		Si("四", "肆"), //
		Wu("五", "伍"), //
		Liu("六", "陆"), //
		Qi("七", "柒"), //
		Ba("八", "捌"), //
		Jiu("九", "玖"), ;

		final String display, displayTraditional;

		Digit(String display, String displayTraditional) {

			this.display = display;
			this.displayTraditional = displayTraditional;

		}
	}

	static enum Power {

		Wan("万", "萬"), // 10^4
		Yi("亿", "億"), // 10^8
		Zhao("兆", "兆"), // 10^12
		Jing("京", "京"), // 10^16 (enough for Long.MAX_VALUE)
		Gai("垓", "垓"), // 10^20
		Zi("秭", "秭"), // 10^24
		Rang("穰", "穰"), // 10^28
		Gou("沟", "溝"), // 10^32
		Jian("涧", "澗"), // 10^36
		Zheng("正", "正"), // 10^40
		Zai("载", "載"), // 10^44
		;

		final String display, displayTraditional;

		Power(String display, String displayTraditional) {

			this.display = display;
			this.displayTraditional = displayTraditional;
		}
	}

	public NumberTextCN() {

	}

	@Override
	public String toText(Number number) {

		StringBuilder builder = new StringBuilder();
		buildText(builder, number);
		return builder.toString();
	}

	protected String getConnectDisplay(Connect connect) {

		return connect.display;

	}

	protected String getDigitDisplay(Digit digit) {

		return digit.display;

	}

	protected String getPowerDisplay(Power power) {

		return power.display;

	}

	private void buildText(StringBuilder builder, Number number) {

		String numString = number.toString();

		if (numString.charAt(0) == '-') {
			builder.append(getConnectDisplay(Connect.Fu));
			numString = numString.substring(1);
		}

		int power = 0;
		while (numString.length() > (power + 1) * 4)
			power++;

		while (power > 0) {
			if (extendToken(builder, numString, power * 4))
				builder.append(getPowerDisplay(Power.values()[power - 1]));
			power--;
		}
		extendToken(builder, numString, 0);

	}

	private boolean extendToken(StringBuilder builder, String number, int suffix) {

		int len = number.length() - suffix;
		int qian = len > 3 ? (int) (number.charAt(len - 4) - '0') : -1;
		int bai = len > 2 ? (int) (number.charAt(len - 3) - '0') : -1;
		int shi = len > 1 ? (int) (number.charAt(len - 2) - '0') : -1;
		int ind = (int) (number.charAt(len - 1) - '0');

		boolean nonZero = false; // true if any of the digits is not zero
		if (qian == 0) {
			if (bai > 0 || shi > 0 || ind > 0)
				builder.append(getConnectDisplay(Connect.Ling));
		} else if (qian > 0) {
			builder.append(getDigitDisplay(Digit.values()[qian])).append(getConnectDisplay(Connect.Qian));
			nonZero = true;
		}

		if (bai == 0) {
			if (qian > 0 && (shi > 0 || ind > 0))
				builder.append(getConnectDisplay(Connect.Ling));
		} else if (bai > 0) {
			builder.append(getDigitDisplay(Digit.values()[bai])).append(getConnectDisplay(Connect.Bai));
			nonZero = true;
		}

		if (shi == 0) {
			if (bai > 0 && ind > 0)
				builder.append(getConnectDisplay(Connect.Ling));
		} else if (shi > 0) {
			if (number.length() > 2 || shi != 1)
				builder.append(getDigitDisplay(Digit.values()[shi]));
			builder.append(getConnectDisplay(Connect.Shi));
			nonZero = true;
		}

		if (ind == 0) {
			boolean addZero = len == 1;
			for (int i = 1; addZero && i <= suffix; i++) {
				if (number.charAt(i) != '0')
					addZero = false;
			}
			if (addZero)
				builder.append(getConnectDisplay(Connect.Ling));
		} else {
			builder.append(getDigitDisplay(Digit.values()[ind]));
			nonZero = true;
		}
		return nonZero;

	}
}
