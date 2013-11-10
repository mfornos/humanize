package humanize.spi.context;

import humanize.spi.MessageFormat;
import humanize.time.PrettyTimeFormat;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public interface StandardContext
{

	String formatRelativeDate(Date duration);

	String formatRelativeDate(Date reference, Date duration);

	DecimalFormat getCurrencyFormat();

	DateFormat getDateFormat(int style);

	DateFormat getDateFormat(String pattern);

	DateFormat getDateTimeFormat();

	DateFormat getDateTimeFormat(int dateStyle, int timeStyle);

	DecimalFormat getDecimalFormat();

	MessageFormat getMessageFormat();

	NumberFormat getNumberFormat();

	DecimalFormat getPercentFormat();

	PrettyTimeFormat getPrettyTimeFormat();

	String ordinalSuffix(int index);

	String timeSuffix(int index);

}
