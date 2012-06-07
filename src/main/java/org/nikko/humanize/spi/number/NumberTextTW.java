package org.nikko.humanize.spi.number;

import org.nikko.humanize.spi.ForLocale;

@ForLocale("zh_TW")
public class NumberTextTW extends NumberTextCN {

	@Override
	protected String getConnectDisplay(Connect connect) {

		return connect.displayTraditional;

	}

	@Override
	protected String getDigitDisplay(Digit digit) {

		return digit.displayTraditional;

	}

	@Override
	protected String getPowerDisplay(Power power) {

		return power.displayTraditional;

	}

}
