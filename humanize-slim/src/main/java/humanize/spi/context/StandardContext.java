package humanize.spi.context;

import humanize.spi.MessageFormat;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public interface StandardContext {
	
	MessageFormat getMessageFormat();

	NumberFormat getNumberFormat();

	DecimalFormat getPercentFormat();

	DecimalFormat getCurrencyFormat();

	DateFormat getDateFormat(int style);

	DateFormat getDateTimeFormat();

	DateFormat getDateTimeFormat(int dateStyle, int timeStyle);

	DecimalFormat getDecimalFormat();
	
	DateFormat getDateFormat(String pattern);
	
}
