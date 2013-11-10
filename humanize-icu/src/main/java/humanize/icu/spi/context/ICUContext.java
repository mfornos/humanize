package humanize.icu.spi.context;

import humanize.icu.spi.MessageFormat;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DurationFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.ULocale;

public interface ICUContext
{

	String getBestPattern(String skeleton);

	DecimalFormat getCurrencyFormat();

	DateFormat getDateFormat(int style);

	DateFormat getDateTimeFormat();

	DateFormat getDateTimeFormat(int dateStyle, int timeStyle);

	DecimalFormat getDecimalFormat();

	DurationFormat getDurationFormat();

	NumberFormat getNumberFormat();

	DecimalFormat getPercentFormat();

	DecimalFormat getPluralCurrencyFormat();

	NumberFormat getRuleBasedNumberFormat(int type);

	ULocale getULocale();

	MessageFormat getMessageFormat();

}
