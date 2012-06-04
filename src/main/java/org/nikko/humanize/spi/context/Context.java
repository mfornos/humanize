package org.nikko.humanize.spi.context;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import org.nikko.humanize.spi.Message;
import org.nikko.humanize.util.RelativeDate;

public interface Context {
	
	String digitStrings(int index);

	String formatDate(int style, Date value);

	String formatMessage(String key, Object... args);

	String formatDecimal(Number value);

	DateFormat getDateFormat(int style);
	
	RelativeDate getRelativeDate();
	
	DateFormat getDateTimeFormat();
	
	String formatDateTime(Date date);

	Message getFormat();

	Locale getLocale();
	
	String getMessage(String key);

	NumberFormat getNumberFormat();
	
	NumberFormat getCurrencyFormat();

	String ordinalSuffix(int index);

	void setLocale(Locale locale);

	String formatRelativeDate(Date duration);

	String formatRelativeDate(Date reference, Date duration);

}
