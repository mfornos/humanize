package org.nikko.humanize.spi.number;

import org.nikko.humanize.spi.ForLocale;

/**
 * <p>
 * English USA {@link NumberText} implementation.
 * </p>
 * 
 * @date 09/02/2012
 * @author na.shi.wu.you (raistlic@gmail.com)
 * 
 */
@ForLocale("en_US")
public class NumberTextUS extends NumberTextGB {
	@Override
	protected String getConnectDisplay(Connect connect) {

		return skipConnect(connect) ? "" : super.getConnectDisplay(connect);

	}

	private boolean skipConnect(Connect connect) {

	    return Connect.AfterAnd == connect || Connect.And == connect;
	    
    }
}
